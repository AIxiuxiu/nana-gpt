package com.website.controller.handler;

import cn.hutool.json.JSONUtil;
import com.website.annotation.PassToken;
import com.website.model.Result;
import com.website.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * 接口token权限拦截
 * @author ahl
 */
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {

        // 如果不是映射到方法直接通过
        if(!(object instanceof HandlerMethod)){
            return true;
        }

        HandlerMethod handlerMethod=(HandlerMethod)object;
        Method method=handlerMethod.getMethod();
        //检查是否有passToken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }

        // jwt TOKEN
        String jwtToken = request.getHeader(JwtUtils.authorization);

        if (JwtUtils.judgeTokenIsExist(jwtToken)) {
            try {
                boolean isExpired = JwtUtils.isTokenExpired(jwtToken);
                if (isExpired) {
                    // token过期
                    return responseResult(response, "403", "凭证过期，请重新登录！");
                }
                String userId = JwtUtils.getUserId(jwtToken);
                if(StringUtils.isEmpty(userId)){
                    //用户不存在
                    return responseResult(response, "403", "错误的凭证！");
                }
            } catch (Exception e) {
                log.error("拦截器校验jwt token 失败", e);
                return responseResult(response, "403", "错误的凭证！");
            }
        } else {
            return responseResult(response, "403", "无凭证，请重新登录！");
        }

        return true;
    }

    private boolean responseResult(HttpServletResponse response, String code, String message) throws Exception {
        Result result = new Result(code, message);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(JSONUtil.toJsonStr(result));
        out.flush();
        out.close();
        return false;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {

    }

}