package com.tuandai.transaction.utils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author: guoguo
 * @Date: 2018/6/4 0004 16:31
 * @Description:
 */
public class MD5Utils {

    private static Logger LOGGER = LoggerFactory.getLogger(MD5Utils.class);

    public static String jdkMD5(String src) {
        Assert.notNull(src,"不能为空");
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5", e);
        }
        if(messageDigest == null){
            return null;
        }
        byte[] digest = messageDigest.digest(src.getBytes());
        return Hex.encodeHexString(digest);
    }

    /**
     *
     * @param src
     * @param time 加密次数
     * @return
     */
    public static String encryption(String src,int time){
        String after = src;
        do {
            after= jdkMD5(after);
            time --;
        }while (time>0);
        return after;
    }

    /*public static void main(String[] args) throws IOException {

        String path = System.getProperty("user.dir");
        System.out.println(path);

    }*/
}
