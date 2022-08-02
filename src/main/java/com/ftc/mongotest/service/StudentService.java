package com.ftc.mongotest.service;

import com.ftc.mongotest.entity.Student;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-07-29 14:15:54
 * @describe: Student业务实现类
 */
@Service
public class StudentService {

    @Resource
    @Qualifier(value = "primaryTemplate")
    private MongoTemplate primaryTemplate;

    @Resource
    @Qualifier(value = "secondaryTemplate")
    private MongoTemplate secondaryTemplate;

    /**
     * 主数据源保存
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "primaryMongoTransactionManager")
    public void savePrimaryStudent() {

        //1.创建对象
        Student student = new Student();
        student.setId(1);
        student.setName("主数据源数据");

        //2.保存
        primaryTemplate.insert(student);

        //3.执行异常
        int error = 1 / 0;
    }

    /**
     * 备数据源保存
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "secondaryMongoTransactionManager")
    public void saveSecondaryStudent() {

        //1.创建对象
        Student   student = new Student();
        student.setId(1);
        student.setName("从数据源数据");

        //2.保存
        secondaryTemplate.insert(student);

        //3.执行异常
        int error = 1 / 0;
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "transactionManagerConfig")
    public void saveStudentPrimaryAndSecondary(){

        //1.创建主数据源对象
        Student student = new Student();
        student.setId(1);
        student.setName("主数据源数据");

        //2.主数据源保存
        primaryTemplate.insert(student);

        //3.创建从数据源对象
        student = new Student();
        student.setId(1);
        student.setName("从数据源数据");

        //4.从数据源保存
        secondaryTemplate.insert(student);

        //5.执行异常
        int error = 1 / 0;
    }
}
