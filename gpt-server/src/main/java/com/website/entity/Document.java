package com.website.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "Document对象", description = "")
public class Document extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("知识库ID")
    private Long kbId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("文档名称")
    private String docName;

    @ApiModelProperty("标签")
    private String tags;

    @ApiModelProperty("0:未向量化 1：向量化中 2：向量化完成 3:向量化失败")
    private Integer embedStatus;

    @ApiModelProperty("0:未生成问答对 1：问答对生成中 2：已生成问答对")
    private Integer qaStatus;

    @ApiModelProperty("0:未总结 1：总结中 2：总结完成 3:总结失败")
    private Integer summaryStatus;

    @ApiModelProperty("总结")
    private String summary;

    @ApiModelProperty("0:不在使用；1:正常使用")
    private Integer status;


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
