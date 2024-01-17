package com.website.websocket.support;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocketServer接口
 */
public interface WebSocketServer {

    /**
     * 记录当前连接数
     */
    AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    /**
     * 存放每个客户端对应的WebSocket
     */
    CopyOnWriteArraySet<WebSocketServer> WEB_SOCKET_SET = new CopyOnWriteArraySet<>();

    /**
     * 存放session对应用户，用于发送信息
     */
    ConcurrentHashMap<String, CustomSession> WEB_SOCKET_MAP = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    void onOpen(Session session, @PathParam("userId") String userId);

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    void onClose(Session session, CloseReason reason);

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    void onMessage(String message, Session session);

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    void onError(Session session, Throwable error);

    /**
     * 发送消息
     *
     * @param session
     * @param message
     * @param userId
     */
    void sendMessage(Session session, String message, String userId) throws IOException;

    int getOnlineCount();

    int addOnlineCount();

    int subOnlineCount();
}

