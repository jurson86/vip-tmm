package com.tuandai.transaction.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class CacheMapFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(CacheMapFileUtils.class);

    //private static File directory = new File(System.getProperty("user.dir") + "/rpcPath");

    public static void writeDataToFile(File file, boolean append, WriteDataToFileProcess writeDataToFileProcess) {
        OutputStreamWriter writer = null;
        try {
            writeDataToFileProcess.beginProcess(file);
            writer = new OutputStreamWriter(new FileOutputStream(file, append));
            writeDataToFileProcess.process(writer);
            writer.flush();
        } catch (Exception e) {
            logger.error("write logMap file error : {}", e);
            throw new IllegalArgumentException("写文件失败！！");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                writeDataToFileProcess.endProcess(file);
            } catch (Exception e) {
                logger.error("write logMap close error: {}", e);
                throw new IllegalArgumentException("写文件失败！！");
            }
        }
    }

    public static void readDataToMap(File file, ReadDataToMapProcess readDataToMapProcess) {
        Reader reader = null;
        String keyValueStr = null;
        try {
             if (!file.exists()) {
                 return;
             }
             if (!readDataToMapProcess.beginProcess(file)) {
                 return;
             }
            reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(reader);
            boolean isContinue = readDataToMapProcess.preProcess(bufferedReader);
            while ((keyValueStr = bufferedReader.readLine()) != null && isContinue) {
                isContinue = readDataToMapProcess.process(keyValueStr);
            }
            bufferedReader.close();
            readDataToMapProcess.endProcess(file);
        } catch (Exception e) {
            logger.error("load logmap error: {}， 错误文本{}", e, keyValueStr);
            throw new IllegalArgumentException("读文件失败！！");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("close reader error: {}", e);
                throw new IllegalArgumentException("读文件失败！！");
            }
        }

    }

    public static File[] searchFile(FilenameFilter fileNameSelector, File directory) {
        // 找到rpc结尾的文件
        File[] listFile = directory.listFiles(fileNameSelector);
        return listFile;
    }

    public static File locationLastModifyFile(File[] listFile, boolean desc) {
        try {
            if (listFile.length > 0) {
                if (desc) {
                    sortDESC(listFile);
                } else {
                    sortASC(listFile);
                }
                return listFile[0];
            }
        } catch (Exception e) {
            logger.error("get check file error: {}", e);
        }
        return null;
    }

    public static File locationLastModifyFile(FileNameSelector fileNameSelector, File directory, boolean desc) {
        File[] listFile = searchFile(fileNameSelector, directory);
        return locationLastModifyFile(listFile, desc);
    }

    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件
     //param path 文件夹完整绝对路径
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    public static boolean existsFileName(String fileName) {
        File rpcFile = new File(fileName);
        return rpcFile.exists();
    }

    public static boolean removeFileName(File rpcFile) {
        boolean result = false;
        try {
            if (rpcFile.exists()) {
                result = rpcFile.delete();
            }
        } catch (Exception e) {
            logger.error("get check file error: {}", e);
        }
        return result;
    }


    public static boolean removeFileName(String fileName) {
        File rpcFile = null;
        boolean result = false;
        try {
            rpcFile = new File(fileName);
            if (rpcFile.exists()) {
                result = rpcFile.delete();
            }
        } catch (Exception e) {
            logger.error("get check file error: {}", e);
        }
        return result;
    }

    public static File createNewFileName(String fileName) {
        File rpcFile = null;
        try {
            rpcFile = new File(fileName);
            if (!rpcFile.exists()) {
                rpcFile.createNewFile();
            }
        } catch (IOException e) {
            logger.error("get check file error: {}", e);
        }
        return rpcFile;
    }

    /**
     * 降序
     */
    public static void sortDESC(File[] listFile) {
        Arrays.sort(listFile, new Comparator<File>() {

            @Override
            public int compare(File f1, File f2) {
                String f1Name = f1.getName();
                long f1Long = Long.valueOf(f1Name.substring(0, f1Name.indexOf(".")));
                String f2Name = f2.getName();
                long f2Long = Long.valueOf(f2Name.substring(0, f2Name.indexOf(".")));
                long diff = f1Long - f2Long;
                if (diff < 0) {
                    return 1;
                } else if (diff == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
            public boolean equals(Object obj) {
                return true;
            }
        });
    }


    /**
     * 文件列表排序, 从小到大
     *
     * @param listFile
     */
    public static void sortASC(File[] listFile) {
        Arrays.sort(listFile, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                String f1Name = f1.getName();
                long f1Long = Long.valueOf(f1Name.substring(0, f1Name.indexOf(".")));
                String f2Name = f2.getName();
                long f2Long = Long.valueOf(f2Name.substring(0, f2Name.indexOf(".")));
                long diff = f1Long - f2Long;
                if (diff > 0) {
                    return 1;
                } else if (diff == 0) {
                    return 0;
                }  else {
                    return -1;
                }
            }
            public boolean equals(Object obj) {
                return true;
            }
        });
    }

    public abstract static class WriteDataToFileProcess {

        protected void beginProcess(File file) throws Exception {
            if (!file.exists()) {
                file.createNewFile();
            }
        }

        protected abstract void process(OutputStreamWriter writer) throws Exception;

        protected void endProcess(File file) throws Exception {}

    }

    public abstract static class ReadDataToMapProcess {

        protected boolean beginProcess(File file) throws Exception {
            return true;
        }

        protected boolean preProcess(BufferedReader bufferedReader) throws Exception {
            return true;
        }

        protected abstract boolean process(String keyValueStr) throws Exception;

        protected void endProcess(File file) throws Exception {}

    }

}
