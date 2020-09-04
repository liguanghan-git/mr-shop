package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/1
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@RestController
public class RunUploadServerApplication {

     public static void main(String[] args) {
             SpringApplication.run(RunUploadServerApplication.class, args);
         }

         @GetMapping("/upload/test")
         public String test(){
              return 1213132+"";
         }
}
