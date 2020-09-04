package com.baidu.shop.entity;

import io.swagger.models.auth.In;
import lombok.Data;

import javax.imageio.ImageTranscoder;
import javax.persistence.Table;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/1
 * @Version V1.0
 **/

@Data
@Table(name = "tb_category_brand")
public class CategoryBrandEntity {

    private Integer categoryId;

    private Integer brandId;
}
