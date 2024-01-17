package com.website.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import com.website.entity.ChatRecord;
import com.website.entity.User;
import com.website.mapper.ChatRecordMapper;
import com.website.service.ChatRecordService;
import com.website.util.ChatGPTUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 聊天记录 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-21
 */
@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements ChatRecordService {

    @Override
    public List<Message> getMessageWithHistory(User user, String content, String prompt) {
        QueryWrapper<ChatRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("kbId", "");
        queryWrapper.eq("userId", user.getId());
        queryWrapper.eq("status", 1);
        DateTime startDate = DateUtil.offsetHour(new Date(), -user.getHowLongTime());
        queryWrapper.ge("createTime", startDate);
        queryWrapper.orderByDesc("createTime");
        String sql="limit " + user.getMaxHistory();
        queryWrapper.last(sql);

        List<ChatRecord> chatRecordList = baseMapper.selectList(queryWrapper);
        int tokens = TikTokensUtil.tokens(user.getTokenModel(), prompt + content) + user.getReplyMaxToken() + 10;
        int maxToken = ChatGPTUtil.getMaxTokensByModel(user.getModel(), tokens);

        Message lastMessage = Message.builder().role(Message.Role.USER).content(content).build();

        List<Message> messageList = new ArrayList<>();
        messageList.add(lastMessage);

        int curTokens = 0;
        for (ChatRecord record : chatRecordList) {
            int tokenNum = (int) record.getTokens();
            // 保证加上当前轮次的聊天对话时token总数不超过最大数量限制
            if(curTokens + tokenNum > maxToken){
                break;
            }
            Message message = Message.builder().role(record.getRole().toLowerCase()).content(record.getContent()).build();
            messageList.add(message);
        };

        if (StringUtils.isNotEmpty(prompt)) {
            // 提示词
            Message message = Message.builder().role(Message.Role.USER).content(prompt).build();
            messageList.add(message);
        }

        Collections.reverse(messageList);
        return messageList;
    }
}
