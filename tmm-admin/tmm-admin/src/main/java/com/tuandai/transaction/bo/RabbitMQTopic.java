package com.tuandai.transaction.bo;

import com.alibaba.fastjson.JSONObject;

public class RabbitMQTopic {
	private String vHost;
	private String exchange;
	private String exchangeType;
	private String routeKey;
	private boolean isCustomExchange; // false 默认创建  true 消费者手动创建

	private String queue;

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public boolean isCustomExchange() {
		return isCustomExchange;
	}

	public void setCustomExchange(boolean customExchange) {
		isCustomExchange = customExchange;
	}

	public String getRouteKey() {
		return routeKey;
	}

	public void setRouteKey(String routeKey) {
		this.routeKey = routeKey;
	}

	public String getvHost() {
		return vHost;
	}

	public void setvHost(String vHost) {
		this.vHost = vHost;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}

	public String toJSONString() {
		return JSONObject.toJSONString(this);
	}

}
