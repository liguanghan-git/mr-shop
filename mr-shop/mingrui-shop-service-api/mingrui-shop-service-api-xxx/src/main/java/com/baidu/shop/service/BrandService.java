package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/31
 * @Version V1.0
 **/

@Api(tags = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "获取品牌信息")
    @GetMapping(value = "brand/getBrandInfo")
    Result<PageInfo<BrandEntity>> getBrandInfo(@SpringQueryMap BrandDTO brandDTO);

    @PostMapping(value = "brand/save")
    @ApiOperation(value = "增加品牌信息")
    Result<JsonObject> saveBrand(@Validated({MingruiOperation.add.class}) @RequestBody BrandDTO brandDTO);

    @PutMapping(value = "brand/save")
    @ApiOperation(value = "修改品牌信息")
    Result<JsonObject> updateBrand(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @DeleteMapping(value = "brand/delete")
    @ApiOperation(value = "删除品牌信息")
    Result<JsonObject> deleteBrand(Integer id);

    @GetMapping(value = "brand/getBrandByCategory")
    @ApiOperation(value = "通过分类id获取品牌")
    Result<List<BrandEntity>> getBrandByCategory(Integer cid);


    @ApiOperation("通过品牌id集合获取品牌")
    @GetMapping("brand/getBrandByIds")
    Result<List<BrandEntity>> getBrandByIds(@RequestParam String brandIds);

}
