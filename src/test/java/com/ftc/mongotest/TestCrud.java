package com.ftc.mongotest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ftc.mongotest.entity.Student;
import com.ftc.mongotest.service.StudentService;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2022-07-23 19:02:32
 * @describe: 增删改查测试
 */
@SpringBootTest
public class TestCrud {

    @Resource
    @Qualifier("primaryGridFsTemplate")
    private GridFsTemplate primaryGridFsTemplate;

    @Resource
    @Qualifier("secondaryGridFsTemplate")
    private GridFsTemplate secondaryGridFsTemplate;

    @Test
    void testPut() {

        //1.获取文件输入流
        BufferedInputStream inputStream = FileUtil.getInputStream("C:\\Users\\86176\\Desktop\\OIP-C.jpg");

        //2.设置文件备注
        HashMap<String, Object> metadata = MapUtil.newHashMap(2);
        metadata.put("作者", "ftc");
        metadata.put("作用", "学习使用");

        //3.主数据源存入文件
        ObjectId store = primaryGridFsTemplate.store(inputStream, "testPrimary.jpg", metadata);
        Assert.isTrue(ObjectUtil.isNotNull(store));

        //4.获取文件输入流
        inputStream = FileUtil.getInputStream("C:\\Users\\86176\\Desktop\\OIP-C.jpg");

        //5.设置文件备注
        metadata = MapUtil.newHashMap(2);
        metadata.put("作者", "ftc");
        metadata.put("作用", "学习使用");

        //4.备数据源存入文件
        store = secondaryGridFsTemplate.store(inputStream, "testSecondary.jpg", metadata);
        Assert.isTrue(ObjectUtil.isNotNull(store));
    }

    @Test
    void testGet() throws IOException {

        //1.主数据源获取文件
        GridFSFile gridFSFile = primaryGridFsTemplate.findOne(new Query(Criteria.where("metadata.作用").regex("学")));

        //2.主数据源获取文件流
        GridFsResource resource = primaryGridFsTemplate.getResource(gridFSFile);

        //3.主数据源文件写入
        InputStream inputStream = resource.getInputStream();
        byte[] bytes = IoUtil.readBytes(inputStream);
        File file = FileUtil.writeBytes(bytes, "C:\\Users\\86176\\Desktop\\copyPrimary.jpg");
        Assert.isTrue(file.exists());

        //4.主数据源获取文件
        gridFSFile = secondaryGridFsTemplate.findOne(new Query(Criteria.where("metadata.作用").regex("学")));

        //5.主数据源获取文件流
        resource = secondaryGridFsTemplate.getResource(gridFSFile);

        //6.主数据源文件写入
        inputStream = resource.getInputStream();
        bytes = IoUtil.readBytes(inputStream);
        file = FileUtil.writeBytes(bytes, "C:\\Users\\86176\\Desktop\\copySecondary.jpg");
        Assert.isTrue(file.exists());
    }

    @Test
    void testList() {

        //1.主数据源获取文件
        GridFSFindIterable gridFSFiles = primaryGridFsTemplate.find(new Query());

        //2.循环遍历
        for (GridFSFile gridFSFile : gridFSFiles) {
            System.out.println(gridFSFile.getFilename());
        }

        //3.备数据源获取文件
        gridFSFiles = secondaryGridFsTemplate.find(new Query());

        //4.循环遍历
        for (GridFSFile gridFSFile : gridFSFiles) {
            System.out.println(gridFSFile.getFilename());
        }
    }

    @Test
    void testDelete() {

        //1.主数据源删除文件
        primaryGridFsTemplate.delete(new Query());

        //2.验证
        GridFSFindIterable gridFSFiles = primaryGridFsTemplate.find(new Query());
        Assert.isTrue(CollUtil.isEmpty(gridFSFiles));

        //3.备数据源删除文件
        secondaryGridFsTemplate.delete(new Query());

        //4.验证
        gridFSFiles = secondaryGridFsTemplate.find(new Query());
        Assert.isTrue(CollUtil.isEmpty(gridFSFiles));
    }


    @Resource
    @Qualifier(value = "primaryTemplate")
    private MongoTemplate primaryTemplate;

    @Resource
    @Qualifier(value = "secondaryTemplate")
    private MongoTemplate secondaryTemplate;

    @BeforeEach
    void beforeAll() {
        primaryTemplate.remove(new Query(), Student.class);
        secondaryTemplate.remove(new Query(), Student.class);
    }

    @Test
    void testInsert() {

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
    }

