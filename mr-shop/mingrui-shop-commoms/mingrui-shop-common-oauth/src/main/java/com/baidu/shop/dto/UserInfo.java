package com.baidu.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/16
 * @Version V1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Integer id;
    private String username;
}
