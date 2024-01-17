package com.website.websocket.support;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.website.util.JwtUtils;
import com.website.websocket.model.WSMessageErrorEntity;
import com.website.websocket.model.WebSocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * WebSocketServer抽象类
 */
@Slf4j
public abstract class AbstractWebsocketServer implements WebSocketServer {

    /** 推送消息实现（暂时，后期可用mq或者redis） */
    public abstract void pushMessage(String message, String userId) throws Exception;
    /** 获取生产者名称 */
    public abstract String getProducer();
    /** 用户首次连接，发送的默认消息 */
    public abstract void sendDefaultMessage(Session session, String userId);
    /** 收到消息的处理，返回true表示已处理 */
    public abstract Boolean handleMessage(Session session, String type, String message, String userId);

    public abstract void handleClose(Session session);

    /** 是否鉴权token */
    public abstract Boolean authToken();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收userId
     */
    private String userId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    @Override
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (authToken() && !authJwtToken(session, userId)) {
            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.NO_EXTENSION,"鉴权失败！");
                session.close(closeReason);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        addOnlineCount();
        this.session = session;
        this.userId = userId;
        log.info("新连接加入！用户ID==>" + userId + ",当前在线人数为:" + getOnlineCount());
        WEB_SOCKET_SET.add(this);
        if (StringUtils.isNotEmpty(userId)){
            CustomSession customSession = WEB_SOCKET_MAP.get(userId);
            if (customSession != null) {
                customSession.getSessionList().add(session);
            } else {
                WEB_SOCKET_MAP.put(userId, new CustomSession(session, userId));
            }
        }
        try {
            //首次连接，发送默认消息
            sendDefaultMessage(session, userId);
        } catch (Exception e) {
            e.printStackTrace();;
        }

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    @Override
    public void onClose(Session session, CloseReason reason) {
        if (reason.getCloseCode() == CloseReason.CloseCodes.NO_EXTENSION) {
            // 鉴权失败关闭无需处理
            return;
        }
        WEB_SOCKET_SET.remove(this);
        WEB_SOCKET_MAP.forEach((key, value) -> {
            value.getSessionList().removeIf(session1 -> session1.getId().equals(session.getId()));
            if (value.getSessionList().isEmpty()){
                WEB_SOCKET_MAP.remove(key);
            }
        });
        subOnlineCount();
        handleClose(session);
        log.info("有一连接退出！“,当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    @Override
    public void onMessage(String message, Session session) {
        log.info("用户:{}发送的消息内容:{}", userId, message);
        if (message != null && message.length() > 0) {
            // 解析发送的报文
            WebSocketEntity messageEntity = new WebSocketEntity();
            try {
                messageEntity = JSON.parseObject(message, WebSocketEntity.class);
            } catch (JSONException e) {
                try {
                    sendMessage(session, WSMessageErrorEntity.builder().message("发送信息格式错误！").build().toString(), userId);
                    return;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            if (messageEntity.getFlag()) {
                // 默认消息拦截处理
                handleMessage(session, messageEntity.getType(), message, this.userId);
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    @Override
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误：{}，Session ID：{}", error.getMessage(), session.getId());
        error.printStackTrace();
    }

    @Override
    public int getOnlineCount() {
        return ONLINE_COUNT.get();
    }

    @Override
    public int addOnlineCount() {
        return ONLINE_COUNT.addAndGet(1);
    }

    @Override
    public int subOnlineCount() {
        if (ONLINE_COUNT.get() > 0) {
            return ONLINE_COUNT.decrementAndGet();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractWebsocketServer that = (AbstractWebsocketServer) o;
        return Objects.equals(session, that.session) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, userId);
    }

    /**
     * 拦截验证jwt token
     * @param session
     * @return
     */
    public boolean authJwtToken(Session session, String userId)  {
        // 获得请求参数
        Map<String, String> params = HttpUtil.decodeParamMap(session.getQueryString(), CharsetUtil.CHARSET_UTF_8);
        String jwtToken = params.get("token");

        if (JwtUtils.judgeTokenIsExist(jwtToken)) {
            try {
                if (JwtUtils.isTokenExpired(jwtToken)) {
                    // token过期
                    log.info("凭证过期,无法连接!");
                    return false;
                }
                String jwtUserId = JwtUtils.getUserId(jwtToken);
                if(StringUtils.isEmpty(jwtUserId)){
                    log.info("用户ID为空,无法连接!");
                    return false;
                }
                if(!jwtUserId.equals(userId)){
                    log.info("用户ID错误,无法连接!");
                    return false;
                }
            } catch (Exception e) {
                log.error("拦截器校验jwt token 失败", e);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
