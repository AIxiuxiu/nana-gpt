package com.website.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.website.entity.PromptEntity;
import com.website.mapper.PromptMapper;
import com.website.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提示词
 */

@Service
@Slf4j
public class PromptServiceImpl extends ServiceImpl<PromptMapper, PromptEntity> implements PromptService {

    private Map<String, PromptEntity> topicCache = ImmutableMap.of();

    @Override
    public IPage<PromptEntity> list(int pageNum, int limit, String content, Integer target) {

        Page<PromptEntity> page = new Page<>(pageNum, limit);
        QueryWrapper<PromptEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(content)){
            wrapper.and(w -> w.like("content", content).or().like("topic", content));
        }
        if(target != null){
            wrapper.and(w -> w.eq("target", target));
        }
        wrapper.orderByDesc("updateTime");
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public String getByTopic(String topic) {
        PromptEntity promptEntity = this.topicCache.get(topic);
        return promptEntity == null ? null : promptEntity.getContent();
    }

    /**
     * 每小时更新缓存
     */
    @Scheduled(initialDelay = 0L, fixedRate = 60 * 60 * 1000L)
    @Override
    public void load() {
        LambdaQueryWrapper<PromptEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PromptEntity::getTarget, 0);
        // 加载主键为topic的缓存
        this.topicCache = ImmutableMap.copyOf(
                baseMapper.selectList(queryWrapper).stream()
                    // 按照topic先分组 -> Map<String, List<PromptEntity>>
                    .collect(Collectors.groupingBy(PromptEntity::getTopic))
                    // 对Map的value进行操作，取得第一个元素
                    .entrySet().stream().collect(
                            Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().findFirst().orElse(null))));

        log.info("加载Prompt库缓存成功！, topicCache size:{}}", this.topicCache.size());
    }
}
