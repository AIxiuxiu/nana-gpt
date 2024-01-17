package com.website.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.website.entity.PromptEntity;

/**
 * @Author: ahl
 * @Date: 2023/4/11 16:38
 */
public interface PromptService extends IService<PromptEntity> {
    /**
     * 查询prompt列表
     * @param pageNum
     * @param limit
     * @param content
     * @param target 0管理员，1用户
     * @return
     */
     IPage<PromptEntity> list(int pageNum, int limit, String content, Integer target);

    /**
     * 根据 `主题` 获取提示
     * 实现：缓存
     * @param topic
     * @return
     */
    String getByTopic(String topic);

    /**
     * 加载缓存
     */
    void load();
}
