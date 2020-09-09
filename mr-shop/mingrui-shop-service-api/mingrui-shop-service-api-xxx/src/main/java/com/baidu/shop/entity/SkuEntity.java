package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/8
 * @Version V1.0
 **/
@Table(name = "tb_sku")
@Data
public class SkuEntity {

    @Id//此处必须写long类型,因为现在新增的id已经超过int的范围了
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer spuId;

    private String title;

    private String images;

    private Integer price;

    private String indexes;

    private String ownSpec;

    private Boolean enable;

    private Date createTime;

    private Date lastUpdateTime;

}
