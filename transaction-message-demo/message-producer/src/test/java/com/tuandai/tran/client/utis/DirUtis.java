package com.tuandai.tran.client.utis;

import com.tuandai.transaction.client.utils.CacheMapFileUtils;
import com.tuandai.transaction.client.utils.ConstantUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirUtis {

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        boolean success = dir.delete();
        System.out.println("删除文件夹：" + dir.getAbsolutePath() + ":" + success);
        return  success;
    }


    public static boolean deleteSubDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return  true;
    }

    public static int checkFileSize(File dir, String pattern) {
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        int size = 0;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i=0; i< children.length; i++) {
                Matcher match = r.matcher(children[i]);
                if (match.find()) {
                    size++;
                }
            }
        }
        return size;
    }

    // 遍历一个目录下的所有文件
    public static void goThroughList(File dir) {
        String[] fs = dir.list();
        for (String fileStr : fs) {
            final Integer counts = 0;
            File file = new File(dir.getAbsolutePath() + "\\" + fileStr);
            CacheMapFileUtils.readDataToMap(file, new CacheMapFileUtils.ReadDataToMapProcess() {

                private int count = 0;
                @Override
                protected boolean process(String keyValueStr) throws Exception {
                    if (keyValueStr.equals("RPC_FILE_END")) {
                        count++;
                        //counts = count;
                    } else if (keyValueStr == null) {
                        return false;
                    }
                    return true;
                }
            });

        }
    }

    public static void main(String[] args) {
          goThroughList(new File(ConstantUtils.DEFAULT_RPC_PATH));
    }


}
