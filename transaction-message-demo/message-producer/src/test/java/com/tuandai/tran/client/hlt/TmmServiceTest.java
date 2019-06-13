package com.tuandai.tran.client.hlt;


import com.tuandai.tran.ProducerApplication;
import com.tuandai.tran.client.utis.DirUtis;
import com.tuandai.transaction.client.bo.EventDefinition;
import com.tuandai.transaction.client.bo.SendState;
import com.tuandai.transaction.client.model.BeginLog;
import com.tuandai.transaction.client.model.EndLog;
import com.tuandai.transaction.client.service.TMMServiceImpl;
import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;
import com.tuandai.transaction.client.utils.FileNameSelector;
import com.tuandai.transaction.producer.model.ExchangeType;
import com.tuandai.transaction.producer.model.RabbitMQTopic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * 设置条件
 * 文件大小为：LogEventServiceImpl.MAX_FILE_SIZE = 100 * 1024;
 * spring.tmmService.batch.size=2000
 * spring.tmmService.check.interval.time=10000
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = ProducerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TmmServiceTest {

    // 精准字符串，切勿改动！！!!!!
    final static String size_1KB = "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwqwqwqweqwewwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwqwwwwwwwwwwwwwwwwwwwwwwwwwwqwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwweeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeweeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeewwwwwwwwwwwwwwwwwwwwwwwewweewweeeeeeewwwwwwwweeeeeeeee";

    @Autowired
    private TMMServiceImpl tmmService;

    private File file =   new File(System.getProperty("user.dir") + "/rpcPath");

    @Value("${rpc.size}")
    private long rpcSize;

    @Before
    public void before() {

    }

    /**
     * 日志数据持久化刷盤【增量刷盘】
     * 条件：
     *     rpc文件是否存在，不存在创建，
     *     存在获取最新文件进行写入，
     *     文件大小满足切分要求，需要创建最新文件；
     */
    /**
     * 获取下一个检测文件
     * 初始化 rpcFile
     * 文件列表为空，创建文件;否则获取最新 .rpc 文件
     * @return
     */
    /**
     * 按更新时间排序,最新文件在前
     * @param listFile
     */
    /**
     * 按照时间戳创建新的rpc文件
     * @return
     */
    @Test
    public void logDataPersistentTest() throws InterruptedException {
        // 前提：删除所有的.rpc文件
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteSubDir(file);

        File fs0 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file, true);
        assertNull(fs0);

        // 1.前提：在.rpc文件不存在，创建.rpc文件，测试文件大小是否满足切分情况
        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
        rabbitMQTopic.setRouteKey("");
        // 打印开始日志
        BeginLog beginLog = new BeginLog();
        beginLog.setCheck("tmm/check");
        beginLog.setMessage(size_1KB);
        beginLog.setServiceName("tmm");
        beginLog.setTopic(rabbitMQTopic.toJSONString());
        beginLog.setUid("uid-test-123");

        // beginLog 大小为1Kb
        int i=1100;
        int b = i; // 101kb一个文件，则1100 = 101 * 9 + 90kb
        while (i-- > 0)  {
            tmmService.sendTransBeginToFlume(beginLog);
        }
        int count = DirUtis.checkFileSize(new File(ConstantUtils.DEFAULT_RPC_PATH), "^1");
        // 遍历检查文件


        assertEquals(count, b/101 + 2);
        // 最新的一个.prc文件大小为90kb
        File fs = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file, true);
        assertEquals(fs.length(), 98912);

        // 2.当.rpc文件存在的时候则写入，写入文件定位为最新的一个rpc文件上
        tmmService.sendTransBeginToFlume(beginLog);
        File fs2 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,true);
        assertEquals(fs2.length(), 100036);
        int a = 11;
        while (a-- > 0) {
            tmmService.sendTransBeginToFlume(beginLog);
        }
        File fs3 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,true);
        assertEquals(fs2.getName(), fs3.getName()); // 另外一个文件

        // 删除最新的一个.rpc文件
        // 3.有.rpc文件并且不全不是.done结尾的，但是文件内容都是以结束符为结尾的，则生在新的文件
        fs3.delete();
        File fs4 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,true);
        tmmService.sendTransBeginToFlume(beginLog);
        File fs5 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file, true);
        assertNotEquals(fs4.getName(), fs5.getName()); // 另外一个文件

        // 4.有.rpc文件但都是.done结尾的创建新的.rpc文件
        fs5.delete();
        Thread.sleep(10000);
        File rpc = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,true);
        assertNull(rpc); // 理论上6s已经所以的文件都被处理成 以.done结尾了，如果此处报错则可加大上面沉睡的时间
        // 写入一条消息
        tmmService.sendTransBeginToFlume(beginLog);
        // 检查是否生产新的文件
        File newRpc = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,true);
        assertNotNull(newRpc);
        assertEquals(newRpc.length(), 1081);

        // 顺序执行以下测试用例
        {
            checkpointTest();
            checkpointTest2();
            beginMapTest();
        }
    }

    /**
     * 测试多线程写入日志，是否能安全的进行写入
     */
    @Test
    public void multWriteLog() throws InterruptedException {
        // 前提：删除所有的.rpc文件
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteSubDir(file);

        File fs0 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file, true);
        assertNull(fs0);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        int i = 1000;
        int b = i;
        while (i-- > 0) {
            int finalI = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
                    rabbitMQTopic.setvHost("myVhost");
                    rabbitMQTopic.setExchange("myExchange2");
                    rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
                    rabbitMQTopic.setRouteKey("");
                    // 打印开始日志
                    BeginLog beginLog = new BeginLog();
                    beginLog.setCheck("tmm/check");
                    beginLog.setMessage(size_1KB);
                    beginLog.setServiceName("tmm");
                    beginLog.setTopic(rabbitMQTopic.toJSONString());
                    beginLog.setUid("uid-test-123--" + finalI);

                    tmmService.sendTransBeginToFlume(beginLog);
                }
            });
        }

        Thread.sleep(10000);
        // 生成的文件数量
        int count = DirUtis.checkFileSize(new File(ConstantUtils.DEFAULT_RPC_PATH), "^4");

        assertEquals(count, b/(rpcSize/1024));

        // 校验是否正确生成
        File dir = new File(ConstantUtils.DEFAULT_RPC_PATH);
        // 得到子列表
        String[] flist = dir.list();






    }



    /**
     * checkpoint 为空
     * 重新获取最旧 check文件；
     * @return
     */
    /**
     * checkpoint 标记文件不存在处理方案,【异常方案】（如kill掉，此时已经更改为： .done）
     * 重新获取最旧 check文件；
     * @return
     */
    //@Test
    public void checkpointTest() throws InterruptedException {
        // 前提：删除所有的.rpc文件以及.done文件
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteSubDir(file);
        // 删除checkpoint文件
        File checkpointfile = new File(ConstantUtils.DEFAULT_RPC_PATH + "/checkpoint");
        Thread.sleep(5000);
        boolean exists = checkpointfile.exists();
        assertEquals(exists, true);
        assertEquals(checkpointfile.length(), 0); // 大小0Kb


        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
        rabbitMQTopic.setRouteKey("");

        // 打印开始日志
        BeginLog beginLog = new BeginLog();
        beginLog.setCheck("tmm/check");
        beginLog.setMessage(size_1KB);
        beginLog.setServiceName("tmm");
        beginLog.setTopic(rabbitMQTopic.toJSONString());
        beginLog.setUid("uid-test-123");
        int i = 102;
        while (i-- > 0) {
            tmmService.sendTransBeginToFlume(beginLog);
        }
        Thread.sleep(2000); // 沉睡一秒，检查checkpoint是否已经变更
        File rpc = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file, false);
        assertNotNull(rpc);
        // 1.checkpoint内容不存在，则提取最旧的rpc文件为checkpoint的值
        boolean exists2 = checkpointfile.exists();
        assertEquals(exists2, true);
        assertNotEquals(checkpointfile.length(), 0); // 不为空
        // 判断checkpoint的值指向的是当前最新的文件名称 和 1023字节处（消息大小位置）
        CacheMapFileUtils.readDataToMap(checkpointfile, new CacheMapFileUtils.ReadDataToMapProcess() {
            @Override
            protected boolean process(String keyValueStr) throws Exception {
                assertEquals(keyValueStr, rpc.getAbsolutePath() + "\t" + "7567");
                return true;
            }
        });

        Thread.sleep(1000);
        // 2.删除checkpoint文件，则还是提取最旧的rpc文件为checkpoint的值
        boolean asdasd = checkpointfile.delete();
        assertEquals(checkpointfile.exists(), false);
        Thread.sleep(1000); // 沉睡一秒，检查checkpoint是否已经变更
        assertEquals(checkpointfile.exists(), true);
        assertNotEquals(checkpointfile.length(), 0); // 不为空
        CacheMapFileUtils.readDataToMap(checkpointfile, new CacheMapFileUtils.ReadDataToMapProcess() {
            @Override
            protected boolean process(String keyValueStr) throws Exception {
                assertEquals(keyValueStr, rpc.getAbsolutePath() + "\t" + "7567");
                return true;
            }
        });

    }

    /**
     * checkpoint 获取数据文件结束，更改文件名
     * 重新获取最新文件数据
     * @return
     */
    //@Test
    public void checkpointTest2() throws InterruptedException {

        // 前提：删除所有的.rpc文件以及.done文件
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteSubDir(file);
        // 删除checkpoint文件
        File checkpointfile = new File(ConstantUtils.DEFAULT_RPC_PATH + "/checkpoint");
        Thread.sleep(5000);
        boolean exists = checkpointfile.exists();
        assertEquals(exists, true);
        assertEquals(checkpointfile.length(), 0); // 大小0Kb

        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
        rabbitMQTopic.setRouteKey("");
        // 打印开始日志
        BeginLog beginLog = new BeginLog();
        beginLog.setCheck("tmm/check");
        beginLog.setMessage(size_1KB);
        beginLog.setServiceName("tmm");
        beginLog.setTopic(rabbitMQTopic.toJSONString());
        beginLog.setUid("uid-test-123");

        int i = 1011;
        while (i-- > 0) {
            tmmService.sendTransBeginToFlume(beginLog);
        }

        Thread.sleep(6000);
        File[] dones = CacheMapFileUtils.searchFile(new FileNameSelector("done"), file);
        assertEquals(dones.length, 10);
        File rpc = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("rpc"), file,false);

        CacheMapFileUtils.readDataToMap(checkpointfile, new CacheMapFileUtils.ReadDataToMapProcess() {
            @Override
            protected boolean process(String keyValueStr) throws Exception {
                assertEquals(keyValueStr, rpc.getAbsolutePath() + "\t" + "65941");
                return true;
            }
        });
    }


    /**处理type 数据
     * 直接进入beginMap
     *
     * 失败：事务结束
     * @return
     */
    //@Test
    public void beginMapTest() throws InterruptedException {
        // beginMap数据持久化
        // 前提：删除所有的.rpc文件以及.done文件
        File file = new File(ConstantUtils.DEFAULT_RPC_PATH);
        DirUtis.deleteSubDir(file);
        Thread.sleep(2000);
        Map<String, EventDefinition> beginMap = tmmService.getBeginMap();
        // 删除内存的数据
        beginMap.clear();
        // 添加测试数据(模拟加载磁盘的未处理数据）
        for (int i = 0; i < 20; i++) {
            String uid = "uid-test-" + i;
            beginMap.put(uid, getEventBegin(uid));
        }

        // 写入日志
        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
        rabbitMQTopic.setRouteKey("");

        int i = 210;
        while (i-- > 100) {
            BeginLog beginLog = new BeginLog();
            beginLog.setCheck("tmm/check");

            beginLog.setMessage(size_1KB);
            beginLog.setServiceName("tmm");
            beginLog.setTopic(rabbitMQTopic.toJSONString());
            beginLog.setUid("uid-test-" + i);
            tmmService.sendTransBeginToFlume(beginLog);

            EndLog endLog = new EndLog();
            endLog.setUid("uid-test-" + i);
            endLog.setTopic(rabbitMQTopic.toJSONString());
            endLog.setServiceName("tmm");
            endLog.setMessage(size_1KB);
            if (i == 120 || i == 130) {
                endLog.setState(SendState.CANCEL);
            } else {
                endLog.setState(SendState.COMMIT);
            }
            tmmService.sendTransEndToFlume(endLog);
        }

        // 20s后则begin.map.check 里面有开始的20条数据
        Thread.sleep(15000);

        // TODO 需要人工查看是否已经发送到mq上 (tmmVhost下的tmm-check-queue队列发送了一条有20条check消息的消息，myVhost下的demoQu有108条新消息产生)

    }



    // 测试rabbitmq挂掉后数据是否正确
    //@Test
    public void rabbitmqTest() {
        // 写入日志
        RabbitMQTopic rabbitMQTopic = new RabbitMQTopic();
        rabbitMQTopic.setvHost("myVhost");
        rabbitMQTopic.setExchange("myExchange2");
        rabbitMQTopic.setExchangeType(ExchangeType.FANOUT.des());
        rabbitMQTopic.setRouteKey("");
        int i = 210;
        while (true) {
            BeginLog beginLog = new BeginLog();
            beginLog.setCheck("tmm/check");
            beginLog.setMessage(size_1KB);
            beginLog.setServiceName("tmm");
            beginLog.setTopic(rabbitMQTopic.toJSONString());
            beginLog.setUid("uid-test-" + i);
            tmmService.sendTransBeginToFlume(beginLog);

            EndLog endLog = new EndLog();
            endLog.setUid("uid-test-" + i);
            endLog.setTopic(rabbitMQTopic.toJSONString());
            endLog.setServiceName("tmm");
            endLog.setMessage(size_1KB);
            endLog.setState(SendState.COMMIT);
            tmmService.sendTransEndToFlume(endLog);
        }


    }


    private EventDefinition getEventBegin(String uid) {
        EventDefinition event = new EventDefinition();
        event.setUid(uid);
        event.setCheckUrl("tmm/check");
        event.setEventType(EventDefinition.EventType.BEGIN);
        event.setTime(new Date().getTime());
        event.setServiceName("tmm");
        event.setMessage("helloword");
        event.setTopic("{'exchange':'myExchange2','exchangeType':'fanout','vHost':'myVhost', 'routeKey':''}");
        return event;
    }

    private EventDefinition getEventEnd(String uid) {
        EventDefinition event = new EventDefinition();
        event.setUid(uid);
        event.setEventType(EventDefinition.EventType.END);
        event.setTime(new Date().getTime());
        event.setServiceName("test");
        event.setSendState(SendState.COMMIT);
        event.setMessage("helloword");
        event.setTopic("{'exchange':'tmm-test','exchangeType':'fanout','vHost':'tmmVhost', 'routeKey':''}");
        return event;
    }

}
