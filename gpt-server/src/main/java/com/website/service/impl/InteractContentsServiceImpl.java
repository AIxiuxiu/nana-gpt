package com.website.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.entity.Interact;
import com.website.entity.InteractContents;
import com.website.entity.InteractQuestion;
import com.website.entity.KbSetting;
import com.website.mapper.InteractContentsMapper;
import com.website.mapper.InteractMapper;
import com.website.service.ChatService;
import com.website.service.InteractContentsService;
import com.website.service.InteractQuestionService;
import com.website.service.http.BaseApiResponse;
import com.website.service.http.DataCenterHttp;
import com.website.service.http.ResultCodeEnum;
import com.website.util.ChatGPTUtil;
import com.website.websocket.endpoint.WSMessageService;
import com.website.websocket.model.WSMessageQuestionProgressEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 互动问答分段内容 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
@Service
@Slf4j
public class InteractContentsServiceImpl extends ServiceImpl<InteractContentsMapper, InteractContents> implements InteractContentsService {

    @Autowired
    DataCenterHttp dataCenterHttp;

    @Resource
    private InteractMapper interactMapper;

    @Resource
    private InteractContentsMapper interactContentsMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private InteractQuestionService interactQuestionService;

    @Autowired
    private WSMessageService wsMessageService;


    @Override
    @Async("queueThreadPool")
    public void getContents(List<String> splitReferences, Interact interact, AtomicInteger atomicTotal) {
        splitReferences.forEach(reference -> {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("url", URLEncoder.encode(reference, "UTF-8"));
                BaseApiResponse baseApiResponse = dataCenterHttp.get("/idol/getContent", params);
                if(baseApiResponse.getCode().equals(ResultCodeEnum.SUCCESS_CODE.getType())) {
                    Map<String, Object> dataMap = (Map<String, Object>) baseApiResponse.getData().get(0);
                    List<Map<String, String>> list = (List<Map<String, String>>) dataMap.get("__KEY_DATAS");
                    if (list.size() > 0) {
                        Map<String, String> detail = list.get(0);
                        String content = detail.get("drecontent");
                        if (content.length() > 200 && content.length() < 14000) {
                            int total = atomicTotal.incrementAndGet();
                            if (total > interact.getMaxNum()) {
                                return;
                            }
                            log.info("保存第《" + total +"》篇新闻："+ detail.get("title"));
                            InteractContents interactContents = new InteractContents()
                                    .setInteractId(interact.getId())
                                    .setTitle(detail.get("title"))
                                    .setContent(detail.get("drecontent"))
                                    .setSummary(detail.get("summary"))
                                    .setPublishDate(detail.get("publishdate"))
                                    .setUserId(interact.getUserId())
                                    .setTokens(TikTokensUtil.tokens(ChatCompletion.Model.GPT_3_5_TURBO_16K.getName(), detail.get("drecontent")));
                            baseMapper.insert(interactContents);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("保存数据失败" + e.getMessage());
            }
        });
    }


    @Override
    @Async("queueThreadPool")
    public void questionContents(List<InteractContents> splitContents, AtomicInteger atomicTotal, Interact interact, Integer total, String userId) {
        splitContents.forEach(contents -> {
            try {
                String qPrompt = interact.getQPrompt();
                String model = ChatCompletion.Model.GPT_3_5_TURBO_16K.getName();
                Message message1 = Message.builder().role(Message.Role.SYSTEM).content(qPrompt).build();
                Message message2 = Message.builder().role(Message.Role.USER).content(contents.getContent()).build();
                List<Message> messageList = Arrays.asList(message1, message2);
                int tokens = TikTokensUtil.tokens(model, messageList);
                int maxTokens = ChatGPTUtil.getMaxTokensByModel(model, tokens);
                ChatCompletion chatCompletion = ChatCompletion.builder().model(model).maxTokens(maxTokens).messages(messageList).build();
                ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);
                String resultStr = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
                List<InteractQuestion> qaList = formatSplitText(resultStr);
                qaList.forEach(question -> {
                    question.setQuestion(optimizeQuestion(interact, question.getQuestion()));
                    question.setContentId(contents.getId());
                    question.setTokens(TikTokensUtil.tokens(model, question.getQuestion()));
                    interactQuestionService.save(question);
                });
                contents.setHasQ(1);
                updateById(contents);
            } catch (Exception e) {
                log.error("问题生成失败" + e.getMessage());
                contents.setHasQ(0);
                updateById(contents);
            }
            int last = atomicTotal.decrementAndGet();
            log.info("剩余" + last);
            try {
                WSMessageQuestionProgressEntity progressEntity = WSMessageQuestionProgressEntity.builder().interactId(interact.getId().toString()).total(total).last(last).percentage((total-last)*100/total).status("").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
            if (last == 0) {
                getQResult(interact.getId().toString(), userId);
            }
        });
    }

    private String optimizeQuestion(Interact interact, String question) {
        if (interact.getQaOptimize() == 0) {
            return question;
        }
        String model = ChatCompletion.Model.GPT_3_5_TURBO_16K.getName();
        Message message1 = Message.builder().role(Message.Role.SYSTEM).content(interact.getOptimizePrompt()).build();
        Message message2 = Message.builder().role(Message.Role.USER).content(question).build();
        List<Message> messageList = Arrays.asList(message1, message2);
        int tokens = TikTokensUtil.tokens(model, messageList);
        int maxTokens = ChatGPTUtil.getMaxTokensByModel(model, tokens);
        ChatCompletion chatCompletion = ChatCompletion.builder().model(model).maxTokens(maxTokens).messages(messageList).build();
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
    private void getQResult(String interactId, String userId) {
        Map<String, Object> qCount = getQCount(interactId);
        Integer total =  Integer.parseInt(qCount.get("total").toString());
        Integer completed =  Integer.parseInt(qCount.get("completed").toString());
        Integer error =  Integer.parseInt(qCount.get("error").toString());

        if (total.equals(completed)) {
            log.info("QA问答对生成完成！！！！！");
            Interact interact = new Interact().setQuestionStatus(2).setId(Long.valueOf(interactId));
            interactMapper.updateById(interact);
            try {
                WSMessageQuestionProgressEntity progressEntity = WSMessageQuestionProgressEntity.builder().interactId(interactId).total(total).last(0).percentage(100).status("success").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        } else {
            log.info("问答对生成失败{}", error);
            Interact interact = new Interact().setQuestionStatus(3).setId(Long.valueOf(interactId));
            interactMapper.updateById(interact);
            try {
                WSMessageQuestionProgressEntity progressEntity = WSMessageQuestionProgressEntity.builder().interactId(interactId).total(total).last(total-completed).percentage(completed*100/total).status("exception").build();
                wsMessageService.pushMessage(progressEntity.toString(), userId);
            } catch (Exception e) {
                log.error("发送消息失败" + userId);
            }
        }
        // 发送消息
    }

    /**
     * 检查文本是否按格式返回
     */
    Pattern pattern = Pattern.compile("Q\\d+:(\\s*)(.*)(\\s*)(?=Q|$)");

    private List<InteractQuestion> formatSplitText(String text) {
        List<InteractQuestion> result = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while(m.find()) {
            String q = m.group(2);
            InteractQuestion question = new InteractQuestion().setQuestion(q);
            result.add(question);
        }
        return result;
    }

    @Override
    public Map<String, Object> getQCount(String interactId) {
        return interactContentsMapper.getQCount(interactId);
    }

}
