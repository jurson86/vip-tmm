package com.tuandai.ar;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableRunnable;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

/**
 * @author Gus Jiang
 * @date 2019/3/12  15:55
 */
public class QuasarTest_01
{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(QuasarTest_01.class);

    private static RestTemplate rt = new RestTemplate();
    /**
     * 方法带有Suspendable 注解
     * 方法带有SuspendExecution
     * 方法为classpath下/META-INF/suspendables、/META-INF/suspendable-supers指定的类或接口,或子类
     * 符合上面条件的method,quasar会对其做call site分析,也许为了效率，quasar并没有对所有方法做call site分析
     *  throws SuspendExecution, InterruptedException
     */
    // or define in META-INF/suspendables
    @Suspendable
    static void m1() throws SuspendExecution, InterruptedException {
        String m = "m1";
        LOGGER.info("m1 begin");
        m = m3();
        m = m2();
        LOGGER.info("m1 end");
    }

    @Suspendable
    static String m2() throws InterruptedException, SuspendExecution {
            Strand.sleep(1000);
//        try {
//            rt.getForObject(URI.create("http://www.baidu.com1"), String.class);
//        }catch (Exception e){
//            ;
//        }
        LOGGER.info("m2=");
        return "m2";
    }

    @Suspendable
    static String m3() {
        LOGGER.info("m3=");
        return "m3";
    }
    static public void main(String[] args) throws ExecutionException, InterruptedException {
        new Fiber<Void>("Caller", new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                m1();
            }
        }).start();

        m3();
    }

}
