package com.website.model;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 请求返回
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@ApiModel(value = "通用PI接口返回", description = "Common Api Response")
public class Result<T> {

    /**
     * 通用返回状态
     */
    @ApiModelProperty(value = "通用返回状态，0成功，其他失败", example = "0")
    @JSONField(ordinal = 1)
    private String code;

    /**
     * 通用返回信息
     */
    @ApiModelProperty(value = "通用返回信息")
    @JSONField(ordinal = 2)
    private String msg;

    /**
     * data 通用返回数据
     */
    @ApiModelProperty(value = "通用返回数据")
    @JSONField(ordinal = 3)
    private T data;


    /**
     * catch 错误信息
     */
    @ApiModelProperty(value = "具体错误信息")
    @JSONField(ordinal = 4)
    private String exception;



    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
