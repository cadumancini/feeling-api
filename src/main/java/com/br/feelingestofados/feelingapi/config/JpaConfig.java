package com.br.feelingestofados.feelingapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration
public class JpaConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

    @Value("${env}")
    private String env;

    @Value("${spring.datasource.username.prod}")
    private String dataBaseUsernameProd;

    @Value("${spring.datasource.password.prod}")
    private String dataBasePasswordProd;

    @Value("${spring.datasource.username.test}")
    private String dataBaseUsernameTest;

    @Value("${spring.datasource.password.test}")
    private String dataBasePasswordTest;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String hibernateDdlAuto;

    @Value("${spring.jpa.database-platform}")
    private String hibernateDialect;

    @Bean(name="entityManagerFactory")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(getDataSource());
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource getDataSource()
    {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(driverClassName);
        dataSourceBuilder.url(dataBaseUrl);
        if (env.equals("prod")) {
            dataSourceBuilder.username(dataBaseUsernameProd);
            dataSourceBuilder.password(dataBasePasswordProd);
        } else {
            dataSourceBuilder.username(dataBaseUsernameTest);
            dataSourceBuilder.password(dataBasePasswordTest);
        }
        return dataSourceBuilder.build();
    }

    @Bean
    public PlatformTransactionManager hibernateTransactionManager() {
        HibernateTransactionManager transactionManager
                = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        hibernateProperties.setProperty("hibernate.dialect", hibernateDialect);

        return hibernateProperties;
    }
}