package com.tuandai.transaction.client.model;


public class BeginLog {

	private String uid;

	private String serviceName;

	// RabbitMQTopic 序列化的对象或者其他
	private String topic;

	private String message;

	private String check;

	// MessageProperties 序列化的对象或者其他
	private String messageProperties;


	public BeginLog() {
	}

	public BeginLog(BeginLogBuilder builder) {
		uid = builder.uid;
		serviceName = builder.serviceName;
		topic = builder.topic;
		message = builder.message;
		check = builder.check;
		messageProperties = builder.messageProperties;
	}

	public static BeginLogBuilder newBeginLogBuilder() {
		return new BeginLogBuilder();
	}

    public static class BeginLogBuilder {

		private String uid;

		private String serviceName;

		private String topic;

		private String message;

		private String check;

		private String messageProperties;

		public BeginLogBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public BeginLogBuilder serviceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		public BeginLogBuilder topic(String topic) {
			this.topic = topic;
			return this;
		}

		public BeginLogBuilder message(String message) {
			this.message = message;
			return this;
		}
		public BeginLogBuilder check(String check) {
			this.check = check;
			return this;
		}

		public BeginLogBuilder messageProperties(String messageProperties) {
			this.messageProperties = messageProperties;
			return this;
		}

		public BeginLog build() {
			return new BeginLog(this);
		}

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

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getMessageProperties() {
		return messageProperties;
	}

	public void setMessageProperties(String messageProperties) {
		this.messageProperties = messageProperties;
	}

	@Override
	public String toString() {
		return "BeginLog{" +
				"uid='" + uid + '\'' +
				", serviceName='" + serviceName + '\'' +
				", topic='" + topic + '\'' +
				", message='" + message + '\'' +
				", check='" + check + '\'' +
				", messageProperties='" + messageProperties + '\'' +
				'}';
	}
}
