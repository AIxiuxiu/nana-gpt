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
@ApiModel(value = "向量化状态", description = "")
public class EmbeddingStatus extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文档ID")
    private String docId;

    @ApiModelProperty("0:未向量化 1：向量化中 2：向量化完成 3:向量化失败")
    private Integer embedStatus;

    @ApiModelProperty("状态 success，exception")
    private String status;

    @ApiModelProperty("进度")
    private int percentage;

}
