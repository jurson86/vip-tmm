package com.tuandai.transaction.client.model;

import com.tuandai.transaction.client.bo.SendState;

public class EndLog {

	private SendState state;

	private String uid;

	private String serviceName;

	private String topic;

	private String message;

	public EndLog() {}

	public EndLog(EndLogBuilder builder) {
		uid = builder.uid;
		serviceName = builder.serviceName;
		topic = builder.topic;
		message = builder.message;
		state = builder.state;
	}

	public static EndLogBuilder newEndLogBuilder() {
		return new EndLogBuilder();
	}

	public static class EndLogBuilder {

		private SendState state;

		private String uid;

		private String serviceName;

		private String topic;

		private String message;

		public EndLogBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public EndLogBuilder serviceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		public EndLogBuilder topic(String topic) {
			this.topic = topic;
			return this;
		}

		public EndLogBuilder message(String message) {
			this.message = message;
			return this;
		}
		public EndLogBuilder state(SendState state) {
			this.state = state;
			return this;
		}

		public EndLog build() {
			return new EndLog(this);
		}

	}

	public SendState getState() {
		return state;
	}

	public void setState(SendState state) {
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

	@Override
	public String toString() {
		return "EndLog{" +
				"state=" + state +
				", uid='" + uid + '\'' +
				", serviceName='" + serviceName + '\'' +
				", topic='" + topic + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
