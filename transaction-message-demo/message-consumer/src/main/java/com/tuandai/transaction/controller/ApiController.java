package com.tuandai.transaction.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(value = "/api")
public class ApiController {

	private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public HashMap<String, Object> get(@RequestParam String name) {
		
    	logger.info("get: {}",name);
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("title", "hello world");
		map.put("name", name);
		
		return map;
	}
	
	/**
	 * 消息消费结果确认
	 */
	@RequestMapping(value = "/messageid/result", method = RequestMethod.POST)
	public ResponseEntity<String> messageResult(@RequestBody String body) {
		// TODO 业务处理
		logger.info("===================messageid result======================= : " + body);
		logger.error("1");
		return new ResponseEntity<String>("", HttpStatus.OK);
	}
}