package com.baidu.shop.response;

import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.status.HTTPStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/21
 * @Version V1.0
 **/
//@NoArgsConstructor: 自动生成无参数构造函数。
//@AllArgsConstructor: 自动生成全参数构造函数。
@Data
@NoArgsConstructor
public class GoodsResponse extends Result<List<GoodsDoc>> {

    private Long total;
    private Long totalPage;
    private List<BrandEntity> brandList;
    private List<CategoryEntity> categoryList;
    private Map<String, List<String>> specParamValueMap;

    public GoodsResponse(Long total, Long totalPage, List<BrandEntity> brandList,
                         List<CategoryEntity> categoryList,
                         List<GoodsDoc> goodsDocs,
                         Map<String, List<String>> specParamValueMap){
        super(HTTPStatus.OK,HTTPStatus.OK + "",goodsDocs);

        this.total = total;
        this.totalPage = totalPage;
        this.brandList = brandList;
        this.categoryList = categoryList;
        this.specParamValueMap = specParamValueMap;

    }
}
