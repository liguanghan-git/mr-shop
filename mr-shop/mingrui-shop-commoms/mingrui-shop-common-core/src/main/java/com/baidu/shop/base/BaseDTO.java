package com.baidu.shop.base;

import com.netflix.discovery.util.StringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @ClassName TestEurekaFeignController
 * @Description: TODO
 * @Author liguanghan
 * @Date 2020/8/31
 * @Version V1.0
 **/
@Data
@ApiModel(value = "BaseDTO用来传输，其他dto来继承这个类")
public class BaseDTO {

    @ApiModelProperty(value = "当前页", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页显示多少条",example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "是否降序")
    private String order;


    @ApiModelProperty(hidden = true)
    public String getOrderByClause(){

        if(!StringUtils.isEmpty(sort))return sort + " " +
                order.replace("false","asc").replace("true","desc");
        return "";
    }

    //    @ApiModelProperty(value = "是否降序")
    //    private Boolean desc;

//       隐藏此函数,不在swagger-ui上显示
//       @ApiModelProperty(hidden = true)
//       public String getOrderByClause(){
//           if(StringUtil.isNotEmpty(sort)) return sort + " " + (desc?"desc":"");
//           return null;
//       }
}
