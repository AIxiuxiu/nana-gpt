package com.website.service;

import com.unfbx.chatgpt.entity.chat.Message;
import com.website.entity.ChatRecord;
import com.website.entity.User;
import com.website.service.base.BaseService;

import java.util.List;

/**
 * <p>
 * 聊天记录 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-06-21
 */
public interface ChatRecordService extends BaseService<ChatRecord> {

    List<Message> getMessageWithHistory(User user, String content, String prompt);

}
