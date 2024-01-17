package com.website.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.website.websocket.model.WSMessageChatEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;

import javax.websocket.Session;
import java.util.Objects;

/**
 * 描述：OpenAI流式输出Socket接收
 *
 * @author ahl
 * @date 2023-03-23
 */
@Slf4j
public class OpenAIWebSocketEventSourceListener extends EventSourceListener {

    private Session session;
    private String kbId;
    private String replyId;
    private String prompt;

    public OpenAIWebSocketEventSourceListener(Session session, String kbId, String replyId, String prompt) {
        this.session = session;
        this.kbId = kbId;
        this.replyId = replyId;
        this.prompt = prompt;
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
        WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId(kbId).replyId(replyId).message(prompt).status("start").build();
        session.getBasicRemote().sendText(chatEntity.toString());
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.debug("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId(kbId).replyId(replyId).message("回答完毕").status("end").build();
            session.getBasicRemote().sendText(chatEntity.toString());
        } else {
            ObjectMapper mapper = new ObjectMapper();
            // 读取Json
            ChatCompletionResponse completionResponse = mapper.readValue(data, ChatCompletionResponse.class);
            String content = completionResponse.getChoices().get(0).getDelta().getContent();
            if (StringUtils.isNotEmpty(content)) {
                WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId(kbId).replyId(replyId).message(content).tokens(0).status("").build();
                session.getBasicRemote().sendText(chatEntity.toString());
            }
        }
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId(kbId).replyId(replyId).message("OpenAI链接失败").status("failure").build();
        session.getBasicRemote().sendText(chatEntity.toString());
        eventSource.cancel();
    }
}
