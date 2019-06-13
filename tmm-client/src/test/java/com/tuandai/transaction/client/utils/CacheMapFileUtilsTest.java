package com.tuandai.transaction.client.utils;

import com.tuandai.transaction.client.exception.ExceptionUtils;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class CacheMapFileUtilsTest {

    /**
     *  写日志，抛出异常
     */
    @Test
    public void writeDataToFileTest() {
        File file = new File("src/test/resources/writeFile.txt"); // 不存在
        try {
            CacheMapFileUtils.writeDataToFile(file, true, new CacheMapFileUtils.WriteDataToFileProcess() {
                @Override
                protected void process(OutputStreamWriter writer) throws Exception {
                    throw new IllegalAccessException("io 异常！！");
                }
            });
            ExceptionUtils.fail("写入日志文件的异常，必须抛出！");
        } catch (Exception e){}
    }

    /**
     *  读日志，抛出异常
     */
    @Test
    public void readDataToMapTest() {
        File file = new File("src/test/resources/writeFile.txt"); // 不存在
        try {
            CacheMapFileUtils.readDataToMap(file,   new CacheMapFileUtils.ReadDataToMapProcess() {

                @Override
                protected boolean process(String keyValueStr) throws Exception {
                    throw new IllegalAccessException("io 异常！！");
                }

                @Override
                protected void endProcess(File file) throws Exception {
                    throw new IllegalAccessException("io 异常！！");
                }
            });
            ExceptionUtils.fail("读文件的异常，必须抛出！");
        } catch (Exception e){}
    }

    @Test
    public void searchFileTest() {
        FilenameFilter fileNameSelector = new FileNameSelector("txt");
        File directory = new File("src/test/resources/");
        File[] files = CacheMapFileUtils.searchFile(fileNameSelector, directory);
        assertNotNull(files);
        assertNotEquals(files.length, 0);
        assertEquals(files[0].getName(), "writeFile.txt");
    }

    @Test
    public void locationLastModifyFileTest() {
        File[] files = new File[2];
        files[0] = new File("src/test/resources/sort/222.txt");
        files[1] = new File("src/test/resources/sort/111.txt");
        File file = CacheMapFileUtils.locationLastModifyFile(files, true); // 降序
        assertEquals(file.getName(), "222.txt");
        File file2 = CacheMapFileUtils.locationLastModifyFile(files, false); // 升序
        assertEquals(file2.getName(), "111.txt");

        //
        File file3 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("txt"),
                new File("src/test/resources/sort/"), true);
        assertEquals(file3.getName(), "222.txt");
        File file4 = CacheMapFileUtils.locationLastModifyFile(new FileNameSelector("txt"),
                new File("src/test/resources/sort/"), false);
        assertEquals(file4.getName(), "111.txt");
    }

    /**
     * 删除指定目录
     */
    @Test
    public void delFolderTest() throws IOException {
        // 创建目录
        File delDir = new File("src/test/resources/del");
        delDir.mkdir();
        File delDir2 = new File("src/test/resources/del/d");
        delDir2.mkdir();
        // 创建文件
        File del1 = new File("src/test/resources/del/del1");
        del1.createNewFile();
        File del2 = new File("src/test/resources/del/del2");
        del2.createNewFile();
        File del3 = new File("src/test/resources/del/d/del3");
        del3.createNewFile();
        assertEquals(delDir2.exists(), true);
        assertEquals(del3.exists(), true);
        assertEquals(delDir.exists(), true);
        assertEquals(del1.exists(), true);
        assertEquals(del2.exists(), true);

        // 删除文件夹
        CacheMapFileUtils.delFolder("src/test/resources/del");
        assertEquals(delDir.exists(), false);
        assertEquals(del1.exists(), false);
        assertEquals(del2.exists(), false);
        assertEquals(delDir2.exists(), false);
        assertEquals(del3.exists(), false);

    }

    @Test
    public void removeFileNameTest() throws IOException {
        File delDir = new File("src/test/resources/remove");
        delDir.createNewFile();
        assertEquals(delDir.exists(), true);
        CacheMapFileUtils.removeFileName(delDir);
        assertEquals(delDir.exists(), false);

        File delDir2 = CacheMapFileUtils.createNewFileName("src/test/resources/remove2");
        delDir2.createNewFile();
        assertEquals(delDir2.exists(), true);
        CacheMapFileUtils.removeFileName("src/test/resources/remove2");
        assertEquals(delDir2.exists(), false);

    }


}
