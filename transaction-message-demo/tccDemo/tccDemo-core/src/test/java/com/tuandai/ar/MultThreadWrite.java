package com.tuandai.ar;

import co.paralleluniverse.fibers.SuspendExecution;

import java.io.*;
import java.lang.management.ThreadInfo;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Gus Jiang
 * @date 2019/3/19  15:07
 */
public class MultThreadWrite {

    static int position = 0;
    static FileChannel fileChannel;
    static File file;

    static void testThreadpool(int count) throws InterruptedException, IOException {

        file = new File("D:\\java_app\\workspace\\tdw\\transaction-message-demo\\tccDemo\\logs\\lllll.txt");
        fileChannel = new RandomAccessFile(file, "rw").getChannel();


        final CountDownLatch latch = new CountDownLatch(count);
        ExecutorService es = Executors.newFixedThreadPool(200);
        LongAdder latency = new LongAdder();
        long t = System.currentTimeMillis();
        for (int i =0; i< count; i++) {
            es.submit(() -> {
                long start = System.currentTimeMillis();
                try {
//                    mwiteData();
                    bwiteData();
                } catch (Exception e) {

                }
                start = System.currentTimeMillis() - start;
                latency.add(start);
                latch.countDown();
            });
        }
        latch.await();
        t = System.currentTimeMillis() - t;
        long l = latency.longValue() / count;
        System.out.println("thread pool took: " + t + ", latency: " + l + " ms");
        es.shutdownNow();


        fileChannel.close();
    }



    static synchronized void bwiteData() {
        OutputStreamWriter writer = null;

        long start = System.currentTimeMillis();
        String data1 = "1baaaaaaaaaaasa";

        int dataLength = data1.getBytes().length ;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file, true));
            writer.append(data1);
            writer.flush();

            start = System.currentTimeMillis() - start;
            System.out.println("===== : " + start);
        } catch (Exception ex) {
        }


        position = position + dataLength;
    }


    static synchronized void witeData() {
        long start = System.currentTimeMillis();
        String data1 = "1baaaaaaaaaaasa";

        int dataLength = data1.getBytes().length ;
        try {
            fileChannel.position(position );
            fileChannel.write((ByteBuffer.wrap(data1.getBytes())));
//            fileChannel.force(true);

            start = System.currentTimeMillis() - start;
            System.out.println("===== : " + start);
        } catch (Exception ex) {
        }


        position = position + dataLength;
    }

    static synchronized void mwiteData() {
        long start = System.currentTimeMillis();
        File file = new File("D:\\java_app\\workspace\\tdw\\transaction-message-demo\\tccDemo\\logs\\lllll.txt");
        String data1 = "1baaaaaaaaaaasa";
        String data2 = "2baaaaaaaacaaaaddd";
        String data3 = "3baaaaaaaaaaacccva";
        String data4 = "4baaaaaaaaaavvvvvvaa";
        String data5 = "5baaaaaaaaaassssssaa";
        String data6 = "6baaaaaaaaaaaaaaacaa";

        int dataLength = data1.getBytes().length + data2.getBytes().length + data3.getBytes().length + data4.getBytes().length + data5.getBytes().length + data6.getBytes().length;
        try {

            fileChannel.position(position + data1.getBytes().length + data2.getBytes().length + data3.getBytes().length + data4.getBytes().length + data5.getBytes().length);
            fileChannel.write((ByteBuffer.wrap(data6.getBytes())));
            fileChannel.position(position + data1.getBytes().length + data2.getBytes().length + data3.getBytes().length + data4.getBytes().length);
            fileChannel.write((ByteBuffer.wrap(data5.getBytes())));
            fileChannel.position(position + data1.getBytes().length + data2.getBytes().length + data3.getBytes().length);
            fileChannel.write((ByteBuffer.wrap(data4.getBytes())));
            fileChannel.position(position + data1.getBytes().length);
            fileChannel.write((ByteBuffer.wrap(data2.getBytes())));
            fileChannel.position(position + data1.getBytes().length + data2.getBytes().length);
            fileChannel.write((ByteBuffer.wrap(data3.getBytes())));
            fileChannel.position(position );
            fileChannel.write((ByteBuffer.wrap(data1.getBytes())));

            fileChannel.force(true);

            start = System.currentTimeMillis() - start;
            System.out.println("===== : " + start);
        } catch (Exception ex) {
        }


        position = position + dataLength;
    }



    static public void main(String[] args) throws Exception{
        testThreadpool(10000);
    }

}
