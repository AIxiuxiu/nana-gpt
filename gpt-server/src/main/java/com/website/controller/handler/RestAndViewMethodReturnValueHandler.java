package com.website.controller.handler;

import com.website.model.RestAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

public class RestAndViewMethodReturnValueHandler extends ModelAndViewMethodReturnValueHandler {

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return RestAndView.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }
        RestAndView restAndView = (RestAndView) returnValue;

        if (restAndView.getRestData() != null) {
            HandlerMethodReturnValueHandler responseBodyReturnValueHandler = this.getResponseBodyReturnValueHandler();
            responseBodyReturnValueHandler.handleReturnValue(restAndView.getRestData(), returnType, mavContainer, webRequest);
        } else {
            ModelAndView mav = restAndView.getModelAndView();
            super.handleReturnValue(mav, returnType, mavContainer, webRequest);
        }
    }

    public HandlerMethodReturnValueHandler getResponseBodyReturnValueHandler() {
        HandlerExceptionResolverComposite handlerExceptionResolverComposite = (HandlerExceptionResolverComposite) handlerExceptionResolver;
        List<HandlerExceptionResolver> exceptionResolvers = handlerExceptionResolverComposite.getExceptionResolvers();
        for (HandlerExceptionResolver exceptionResolver : exceptionResolvers) {
            if (exceptionResolver instanceof ExceptionHandlerExceptionResolver) {
                ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = (ExceptionHandlerExceptionResolver) exceptionResolver;
                HandlerMethodReturnValueHandlerComposite returnValueHandlers = exceptionHandlerExceptionResolver.getReturnValueHandlers();
                List<HandlerMethodReturnValueHandler> handlers = returnValueHandlers.getHandlers();
                for (HandlerMethodReturnValueHandler handler : handlers) {
                    if (handler instanceof RequestResponseBodyMethodProcessor) {
                        return handler;
                    }
                }
            }
        }
        //正常配置不可能发生
        throw new RuntimeException("没有找到RequestResponseBodyMethodProcessor");
    }
}