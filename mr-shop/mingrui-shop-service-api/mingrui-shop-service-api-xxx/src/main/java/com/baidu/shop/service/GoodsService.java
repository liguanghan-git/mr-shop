package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO);

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
    Result<SpuDetailEntity> getSpuDetailBydSpu(Integer spuId);

    @ApiOperation(value = "获取sku详情信息")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(Integer spuId);

}
