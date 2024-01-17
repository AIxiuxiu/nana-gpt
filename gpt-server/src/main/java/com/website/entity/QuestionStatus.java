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
@ApiModel(value = "问题生成状态", description = "")
public class QuestionStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("互动问答id")
    private String interactId;

    @ApiModelProperty("0:未生成问题 1：生成问题中 2：问答对完成 3:问题失败")
    private Integer questionStatus;

    @ApiModelProperty("状态 success，exception")
    private String status;

    @ApiModelProperty("进度")
    private int percentage;

}