    @Test
    void mapReduce() {

        //1.定义存入集合
        List<Student> students = CollUtil.newArrayList();

        //2.循环封装数据
        for (int i = 0; i < 10; i++) {
            Student student = new Student();
            student.setId(i);
            student.setName("ftc" + i);
            student.setSex(i % 2 == 0 ? "男" : "女");
            student.setAge(i);
            student.setGrade((double) (i + 10));
            students.add(student);
        }

        //3.测试数据存入集合
        primaryTemplate.insert(students, Student.class);

        //4.定义Map以及Reduce方法
        String mapFunction = "function() { emit(this.sex,this.name); }";
        String reduceFunction = "function(key, values) { return values.join('; ')}";

        //5.定义集合过滤条件
        Query query = new Query(Criteria.where("age").gt(3))
                .with(Sort.by(Sort.Direction.DESC, "grade"));

        //6.定义mapReduce并获取结果
        List<JSONObject> mapReduceResult = primaryTemplate
                .mapReduce(Student.class)
                .map(mapFunction)
                .reduce(reduceFunction)
                .with(MapReduceOptions.options().limit(3))
                .as(JSONObject.class)
                .matching(query)
                .all();

        //7.校验结果
        String result = "[{\"_id\":\"男\",\"value\":\"ftc8\"},{\"_id\":\"女\",\"value\":\"ftc7; ftc9\"}]";
        Assert.isTrue(result.equals(JSONUtil.toJsonStr(mapReduceResult)));
    }

    @Test
    void testRegex() {

        //1.定义存入集合
        List<Student> students = CollUtil.newArrayList();

        //2.循环封装数据
        for (int i = 0; i < 10; i++) {
            Student student = new Student();
            student.setId(i);
            student.setName("ftc" + i);
            students.add(student);
        }

        //3.存入数据
        primaryTemplate.insert(students, Student.class);

        //4.正则查询校验
        Student result = primaryTemplate.findOne(new Query(Criteria.where("name").regex("1")), Student.class);
        Assert.isTrue("ftc1".equals(result.getName()));

        //5.正则查询携带首选项校验
        result = primaryTemplate.findOne(new Query(Criteria.where("name").regex("FTC2", "i")), Student.class);
        Assert.isTrue("ftc2".equals(result.getName()));
    }

    @Autowired
    private StudentService studentService;

    @Test
    void testSavePrimaryStudent() {
        studentService.savePrimaryStudent();
    }

    @Test
    void testSaveSecondaryStudent() {
        studentService.saveSecondaryStudent();
    }

