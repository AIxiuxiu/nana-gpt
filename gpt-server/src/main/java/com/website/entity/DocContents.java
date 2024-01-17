package com.website.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * <p>
 * 文档分段内容
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("doc_contents")
@ApiModel(value = "DocContents对象", description = "文档分段内容")
public class DocContents extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联文档ID")
    private Long docId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("总结")
    private String summary;

    @ApiModelProperty("消耗token数量")
    private Integer tokens;

    @ApiModelProperty("0：未向量化，1：已向量化 2：向量化失败")
    private Integer embedStatus;

    @ApiModelProperty("embedding向量化数据")
    private String embedding;

    @ApiModelProperty("0:未总结 1：已总结中 2:总结失败")
    private Integer summaryStatus;

    @ApiModelProperty("1：问答对")
    private Integer hasQA;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}
