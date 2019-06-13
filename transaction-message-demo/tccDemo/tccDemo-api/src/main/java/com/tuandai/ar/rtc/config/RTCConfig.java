package com.tuandai.ar.rtc.config;

import lombok.Data;

/**
 * @author Gus Jiang
 * @date 2019/3/11  15:54
 */
@Data
public class RTCConfig {

    /**
     * 存储方式  file , mysql.
     */
    private String repositorySupport = "mysql";

    /**
     * 事务日志存储表或日志前缀
     */
    private String repositoryPrefix = "rtc";

    /**
     * 事务参与对象序列化方式
     */
    private String repositoryObjSerializer = "kryo";

    /**
     *  事务超时时间: 生命周期（Time To Live）， 事务超时后，此事务将不再由生成事务的实例处理，将交付定时检索实例处理；因此此时间不宜设置过短！
     */
    private int scheduleTll = 60;

    /**
     *  同一事务定时获取try数据状态的阀值
     */
    private int scheduleTryRetryMax = 5;

    /**
     *  定时获取try状态的任务延时策略,按执行次数，进行不同的延时时间； 如： 1,2,3 则 第一次延时1s，第二次2s，第三次 3s ；
     */
    private String scheduleTryDelayTime = "2,3,4,5,10";

    /**
     *  同一事务定时CC的阀值
     */
    private int scheduleCCRetryMax = 5;

    /**
     *  CC定时任务延时策略,按执行次数，进行不同的延时时间； 如： 1,2,3 则 第一次延时1s，第二次2s，第三次 3s ；
     */
    private String scheduleCCDelayTime = "2,3,4,5,10";

    /**
     * 执行事务CC操作的线程池大小
     */
    private int scheduleCCThreadMax = Runtime.getRuntime().availableProcessors() << 1;

}
