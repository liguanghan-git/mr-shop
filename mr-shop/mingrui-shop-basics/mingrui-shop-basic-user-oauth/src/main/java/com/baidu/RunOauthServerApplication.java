package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/16
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
public class RunOauthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunOauthServerApplication.class);
    }

}
