package com.ftc.mongotest.config;

import lombok.Data;

import java.util.List;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-07-29 15:40:07
 * @describe: mongo配置属性
 */
@Data
public class MongoConfigProperties {

    /**
     * 数据源地址集合 格式{host}:{port}
     */
    private List<String> address;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 数据库名称
     */
    private String database;
}
