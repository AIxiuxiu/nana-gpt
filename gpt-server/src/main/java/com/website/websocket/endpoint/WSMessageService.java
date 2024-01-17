package com.website.websocket.endpoint;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.website.service.ChatService;
import com.website.util.MilvusClientUtil;
import com.website.websocket.model.*;
import com.website.websocket.support.AbstractWebsocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahl
 * @desc webscoket
 * @create 2023/6/11 11:01
 */
@Slf4j
@Component
@ServerEndpoint("/ws/message/{userId}")
public class WSMessageService extends AbstractWebsocketServer {

    private static ChatService chatService;

    @Autowired
    public void setChatService(ChatService chatService) {
        WSMessageService.chatService = chatService;
    }

    /**
     * 存放合集和对应sessionID
     */
    ConcurrentHashMap<String, List<String>>  MILVUS_SESSION_MAP = new ConcurrentHashMap<>();


    /**
     * 消息推送 入口
     *
     * @param session
     * @param message
     * @param userId
     * @throws IOException
     */
    @Override
    public void sendMessage(Session session, String message, String userId) throws IOException {
        log.info("[WSMessageService.sendMessage] session:==>{}, message:==>{},userId:==>{}", session, message, userId);
        if (message == null) {
            log.warn("推送消息不能为空！");
            return;
        }
        if (session != null) {
            //正常发送
            sendMessage(session, message);
        } else {
            if (StringUtils.isEmpty(userId)) {
                //群发
                WEB_SOCKET_MAP.values().forEach(userItem -> {
                    userItem.getSessionList().forEach(item -> {
                        try {
                            sendMessage(item, message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                });
            } else {
                // 给单个用户发送
                WEB_SOCKET_MAP.get(userId).getSessionList().forEach(item -> {
                    try {
                        sendMessage(item, message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    /**
     * 发送消息 具体实现
     *
     * @param session
     * @param message
     * @throws IOException
     */
    private void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 推送消息（生产者推送器）
     *
     * @param message
     * @param userId
     * @throws Exception
     */
    @Override
    public void pushMessage(String message, String userId) throws Exception {
        sendMessage(null, message, userId);
    }

    @Override
    public String getProducer() {
        return "system";
    }

    @Override
    public Boolean authToken() {
        return true;
    }

    @Override
    public Boolean handleMessage(Session session, String type, String message, String userId) {
        if (type.equals(WebScoketType.DEFAULT.getType())) {
            Integer online = getOnlineCount();
            WebSocketEntity sendMessage = WebSocketEntity.builder().message("online"+ online).build();
            try {
                sendMessage(session, sendMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (type.equals(WebScoketType.CHAT.getType())) {
            WSMessageChatEntity chatEntity = null;
            try {
                chatEntity = JSON.parseObject(message, WSMessageChatEntity.class);
                // chat提问
                try {
                    if (StringUtils.isNotEmpty(chatEntity.getKbId())) {
                        WSMessageService.chatService.onWSMessageByKbId(session, chatEntity.getMessage(), userId, chatEntity.getKbId());
                    } else {
                        if (StringUtils.isNotEmpty(chatEntity.getSearch())) {
                            WSMessageService.chatService.onWSMessageBySearch(session, userId, chatEntity);
                        } else {
                            WSMessageService.chatService.onWSMessage(session, chatEntity.getMessage(), userId, chatEntity.getPrompt());
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    try {
                        chatEntity.setMessage(e.getMessage());
                        chatEntity.setStatus("failure");
                        sendMessage(session, chatEntity.toString());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                log.error(e.getMessage());
                try {
                    sendMessage(session, WSMessageErrorEntity.builder().message("发送信息格式错误！").build().toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            return true;
        }  else if (type.equals(WebScoketType.MILVUS.getType())){
            WSMessageMilvusEntity chatEntity = JSON.parseObject(message, WSMessageMilvusEntity.class);
            String collectionName = "kb_"+ chatEntity.getKbId();
            if (chatEntity.getMessage().equals("open")) {
                if (MILVUS_SESSION_MAP.containsKey(collectionName)) {
                    MILVUS_SESSION_MAP.get(collectionName).add(session.getId());
                } else {
                    List<String> sessionList = new ArrayList<>();
                    sessionList.add(session.getId());
                    MILVUS_SESSION_MAP.put(collectionName, sessionList);
                    MilvusClientUtil.loadCollection(collectionName);
                }
            } else  if (chatEntity.getMessage().equals("close")) {
                if (MILVUS_SESSION_MAP.containsKey(collectionName)) {
                    MILVUS_SESSION_MAP.get(collectionName).remove(session.getId());
                    if (MILVUS_SESSION_MAP.get(collectionName).size()== 0) {
                        MILVUS_SESSION_MAP.remove(collectionName);
                    }
                    MilvusClientUtil.releaseCollection(collectionName);
                }
            }
        }
        return false;
    }

    @Override
    public void sendDefaultMessage(Session session, String userId) {
        try {
            Integer online = getOnlineCount();
            WebSocketEntity sendMessage = WebSocketEntity.builder().message(String.format("online{}", online)).build();
            sendMessage(session, sendMessage.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     * @param session
     */
    @Override
    public void handleClose(Session session)  {
        MILVUS_SESSION_MAP.forEach((key, value) -> {
            value.remove(session.getId());
            if (value.size() == 0) {
                MILVUS_SESSION_MAP.remove(key);
                MilvusClientUtil.releaseCollection(key);
            }
        });
    }

}
