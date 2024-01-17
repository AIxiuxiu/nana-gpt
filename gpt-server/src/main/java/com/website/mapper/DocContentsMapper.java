package com.website.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.website.entity.DocContents;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档分段内容 Mapper 接口
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface DocContentsMapper extends BaseMapper<DocContents> {

    Map<String, Object> getEmbedCount(String docId);

    Map<String, Object> getQaCount(String docId);

    Map<String, Object> getSummaryCount(String docId);

    List<Map<String, String>> getContextList(List<String> contentIds, List<String> qaIds);

}
