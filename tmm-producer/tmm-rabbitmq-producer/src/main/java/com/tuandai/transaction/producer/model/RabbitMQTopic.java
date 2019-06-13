package com.tuandai.transaction.producer.model;

import com.alibaba.fastjson.JSONObject;
import com.tuandai.transaction.producer.utils.Constants;

public class RabbitMQTopic {

	private String ip; // 对应的集群Ip

	private String vHost;

	private String exchange;

	private String exchangeType;

	private String routeKey;

	private boolean isCustomExchange = false; // false 默认创建  true 消费者手动创建

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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


	public RabbitMQTopic() {}

	public RabbitMQTopic(RabbitMQTopicBuilder builder) {
		ip = builder.ip;
		vHost = builder.vHost;
		exchange = builder.exchange;
		exchangeType = builder.exchangeType;
		routeKey = builder.routeKey;
		isCustomExchange = builder.isCustomExchange;
	}

	public static RabbitMQTopicBuilder newRabbitMQTopicBuilder() {
		return new RabbitMQTopicBuilder();
	}

	public static class RabbitMQTopicBuilder {

		private  String ip;

		// 必填
		private  String vHost = Constants.TMM_VHOST;

		// 必填
		private  String exchange;

		private  String exchangeType = ExchangeType.FANOUT.des();

		private  String routeKey;

		private  boolean isCustomExchange;

		public  RabbitMQTopicBuilder ip(String ip) {
			this.ip = ip;
			return this;
		}

		public  RabbitMQTopicBuilder vHost(String vHost) {
			this.vHost = vHost;
			return this;
		}

		public  RabbitMQTopicBuilder exchange(String exchange) {
			this.exchange = exchange;
			return this;
		}

		public  RabbitMQTopicBuilder exchangeType(String exchangeType) {
			this.exchangeType = exchangeType;
			return this;
		}

		public  RabbitMQTopicBuilder routeKey(String routeKey) {
			this.routeKey = routeKey;
			return this;
		}

		public  RabbitMQTopicBuilder isCustomExchange(boolean isCustomExchange) {
			this.isCustomExchange = isCustomExchange;
			return this;
		}

		public RabbitMQTopic build() {
			return new RabbitMQTopic(this);
		}

	}

}
