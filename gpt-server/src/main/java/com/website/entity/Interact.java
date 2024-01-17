package com.website.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 互动问答
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "Interact对象", description = "互动问答")
public class Interact extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("公司名")
    private String companyName;

    @ApiModelProperty("公司简称")
    private String companyShortName;

    @ApiModelProperty("公司代码")
    private String companyCode;

    @ApiModelProperty("oid")
    private String orgId;

    @ApiModelProperty("开始时间")
    private String startDate;

    @ApiModelProperty("结束时间")
    private String endDate;

    @ApiModelProperty("最大数量")
    private Integer maxNum;

    @ApiModelProperty("是否是核心新闻")
    private Integer isCore;

    @ApiModelProperty("0:未生成题 1：问题生成中 2：已生成问题")
    private Integer questionStatus;

    @ApiModelProperty("0:不在使用；1:正常使用")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("Q生成提示词")
    private String qPrompt;

    @ApiModelProperty("问题优化 0：否 1：是")
    private Integer qaOptimize;

    @ApiModelProperty("问题优化提示词")
    private String optimizePrompt;
}