    @Test
    void testSaveStudentPrimaryAndSecondary() {
        studentService.saveStudentPrimaryAndSecondary();
    }

//    @BeforeEach
//    void beforeAll() {
//        mongoTemplate.remove(new Query(), Student.class);
//        bakMongoTemplate.remove(new Query(), Student.class);
//    }
//
//    @Test
//    void testHint() {
//
//        //1.设置年龄索引
//        Index ageIndex = new Index().on("age", Sort.Direction.ASC);
//
//        //2.创建索引
//        String indexName = mongoTemplate.indexOps(Student.class).ensureIndex(ageIndex);
//
//        //3.创建查询条件
//        Query query = new Query(Criteria.where("age").is(1)).withHint(indexName);
//
//        //4.指定使用年龄索引查询
//        List<Student> students = mongoTemplate.find(query, Student.class);
//    }
//
//    @Test
//    void testTtl() {
//
//        //1.设置ttl索引
//        Index createTimeIndex = new Index().on("createTime", Sort.Direction.ASC).expire(10, TimeUnit.SECONDS);
//        mongoTemplate.indexOps(Student.class).ensureIndex(createTimeIndex);
//
//        //2.存入数据
//        Student student = new Student();
//        student.setId(1);
//        student.setCreateTime(new Date());
//
//        //3.沉睡10s钟
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        //4.再次查询
//        Student result = mongoTemplate.findOne(new Query(), Student.class);
//        Assert.isNull(result);
//    }
//
//    @Test
//    void testPartial() {
//
//        //1.设置索引
//        Index ageIndex = new Index().on("age", Sort.Direction.ASC)
//                .partial(PartialIndexFilter.of(Criteria.where("age").gt(6)));
//
//        //2.创建索引
//        mongoTemplate.indexOps(Student.class).ensureIndex(ageIndex);
//    }
//
//    @Test
//    void testSparse() {
//
//        //1.创建昵称唯一索引
//        Index nikeNameIndex = new Index().on("nikeName", Sort.Direction.ASC).unique();
//        String indexName = mongoTemplate.indexOps(Student.class).ensureIndex(nikeNameIndex);
//
//        //2.保存数据
//        Student student = new Student();
//        student.setId(1);
//        student.setNikeName("xxx1");
//        mongoTemplate.insert(student);
//
//        //3.保存1条不包含昵称的数据
//        student = new Student();
//        student.setId(2);
//        mongoTemplate.insert(student);
//
//        //4.再次保存1条不包含昵称的数据
//        student = new Student();
//        student.setId(3);
//        try {
//            mongoTemplate.insert(student);
//        } catch (Exception e) {
//            Assert.isTrue(e.getMessage().contains("nikeName_1 dup key: { nikeName: null }"));
//        }
//
//        //5.重新创建索引设置唯一性和稀疏性
//        mongoTemplate.indexOps(Student.class).dropIndex(indexName);
//        nikeNameIndex = new Index().on("nikeName", Sort.Direction.ASC).unique().sparse();
//        mongoTemplate.indexOps(Student.class).ensureIndex(nikeNameIndex);
//
//        //6.再次保存2条不包含昵称的数据,无异常抛出
//        student = new Student();
//        student.setId(3);
//        mongoTemplate.insert(student);
//        student = new Student();
//        student.setId(4);
//        mongoTemplate.insert(student);
//    }
//
//    @Test
//    void testUnique() {
//
//        //1.创建学号唯一索引
//        Index snoIndex = new Index().on("sno", Sort.Direction.ASC).unique();
//        mongoTemplate.indexOps(Student.class).ensureIndex(snoIndex);
//
//        //2.保存数据
//        Student student = new Student();
//        student.setId(1);
//        student.setSno("1");
//        mongoTemplate.insert(student);
//
//        //3.再次保存数据
//        student = new Student();
//        student.setId(2);
//        student.setSno("1");
//        try {
//            mongoTemplate.insert(student);
//        } catch (Exception e) {
//            Assert.isTrue(e.getMessage().contains("java_test.student index: sno_1 dup key: { sno: \"1\" }"));
//        }
//
//        //2.创建昵称稀疏索引
//        Index nikeNameIndex = new Index().on("nikeName", Sort.Direction.ASC).sparse();
//        mongoTemplate.indexOps(Student.class).ensureIndex(nikeNameIndex);
//
//        //3.创建年龄部分索引
//
//
//        //4.创建创建时间ttl索引
//        Index createTimeIndex = new Index().on("createTime", Sort.Direction.ASC).expire(10, TimeUnit.SECONDS);
//        mongoTemplate.indexOps(Student.class).ensureIndex(createTimeIndex);
//
//
////        //1.保存测试数据
////        for (int i = 1; i <= 10; i++) {
////            Student student = new Student();
////            student.setId(i);
////            student.setSno("00000FTC" + i);
////            student.setName("ftc" + i);
////            student.setAge(i);
////
////            if(i%2 == 0){
////                student.setNikeName("大胖子"+);
////            }
////
////            mongoTemplate.insert(student);
////        }
//
////        //2.验证数据存入
////        long count = mongoTemplate.count(new Query(), Student.class);
////        Assert.isTrue(10 == count);
//
//
////        mongoTemplate.find(new Query(Criteria.where("age").is(2)).withHint(index.getIndexOptions()))
//    }
//
//    @Test
//    void testSelect() {
//
//        //1.定义平面坐标点
//        List<Flat> flats = CollUtil.newArrayList();
//        Flat flat = new Flat();
//        flat.setName("北京");
//        flat.setPoint(new GeoJsonPoint(115.25, 39.26));
//        flats.add(flat);
//
//        flat = new Flat();
//        flat.setName("大同");
//        flat.setPoint(new GeoJsonPoint(112.34, 39.03));
//        flats.add(flat);
//
//        flat = new Flat();
//        flat.setName("太原");
//        flat.setPoint(new GeoJsonPoint(111.3, 37.27));
//        flats.add(flat);
//
//        flat = new Flat();
//        flat.setName("湖南");
//        flat.setPoint(new GeoJsonPoint(108.47, 24.38));
//        flats.add(flat);
//        mongoTemplate.insert(flats, Flat.class);
//
//        //2.定义圆心点
//        GeoJsonPoint point = new GeoJsonPoint(115.28, 39.23);
//
//        //3.定义半径
//        Distance distance = new Distance(300, Metrics.KILOMETERS);
//
//        //4.绘制原型
//        Circle circle = new Circle(point, distance);
//
//        //5.查询
//        List<Flat> results = mongoTemplate.find(new Query(Criteria.where("point").withinSphere(circle)), Flat.class);
//        Assert.isTrue(2 == results.size());
//    }
//
//    @Test
//    void testCreateIndex() {
//
//        //1.创建单列索引
//        Index index = new Index().on("age", Sort.Direction.ASC);
//        String indexName = mongoTemplate.indexOps(Student.class).ensureIndex(index);
//
//        List<Student> students = mongoTemplate.find(new Query(Criteria.where("age").is(1)), Student.class);
//
//    }
}
