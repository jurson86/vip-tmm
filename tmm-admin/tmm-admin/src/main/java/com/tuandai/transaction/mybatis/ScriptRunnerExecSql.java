package com.tuandai.transaction.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author: guoguo
 * @Date: 2018/6/11 0011 14:09
 * @Description:
 */

@Component
public class ScriptRunnerExecSql {


    @Autowired
    private DataSource dataSource;

    public void runnerSql() {

       /* if (checkRunBefore()) {
            return;
        }*/
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ScriptRunner runner = new ScriptRunner(conn);
            Resources.setCharset(Charset.forName("UTF-8")); //设置字符集,不然中文乱码插入错误
            runner.setLogWriter(null);//设置是否输出日志
            runner.setStopOnError(true);
            runner.setAutoCommit(false);
            runner.runScript(Resources.getResourceAsReader("sql/init.sql"));
            runner.runScript(Resources.getResourceAsReader("sql/insert.sql"));
            conn.commit();
            conn.close();
            runner.closeConnection();
        } catch (Exception e) {
            try {
                if(conn!=null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private boolean checkRunBefore() {
        boolean flag = false;
        File file = new File(this.getPath() + "sql/init.ok");
        if (file.exists()) {
            flag = true;
        }
        return flag;
    }

    private String getPath() {
        /**String path = Thread.currentThread().getContextClassLoader().getResource("").toString();
        System.out.println(path);
        path = path.replace('/', '\\'); // 将/换成\
        path = path.replace("file:", ""); //去掉file:
        //path = path.replace("classes\\", ""); //去掉classes\
        path = path.replace("target\\", ""); //去掉target\
        path = path.substring(1); //去掉第一个\,如 \D:\JavaWeb...*/
        //文件添加下级目录地址

       return System.getProperty("user.dir")+File.separator;

    }
}
