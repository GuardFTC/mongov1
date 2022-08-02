package com.ftc.mongotest.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2022-07-20 19:12:01
 * @describe: MongoDB实体类
 * <p>
 * “@Document(collection = "student")” 用于指定对应哪个集合
 */
@Data
@Document(collection = "student")
public class Student {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 学号
     */
    private String sno;

    /**
     * 性别
     */
    private String sex;

    /**
     * 名称
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 昵称
     */
    private String nikeName;

    /**
     * 描述
     */
    private String des;

    /**
     * 成绩
     */
    private Double grade;

    /**
     * 创建时间
     */
    private Date createTime;
}