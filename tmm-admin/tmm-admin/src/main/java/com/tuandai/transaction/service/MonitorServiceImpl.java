package com.tuandai.transaction.service;

import com.alibaba.fastjson.JSONObject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.tuandai.transaction.bo.MessageState;
import com.tuandai.transaction.bo.MonitorAgentJson;
import com.tuandai.transaction.bo.ThreeTuple;
import com.tuandai.transaction.bo.TwoTuple;
import com.tuandai.transaction.config.Constants;
import com.tuandai.transaction.dao.MonitorAgentDao;
import com.tuandai.transaction.dao.RegistryAgentDao;
import com.tuandai.transaction.dao.TransactionCheckDao;
import com.tuandai.transaction.domain.MonitorAgent;
import com.tuandai.transaction.domain.RegistryAgent;
import com.tuandai.transaction.service.inf.MonitorService;
import com.tuandai.transaction.utils.ThreadPoolExecutorUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class MonitorServiceImpl implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    private TransactionCheckDao transactionCheckDao;

    @Autowired
    @Qualifier("httpRest")
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RegistryAgentDao registryAgentDao;

    @Autowired
    private MonitorAgentDao monitorAgentDao;

    @Override
    public Map<MessageState, Long> messageStateMonitor(List<String> serviceNames) {
        Map<MessageState, Long> map = new HashMap<>();
        List<TwoTuple<Integer, Long>> list = transactionCheckDao.messageStateCountMap(serviceNames);
        if (!CollectionUtils.isEmpty(list)) {
            for (TwoTuple<Integer, Long> tuple : list) {
                MessageState state = MessageState.findByValue(tuple.a);
                if (state != null) {
                    map.put(state, tuple.b);
                }

            }
        }
        return map;
    }

    @Override
    //@Scheduled(cron = "0/10 * * * * ?")
    public void monitorAgentService() {
        Map<String, List<String>> urlMap = getRegistryEurekaServiceList();
        List<MonitorAgent> monitorAgents = new ArrayList<>();
        if (!CollectionUtils.isEmpty(urlMap)) {
            // 多线程去调用代理端的监控
            ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getTaskThreadPoolExecutorUtils();

            List<Future<ThreeTuple<String, String, String>>> futureList = new ArrayList<>();

            for (Map.Entry<String, List<String>> tmp : urlMap.entrySet()) {
                List<String> ipList = tmp.getValue();
                String serviceName = tmp.getKey();
                for (String url : ipList) {
                    Future<ThreeTuple<String, String, String>> future = executor.submit(() -> {
                        // 单个处理
                        return doMonitorAgentService(url, serviceName);
                    });
                    futureList.add(future);
                }
            }

            Date time = new Date(System.currentTimeMillis());
            Set<String> set = new HashSet<>();
            for (Future<ThreeTuple<String, String, String>> future : futureList) {
                try {
                    // 设置超时
                    ThreeTuple<String, String, String> tuple = future.get(Constants.RESTFUL_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (tuple != null) {
                        MonitorAgent monitorAgent = new MonitorAgent();
                        monitorAgent.setCreateTime(time);
                        monitorAgent.setServiceName(tuple.getFirst());
                        monitorAgent.setUpdateTime(time);
                        monitorAgent.setUrl(tuple.getSecond());
                        monitorAgent.setMonitor(tuple.getThree());
                        // 去重
                        if (!set.contains(tuple.getSecond())) {
                            monitorAgents.add(monitorAgent);
                            set.add(tuple.getSecond());
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("doMonitorAgentService 任务异常中止 : {}", e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.error("doMonitorAgentService 计算出现异常: {}", e.getMessage());
                } catch (TimeoutException e) {
                    logger.error("doMonitorAgentService 超时异常: {}", e.getMessage());
                    // 超时后取消任务
                    future.cancel(true);
                }
            }
        }
        // 删除数据库数据
        monitorAgentDao.deleteMonitorAgent();
        if (!CollectionUtils.isEmpty(monitorAgents)) {
            // 插入数据库
            monitorAgentDao.addMonitorAgent(monitorAgents);
        }

    }

    @Override
    public List<String> getApplicationName() {
        List<String> list = new ArrayList<>();
        Map<String, List<ServiceInstance>> map = getEurekaServiceList();
        if (map.size() != 0) {
            Set<String> keySet = map.keySet();
            list.addAll(keySet);
        }
        return list;
    }

    @Override
    public List<MonitorAgentJson> getAgentMonitor() {
        List<MonitorAgent> list = monitorAgentDao.queryMonitorAgentAll();
        List<MonitorAgentJson> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (MonitorAgent monitorAgent : list) {
                String monitor = monitorAgent.getMonitor();
                if (!StringUtils.isEmpty(monitor)) {
                    String data = JSONObject.parseObject(monitor).getString("data");
                    if (!StringUtils.isEmpty(data)) {
                        result.add(new MonitorAgentJson(monitorAgent.getServiceName(), monitorAgent.getUrl(), data));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean addRegistryAgent(List<RegistryAgent> registryAgents) {
        registryAgentDao.addRegistryAgent(registryAgents);
        return true;
    }

    @Override
    public boolean deleteRegistryAgent(String serviceName) {
        RegistryAgent registryAgent = registryAgentDao.queryRegistryAgentByServerName(serviceName);
        if (registryAgent != null) {
            List<Integer> ids = new ArrayList<>();
            ids.add(registryAgent.getPid());
            registryAgentDao.deleteRegistryAgentById(ids);
        }
        return true;
    }

    private Map<String, List<String>> getRegistryEurekaServiceList() {
        Map<String, List<ServiceInstance>> allMap = getEurekaServiceList();
        Map<String, List<String>> map = new CaseInsensitiveMap();
        // 查询数据库中注册的服务列表
        List<RegistryAgent> registryAgents = registryAgentDao.queryRegistryAgentAll();
        if (!CollectionUtils.isEmpty(registryAgents)) {
            for (RegistryAgent registryAgent : registryAgents) {
                List<ServiceInstance> rList = allMap.get(registryAgent.getServiceName());
                if (CollectionUtils.isEmpty(rList)) {
                    logger.warn(registryAgent.getServiceName() + "没有找到注册器上的Ip地址！");
                } else {
                    List<String> resultUrls = new ArrayList<>();
                    for (ServiceInstance serviceInstance : rList) {
                        EurekaDiscoveryClient.EurekaServiceInstance o = (EurekaDiscoveryClient.EurekaServiceInstance)serviceInstance;
                        String homePageUrl = o.getInstanceInfo().getHomePageUrl();
                        homePageUrl = homePageUrl.endsWith("/") ? homePageUrl : (homePageUrl+"/");
                        String prefixUrl = StringUtils.isEmpty(registryAgent.getPrefixUrl()) ? "" : registryAgent.getPrefixUrl();
                        prefixUrl = prefixUrl.startsWith("/") ? prefixUrl.substring(1) : prefixUrl;
                        String resultUrl = homePageUrl + prefixUrl;
                        resultUrls.add(resultUrl);
                    }
                    map.put(registryAgent.getServiceName(), resultUrls);
                }
            }
        }
       return map;
    }

    private Map<String, List<ServiceInstance>> getEurekaServiceList() {
        Map<String, List<ServiceInstance>> map = new CaseInsensitiveMap();
        List<String> services = discoveryClient.getServices();
        if (!CollectionUtils.isEmpty(services)) {
            for (String service : services) {
                List<ServiceInstance> serviceInstances  = discoveryClient.getInstances(service);
                map.put(service, serviceInstances);
            }
        }
        logger.info("拉取EurekaClient 注册的服务:" + JSONObject.toJSONString(map));
        return map;
    }

    private ThreeTuple<String, String, String> doMonitorAgentService(String url, String serviceName) {
        try {
            url = url.endsWith("/") ? (url + Constants.MONITOR_TAIL) : (url + "/" + Constants.MONITOR_TAIL);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                logger.info("tmm-admin-monitor ,serviceName:" + serviceName +" , IP:" + url + " , value:" + body);
                return new ThreeTuple<>(serviceName, url, body);
            } else {
                logger.warn("tmm-admin , 监控调用tmm-agent失败");
            }
        } catch (Exception e) {
           logger.error("doMonitorAgentService 异常，url：" + url + " , serviceName:" + serviceName + " ，异常：e" + e);
        }
        return null;
    }

}
