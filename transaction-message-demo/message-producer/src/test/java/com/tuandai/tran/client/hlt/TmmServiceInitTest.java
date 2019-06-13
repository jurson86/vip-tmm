package com.tuandai.tran.client.hlt;

import com.tuandai.tran.client.utis.DirUtis;
import com.tuandai.transaction.client.utils.ConstantUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class TmmServiceInitTest {


    @Before
    public void before() {
        // 删除rpc文件
        // 1 rpc文件夹不存在，创建rpc文件，且满足文件大小切分要求
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteDir(file);
    }



    @Test
    public void logDataPersistentTest() {


    }
}
