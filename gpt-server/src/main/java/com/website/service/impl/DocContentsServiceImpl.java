package com.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.knuddels.jtokkit.api.EncodingType;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.entity.*;
import com.website.enums.Prompt;
import com.website.mapper.DocContentsMapper;
import com.website.service.*;
import com.website.util.ChatGPTUtil;
import com.website.util.MilvusClientUtil;
import com.website.websocket.endpoint.WSMessageService;
import com.website.websocket.model.WSMessageEmbedProgressEntity;
import com.website.websocket.model.WSMessageQaProgressEntity;
import com.website.websocket.model.WSMessageSummaryProgressEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 文档分段内容 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Service
@Slf4j
public class DocContentsServiceImpl extends ServiceImpl<DocContentsMapper, DocContents> implements DocContentsService {

    @Autowired
    private ChatService chatService;

    @Resource
    private DocContentsMapper docContentsMapper;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private QaService qaService;

    @Autowired
    private WSMessageService wsMessageService;

    @Autowired
    private PromptService promptService;

    @Override
    @Transactional
   public  boolean saveContents(List<String> contents, Document document) {
        List<DocContents> docContentsList = new ArrayList<>();
        contents.forEach(content -> {
            DocContents docContents = new DocContents()
                    .setUserId(document.getUserId())
                    .setDocId(document.getId())
                    .setContent(content)
                    .setTokens(TikTokensUtil.tokens(EncodingType.CL100K_BASE, content));
            docContentsList.add(docContents);
        });
        return saveBatch(docContentsList);
    }


