package com.website.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "response 返回 code", description = "code代表的意思")
public class CodeConstant {
    /**
     * 成功
     */
    @ApiModelProperty(value = "请求成功")
    public static final String SUCCESS = "0";
    /**
     * 失败
     */
    @ApiModelProperty(value = "请求失败")
    public static final String FAIL = "1";


    @ApiModelProperty(value = "程序出错获取到 catch")
    public static final String CATCH_FAIL = "101";


    @ApiModelProperty(value = "系统发生未知异常")
    public static final String EXCEPTION = "1010";

}
