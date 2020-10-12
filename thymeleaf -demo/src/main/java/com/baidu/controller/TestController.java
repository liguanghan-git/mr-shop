package com.baidu.controller;

import com.baidu.entity.StudentEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/15
 * @Version V1.0
 **/
@Controller//用来返回页面
public class TestController {

    @GetMapping("test")//定义url使用modelmap返回数据
    public String test(ModelMap map){

        map.put("name","tomcat");
        return  "test";
    }


    @GetMapping("stu")
    public String Student(ModelMap map){

        StudentEntity stue = new StudentEntity();
        stue.setCode("008");
        stue.setPass("6737");
        stue.setAge(50);
        stue.setLikeColor("<font color='red'>红色</font>");
        map.put("stu",stue);
        return "test";
    }


    @GetMapping("list")
    public String list(ModelMap map){

        StudentEntity s1 = new StudentEntity("001","111",18,"red");
        StudentEntity s2 = new StudentEntity("002","222",19,"red");
        StudentEntity s3 = new StudentEntity("003","333",16,"blue");
        StudentEntity s4 = new StudentEntity("004","444",65,"blue");
        StudentEntity s5 = new StudentEntity("005","444",65,"blue");

        //转为List
        map.put("stuList", Arrays.asList(s1,s2,s3,s4,s5));
        return "listString";
    }
}
