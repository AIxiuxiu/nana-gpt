package com.website.websocket.model;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @author ahl
 * @desc 进度消息
 * @create 2023/1/11 14:10
 */
@Data
@SuperBuilder
public class WSMessageEmbedProgressEntity extends WebSocketEntity {
    /**
     * 类型,默认 embedProgress
     */
    @Builder.Default
    String type = WebScoketType.EMBED_PROGRESS.getType();

    /**
     * 是否为有效信息
     */
    @Builder.Default
    Boolean flag = true;

    /**
     * 文档ID
     */
    String docId;
    /**
     * 总共
     */
    Integer total;

    /**
     * 剩余
     */
    Integer last;

    /**
     * 进度
     */
    int percentage;

    /**
     * 进度状态 success，exception
     */
    String status;

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }


}
