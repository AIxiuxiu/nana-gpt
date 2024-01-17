package com.website.websocket.model;

import cn.hutool.json.JSONUtil;
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
public class WSMessageMilvusEntity extends WebSocketEntity {
    /**
     * 类型,默认 embedProgress
     */
    @Builder.Default
    String type = WebScoketType.MILVUS.getType();

    /**
     * 是否为有效信息
     */
    @Builder.Default
    Boolean flag = true;
    
    /**
     * 知识库ID
     */
    String kbId;

    
    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }

}
