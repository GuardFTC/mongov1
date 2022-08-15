package com.ftc.mongotest;

import cn.hutool.core.lang.Assert;
import cn.hutool.db.nosql.mongo.MongoFactory;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.junit.jupiter.api.Test;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-08-15 12:56:21
 * @describe: HutoolMongoTest
 */
public class HutoolMongoTest {

    @Test
    void testInsert(){

        //1.获取mongo客户端
        MongoDatabase db = MongoFactory.getDS("master").getDb("java_test");

        //2.存储数据
        Document document = new Document();
        document.put("name","ftc");
        InsertOneResult result = db.getCollection("student").insertOne(document);
        Assert.isTrue(result.wasAcknowledged());

        //3.查询数据
        FindIterable<Document> student = db.getCollection("student").find();
        System.out.println(1);
    }
}
