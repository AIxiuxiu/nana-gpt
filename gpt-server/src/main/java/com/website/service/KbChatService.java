package com.website.service;

import com.unfbx.chatgpt.entity.chat.Message;
import com.website.entity.KbSetting;

import java.util.List;

/**
 * <p>
 *  知识库聊天
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface KbChatService {

    List<Message> getPromptByKB(String userId, String message, KbSetting kbSetting);

}
