package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/27
 * @Version V1.0
 **/

@SpringBootApplication
@EnableEurekaServer
public class RunEurekaServerApplication {

     public static void main(String[] args) {

             SpringApplication.run(RunEurekaServerApplication.class, args);
     }
}
