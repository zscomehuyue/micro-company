package com.idugalic.commandside.blog.configuration;


import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.config.java.ServiceConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

public class CloudConfiguration extends AbstractCloudConfig{
    
    @Bean
    public DataSource dataSource() {

        ServiceConnectionFactory connectionFactory = connectionFactory();
        System.err.println("=dataSource=>connectionFactory: "+connectionFactory.getClass().getName());

        DataSource dataSource = connectionFactory.dataSource();
        System.err.println("=dataSource=>dataSource: "+dataSource.getClass().getName());
        return dataSource;
    }
    
    @Profile("cloud")
    @Bean()
    public ConnectionFactory rabbitFactory() {
        ServiceConnectionFactory connectionFactory = connectionFactory();
        System.err.println("=rabbitFactory=>connectionFactory: "+connectionFactory.getClass().getName());
        return connectionFactory.rabbitConnectionFactory();
    }


}
