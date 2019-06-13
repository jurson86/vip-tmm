package com.tuandai.transaction.producer.controller;

import com.tuandai.transaction.client.service.inf.TMMService;
import com.tuandai.transaction.client.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/monitor")
@ConditionalOnProperty(name = "spring.rabbitmq.tmm.producer.enabled", havingValue = "true")
public class TMMController {

    private static final Logger logger = LoggerFactory.getLogger(TMMController.class);

    @Autowired
    private TMMService tmmService;

    @RequestMapping(value = "/tmm", method = RequestMethod.POST)
    public Object queryRpcDoneFileCount() {
        logger.debug("queryRpcDoneFileCount 监控查asd询");
        Map<String, Long> result = tmmService.monitorData();
        return new ResponseEntity<Result<Map<String, Long>>>(new Result<Map<String, Long>>(result), HttpStatus.OK);
    }

}
