package com.baidu.shop.mapper;

import com.baidu.shop.entity.CategoryEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
//什么时候用呢? 当你这个参数是个list集合,这个list集里面又存储了任意个id, 这个方法会自动的遍历集合 ,根据每个id进行查询封装,返回一个list
public interface CategoryMapper extends Mapper<CategoryEntity> , SelectByIdListMapper<CategoryEntity,Integer> {

    @Select(value = "select c.id,c.name from tb_category c where c.id in (select cb.category_id from tb_category_brand cb where cb.brand_id=#{brandId})")
    List<CategoryEntity> getByBrandId(Integer brandId);

}
