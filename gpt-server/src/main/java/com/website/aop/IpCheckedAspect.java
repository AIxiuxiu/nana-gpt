package com.website.aop;

import com.website.annotation.IpChecked;
import com.website.model.CodeConstant;
import com.website.model.Result;
import com.website.util.IpUtil;
import com.website.util.IpWhiteCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 限制IP白名单
 *
 * @author ahl
 */
@Slf4j
@Component
@Aspect
public class IpCheckedAspect {

    /**
     * 配置切入点
     */
    @Pointcut("execution(* com.website.controller..*(..)) && @annotation(com.website.annotation.IpChecked)")
    public void checkPointcut() {
    }

    /**
     * 配置环绕通知
     */
    @Around(value = "checkPointcut() && @annotation(ipChecked)")
    public Object interceptor(ProceedingJoinPoint point, IpChecked ipChecked) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = IpUtil.getIpAddrByRequest(request);
        log.warn("请求IP:" + ip);
        if (IpWhiteCheckUtil.isAdminIp(ip, ipChecked.value())) {
            return point.proceed();
        } else {
            return new Result<>(CodeConstant.FAIL, "无访问权限");
        }
    }
}
