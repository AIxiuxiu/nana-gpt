package com.website.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户
 * @author ahl
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user")
@ApiModel(value = "user对象", description = "用户表")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("模型")
    private String model;

    @ApiModelProperty("回复消息最大token数")
    private int replyMaxToken;

    @ApiModelProperty("展示多久历史记录")
    private int howLongTime;

    @ApiModelProperty("上下文添加最大历史条数")
    private int maxHistory;

    public String getTokenModel() {
        if (this.model.equals("gpt-4-1106-preview")) {
            return ChatCompletion.Model.GPT_4.getName();
        }
        return this.model;
    }
}
