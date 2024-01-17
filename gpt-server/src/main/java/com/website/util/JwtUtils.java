package com.website.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.website.vo.UserInfoVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtils {

    public static String authorization = "token";

    /**
     * 全局使用，获取userId
     */
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(String id) {
        threadLocal.set(id);
    }

    public static String getCurrentId() {
        return threadLocal.get();
    }

    /**
     * token 过期时间, 单位: 天. 这个值表示 1 天
     */
    private static final Integer TOKEN_EXPIRED_TIME = 1;

    /**
     * jwt 加密解密密钥,
     */
    private static final String JWT_SECRET = "p5w.gpt.net";

    /**
     * 1 生成jwt令牌的方法
     * @param user
     * @return
     */
    public static String doGenerateToken(UserInfoVo user){

        if(user == null
                || StringUtils.isEmpty(user.getId())
                || StringUtils.isEmpty(user.getUsername())){
            return null;
        }

        Date exporateDate = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, TOKEN_EXPIRED_TIME);
        log.info("生成 jwt token，过期时间：{}", DateUtil.formatDateTime(exporateDate));
        return Jwts.builder().setSubject(user.getId())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(exporateDate)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET).compact();
    }


    /**
     * 判断token是否存在
     *
     * @param token
     * @return
     */
    public static boolean judgeTokenIsExist(String token) {
        return token != null && !"".equals(token) && !"null".equals(token);
    }

    /**
     * 获取用户ID
     * @param token
     * @return
     */
    public static String getUserId(String token) {
        String userId = getClaimFromToken(token).getSubject();
        JwtUtils.setCurrentId(userId);
        return userId;
    }

    /**
     * 获取新token
     * @param token
     * @return
     */
    public static String generateNewToken(String token){

        Date exporateDate = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, TOKEN_EXPIRED_TIME);
        log.info("生成 jwt token，过期时间：{}", DateUtil.formatDateTime(exporateDate));
        String userId = getUserId(token);
        String username = getClaimFromToken(token).get("username").toString();
        return Jwts.builder().setSubject(userId)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(exporateDate)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET).compact();
    }

    public static String generateNewTokenbm(String token){

        Date exporateDate = DateUtil.offset(new Date(), DateField.DAY_OF_MONTH, TOKEN_EXPIRED_TIME);
        log.info("生成 jwt token，过期时间：{}", DateUtil.formatDateTime(exporateDate));
        String userId = getUserId(token);
        String loginName = getClaimFromToken(token).get("loginName").toString();
        return Jwts.builder().setSubject(userId)
                .claim("loginName", loginName)
                .setIssuedAt(new Date())
                .setExpiration(exporateDate)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET).compact();
    }


    /**
     *  验证token是否失效
     *  true:过期   false:没过期
     */
    public static Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException expiredJwtException) {
            return true;
        }
    }


    /**
     * 获取jwt发布时间
     */
    public static Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token).getIssuedAt();
    }

    /**
     * 获取jwt失效时间
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token).getExpiration();
    }

    /**
     * 获取jwt的payload部分
     */
    public static Claims getClaimFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

}
