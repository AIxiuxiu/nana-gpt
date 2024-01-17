package com.website.controller.base;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.website.model.CodeConstant;
import com.website.model.Result;
import com.website.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础类 base controller
 */
public class BaseController {

    private HttpServletRequest request;
    private HttpServletResponse response;

    @ModelAttribute
    protected void setReqAndRes(HttpServletRequest request, HttpServletResponse response){
        this.request = request;
        this.response = response;
    }

    /**
     * 获取用户 ID
     * @return userId
     */
    protected String getUserId() {
        String jwtToken = request.getHeader(JwtUtils.authorization);
        return JwtUtils.getUserId(jwtToken);
    }

    protected String getNewToken() {
        String jwtToken = request.getHeader(JwtUtils.authorization);
        if(StringUtils.isEmpty(jwtToken) || JwtUtils.isTokenExpired(jwtToken)) {
            return null;
        }
        return JwtUtils.generateNewToken(jwtToken);
    }


    /**
     * 返回成功数据
     * @param data 数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    protected static <T> Result<T> success(T data) {
        return new Result<>(CodeConstant.SUCCESS, "请求成功", data);
    }

    protected static <T> Result<T> success() {
        return new Result<>(CodeConstant.SUCCESS, "请求成功", null);
    }

    protected static <T> Result<T> fail() {
        return new Result<>(CodeConstant.FAIL, "请求失败");
    }

    protected static <T> Result<T> fail(String msg) {
        return new Result<>(CodeConstant.FAIL, msg);
    }

    /**
     * 程序异常catch时的返回错误
     * @param msg 提示信息
     * @param exception 错误信息
     * @return 失败结果
     */
    protected static <T> Result<T> failByCatch(String msg, Exception exception) {
        return new Result<T>(CodeConstant.CATCH_FAIL, msg, null, ExceptionUtil.stacktraceToString(exception));
    }

    protected static <T> Result<T> fail(String code, String msg) {
        return new Result<>(code, msg);
    }

    protected static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(String.valueOf(code), msg);
    }


}

