package com.ftc.mongotest.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2022-07-25 18:26:51
 * @describe:
 */
@Data
@Document(collection = "grade")
public class Grade {

    private String id;

    private String studentId;

    private String name;

    private Double score;
}
