package com.website.entity;

import lombok.Data;

import java.util.List;

/**
 * @author ahl
 * @Desriiption: 向量库实体
 */
@Data
public class DataSqlEntity {

    /**
     * 分段后的每一段的向量
     */
    private List<List<Float>> ll;

    /**
     *  id
     */
    private List<String> docId;

    /**
     *  总共token数量
     */
    private Integer totalToken;

}
