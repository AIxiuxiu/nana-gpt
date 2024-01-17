package com.website.service;

import com.website.entity.KnowledgeBase;
import com.website.service.base.BaseService;

/**
 * <p>
 * 知识库 服务类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface KnowledgeBaseService extends BaseService<KnowledgeBase> {

    String setLastPrompt(String prompt, Long kbId);

}
