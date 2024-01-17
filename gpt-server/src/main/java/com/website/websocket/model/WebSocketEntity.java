package com.website.websocket.model;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author ahl
 * @desc WebSocket 消息实体
 * @create 2023/1/11 12:19
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class WebSocketEntity {

    /**
     * 类型,默认零
     */
    @Builder.Default
    String type = WebScoketType.DEFAULT.getType();

    /**
     * 消息
     */
    String message;

    /**
     * 是否为有效信息
     */
    @Builder.Default
    Boolean flag = false;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
