package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Resource
    private GoodsFeign goodsFeign;
    @Resource
    private BrandFeign brandFeign;
    @Resource
    private CategoryFeign categoryFeign;
    @Resource
    private SpecificationFeign specificationFeign;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    //静态文件生成的路径
    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;

    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {

        File file = new File(staticHTMLPath + File.separator + spuId + ".html");

        //file.delete() 删除文件 --> boolean(true:删除成功 false:删除失败)
        if(!file.delete()){
            return this.setResultError("文件删除失败");
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {
        //获取数据获取上下文
        Map<String, Object> map = this.getPageInfoBySpuId(spuId);
        //创建模板引擎上下文
        Context context = new Context();
        //将所有准备的数据放到模板中
        context.setVariables(map);

        //JDBC步骤
        //加载驱动
        //创建链接
        //sql
        // rsult
        //关流-->释放资源
        File file = new File(staticHTMLPath, spuId + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file,"UTF-8");
            //关流之后放到这里来
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }


        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        //获取所有spu数据
        Result<List<SpuDTO>> spuDTOResult = goodsFeign.getSpuInfo(new SpuDTO());
        if (spuDTOResult.getCode() == 200) {
            List<SpuDTO> spuDTOList = spuDTOResult.getData();
            spuDTOList.stream().forEach(spuDTO -> {
                createStaticHTMLTemplate(spuDTO.getId());
            });
        }

        return this.setResultSuccess();
    }


    private Map<String, Object> getPageInfoBySpuId(Integer spuId) {

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
