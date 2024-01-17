package com.website.service;

import com.website.entity.Interact;
import com.website.entity.InteractContents;
import com.website.service.base.BaseService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 互动问答分段内容 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
public interface InteractContentsService extends BaseService<InteractContents> {

    void getContents(List<String> splitReferences, Interact interact, AtomicInteger atomicTotal);

    void questionContents(List<InteractContents> splitContents, AtomicInteger atomicTotal, Interact interact, Integer total, String userId);

    Map<String, Object> getQCount(String interactId);
}
