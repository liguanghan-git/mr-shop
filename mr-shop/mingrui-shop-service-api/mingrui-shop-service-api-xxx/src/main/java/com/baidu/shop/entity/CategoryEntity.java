package com.baidu.shop.entity;

import com.baidu.shop.validate.group.MingruiOperation;
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
    //此处应该引入分组的概念，用来校验当前参数属于哪个分组
    //有的参数就不需要验证，新增不需要校验id，修改要校验id
    @NotNull(message = "主键不能为空",groups = {MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "类目名称")
    //增加和修改都需要校验这个参数
    @NotEmpty(message = "类目名称不能为空",groups = {MingruiOperation.add.class,MingruiOperation.Update.class})
    private String name;

    @ApiModelProperty(value = "父类目id",example = "1")
    @NotNull(message = "父类目id不能为空",groups = {MingruiOperation.add.class})
    private Integer parentId;

    @ApiModelProperty(value = "是否是父级节点，0为否，1为是",example = "0")
    @NotNull(message = "是否是父级节点不能为空",groups = {MingruiOperation.add.class})
    private Integer isParent;

    @ApiModelProperty(value = "排序指数，越小越靠前",example = "1")
    @NotNull(message = "排序指数不能为空",groups = {MingruiOperation.add.class})
    private Integer sort;

}
