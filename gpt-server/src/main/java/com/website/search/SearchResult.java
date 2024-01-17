package com.website.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author ahl
 * @desc
 * @create 2023/8/16 13:41
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "搜索结果", description = "")
public class SearchResult {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("URL地址")
    private String url;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("快照，摘要")
    private String snippet;

    @ApiModelProperty("正文内容")
    private String content;

}
