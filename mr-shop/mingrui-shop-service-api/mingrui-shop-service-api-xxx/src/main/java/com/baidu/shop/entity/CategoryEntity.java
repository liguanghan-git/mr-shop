package com.baidu.shop.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/27
 * @Version V1.0
 **/

@Data
@Table(name = "tb_category")
@ApiModel(value = "类目/分类实体类")
public class CategoryEntity {

    @Id
    @ApiModelProperty(value = "主键",example = "1")
    @NotNull(message = "主键不能为空")
    private Integer id;

    @ApiModelProperty(value = "类目名称")
    @NotEmpty(message = "类目名称不能为空")
    private String name;

    @ApiModelProperty(value = "父类目id",example = "1")
    @NotNull(message = "父类目id不能为空")
    private Integer parentId;

    @ApiModelProperty(value = "是否是父级节点，0为否，1为是",example = "0")
    @NotNull(message = "是否是父级节点不能为空")
    private Integer isParent;

    @ApiModelProperty(value = "排序指数，越小越靠前",example = "1")
    @NotNull(message = "排序指数不能为空")
    private Integer sort;

}
