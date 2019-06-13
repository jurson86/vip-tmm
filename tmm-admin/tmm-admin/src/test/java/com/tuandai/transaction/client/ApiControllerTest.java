package com.tuandai.transaction.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.bo.MessageAck;
import com.tuandai.transaction.bo.RabbitMQTopic;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping(value = "/test")
public class ApiControllerTest {

    private static Random random = new Random();

    public class EndLog {
        private MessageAck.SendState state;
        private String uid;
        private String serviceName;
        private String topic;
        private String message;

        public MessageAck.SendState getState() {
            return state;
        }

        public void setState(MessageAck.SendState state) {
            this.state = state;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }



    @RequestMapping(value = "/tmm/check", method = RequestMethod.POST)
    public String check(@RequestBody String body) {
        String uid = JSON.parseObject(body).getString("uid");


        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange4");
        rabbitMQTopic.setExchangeType("direct");
        rabbitMQTopic.setRouteKey("route");
        rabbitMQTopic.setCustomExchange(true);

        EndLog endLog = new EndLog();
        if (random.nextInt(4)  == 3) {
            endLog.setState(MessageAck.SendState.CANCEL);
        } else {
            endLog.setState(MessageAck.SendState.COMMIT);
        }
        endLog.setMessage("重发！check");
        endLog.setServiceName("transaction-producer");
        endLog.setUid(uid);
        endLog.setTopic(rabbitMQTopic.toJSONString());
        String result = JSONObject.toJSONString(endLog);
        return result;
    }
}
