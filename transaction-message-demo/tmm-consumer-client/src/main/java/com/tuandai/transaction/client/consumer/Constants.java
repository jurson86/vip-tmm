package com.tuandai.transaction.client.consumer;

public class Constants {
	public static final String DEAD_LETTER_EXCHANGE_NAME = "dle-tmm";

	public static final String DEAD_LETTER_PREFIX = "dlq--";

	public static final String DEAD_LETTER_SPLIT = "--";

	// 业务消息最大生命时长
	public static final int RETRY_DELAY = 6 * 3600 * 1000;

}
