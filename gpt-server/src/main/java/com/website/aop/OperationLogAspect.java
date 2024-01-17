package com.website.aop;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import com.website.annotation.OperationLogger;
import com.website.entity.OperationLog;
import com.website.service.OperationLogService;
import com.website.util.AopUtils;
import com.website.util.IpUtil;
import com.website.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 记录操作日志
 * @author ahl
 */
@Slf4j
@Component
@Aspect
public class OperationLogAspect {

    private OperationLogService operationLogService;

    private ThreadLocal<Long> currentTime = new ThreadLocal<>();

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 配置切入点
     */
    @Pointcut("@annotation(operationLogger)")
    public void logPointcut(OperationLogger operationLogger) {
    }

    /**
     * 配置环绕通知
     */
    @Around("logPointcut(operationLogger)")
    public Object logAround(ProceedingJoinPoint joinPoint, OperationLogger operationLogger) throws Throwable {
        currentTime.set(System.currentTimeMillis());
        Object result = joinPoint.proceed();
        int times = (int) (System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        OperationLog operationLog = handleLog(joinPoint, operationLogger, times);
        if (operationLog != null) {
            try {
                operationLogService.saveOperationLog(operationLog);
            } catch (Exception e) {
                log.error("切面日志入库错误：" + operationLog.toString(), e);
            }
        }
        return result;
    }

    /**
     * 获取HttpServletRequest请求对象，并设置OperationLog对象属性
     *
     */
    private OperationLog handleLog(ProceedingJoinPoint joinPoint, OperationLogger operationLogger, int times) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(JwtUtils.authorization);
        String userId = null;
        if (JwtUtils.judgeTokenIsExist(token) && !JwtUtils.isTokenExpired(token)) {
            userId = JwtUtils.getUserId(token);
        }
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = IpUtil.getIpAddrByRequest(request);
        String userAgent = request.getHeader("User-Agent");
        Map<String, Object> requestParams = AopUtils.getRequestParams(joinPoint);
        // 截取最大只保留2000字符
        String param = StringUtils.substring(JSONUtil.toJsonStr(requestParams), 0, 2000);
        UserAgent ua = UserAgentUtil.parse(userAgent);
        String os = ua.getOs().getName();
        String browser = ua.getBrowser().getName();
        String platform = ua.getPlatform().getName();
        String operatingTypeId =  operationLogger.type().getOperatingType();
        String description = operationLogger.type().getDescription();
        String value = AopUtils.resolver(joinPoint, operationLogger.value()).toString();
        return OperationLog.builder()
                .userId(userId)
                .uri(uri)
                .method(method)
                .param(param)
                .ip(ip)
                .times(times)
                .userAgent(userAgent)
                .os(os)
                .browser(browser)
                .platform(platform)
                .operatingTypeId(operatingTypeId)
                .description(description)
                .value(value)
                .build();
    }
}
