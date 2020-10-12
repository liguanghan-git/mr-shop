package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/27
 * @Version V1.0
 **/

@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private SpecGroupMapper specGroupMapper;
    @Resource
    private CategoryBrandMapper categoryBrandMapper;
    @Resource
    private SpuMapper spuMapper;


    @Override
    public Result<List<CategoryEntity>> getCateByIds(String cateIds) {

        List<Integer> cateIdsArr = Arrays.asList(cateIds.split(","))
                .stream().map(idsArr -> Integer.parseInt(idsArr))
                .collect(Collectors.toList());
        //查询
        List<CategoryEntity> list = categoryMapper.selectByIdList(cateIdsArr);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> select = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(select);
    }

    //增删改必加注解
    //只要代码出问题就会回滚，回到原始，保证程序不会出问题，不加这个注解，一但出错，会影响数据库
    @Transactional
    @Override
    public Result<JSONObject> addCategory(CategoryEntity categoryEntity) {

        //通过页面传递过来的ParentId查询ParentId对应的数据是否为父节点 isParent==1
        //如果ParentId对应的 isParent != 1
        //需要修改为1  -->调用的是查询
        //调用查询繁琐，不建议使用

        //通过新增节点的父id将父节点的状态改为1  -->调用的是修改
        //这种代码是最牛逼的
        CategoryEntity parentEntity = new CategoryEntity();
        parentEntity.setId(categoryEntity.getParentId());
        parentEntity.setIsParent(1);

        categoryMapper.updateByPrimaryKeySelective(parentEntity);

        //错误代码 这里 千万不能 try
        //因为 try后，会被捕获异常，说明异常被处理，上面代码就不会执行了，不会回滚
        //System.out.println((1 / 0));

        categoryMapper.insertSelective(categoryEntity);


        return this.setResultSuccess();
    }


    @Transactional
    @Override
    public Result<JSONObject> updateCategory(CategoryEntity categoryEntity) {

        categoryMapper.updateByPrimaryKeySelective(categoryEntity);

        return this.setResultSuccess();
    }


    String msg = " ";
    @Transactional
    @Override
    public Result<JSONObject> deleteCategory(Integer id) {
        msg = "";

        //验证传入的id是否有效,并且查询出来的数据对接下来的程序有用
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        //判断是否有数据(安全)
        if(categoryEntity == null){
            return this.setResultSuccess("当前id不存在");
        }
        //判断当前节点是否是父级节点(安全)
        if(categoryEntity.getParentId() == 1){
            return this.setResultSuccess("当前节点为父节点，不能删除");
        }

        //被规格组绑定分类不能被删除
        Example example1 = new Example(SpecGroupEntity.class);
        example1.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> list1 = specGroupMapper.selectByExample(example1);
        if(list1.size() != 0){
            for(SpecGroupEntity specGroupEntity : list1){
                msg += specGroupEntity.getName();
            }
            return this.setResultError(msg + "被规格组绑定分类不能被删除");
        }

        //被品牌和分类中间表绑定不能被删除
        Example example2 = new Example(CategoryBrandEntity.class);
        example2.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example2);
        if(categoryBrandEntities.size() != 0){
            for(CategoryBrandEntity categoryBrandEntity : categoryBrandEntities){
                msg += categoryBrandEntity.getCategoryId();
            }
            return this.setResultError(msg + "被品牌和分类中间表绑定不能被删除");
        }


        Example example3 = new Example(SpuEntity.class);
        Example.Criteria cid3 = example3.createCriteria().andEqualTo("cid3", id);
        List<SpuEntity> SpuList = spuMapper.selectByExample(cid3);
        if(SpuList.size() != 0){
            for (SpuEntity spuEntity : SpuList){
                msg += spuEntity.getTitle();
            }
            return this.setResultError((msg + ": 绑定商品不能被删除"));
        }


        //判断当前节点的父节点下 除了当前节点是否还有别的节点(业务)
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());

        List<CategoryEntity> list = categoryMapper.selectByExample(example);
        if (list.size() == 1){
            // 如果没有就将当前节点的父节点isParent的值修改为0
            CategoryEntity parentCateEntity = new CategoryEntity();
            parentCateEntity.setId(categoryEntity.getParentId());
            parentCateEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentCateEntity);
        }



        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }


    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {

        List<CategoryEntity> byBrand = categoryMapper.getByBrandId(brandId);

        return this.setResultSuccess(byBrand);

    }



}
