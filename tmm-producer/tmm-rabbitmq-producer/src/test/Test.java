import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.client.model.MqLog;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.producer.config.RabbitConfig;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import org.springframework.amqp.core.MessageProperties;

import java.util.HashMap;
import java.util.Map;

public class Test {


    public static void main(String[] args) {
        // 引用 jar依赖
//        <dependency>
//            <groupId>com.tuandai.architecture</groupId>
//            <artifactId>tmm-rabbitmq-producer</artifactId>
//            <version>1.0-SNAPSHOT</version>
//        </dependency>

        // 1.创建tmm服务实例。 此服务实例保存了链接，非常重，建议保存，而不是随便new
        TMMServiceImpl tmmService = new TMMServiceImpl();

        // 2. 对tmm 进行配置
        // Rabbitmq配置， Rocket配置同理 使用 RocketConfig ....
        // 这里如果只有一个mq集群的话可以省略 前面的分组名，默认会自动填充default，
        // 下面的ip 也不需要指定，他会自动匹配到这个集群，如果有多个集群的话，就必需都要指定了
        RabbitConfig rabbitConfig = new RabbitConfig();
        Map<String, String> rabbitmq = new HashMap<>();
        rabbitmq.put("default.password", "root");
        rabbitmq.put("default.host", "10.100.11.73");
        rabbitmq.put("default.port", "5672");
        rabbitmq.put("default.username", "root");

        rabbitmq.put("first.password", "admin");
        rabbitmq.put("first.port", "5672");
        rabbitmq.put("first.username", "admin");
        rabbitmq.put("first.host", "10.100.14.6");

        // 必填，rabbitmq集群的配置信息，default，first或者任何一个非唯一字段 代表集群的分组 key
        rabbitConfig.setRabbitmq(rabbitmq);
        // 必填，mq 的类型，目前支持rabbitmq，rocketmq，后面可支持其他类型的mq
        rabbitConfig.setMqType("rabbitmq");
        // 必填， 注册中心的服务名
        rabbitConfig.setServerName("fengpengyong");

        // 选填，默认值2000
        rabbitConfig.setBatchSize(2000);
        // 选填，数据 存放路径 默认为""代表使用用户部署的路径
        rabbitConfig.setRpcDirName("");
        // 选填， 默认60000ms（1分钟），对于事务消息来说，一分钟没有检测到endlog 则判断为check
        rabbitConfig.setCheckIntervalTime(5000);
        // 选填，默认 104857600 字节（10M），当数据文件到达10M的时候进行文件切割
        rabbitConfig.setRpcSize(102400);
        // 选填，发送mq的线程池，核心线程数为5
        rabbitConfig.setSendMqCorePoolThreadNum(5);
        // 选填， 发送mq的线程池，最大线程数为200
        rabbitConfig.setSendMqMaxPoolThreadNum(200);
        // 选填， 默认发送存储mq queue 的大小，默认为5000
        rabbitConfig.setSendMqQueueSize(5000);
        // 选填， web容器的根路径，如servelt容器的根路径，没有可不填
        rabbitConfig.setPrefixUrl("/test");
        // 选填， 默认为true 表示使用tmm封装的消息体（"message": ...., "uid":"..."）, false 表示 使用用户的默认的消息内容，主要为了兼容老版本，新的对接统一使用false
        rabbitConfig.setTmmMessage(false);

        // 3.启动tmm
        tmmService.start(rabbitConfig);

        MqLog mqLog = new MqLog();
        mqLog.setMessage("api 使用tmm 测试用例");
        mqLog.setServiceName("fengpengyong");
        // exchange, 必填
        // exchangeType, 选填 有默认值 fanout
        // vhost 必填, 默认值 /tmm_vhost
        // ip 单mq机群选填，多mq集群必填，默认值 default
        RabbitMQTopic rabbitMQTopic = RabbitMQTopic.newRabbitMQTopicBuilder().exchange("tmmTest")
                .exchangeType("fanout").vHost("TMM").ip("default").build();
        mqLog.setTopic(rabbitMQTopic.toJSONString());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("name", "tmm");
        mqLog.setMessageProperties(JSONObject.toJSONString(messageProperties));

        mqLog.setUid("1213213123123123");

        // 4. 使用tmm 发送可靠消息，或者事务消息
        // 4.1.发送可靠消息
        tmmService.sendNTrans(mqLog);

        // 4.2.事务消息同理,同理
        //tmmService.sendTransBeginToFlume();
        //tmmService.sendTransEndToFlume();

        // 5.使用完成后，关闭tmm 服务.(这里是例子，真正服务中应该讲 tmmSercvice放到 容器里面保存如spring，
        // 容器启动则生成实例，容器销毁则，关闭TMMService)
        tmmService.shutdown();
    }
}
