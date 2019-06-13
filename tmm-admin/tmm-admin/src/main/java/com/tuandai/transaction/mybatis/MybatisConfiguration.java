package com.tuandai.transaction.mybatis;

import com.github.pagehelper.PageHelper;
import com.tuandai.transaction.plugins.CatMybatisPlugins;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;


@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan("com.tuandai.transaction.repository")
public class MybatisConfiguration {

    /**
     * mybatis 配置路径
     */
    private static String MYBATIS_CONFIG = "META-INF/mybatis-config.xml";
    /**
     * mybatis mapper resource 路径
     */
    private static String MAPPER_PATH = "META-INF/mappers/**.xml";

    @Autowired
    private DataSource dataSource;

    private String typeAliasPackage = "com.tuandai.transaction.domain";


    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactoryBean createSqlSessionFactoryBean() throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource(MYBATIS_CONFIG));

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + MAPPER_PATH;
        sqlSessionFactoryBean.setMapperLocations(pathMatchingResourcePatternResolver.getResources(packageSearchPath));
        sqlSessionFactoryBean.setDataSource(dataSource);

        sqlSessionFactoryBean.setPlugins(new Interceptor[]{new CatMybatisPlugins(), pageHelper()});

        //sqlSessionFactoryBean.setTypeAliasesPackage(typeAliasPackage);
        return sqlSessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public PageHelper pageHelper() {
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("dialect", "mysql");    //配置mysql数据库的方言
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
