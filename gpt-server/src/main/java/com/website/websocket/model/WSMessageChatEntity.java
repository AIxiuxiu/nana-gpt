package com.website.websocket.model;

import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author ahl
 * @desc 对话消息
 * @create 2023/1/11 14:10
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class WSMessageChatEntity extends WebSocketEntity {
    /**
     * 类型,默认 embedProgress
     */
    @Builder.Default
    String type = WebScoketType.CHAT.getType();

    /**
     * 是否为有效信息
     */
    @Builder.Default
    Boolean flag = true;

    /**
     * 角色
     */
    @Builder.Default
    String role = Message.Role.SYSTEM.name();

    /**
     * 知识库ID
     */
    String kbId;

    /**
     * 回复ID
     */
    String replyId;

    /**
     * 状态，start开始回答，end:回答完毕
     */
    String status;

    /**
     * 提示词
     */
    String prompt = "";

    /**
     * tokens 数量
     */
    long tokens;

    /**
     * 搜索方式
     */
    String search = "";

    /**
     * 搜索多少
     */
    int searchCount = 3;

    /**
     * 搜索方式 quick || full
     */
    String searchDepth = "quick";


    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }

}
