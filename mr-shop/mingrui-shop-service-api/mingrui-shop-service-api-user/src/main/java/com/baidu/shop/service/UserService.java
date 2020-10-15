package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/14
 * @Version V1.0
 **/
@Api(tags = "用户接口")
public interface UserService{

    @ApiOperation(value = "用户注册")
    @PostMapping(value = "user/register")
    Result<JSONObject> register(@Validated({MingruiOperation.add.class}) @RequestBody UserDTO userDTO);

    @ApiOperation(value = "给手机号发送验证码")
    @PostMapping(value = "user/sendValidCode")
    Result<JSONObject> sendValidCode(@RequestBody UserDTO userDTO);

    @ApiOperation(value = "检验用户名/手机号")
    @GetMapping(value = "user/check/{value}/{type}")
        //应该定义常量,并返回常量,前台判断常量的值就可以
        // public static final Integer USERNAME_IS_EXIST="1";
    Result<List<UserEntity>> checkUsernameOrPhone(@PathVariable(value = "value") String value, @PathVariable(value = "type") Integer type);

    @GetMapping(value = "/user/checkCode")
    @ApiOperation(value = "校验用户输入的手机验证码")
    Result<JSONObject> checkCode(String phone ,String code);

}
