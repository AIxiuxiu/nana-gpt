package com.website.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.website.entity.base.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 互动问答分段内容
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("interact_contents")
@ApiModel(value = "InteractContents对象", description = "互动问答分段内容")
public class InteractContents extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联文档ID")
    private Long interactId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("总结")
    private String summary;

    @ApiModelProperty("发布时间")
    private String publishDate;

    @ApiModelProperty("消耗token数量")
    private Integer tokens;

    @ApiModelProperty("1：问题生成")
    private Integer hasQ;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;


}
