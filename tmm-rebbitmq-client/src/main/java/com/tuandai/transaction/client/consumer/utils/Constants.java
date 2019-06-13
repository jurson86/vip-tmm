package com.tuandai.transaction.client.consumer.utils;

public class Constants {

	public static final String DEAD_LETTER_EXCHANGE_NAME = "dle-tmm";

	public static final String DEAD_LETTER_PREFIX = "dlq--";

	public static final String DEAD_LETTER_SPLIT = "--";

	// 业务消息最大生命时长
	public static final int RETRY_DELAY = 6 * 3600 * 1000;

	// 死信队列
	public static final String dlq = "dlq-tmm";

	public static final long x_message_ttl_value = 21600000L;
}
