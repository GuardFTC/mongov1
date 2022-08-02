package com.ftc.mongotest.config;

import cn.hutool.core.util.StrUtil;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-07-29 15:44:02
 * @describe: mongo主数据源各种配置类
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.ftc.mongotest", mongoTemplateRef = "primaryTemplate")
public class PrimaryMongoTemplate {

    @Resource
    @Qualifier("primaryMongoProperties")
    private MongoConfigProperties primaryProperties;

    @Primary
    @Bean(name = "primaryTemplate")
    public MongoTemplate primaryTemplate() {

        //1.获取primaryTemplate
        SimpleMongoClientDatabaseFactory primaryFactory = getFactory(this.primaryProperties);
        MongoTemplate primaryTemplate = new MongoTemplate(primaryFactory);

        //2.默认数据源监听处理
        String type = "_class";
        MongoConverter converter = primaryTemplate.getConverter();
        if (converter.getTypeMapper().isTypeKey(type)) {
            ((MappingMongoConverter) converter).setTypeMapper(new DefaultMongoTypeMapper(null));
        }

        //3.返回
        return primaryTemplate;
    }

    @Bean(name = "primaryMongoTransactionManager")
    public MongoTransactionManager primaryTransactionManager() {
        SimpleMongoClientDatabaseFactory factory = getFactory(this.primaryProperties);
        return new MongoTransactionManager(factory);
    }

    @Primary
    @Bean(name = "primaryGridFsTemplate")
    public GridFsTemplate primaryGridFsTemplate() {
        SimpleMongoClientDatabaseFactory factory = getFactory(this.primaryProperties);
        MappingMongoConverter converter = new MappingMongoConverter(factory, new MongoMappingContext());
        return new GridFsTemplate(factory, converter);
    }

    @Primary
    @Bean("primaryFactory")
    public SimpleMongoClientDatabaseFactory getFactory(MongoConfigProperties properties) {

        //1.设置链接地址
        List<ServerAddress> hosts = new ArrayList<>();
        properties.getAddress().forEach(address -> {
            List<String> addressInfos = StrUtil.split(address, StrUtil.COLON);
            hosts.add(new ServerAddress(addressInfos.get(0), Integer.parseInt(addressInfos.get(1))));
        });

        //2.初始化连接池参数
        ConnectionPoolSettings poolSetting = ConnectionPoolSettings
                .builder()
                .maxWaitTime(10000, TimeUnit.MILLISECONDS)
                .build();

        //3.构造基础链接参数
        MongoClientSettings.Builder settingBuilder = MongoClientSettings
                .builder()
                .applyToConnectionPoolSettings(builder -> builder.applySettings(poolSetting))
                .applyToClusterSettings(builder -> builder.hosts(hosts))
                .readPreference(ReadPreference.secondaryPreferred());

        //4.初始链接参数以及连接池参数
        MongoClientSettings settings;

        //5.根据用户名是否为空判定是否鉴权
        if (StrUtil.isNotBlank(properties.getUsername())) {

            //6.添加授权参数
            MongoCredential credential = MongoCredential.createScramSha1Credential(
                    properties.getUsername(), properties.getDatabase(), properties.getPassword().toCharArray()
            );

            //7.添加链接参数
            settings = settingBuilder.credential(credential).build();
        } else {

            //7.添加链接参数
            settings = settingBuilder.build();
        }

        //8.创建工厂返回
        return new SimpleMongoClientDatabaseFactory(MongoClients.create(settings), properties.getDatabase());
    }
}
