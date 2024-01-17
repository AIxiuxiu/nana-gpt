package com.website.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Author: ahl
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "prompt")
public class PromptEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("prompt的具体内容")
    private String content;

    @ApiModelProperty("对prompt的描述")
    private String description;

    @ApiModelProperty("prompt的简单文字标识")
    private String topic;

    @ApiModelProperty("针对的目标群体，0管理员，1用户")
    private Integer target;

    @ApiModelProperty("是否被删除,  0: 未删除, 1: 已删除")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

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
