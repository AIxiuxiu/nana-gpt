package com.website.websocket.model;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author ahl
 * @desc 错误消息
 * @create 2023/1/11 14:10
 */
@Data
@SuperBuilder
public class WSMessageErrorEntity extends WebSocketEntity {
    /**
     * 类型,默认 embedProgress
     */
    @Builder.Default
    String type = WebScoketType.ERROR.getType();

    /**
     * 是否为有效信息
     */
    @Builder.Default
    Boolean flag = true;

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }

}
