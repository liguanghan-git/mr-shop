package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "查询商品分类")
    @GetMapping(value = "category/list")
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "增加商品分类")
    @PostMapping(value = "category/add")
    Result<JSONObject> addCategory(@Validated({MingruiOperation.add.class}) @RequestBody CategoryEntity categoryEntity);

    @ApiOperation(value = "修改商品分类")
    @PutMapping(value = "category/update")
    Result<JSONObject> updateCategory(@Validated({MingruiOperation.Update.class}) @RequestBody CategoryEntity categoryEntity);

    @ApiOperation(value = "删除商品分类")
    @DeleteMapping(value = "category/del")
    Result<JSONObject> deleteCategory(Integer id);

    @ApiOperation(value = "通过品牌id查询商品分类")
    @GetMapping(value = "category/getByBrand")
    Result<List<CategoryEntity>> getByBrand(Integer  brandId);

}
