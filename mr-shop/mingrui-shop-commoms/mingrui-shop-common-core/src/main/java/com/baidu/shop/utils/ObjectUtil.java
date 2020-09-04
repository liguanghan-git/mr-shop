package com.baidu.shop.utils;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/3
 * @Version V1.0
 **/

public class ObjectUtil {

    public static Boolean isNull(Object obj){

        return  null == obj;
    }
    public static Boolean isNotNull(Object obj){

        return  null != obj;
    }



}
