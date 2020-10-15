package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.MRshopConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/10/14
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {
        //注册

        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);

        userEntity.setCreated(new Date());
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));

        userMapper.insertSelective(userEntity);

        return this.setResultSuccess();
    }


    @Override
    public Result<List<UserEntity>> checkUsernameOrPhone(String value, Integer type) {

        redisRepository.set("name","gengzifang");

        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        // enum枚举  constant常量

        if (type != null && value != null ){
            if (type == 1){
                //通过用户名校验
                criteria.andEqualTo("username",value);
            }else {
                //通过手机号校验
                criteria.andEqualTo("phone",value);
            }
        }

        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {

        //生成随机6位验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";

        //短信条数只有10条,不够我们测试.所以就不发送短信验证码了,直接在控制台打印就可以
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        redisRepository.set(MRshopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(), code);
        redisRepository.expire(MRshopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(),60);

        //发送短信验证码
        //LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);

        //System.out.println(code);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkCode(String phone, String code) {

        String s = redisRepository.get(MRshopConstant.USER_PHONE_CODE_PRE + phone);
        if(!code.equals(s)){
            return this.setResultError(HTTPStatus.VALID_CODE_ERROR,"验证码输入错误");
        }

        return this.setResultSuccess();
    }

}








