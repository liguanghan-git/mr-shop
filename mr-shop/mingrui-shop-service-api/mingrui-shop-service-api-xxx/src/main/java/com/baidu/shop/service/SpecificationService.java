package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "规格接口")
public interface SpecificationService {

    @ApiModelProperty(value = "通过条件查询规格组")
    @GetMapping(value = "specgroup/specification")
    Result<List<SpecGroupEntity>> specification (@SpringQueryMap SpecGroupDTO specGroupDTO);

    @ApiModelProperty(value = "新增规格组")
    @PostMapping(value = "specgroup/saveOrUpdate")
    Result<JSONObject> add(@Validated({MingruiOperation.add.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiModelProperty(value = "修改规格组")
    @PutMapping(value = "specgroup/saveOrUpdate")
    Result<JSONObject> update(@Validated({MingruiOperation.Update.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiModelProperty(value = "删除规格组")
    @DeleteMapping(value = "specgroup/delete")
    Result<JSONObject> delete(Integer id);

    @ApiModelProperty(value = "通过条件查询规格参数")
    @GetMapping(value = "specgroup/getSpecParam")//SpecGroupEntity
    Result<List<SpecParamEntity>> getSpecParam(@SpringQueryMap SpecParamDTO specParamDTO);

    @ApiModelProperty(value = "新增规格参数")
    @PostMapping(value = "specgroup/addOrUpdateParam")
    Result<JsonObject> addParam(@Validated({MingruiOperation.add.class}) @RequestBody SpecParamDTO specParamDTO);

    @ApiModelProperty(value = "修改规格参数")
    @PutMapping(value = "specgroup/addOrUpdateParam")
    Result<JsonObject> updateParam(@Validated({MingruiOperation.Update.class}) @RequestBody SpecParamDTO specParamDTO);

    @ApiModelProperty(value = "删除规格参数")
    @DeleteMapping(value = "specgroup/del")
    Result<JSONObject> del(Integer id);

}
