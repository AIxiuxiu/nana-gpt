package com.website.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.entity.KnowledgeBase;
import com.website.mapper.KnowledgeBaseMapper;
import com.website.service.KnowledgeBaseService;
import com.website.util.PlaceholderResolver;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 知识库 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    @Override
    public String setLastPrompt(String prompt, Long kbId) {
        KnowledgeBase knowledgeBase = getById(kbId);

        Map<String, Object> params = new HashMap<>();
        params.put("companyName", knowledgeBase.getCompanyName());
        params.put("companyCode", knowledgeBase.getCompanyCode());
        params.put("kbName", knowledgeBase.getKbName());
        return PlaceholderResolver.getDefaultResolver().resolveByMap(prompt, params);
    }

}
