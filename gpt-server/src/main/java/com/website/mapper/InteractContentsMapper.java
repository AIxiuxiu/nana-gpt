package com.website.mapper;

import com.website.entity.InteractContents;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * <p>
 * 互动问答分段内容 Mapper 接口
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
public interface InteractContentsMapper extends BaseMapper<InteractContents> {

    Map<String, Object> getQCount(String interactId);
}
