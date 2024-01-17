package com.website.service.impl;

import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.entity.KbSetting;
import com.website.enums.Prompt;
import com.website.service.*;
import com.website.util.MilvusClientUtil;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Service
@Slf4j
public class KbChatServiceImpl implements KbChatService {

    @Autowired
    private ChatService chatService;

    @Autowired
    private PromptService promptService;

    @Autowired
    private DocContentsService docContentsService;

    @Autowired
    ChatRecordService chatRecordService;

    @Override
    public List<Message> getPromptByKB(String userId, String message, KbSetting kbSetting) {
        // 优化问题
        String newQaPrompt = getStandaloneQuestion(message);
        // 根据优化后的问题 和 索引信息，构造文件对话问题。
        return getContext(kbSetting, newQaPrompt);
    }


    // 获取一个独立的问题，确保ChatGPT不会回答文章无关的内容
    private String getStandaloneQuestion(String message){
        message = message.trim().replace("\n", " ");
        return message;
//        String qaPromptTemplate = promptService.getByTopic(Prompt.QA_PROMPT_TEMPLATE.topic);
//        // 优化问题
//        return String.format(qaPromptTemplate, message);
    }

    /**
     *  搜索相似上下文，并且构造最终问题
     */
    private List<Message> getContext(KbSetting kbSetting, String message) {
        long begin = System.currentTimeMillis();
        /* ↓检索文档 */
        EmbeddingResponse embeddingResp = chatService.embeddings(message);
        if(embeddingResp == null){
            return null;
        }
        long end1 = System.currentTimeMillis();
        log.info("embeddings time cost: {}", end1 - begin);

        String collectionName = "kb_" + kbSetting.getKbId();
        if(!MilvusClientUtil.getLoadState(collectionName)) {
            MilvusClientUtil.loadCollection(collectionName);
        }

        if (StringUtils.isEmpty(kbSetting.getReplyPrompt() )) {
            kbSetting.setReplyPrompt(promptService.getByTopic(Prompt.KB.topic));
        }
        List<List<Float>> qaEmbed = embeddingResp.getData().stream().map(v -> {
            return v.getEmbedding().stream().map(BigDecimal::floatValue).collect(Collectors.toList());
        }).collect(Collectors.toList());
        R<SearchResults> searchResults = MilvusClientUtil.searchContent(qaEmbed, collectionName, kbSetting.getReplySearchTopK());
        if (searchResults.getStatus() != R.Status.Success.getCode() || StringUtils.isNotEmpty(searchResults.getData().getStatus().getReason())) {
            return null;
        }
        SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResults.getData().getResults());
        List<String> docIdList = (List<String>) wrapper.getFieldData(MilvusClientUtil.ID_FIELD, 0);
        if (docIdList.size() == 0) {
            return null;
        }
        List<SearchResultsWrapper.IDScore> scoreList = wrapper.getIDScore(0).stream()
                .filter((SearchResultsWrapper.IDScore score) -> score.getScore() >= kbSetting.getReplyScore())
                .collect(Collectors.toList());
        log.info(scoreList.toString());
        docIdList = docIdList.subList(0, scoreList.size());

        long end2 = System.currentTimeMillis();
        log.info("search embeddings time cost: {}", end2 - begin);
        // 添加相关上下文在最终问答
        StringBuilder context = new StringBuilder();
        String prompt = kbSetting.getReplyPrompt();
        Message limitPrompt = Message.builder().content(prompt).role(Message.Role.SYSTEM).build();
        Message questionPrompt = Message.builder().content("用户问题:" + message).role(Message.Role.USER).build();

        int contextRecordsTokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), Arrays.asList(limitPrompt, questionPrompt));
        int count = 0;
        int num = 1;

        List<String> contentIds = docIdList.stream().filter(v -> v.startsWith("docContents_")).map(v -> v.replace("docContents_", "")).collect(Collectors.toList());
        List<String> qaIds = docIdList.stream().filter(v -> v.startsWith("qa_")).map(v -> v.replace("qa_", "")).collect(Collectors.toList());
        List<Map<String, String>> contextList = docContentsService.getContextList(contentIds, qaIds);
        Map<String, String> contextMap = contextList.stream().collect(Collectors.toMap(e -> e.get("id"), e -> e.get("content")));

        for(String docId: docIdList){
            if (!contextMap.containsKey(docId)) {
                continue;
            }
            String content = num + ":" + contextMap.get(docId);
            int curCandidateToken = TikTokensUtil.tokens(kbSetting.getTokenModel(), content);
            if(curCandidateToken + contextRecordsTokens > kbSetting.getReplyMaxToken()){
                break;
            }
            context.append(content);
            contextRecordsTokens += curCandidateToken;
            count++;
            num++;
        }
        log.info("当前向量相关上下文总token数为{}，向量个数为{}", contextRecordsTokens, count);
        prompt = String.format(prompt, context, message);
        Message kbPrompt = Message.builder().content(prompt).role(Message.Role.USER).build();

        long end3 = System.currentTimeMillis();
        log.info("getContext time cost: {}", end3 - begin);
        return Arrays.asList(kbPrompt, questionPrompt);
    }

}
