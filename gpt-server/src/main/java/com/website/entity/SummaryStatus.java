package com.website.entity;

import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "总结状态", description = "")
public class SummaryStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文档ID")
    private String docId;

    @ApiModelProperty("0:未生成问答对 1：生成问答对中 2：问答对完成 3:问答对失败")
    private Integer summaryStatus;

    @ApiModelProperty("状态 success，exception")
    private String status;

    @ApiModelProperty("进度")
    private int percentage;

}