    @Override
    @Async("queueThreadPool")
    public void embeddingContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total) {
        splitContents.forEach(docContents -> {
            try {
                EmbeddingResponse embeddingResponse = chatService.embeddings(docContents.getContent());
                log.info("tokens >> " + embeddingResponse.getUsage().getTotalTokens());
                List<BigDecimal> embedding = embeddingResponse.getData().get(0).getEmbedding();
                String embeddingString = embedding.stream().map(String::valueOf)
                        .collect(Collectors.joining(","));
                log.info("embeddingString" + embeddingString.length());
                docContents.setEmbedding(embeddingString);
                docContents.setEmbedStatus(1);
                updateById(docContents);
            } catch (Exception e) {
                log.error("内容向量化失败" + e.getMessage());
                docContents.setEmbedStatus(2);
                updateById(docContents);
            }
            int last = atomicTotal.decrementAndGet();
            log.info("剩余" + last);
            try {
                WSMessageEmbedProgressEntity progressEntity = WSMessageEmbedProgressEntity.builder().docId(docId).total(total).last(last).percentage((total-last)*100/total).status("").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
            if (last == 0) {
                getEmbeddingResult(docId, userId);
            }
        });
    }

    /**
     * 向量化的结果
     */
    private void getEmbeddingResult(String docId, String userId) {
       Map<String, Object> embedCount = getEmbedCount(docId);
       Integer total =  Integer.parseInt(embedCount.get("total").toString());
       Integer completed =  Integer.parseInt(embedCount.get("completed").toString());
       Integer error =  Integer.parseInt(embedCount.get("error").toString());

        if (total.equals(completed)) {
            log.info("向量化完成！！！！！");
            Document document = new Document().setEmbedStatus(2).setId(Long.valueOf(docId));
            //保存向量数据
            saveEmbed(docId);
            documentService.updateById(document);
            try {
                WSMessageEmbedProgressEntity progressEntity = WSMessageEmbedProgressEntity.builder().docId(docId).total(total).last(0).percentage(100).status("success").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        } else {
            log.info("向量化失败{}", error);
            Document document = new Document().setEmbedStatus(3).setId(Long.valueOf(docId));
            documentService.updateById(document);
            try {
                WSMessageEmbedProgressEntity progressEntity = WSMessageEmbedProgressEntity.builder().docId(docId).total(total).last(total-completed).percentage(completed*100/total).status("exception").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        }
        // 发送消息
    }


    /**
     * 保存向量数据库
     */
    public void saveEmbed(String docId) {

        Document document = documentService.getById(docId);

        DataSqlEntity dataSqlEntity = new DataSqlEntity();
        LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocContents::getId, DocContents::getEmbedding);
        queryWrapper.eq(DocContents::getDocId, docId);
        List<DocContents>  docContentsList = list(queryWrapper);

        List<List<Float>> ll = docContentsList.stream().map(doc -> {
            return Arrays.stream(doc.getEmbedding().split(",")).map(Float::new).collect(Collectors.toList());
        }).collect(Collectors.toList());
        List<String> ids = docContentsList.stream().map(v -> {
            return "docContents_" + v.getId();
        }).collect(Collectors.toList());

        dataSqlEntity.setLl(ll);
        dataSqlEntity.setDocId(ids);

        String collectionName = "kb_"+ document.getKbId();
        String partitionName = "docId_" + docId;
        if (MilvusClientUtil.hasPartitionName(collectionName, partitionName)) {
            MilvusClientUtil.dropPartitions(collectionName, partitionName);
        }
        MilvusClientUtil.createPartition(collectionName, partitionName);
        MilvusClientUtil.insert(dataSqlEntity, collectionName, partitionName);
    }


    @Override
    @Async("queueThreadPool")
    public void qaContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total, KbSetting kbSetting, String summary) {
        final StringBuilder docSummary = new StringBuilder(summary);
        splitContents.forEach(docContents -> {
            try {
                String qaPrompt = kbSetting.getQaPrompt();
                Message message1 = Message.builder().role(Message.Role.SYSTEM).content(qaPrompt).build();
                Message message2 = Message.builder().role(Message.Role.USER).content(docSummary.toString() + docContents.getContent()).build();
                List<Message> messageList = Arrays.asList(message1, message2);
                int tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), messageList);
                int maxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), tokens);
                ChatCompletion chatCompletion = ChatCompletion.builder().model(kbSetting.getModel()).maxTokens(maxTokens).messages(messageList).build();
                ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
                String resultStr = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
                List<Qa> qaList = formatSplitText(resultStr);
                qaList.forEach(qa -> {
                    qa.setQuestion(optimizeQuestion(kbSetting, qa.getQuestion()));
                    if (kbSetting.getQaEmbed() == 1) {
                        EmbeddingResponse embeddingResponse = chatService.embeddings(qa.getQuestion());
                        log.info("tokens >> " + embeddingResponse.getUsage().getTotalTokens());
                        List<BigDecimal> embedding = embeddingResponse.getData().get(0).getEmbedding();
                        String embeddingString = embedding.stream().map(String::valueOf)
                                .collect(Collectors.joining(","));
                        log.info("embeddingString" + embeddingString.length());
                        qa.setEmbedding(embeddingString);
                        qa.setEmbedStatus(1);
                    } else {
                        qa.setEmbedStatus(0);
                    }
                    qa.setContentId(docContents.getId());
                    qa.setTokens(TikTokensUtil.tokens(EncodingType.CL100K_BASE, qa.getQuestion()));
                    qaService.save(qa);
                });
                docContents.setHasQA(1);
                updateById(docContents);
                if (kbSetting.getQaAddSummary() == 1) {
                    docSummary.setLength(0);
                    docSummary.append(docContents.getSummary());
                }
            } catch (Exception e) {
                log.error("问答对生成失败" + e.getMessage());
                docContents.setHasQA(0);
                updateById(docContents);
            }
            int last = atomicTotal.decrementAndGet();
            log.info("剩余" + last);
            try {
                WSMessageQaProgressEntity progressEntity = WSMessageQaProgressEntity.builder().docId(docId).total(total).last(last).percentage((total-last)*100/total).status("").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
            if (last == 0) {
                getQaResult(docId, userId);
            }
        });
    }

    private String optimizeQuestion(KbSetting kbSetting, String question) {
        if (kbSetting.getQaOptimize() == 0) {
            return question;
        }
        Message message1 = Message.builder().role(Message.Role.SYSTEM).content(kbSetting.getOptimizePrompt()).build();
        Message message2 = Message.builder().role(Message.Role.USER).content(question).build();
        List<Message> messageList = Arrays.asList(message1, message2);
        int tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), messageList);
        int maxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), tokens);
        ChatCompletion chatCompletion = ChatCompletion.builder().model(kbSetting.getModel()).maxTokens(maxTokens).messages(messageList).build();
        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
        String resultStr = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
        if (StringUtils.isEmpty(resultStr)) {
            return question;
        }
        return resultStr;
    }


    /**
     * 向量化的结果
     */
    private void getQaResult(String docId, String userId) {
        Map<String, Object> qaCount = getQaCount(docId);
        Integer total =  Integer.parseInt(qaCount.get("total").toString());
        Integer completed =  Integer.parseInt(qaCount.get("completed").toString());
        Integer error =  Integer.parseInt(qaCount.get("error").toString());

        if (total.equals(completed)) {
            log.info("QA问答对生成完成！！！！！");
            Document document = new Document().setQaStatus(2).setId(Long.valueOf(docId));
            //保存向量数据
            saveQAEmbed(docId);
            documentService.updateById(document);
            try {
                WSMessageQaProgressEntity progressEntity = WSMessageQaProgressEntity.builder().docId(docId).total(total).last(0).percentage(100).status("success").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        } else {
            log.info("问答对生成失败{}", error);
            Document document = new Document().setQaStatus(3).setId(Long.valueOf(docId));
            documentService.updateById(document);
            try {
                WSMessageQaProgressEntity progressEntity = WSMessageQaProgressEntity.builder().docId(docId).total(total).last(total-completed).percentage(completed*100/total).status("exception").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        }
        // 发送消息
    }

    /**
     * 保存向量数据库
     */
    public void saveQAEmbed(String docId) {

        Document document = documentService.getById(docId);

        DataSqlEntity dataSqlEntity = new DataSqlEntity();

        MPJLambdaWrapper<Qa> queryWrapper = JoinWrappers.lambda(Qa.class)
                .selectAll(Qa.class)
                .leftJoin(DocContents.class, DocContents::getId, Qa::getContentId)
                .leftJoin(Document.class, Document::getId, DocContents::getDocId)
                .eq(Document::getId, docId)
                .eq(Qa::getEmbedStatus, 1);

        List<Qa>  qaList = qaService.list(queryWrapper);
        if (qaList.size() == 0) {
            log.warn("无问答对，请查看！！！");
            return;
        }

        List<List<Float>> ll = qaList.stream().map(doc -> {
            return Arrays.stream(doc.getEmbedding().split(",")).map(Float::new).collect(Collectors.toList());
        }).collect(Collectors.toList());
        List<String> ids = qaList.stream().map(v -> {
            return "qa_" + v.getId();
        }).collect(Collectors.toList());

        dataSqlEntity.setLl(ll);
        dataSqlEntity.setDocId(ids);

        String collectionName = "kb_"+ document.getKbId();
        String partitionName = "qaId_" + docId;
        if (MilvusClientUtil.hasPartitionName(collectionName, partitionName)) {
            MilvusClientUtil.dropPartitions(collectionName, partitionName);
        }
        MilvusClientUtil.createPartition(collectionName, partitionName);
        MilvusClientUtil.insert(dataSqlEntity, collectionName, partitionName);
    }

    /**
     * 检查文本是否按格式返回
     */
    Pattern pattern = Pattern.compile("Q\\d+:(\\s*)(.*)(\\s*)A\\d+:(\\s*)([\\s\\S]*?)(?=Q|$)");

    private List<Qa> formatSplitText(String text) {
        List<Qa> result = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while(m.find()) {
            String q = m.group(2);
            String a = m.group(5).trim();
            if (!q.equals(a)) {
                Qa qa = new Qa().setAnswer(a).setQuestion(q);
                result.add(qa);
            }
        }
        return result;
    }


    @Override
    public Map<String, Object> getEmbedCount(String docId) {
        return docContentsMapper.getEmbedCount(docId);
    }

    @Override
    public Map<String, Object> getQaCount(String docId) {
        return docContentsMapper.getQaCount(docId);
    }

    @Override
    public Map<String, Object> getSummaryCount(String docId) {
        return docContentsMapper.getSummaryCount(docId);
    }


    @Override
    public List<Map<String, String>> getContextList(List<String> contentIds, List<String> qaIds) {
        return docContentsMapper.getContextList(contentIds, qaIds);
    }


    @Override
    @Async("queueThreadPool")
    public void summaryContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total, KbSetting kbSetting) {
        splitContents.forEach(docContents -> {
            try {
                String summaryPrompt = kbSetting.getSummaryPrompt();
                Message message1 = Message.builder().role(Message.Role.SYSTEM).content(summaryPrompt).build();
                Message message2 = Message.builder().role(Message.Role.USER).content(docContents.getContent()).build();
                List<Message> messageList = Arrays.asList(message1, message2);
                int tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), messageList);
                int maxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), tokens);
                ChatCompletion chatCompletion = ChatCompletion.builder().model(kbSetting.getModel()).maxTokens(maxTokens).messages(messageList).build();
                ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
                String resultStr = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
                docContents.setSummary(resultStr);
                docContents.setSummaryStatus(1);
                updateById(docContents);
            } catch (Exception e) {
                log.error("总结生成失败" + e.getMessage());
                docContents.setSummaryStatus(2);
                updateById(docContents);
            }
            int last = atomicTotal.decrementAndGet();
            log.info("剩余" + last);
            try {
                WSMessageSummaryProgressEntity progressEntity = WSMessageSummaryProgressEntity.builder().docId(docId).total(total).last(last).percentage((total-last)*100/total).status("").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
            if (last == 0) {
                getSummaryResult(docId, userId, kbSetting);
            }
        });
    }


    /**
     * 总结的结果
     */
    private void getSummaryResult(String docId, String userId, KbSetting kbSetting) {
        Map<String, Object> summaryCount = getSummaryCount(docId);
        Integer total =  Integer.parseInt(summaryCount.get("total").toString());
        Integer completed =  Integer.parseInt(summaryCount.get("completed").toString());
        Integer error =  Integer.parseInt(summaryCount.get("error").toString());

        if (total.equals(completed)) {
            Document document = new Document().setSummaryStatus(2).setId(Long.valueOf(docId));
            String summary = summaryPDF(docId, kbSetting);
            log.info("总结生成完成:\n" + summary);
            document.setSummary(summary);
            documentService.updateById(document);
            try {
                WSMessageSummaryProgressEntity progressEntity = WSMessageSummaryProgressEntity.builder().docId(docId).total(total).last(0).percentage(100).status("success").summary(summary).build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        } else {
            log.info("总结生成失败{}", error);
            Document document = new Document().setSummaryStatus(3).setId(Long.valueOf(docId));
            documentService.updateById(document);
            try {
                WSMessageSummaryProgressEntity progressEntity = WSMessageSummaryProgressEntity.builder().docId(docId).total(total).last(total-completed).percentage(completed*100/total).status("exception").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        }
        // 发送消息
    }


    private String summaryPDF(String docId, KbSetting kbSetting) {
        LambdaQueryWrapper<DocContents> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocContents::getId,DocContents::getSummary);
        queryWrapper.eq(DocContents::getSummaryStatus, 1);
        queryWrapper.eq(DocContents::getDocId, docId);
        List<DocContents> docContentsList = list(queryWrapper);
        List<String> summaryResult = summaryContentList(docContentsList.stream().map((DocContents::getSummary)).collect(Collectors.toList()), kbSetting);
        return summaryResult.get(0);
    }

    private List<String> summaryContentList(List<String> contentList, KbSetting kbSetting) {

        List<String> summaryResult = new ArrayList<>();

//        String summaryPrompt = kbSetting.getSummaryPrompt();
        String summaryPrompt = promptService.getByTopic(Prompt.SUMMARY_ALL.topic);

        int sumMaxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), TikTokensUtil.tokens(kbSetting.getModel(), summaryPrompt) + 1000);
        int hasToken = 0;
        String summarys = "";
        for (String content : contentList) {
            hasToken += TikTokensUtil.tokens(kbSetting.getTokenModel(), content);
            if (hasToken > sumMaxTokens) {
                String summary = getSummaryStr(summarys, kbSetting, summaryPrompt);
                summaryResult.add(summary);
                hasToken = 0;
                summarys = "";
            } else {
                summarys += content;
            }
        }
        if (500 < hasToken) {
            String summary = getSummaryStr(summarys, kbSetting, summaryPrompt);
            summaryResult.add(summary);
        } else {
            summaryResult.add(summarys);
        }

        if (summaryResult.size() > 1) {
            return summaryContentList(summaryResult, kbSetting);
        } else {
            return summaryResult;
        }
    }


    private  String getSummaryStr(String summarys, KbSetting kbSetting, String summaryPrompt) {
        Message message1 = Message.builder().role(Message.Role.SYSTEM).content(summaryPrompt).build();
        Message message2 = Message.builder().role(Message.Role.USER).content(summarys).build();
        List<Message> messageList = Arrays.asList(message1, message2);
        int tokens = TikTokensUtil.tokens(kbSetting.getTokenModel(), messageList);
        int maxTokens = ChatGPTUtil.getMaxTokensByModel(kbSetting.getModel(), tokens);
        ChatCompletion chatCompletion = ChatCompletion.builder().model(kbSetting.getModel()).maxTokens(maxTokens).messages(messageList).build();
        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
        return chatCompletionResponse.getChoices().get(0).getMessage().getContent();
    }
}
