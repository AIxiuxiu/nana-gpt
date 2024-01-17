package com.website.controller.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.website.annotation.ErrorPage;
import com.website.model.RestAndView;
import com.website.model.CodeConstant;
import com.website.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 未知异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestAndView handleException(HttpServletRequest req, Exception ex, HandlerMethod handlerMethod) {
        log.error("未知的异常,URI:" + req.getRequestURI(), ex);
        return buildModelAndView(req, CodeConstant.EXCEPTION, "系统发生未知异常",ExceptionUtil.stacktraceToString(ex), handlerMethod);
    }


    private RestAndView buildModelAndView(HttpServletRequest request, String errorCode, String errorMsg, String exception, HandlerMethod handlerMethod) {

        RestAndView restAndView = new RestAndView();

        if (handlerMethod == null || handlerMethod.getMethod().getReturnType().getName().equals(Result.class.getName()) && isAjaxRequest(request)) {
            Result result =  Result.builder().code(errorCode).msg(errorMsg).exception(exception).build();
            restAndView.setRestData(result);
        } else {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("errorCode", errorCode);
            modelAndView.addObject("errorMsg", errorMsg);
            modelAndView.addObject("exception", exception);

            ErrorPage errorPage = handlerMethod.getMethodAnnotation(ErrorPage.class);
            if (errorPage != null) {
                String value = errorPage.value();
                modelAndView.setViewName(value);
            } else {
                modelAndView.setViewName(ErrorPage.ERROR_PAGE);
            }
            restAndView.setModelAndView(modelAndView);
        }
        return restAndView;
    }

    /**
     * 是否是Ajax异步请求 或  Knife4j
     */
    private boolean isAjaxRequest(HttpServletRequest request) {

        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1) {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1) {
            return true;
        }

        //  Knife4j
        String origion = request.getHeader("Request-Origion");
        if (origion != null && origion.indexOf("Knife4j") != -1) {
            return true;
        }

        return false;
    }
}
