package com.website.service;

import com.website.entity.DocContents;
import com.website.entity.Document;
import com.website.entity.KbSetting;
import com.website.service.base.BaseService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 文档分段内容 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface DocContentsService extends BaseService<DocContents> {

    boolean saveContents(List<String> contents, Document document);

    void embeddingContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total);

    void qaContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total, KbSetting kbSetting, String summary);

    void summaryContents(List<DocContents> splitContents, AtomicInteger atomicTotal, String docId, String userId, Integer total, KbSetting kbSetting);

    Map<String, Object> getEmbedCount(String docId);

    Map<String, Object> getQaCount(String docId);

    Map<String, Object> getSummaryCount(String docId);

    /**
     * 获取上下文
     * @param contentIds
     * @param qaIds
     * @return {id, content}
     */
    List<Map<String, String>> getContextList(List<String> contentIds, List<String> qaIds);
}
