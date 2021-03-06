package com.baidu.shop.service.impl;


import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.BrandService;
import com.github.pagehelper.PageHelper;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuMapper spuMapper;


    @Override
    public Result<List<BrandEntity>> getBrandByIds(String brandIds) {

        //Arrays.asList()-->将数组转化为list   brandIds.split(",")-->把通过brands查询出的集合用逗号分开
        //通过.stream().map()遍历 将对象转换成另一个对象
        //获取列表中所有用户的用户名集合-->  .collect(Collectors.toList()
        List<Integer> brandIdsArr = Arrays.asList(brandIds.split(","))
                .stream().map(brandstr -> Integer.parseInt(brandstr))
                .collect(Collectors.toList());
        //批量查询
        List<BrandEntity> list = brandMapper.selectByIdList(brandIdsArr);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<BrandEntity>> getBrandByCategory(Integer cid) {

        List<BrandEntity> list = brandMapper.getBrandByCategory(cid);
        return this.setResultSuccess(list);
    }


    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        if(ObjectUtil.isNotNull(brandDTO.getPage())
                && ObjectUtil.isNotNull(brandDTO.getRows()))
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        //分页
        //PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        //排序
        Example example = new Example(BrandEntity.class);
                                            //是否降序
        //if(!StringUtils.isEmpty(brandDTO.getOrder())) example.setOrderByClause(brandDTO.getOrderByClause());
                                            //排序字段
        if(StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());

        //多条件
        Example.Criteria criteria = example.createCriteria();
        if(ObjectUtil.isNotNull(brandDTO.getId()))
                    //andEqualTo加上空格也能查询出数据
            criteria.andEqualTo("id",brandDTO.getId());

        if(ObjectUtil.isNotNull(brandDTO.getName()))
            criteria.andLike("name","%" + brandDTO.getName() + "%");


        //查询
        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

       return this.setResultSuccess(pageInfo);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {


        //获取到品牌名称
        //String name = brandEntity.getName();
        //获取到品牌名称第一个字符
        //char c = name.charAt(0);
        //将第一个字符转换为pinyin
        //String upperCase = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
        //获取拼音的首字母
        //统一转为大写
        //brandEntity.setLetter(upperCase.charAt(0));

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO,BrandEntity.class);
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);

        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();

    }

    @Transactional
    @Override
    public Result<JsonObject> updateBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0))
                ,PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //修改操作
        brandMapper.updateByPrimaryKey(brandEntity);

        //通过brandId删除中间表的数据
        this.deleteCategoryAndBrand(brandEntity.getId());

        //新增新的数据
        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    String mag = "  ";
    @Transactional
    @Override
    public Result<JsonObject> deleteBrand(Integer id) {

        mag = "  ";

        Example example = new Example(SpuEntity.class);
        Example.Criteria brandId = example.createCriteria().andEqualTo("brandId", id);
        List<SpuEntity> SpuList = spuMapper.selectByExample(brandId);
        if(SpuList.size() != 0){
            for (SpuEntity spuEntity : SpuList){
                mag += spuEntity.getTitle();
            }
            return this.setResultError((mag + ": 绑定商品不能被删除"));
        }

        //删除品牌
        brandMapper.deleteByPrimaryKey(id);

        //删除中间表的关系信息
        this.deleteCategoryAndBrand(id);

        return this.setResultSuccess();
    }


    // @Transactional
    // 回滚？？？   事务？？？
    private void insertCategoryAndBrand(BrandDTO brandDTO,BrandEntity brandEntity){

        if(brandDTO.getCategory().contains(",")){

            //通过split方法分割数组
            //通过Arrays.asList的作用是将数组转化为list,一般是用于在初始化的时候,设置几个值进去,简化代码,省去add的部分
            //使用JDK1.8的stream，它没有内部存储，它只是用操作管道从 source（数据结构、数组、IO channel）抓取数据。
            //使用map函数返回一个新的数据,Stream中map元素类型转化方法
            //collect 转换集合类型Stream<T>
            //Collectors.toList())将集合转换为List类型

            //该方法是将数组转化成List集合的方法
            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
                    .stream().map(cid -> {

                        CategoryBrandEntity entity = new CategoryBrandEntity();
                        entity.setCategoryId(StringUtil.toInteger(cid));
                        entity.setBrandId(brandEntity.getId());
                        return entity;
                    }).collect(Collectors.toList());

            //批量新增
            categoryBrandMapper.insertList(categoryBrandEntities);

        }else{

            //新增
            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(entity);
        }

        //return this.setResultSuccess();
    }

    private void deleteCategoryAndBrand(Integer id){

        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

}
