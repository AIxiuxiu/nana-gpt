package com.website.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 操作日志
 * @author ahl
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@ApiModel(value = "日志信息", description = "数据库记录的的日志信息")
public class OperationLog {

    @ApiModelProperty(value = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "操作者用户ID")
    private String userId;

    @ApiModelProperty(value = "请求的接口")
    private String uri;

    @ApiModelProperty(value = "请求方式")
    private String method;

    @ApiModelProperty(value = "请求参数")
    private String param;

    @ApiModelProperty(value = "IP地址")
    private String ip;

    @ApiModelProperty(value = "客户操作系统")
    private String os;

    @ApiModelProperty(value = "客户浏览器")
    private String browser;

    @ApiModelProperty(value = "客户平台")
    private String platform;

    @ApiModelProperty(value = "请求耗时（毫秒）")
    private Integer times;

    @ApiModelProperty(value = "userAgent")
    private String userAgent;

    @ApiModelProperty(value = "操作描述ID")
    private String operatingTypeId;

    @ApiModelProperty(value = "操作描述")
    private String description;

    @ApiModelProperty(value = "操作值")
    private String value;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
