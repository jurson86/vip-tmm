package com.tuandai.ar.utils;

import com.tuandai.ar.domain.TModel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;

public class FileUtils {

    /**
     * Write file.
     *
     * @param fullFileName the full file name
     * @param contents     the contents
     */
    public static void writeFile(final String fullFileName, final byte[] contents) {
        try {
            FileOutputStream raf = new FileOutputStream(fullFileName,true);
            try (FileChannel channel = raf.getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocate(contents.length);
                buffer.put(contents);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
                channel.close();
            }
            raf.flush();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void writeObj(final String fullFileName, final byte[] contents) {
        try {
//            RandomAccessFile raf = new RandomAccessFile(fullFileName, "rw");
            FileOutputStream raf = new FileOutputStream(fullFileName,true);
            try (FileChannel channel = raf.getChannel()) {
                channel.write(ByteBuffer.wrap(contents), channel.size());
                channel.force(true);
                channel.close();
            }
            raf.flush();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static  byte[] readObj(final File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] content = new byte[1024];
            fis.read(content);
            return content;
        }
    }



}