package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.service.BaseApiService;
import com.baidu.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/27
 * @Version V1.0
 **/

@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> select = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(select);
    }

    //在公司不加这个注解 会被开除的
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

    @Transactional
    @Override
    public Result<JSONObject> deleteCategory(Integer id) {

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


        //判断当前节点的父节点下 除了当前节点是否还有别的节点(业务)

        //这里需要在调研调研

        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());

        List<CategoryEntity> list = categoryMapper.selectByExample(example);
        if (list.size() == 1){

            CategoryEntity parentCateEntity = new CategoryEntity();
            parentCateEntity.setId(categoryEntity.getParentId());
            parentCateEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentCateEntity);

        }

        // 如果没有就将当前节点的父节点isParent的值修改为0


        categoryMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
