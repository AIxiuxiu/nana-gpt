package com.website.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 分页查询基础类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "分页参数", description = "分页查询基础类")
public class PageVo {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum;

    /**
     * 页距（条/页）
     */
    @ApiModelProperty(value = "每页数量，默认10", example = "10")
    private Integer pageSize;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序，如 id desc，多个逗号隔开")
    private String orderBy;

}
