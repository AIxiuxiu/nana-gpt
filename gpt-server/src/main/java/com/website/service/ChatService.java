package com.website.service;

import com.unfbx.chatgpt.entity.billing.BillingUsage;
import com.unfbx.chatgpt.entity.billing.Subscription;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import com.website.entity.ChatRecord;
import com.website.websocket.model.WSMessageChatEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.websocket.Session;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author: ahl
 */
public interface ChatService {

    /**
     * 单轮聊天-普通输出
     * @param message
     * @return ChatRecordEntity
     */
    ChatRecord oneShotChat(String message);

    /**
     * 问答
     * @param messageList
     * @return
     */
    ChatCompletionResponse chatCompletion(List<Message> messageList);

    /**
     * 问答
     * @param chatCompletion
     * @return
     */
    ChatCompletionResponse chatCompletion(ChatCompletion chatCompletion);

    /**
     * 单轮聊天-流式输出
     * @param prompt
     * @return SseEmitter
     */
    SseEmitter sseShotChat(String prompt);

    /**
     * webscoket消息
     * @param session
     * @param content
     * @param userId
     * @param prompt
     */
    void onWSMessage(Session session, String content, String userId, String prompt);

    /**
     * webscoket消息
     * @param session
     * @param content
     * @param userId
     * @param prompt
     */
    void onWSMessageBySearch(Session session, String userId, WSMessageChatEntity chatEntity);


    /**
     * webscoket消息
     * @param session
     * @param content
     * @param userId
     * @param kbId
     */
    void onWSMessageByKbId(Session session, String content, String userId, String kbId);

        /**
         * 文本向量化
         * @param content
         * @return EmbeddingResponse
         */
    EmbeddingResponse embeddings(String content);

    /**
     * 文本向量化
     * @param contents
     * @return EmbeddingResponse
     */
    EmbeddingResponse embeddings(List<String> contents);

    /**
     * 获取API-Key的余额
     * @return Subscription
     */
    Subscription subscription();

    /**
     * 使用金额
     * @return BigDecimal
     */
    BillingUsage billingUsage(LocalDate startDate, LocalDate endDate);
}
