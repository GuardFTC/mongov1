package com.ftc.mongotest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-07-29 15:41:02
 * @describe: mongo配置类
 */
@Configuration
public class MongoConfig {

    @Primary
    @Bean(name = "primaryMongoProperties")
    @ConfigurationProperties(prefix = "spring.data.mongodb.primary")
    public MongoConfigProperties primaryMongoProperties() {
        return new MongoConfigProperties();
    }

    @Bean(name = "secondaryMongoProperties")
    @ConfigurationProperties(prefix = "spring.data.mongodb.secondary")
    public MongoConfigProperties secondaryMongoProperties() {
        return new MongoConfigProperties();
    }
}
