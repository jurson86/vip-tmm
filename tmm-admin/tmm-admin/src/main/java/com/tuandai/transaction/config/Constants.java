package com.tuandai.transaction.config;

import com.google.common.collect.ImmutableList;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class Constants {

    public static final String MESSAGE_ID_SPLIT = ":";

    public static final String HTTP_HEAD = "http://";

    public static final String MONITOR_TAIL = "monitor/tmm";

    public static final int RESTFUL_MAX_TIMEOUT_SECONDS = 5;

    public static final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();

    public static final HttpHeaders header = new HttpHeaders();

    static {
        header.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON_UTF8));
        header.setContentType(MediaType.APPLICATION_JSON_UTF8);

        httpRequestFactory.setConnectionRequestTimeout(2000);
        httpRequestFactory.setConnectTimeout(2000);
        httpRequestFactory.setReadTimeout(2000);
    }


}
