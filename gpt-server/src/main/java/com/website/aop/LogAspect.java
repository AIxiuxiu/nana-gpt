package com.website.aop;

import cn.hutool.json.JSONUtil;
import com.website.util.AopUtils;
import com.website.util.IpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * 接口日志 AOP
 */
@Aspect
@Slf4j
public class LogAspect {

    /**
     * 请求警告时长
     */
    private static final int WARNING_TIME = 10000;

    /**
     * 定义切入点，controller下面的所有类的所有公有方法，这里需要更改成自己项目的
     */
    @Pointcut("execution(public * com.website.controller..*.*(..))")
    public void requestLog(){};

    /**
     * 请求日志
     */
    @Around("requestLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Object result = proceedingJoinPoint.proceed();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setIp(IpUtil.getIpAddrByRequest(request));
        requestInfo.setUrl(request.getRequestURL().toString());
        requestInfo.setHttpMethod(request.getMethod());
        requestInfo.setClassMethod(String.format("%s.%s", proceedingJoinPoint.getSignature().getDeclaringTypeName(),
                proceedingJoinPoint.getSignature().getName()));
        requestInfo.setRequestParams(AopUtils.getRequestParams(proceedingJoinPoint));
        requestInfo.setResult(result);
        requestInfo.setTimeCost(System.currentTimeMillis() - start);
        if (requestInfo.getTimeCost() > WARNING_TIME) {
            log.warn("Request Info: {}", JSONUtil.toJsonPrettyStr(requestInfo));
        }
        log.info("Request Info: {}", JSONUtil.toJsonPrettyStr(requestInfo));
        return result;
    }

    /**
     * 异常抛出增强
     */
    @AfterThrowing(pointcut = "requestLog()", throwing = "e")
    public void doAfterThrow(JoinPoint joinPoint, RuntimeException e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        RequestErrorInfo requestErrorInfo = new RequestErrorInfo();
        requestErrorInfo.setIp(IpUtil.getIpAddrByRequest(request));
        requestErrorInfo.setUrl(request.getRequestURL().toString());
        requestErrorInfo.setHttpMethod(request.getMethod());
        requestErrorInfo.setClassMethod(String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()));
        requestErrorInfo.setRequestParams(AopUtils.getRequestParams(joinPoint));
        requestErrorInfo.setException(e.getMessage());
        log.info("Error Request Info : {}", JSONUtil.toJsonPrettyStr(requestErrorInfo));
    }

    /**
     * 请求信息
     */
    @Data
    public class RequestInfo implements Serializable {
        private String url; //请求路径
        private String httpMethod; //方法类型
        private String classMethod; //类和方法名称
        private Object requestParams; //请求参数
        private Object result; //返回的结果
        private String ip; //ip地址
        private Long timeCost; //执行时长
    }

    /**
     * 请求失败信息
     */
    @Data
    public class RequestErrorInfo implements Serializable  {
        private String url;
        private String httpMethod;
        private String classMethod;
        private Object requestParams;
        private String ip;
        private String exception; //异常
    }
}
