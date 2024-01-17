package com.website.enums;

/**
 * @Author: ahl
 */
public enum  Prompt {
    /**
     *
     */
    QA("QA生成"),
    KB("知识库问答"),
    SUMMARY("总结内容"),
    SUMMARY_ALL("总结多段内容"),
    QUESTION("生成问题"),
    OPTIMIZE("优化问题"),
    SEARCH_QUERY("提取搜索的关键词"),
    SEARCH_CONTENT("联网搜索问题"),
    ;

    public final String topic;

    Prompt(String topic){
        this.topic = topic;
    }
}
