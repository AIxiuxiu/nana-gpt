package com.website.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信类型枚举
 */
@Getter
@AllArgsConstructor
public enum WebScoketType {

    /**
     * 长链接类型
     */
    DEFAULT("default", "默认"),
    ERROR("error", "错误"),
    EMBED_PROGRESS("embedProgress", "向量化进度"),
    QA_PROGRESS("qaProgress", "问答对进度"),
    SUMMARY_PROGRESS("summaryProgress", "总结进度"),
    CHAT("chat", "对话"),
    MILVUS("milvus", "向量库消息"),
    QUESTION_PROGRESS("questionProgress", "问题生成进度"),
    ;

    /**
     * 类型
     */
    private final String type;

    /**
     * 名称
     */
    private final String name;


    public static WebScoketType getSmsEnumByType(String type) {
        for (WebScoketType typeEnum : WebScoketType.values()) {
            if (typeEnum.type.equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }
}
