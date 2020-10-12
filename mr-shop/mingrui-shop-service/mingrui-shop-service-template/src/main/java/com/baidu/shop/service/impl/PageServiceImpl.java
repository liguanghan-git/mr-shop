package com.baidu.shop.service.impl;

import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.base.Result;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

    //@Resource
    private GoodsFeign goodsFeign;
    //@Resource
    private BrandFeign brandFeign;
    //@Resource
    private CategoryFeign categoryFeign;
    //@Resource
    private SpecificationFeign specificationFeign;


    @Override
    public Map<String, Object> getPageInfoBySpuId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);//通过id查询只能查询一条数据
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);

        if (spuInfoResult.getCode() == 200) {
            if (spuInfoResult.getData().size() == 1) {
                //spu信息
                SpuDTO spuInfo = spuInfoResult.getData().get(0);
                map.put("spuInfo",spuInfo);

                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());//通过spuDTO得到品牌信息
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if (brandInfoResult.getCode() == 200) {
                    PageInfo<BrandEntity> data = brandInfoResult.getData();

                    List<BrandEntity> brandList = data.getList();
                    if (brandList.size() == 1) {
                        map.put("brandInfo",brandList.get(0));
                    }
                }

                //查询分类信息
                Result<List<CategoryEntity>> categoryResult = categoryFeign.getCateByIds(
                        String.join(
                                ","
                                , Arrays.asList(
                                        spuInfo.getCid1() + "",
                                        spuInfo.getCid2() + "",
                                        spuInfo.getCid3() + "")
                        )
                );
                if (categoryResult.getCode() == 200) {
                    map.put("categoryList",categoryResult.getData());
                }

                //通过spu详情信息查询sku集合-->获得spuDetail
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpu(spuId);
                if (spuDetailResult.getCode() == 200) {
                    SpuDetailEntity spuDetailEntity = spuDetailResult.getData();
                    map.put("spuDetailEntity",spuDetailEntity);
                }

                //skus
                //通过spuID查询sku集合
                Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuInfo.getId());
                if (skusResult.getCode() == 200) {
                    List<SkuDTO> skuList = skusResult.getData();
                    map.put("skus",skuList);
                }

                //特有规格参数   SpecParamDTO对应 --> tb_spec_param
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParamInfoResult = specificationFeign.getSpecParam(specParamDTO);
                if (specParamInfoResult.getCode() == 200) {
                    List<SpecParamEntity> specParamList = specParamInfoResult.getData();
                    Map<Integer, String> specParamMap = new HashMap<>();
                    specParamList.stream().forEach(param -> {
                        specParamMap.put(param.getId(),param.getName());
                    });
                    map.put("specParamMap",specParamMap);
                }

                //规格  SpecGroupDTO 对应-->tb_spec_group表
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();//规格组数据传输DTO
                specGroupDTO.setCid(spuInfo.getCid3());//通过spu里的cid3获得规格组信息
                //  specification --> 通过条件查询规格组
                Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.specification(specGroupDTO);

                if (specGroupResult.getCode() == 200) {
                List<SpecGroupEntity> specGroupInfo = specGroupResult.getData();
                //规格组 和规格参数
                List<SpecGroupDTO> specGroupList = specGroupInfo.stream().map(specGroupEntity -> {
                    SpecGroupDTO specGroup = BaiduBeanUtil.copyProperties(specGroupEntity, SpecGroupDTO.class);
                    //GroupDTO
                    SpecParamDTO paramDTO = new SpecParamDTO();

                    paramDTO.setGroupId(specGroup.getId());//规格组id
                    paramDTO.setGeneric(true);//通用属性
                    Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(paramDTO);

                    if (specParamResult.getCode() == 200){
                        specGroup.setSpecParams(specParamInfoResult.getData());
                    }

                    return specGroup;
                }).collect(Collectors.toList());

                    map.put("specGroupList",specGroupList);
                }


            }
        }

        return map;
    }

}


