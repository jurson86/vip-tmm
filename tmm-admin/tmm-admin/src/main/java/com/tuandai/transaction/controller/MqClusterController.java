package com.tuandai.transaction.controller;

import com.tuandai.transaction.domain.MqCluster;
import com.tuandai.transaction.domain.TransactionCheck;
import com.tuandai.transaction.service.inf.MqClusterService;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.utils.ServiceException;
import com.tuandai.transaction.vo.MqClusterVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mq")
public class MqClusterController {

    private static final Logger logger = LoggerFactory.getLogger(MqClusterController.class);

    @Autowired
    private MqClusterService mqClusterService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Response addMqCluster(@RequestBody MqClusterVo mqClusterVo) {
        logger.info("MqClusterController.addMqCluster {} ", mqClusterVo);
        try {
            mqClusterService.addMqCluster(mqClusterVo);
        } catch (ServiceException e) {
            return new Response(e.getMessage());
        } catch (Exception e) {
            logger.error("MqClusterController.addMqCluster 未知错误", e);
            return new Response("mqKey重复");
        }
        return Response.success("成功");
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Response deleteMqCluster(@RequestParam String pids) {
        logger.info("MqClusterController.deleteMqCluster {} ", pids);
        try {
            mqClusterService.deleteMqCluster(pids);
        } catch (ServiceException e) {
            return new Response(e.getMessage());
        } catch (Exception e) {
            logger.error("MqClusterController.deleteMqCluster 未知错误", e);
            return new Response("未知异常");
        }
        return Response.success("成功");
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public Response updateMqCluster(@RequestBody MqClusterVo mqClusterVo) {
        logger.info("MqClusterController.updateMqCluster {} ", mqClusterVo);
        try {
            mqClusterService.updateMqCluster(mqClusterVo);
        } catch (ServiceException e) {
            return new Response(e.getMessage());
        } catch (Exception e) {
            logger.error("MqClusterController.updateMqCluster 未知错误", e);
            return new Response("未知异常");
        }
        return Response.success("成功");
    }

    @RequestMapping(value = "/query/list", method = RequestMethod.GET)
    public Response queryMqCluster(@RequestBody(required = false) MqClusterVo mqClusterVo) {
        logger.info("MqClusterController.queryMqCluster {} ", mqClusterVo);
        List<MqCluster> list = null;
        try {
            list = mqClusterService.queryMqCluster(mqClusterVo);
        } catch (ServiceException e) {
            return new Response(e.getMessage());
        } catch (Exception e) {
            logger.error("MqClusterController.queryMqCluster 未知错误", e);
            return new Response("未知异常");
        }
        return Response.success(list);
    }

    @RequestMapping(value = "/query/details", method = RequestMethod.GET)
    public Response queryMqCluster(@RequestParam Long pid) {
        logger.info("MqClusterController.pid {} ", pid);
        if (pid == null || pid.equals(0)) {
            return new Response("pid参数为空");
        }
        MqCluster result = null;
        try {
            MqClusterVo mqClusterVo = new MqClusterVo();
            mqClusterVo.setPid(pid);
            List<MqCluster> list = mqClusterService.queryMqCluster(mqClusterVo);
            if (!CollectionUtils.isEmpty(list)) {
                result = list.get(0);
            }
        } catch (ServiceException e) {
            return new Response(e.getMessage());
        } catch (Exception e) {
            logger.error("MqClusterController.queryMqCluster 未知错误", e);
            return new Response("未知异常");
        }
        return Response.success(result);
    }
}
