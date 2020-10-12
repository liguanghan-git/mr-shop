package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(@SpringQueryMap SpuDTO spuDTO);//复杂

    @ApiOperation(value = "增加商品信息")
    @PostMapping(value = "goods/save")
    Result<JSONObject> addInfo(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "修改商品信息")
    @PutMapping(value = "goods/save")
    Result<JSONObject> editGoots(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品信息")
    @DeleteMapping(value = "goods/del")
    Result<JSONObject> del(Integer spuId);

    @ApiOperation(value = "获取spu详情信息")
    @GetMapping(value = "goods/getSpuDetailBydSpu")
    Result<SpuDetailEntity> getSpuDetailBydSpu(@RequestParam Integer spuId);//简单

    @ApiOperation(value = "获取sku详情信息")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "设置商品下架信息")
    @PutMapping(value = "goods/xiajia")
    Result<List<SpuDTO>> xiajia(@RequestBody SpuEntity spuEntity);

}
