package com.website.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.billing.BillingUsage;
import com.unfbx.chatgpt.entity.billing.Subscription;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.common.Choice;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.entity.ChatRecord;
import com.website.entity.KbSetting;
import com.website.entity.User;
import com.website.enums.Prompt;
import com.website.listener.OpenAISSEEventSourceListener;
import com.website.listener.OpenAIWebSocketEventSourceListener;
import com.website.search.SearchResult;
import com.website.search.SearchService;
import com.website.service.*;
import com.website.util.ChatGPTUtil;
import com.website.util.PlaceholderResolver;
import com.website.websocket.model.WSMessageChatEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ahl
 */

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private OpenAiStreamClient openAiStreamClient;

    @Autowired
    private KbChatService kbChatService;

    @Autowired
    private ChatRecordService chatRecordService;

    @Autowired
    private KbSettingService kbSettingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ThreadPoolTaskExecutor queueThreadPool;

    @Autowired
    private PromptService promptService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SearchService searchService;

    @Override
    public ChatRecord oneShotChat(String message) {
        CompletionResponse completions = openAiClient.completions(message);
        List<String> collect = Arrays.stream(completions.getChoices()).map(Choice::getText).collect(Collectors.toList());
        String ask = StrUtil.join("", collect);
        long tokens = completions.getUsage().getTotalTokens();
        log.info("Open AI 官方计算的总的tokens数{}", tokens);
        return new ChatRecord().setContent(ask).setRole(Message.Role.SYSTEM.name()).setTokens(tokens);
    }


    @Override
    public ChatCompletionResponse chatCompletion(List<Message> messageList) {
        return openAiClient.chatCompletion(messageList);
    }

    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletion chatCompletion) {
        return openAiClient.chatCompletion(chatCompletion);
    }

    @Override
    public SseEmitter sseShotChat(String messages) {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        OpenAISSEEventSourceListener openAiEventSourceListener = new OpenAISSEEventSourceListener(sseEmitter);
        openAiStreamClient.streamCompletions(messages, openAiEventSourceListener);
        return sseEmitter;
    }

    @Override
    public void onWSMessage(Session session, String content, String userId, String prompt) {
        log.info("userId {} 发送消息:{}", userId, content);
        long begin = System.currentTimeMillis();

        User user = userService.getById(userId);

        queueThreadPool.execute(()->{
            long tokens = TikTokensUtil.tokens(user.getTokenModel(), content);
            ChatRecord record = new ChatRecord().setUserId(userId).setKbId("").setContent(content).setRole(Message.Role.USER.name()).setTokens(tokens);
            chatRecordService.save(record);
            WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId("").replyId(record.getId()).role(Message.Role.USER.name()).message(content).tokens(tokens).status("send").build();
            try {
                session.getBasicRemote().sendText(chatEntity.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        String replyId = userId + "_" +System.currentTimeMillis();

        List<Message> messageList = chatRecordService.getMessageWithHistory(user, content, prompt);

        // webScoket监听
        OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(session, "", replyId, "");

        ChatCompletion q = ChatCompletion.builder().messages(messageList).model(user.getModel()).maxTokens(user.getReplyMaxToken()).stream(true).build();
        openAiStreamClient.streamChatCompletion(q, eventSourceListener);

        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("waite reply time cost: {}", cost);
    }

    @Override
    public void onWSMessageBySearch(Session session, String userId, WSMessageChatEntity chatEntity) {
        String content = chatEntity.getMessage();
        log.info("userId {} 发送消息:{}", userId, content);
        long begin = System.currentTimeMillis();

        User user = userService.getById(userId);

        String replyId = userId + "_" +System.currentTimeMillis();

        String prompt = content;
        String query = getSearchQuery(content);
        log.info(content + "提取关键词为=>" + query);
        String webResults = "";
        if (StringUtils.isNotEmpty(query)) {
            int tokens = TikTokensUtil.tokens(user.getTokenModel(), content) + user.getReplyMaxToken() + 10;
            int maxToken = ChatGPTUtil.getMaxTokensByModel(user.getModel(), tokens);
            webResults = getSearchResult(query, user.getModel(), maxToken, chatEntity);
        }
        if (StringUtils.isNotEmpty(webResults)) {
            prompt = getLastQuestion(content, webResults);
        }

        queueThreadPool.execute(()->{
            long tokens = TikTokensUtil.tokens(user.getTokenModel(), content);
            ChatRecord record = new ChatRecord().setUserId(userId).setKbId("").setContent(content).setRole(Message.Role.USER.name()).setTokens(tokens);
            chatRecordService.save(record);
            WSMessageChatEntity chatEntity1 = WSMessageChatEntity.builder().kbId("").replyId(record.getId()).role(Message.Role.USER.name()).message(content).tokens(tokens).status("send").build();
            try {
                session.getBasicRemote().sendText(chatEntity1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        String fullPrompt = prompt.replace("Search results:", "Search value: " + query + "\n Search results:");
        // webScoket监听
        OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(session, "", replyId, fullPrompt);
        Message message = Message.builder().role(Message.Role.USER).content(prompt).build();
        List<Message> messageList = Arrays.asList(message);
        ChatCompletion q = ChatCompletion.builder().messages(messageList).model(user.getModel()).maxTokens(user.getReplyMaxToken()).stream(true).build();
        openAiStreamClient.streamChatCompletion(q, eventSourceListener);
        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("waite reply time cost: {}", cost);
    }

    /**
     * 获取搜索关键字
     * @param content
     * @return
     */
    private String getSearchQuery(String content) {
        String searchQueryPrompt = promptService.getByTopic(Prompt.SEARCH_QUERY.topic);
        Map<String, Object> params = new HashMap<>();
        params.put("currentDate", DateUtil.now());
        params.put("question", content);
        searchQueryPrompt = PlaceholderResolver.getDefaultResolver().resolveByMap(searchQueryPrompt, params);

        Message message2 = Message.builder().role(Message.Role.USER).content(searchQueryPrompt).build();
        List<Message> messageList = Arrays.asList(message2);
        ChatCompletion chatCompletion = ChatCompletion.builder().model(ChatCompletion.Model.GPT_3_5_TURBO.getName()).maxTokens(1000).messages(messageList).build();
        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
        String resultStr = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
        if (StringUtils.isEmpty(resultStr)) {
            return null;
        }
        return resultStr;
    }


    private String getSearchResult(String query, String model, int maxToken, WSMessageChatEntity chatEntity) {
        List<SearchResult> searchResults = new ArrayList<>();
        StringBuilder webResults = new StringBuilder();
        int num = 1;
        int curTokens = 0;
        searchResults = chatEntity.getSearchDepth().equals("full") ? searchService.searchContentByType(chatEntity.getSearch(), query) : searchService.searchListByType(chatEntity.getSearch(), query);
        if (searchResults != null && searchResults.size() > 0) {
            for (SearchResult searchResult :searchResults) {
                String webContent;
                if (chatEntity.getSearchDepth().equals("full")) {
                    webContent = searchResult.getContent();
                    if (webContent.length() > 10000) {
                        webContent = webContent.substring(0, 10000);
                    }
                } else {
                    webContent = searchResult.getSnippet();
                }
                if (StringUtils.isEmpty(webContent) || webContent.length() < 20) {
                   break;
                }

                StringBuilder result = new StringBuilder().append("NUMBER: ").append(num).append("\n").append("URL: ").append(searchResult.getUrl()).append("\n").append("TITLE: ").append(searchResult.getTitle()).append("\n").append("CONTENT: ").append(webContent).append("\n");
                int tokenNum = TikTokensUtil.tokens(model, result.toString());
                if(num > chatEntity.getSearchCount()){
                    break;
                }
                if (curTokens + tokenNum > maxToken) {
                    continue;
                }
                webResults.append(result);
                curTokens += tokenNum;
                num++;
            }
        }

        return webResults.toString();
    }

    private String getLastQuestion(String content, String webResults) {
        String searchContentPrompt = promptService.getByTopic(Prompt.SEARCH_CONTENT.topic);
        Map<String, Object> params = new HashMap<>();
        params.put("currentDate", DateUtil.now());
        params.put("query", content);
        params.put("webResults", webResults);
        return PlaceholderResolver.getDefaultResolver().resolveByMap(searchContentPrompt, params);
    }

    @Override
    public void onWSMessageByKbId(Session session, String content, String userId, String kbId) {
        log.info("userId {} 发送消息:{}", userId, content);
        long begin = System.currentTimeMillis();

        KbSetting kbSetting = kbSettingService.getSettingByKbId(kbId);

        queueThreadPool.execute(()->{
            long tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), content);
            ChatRecord record = new ChatRecord().setUserId(userId).setKbId(kbId).setContent(content).setRole(Message.Role.USER.name()).setTokens(tokens);
            chatRecordService.save(record);
            WSMessageChatEntity chatEntity = WSMessageChatEntity.builder().kbId(kbId).replyId("").role(Message.Role.USER.name()).message(content).tokens(tokens).status("send").build();
            try {
                session.getBasicRemote().sendText(chatEntity.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        String replyId = kbId+"_"+System.currentTimeMillis();
        List<Message> newPrompt = kbChatService.getPromptByKB(userId, content, kbSetting);
        if (newPrompt == null) {
            WSMessageChatEntity chatEntity1 = WSMessageChatEntity.builder().kbId(kbId).replyId(replyId).message("你的问题不在知识库中。").status("empty").build();
            try {
                session.getBasicRemote().sendText(chatEntity1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // webScoket监听
            OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(session, kbId, replyId, newPrompt.get(0).getContent());
            int tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), newPrompt);
            int maxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), tokens);
            ChatCompletion q = ChatCompletion.builder().messages(newPrompt).model(kbSetting.getModel()).maxTokens(maxTokens).stream(true).build();
            openAiStreamClient.streamChatCompletion(q, eventSourceListener);
        }
        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("waite reply time cost: {}", cost);
    }


    @Override
    public EmbeddingResponse embeddings(String content) {
        return openAiClient.embeddings(content);
    }

    @Override
    public EmbeddingResponse embeddings(List<String> contents) {
        return openAiClient.embeddings(contents);
    }

    @Override
    public Subscription subscription() {
        Subscription subscription = openAiClient.subscription();
        /**
         * has_payment_method：是否有支付方式。
         * canceled：订阅是否已取消。
         * canceled_at：订阅取消时间。
         * delinquent：是否逾期未付款。
         * access_until：订阅有效期截止时间。
         * soft_limit：软限制，即每月可使用的API请求次数上限。
         * hard_limit：硬限制，即每月可使用的API请求次数上限。
         * system_hard_limit：系统硬限制，即每月可使用的API请求次数上限。
         * soft_limit_usd：软限制对应的美元价格。
         * hard_limit_usd：硬限制对应的美元价格。
         * system_hard_limit_usd：系统硬限制对应的美元价格。
         * plan：订阅计划信息。
         *   title：计划名称。
         *   id：计划ID。
         * account_name：账户名称。
         * po_number：采购订单号。
         * billing_email：账单邮件地址。
         * tax_ids：税务ID。
         * billing_address：账单地址。
         * business_address：商业地址。
         */
        log.info("用户名：{}", subscription.getAccountName());
        log.info("用户总余额（美元）：{}", subscription.getHardLimitUsd());
        return subscription;
    }

    @Override
    public BillingUsage billingUsage(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now().plusDays(1);
        }
        BillingUsage billingUsage = openAiClient.billingUsage(startDate, endDate);
        /**
         * daily_costs：每日使用情况列表。
         *     timestamp：时间戳，表示当天的日期。
         *     line_items：使用明细列表。
         *         name：使用项目名称。
         *         cost：使用费用。
         * total_usage：总使用量。
         */
        log.info("{}至{}使用金额（美分）：{}", startDate, endDate, billingUsage.getTotalUsage());
        return billingUsage;
    }

}
