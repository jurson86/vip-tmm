package com.tuandai.transaction.client.service;

import com.tuandai.transaction.client.model.BeginLog;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * @author Gus Jiang
 * @date 2018/4/28  14:48
 */
public class TMMServiceHelperTest {

    @InjectMocks
    private TMMServiceHelper tmmServiceHelper;

    @Test(expected=IllegalArgumentException.class)
    public void checkEndLogParam_log_exception() throws IllegalArgumentException {
        tmmServiceHelper.checkBeginLogParam(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void checkEndLogParam_uid_exception() throws IllegalArgumentException {
        BeginLog beginlog = new BeginLog();
        tmmServiceHelper.checkBeginLogParam(beginlog);
    }



}
