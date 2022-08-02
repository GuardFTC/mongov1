package com.ftc.mongotest.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2022-07-25 20:18:40
 * @describe:
 */
@Data
@Document(collection = "geo")
public class Flat {

    private String id;

    private String name;

    private GeoJsonPoint point;
}
