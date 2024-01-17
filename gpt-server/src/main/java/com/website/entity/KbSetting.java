package com.website.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ahl
 * @since 2023-07-27
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("kb_setting")
@ApiModel(value = "KbSetting对象", description = "")
public class KbSetting extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("知识库ID")
    private Long kbId;

    @ApiModelProperty("模型")
    private String model;

    @ApiModelProperty("QA生成提示词")
    private String qaPrompt;

    @ApiModelProperty("回答提示词")
    private String replyPrompt;

    @ApiModelProperty("总结生成提示词")
    private String summaryPrompt;

    @ApiModelProperty("文本分割大小")
    private Integer splitterChunkSize;

    @ApiModelProperty("文本分割每个块有多少重叠")
    private Integer splitterChunkOverlap;

    private String splitterSeparators;

    @ApiModelProperty("进程数")
    private Integer poolSize;

    @ApiModelProperty("回复最大提示词数量")
    private Integer replyMaxToken;

    @ApiModelProperty("回复向量库查询数量")
    private Integer replySearchTopK;

    @ApiModelProperty("相似度限制")
    private Float replyScore;

    @ApiModelProperty("问答对向量化0：否 1：是")
    private Integer qaEmbed;

    @ApiModelProperty("问答对添加上文总结0：否 1：是")
    private Integer qaAddSummary;

    @ApiModelProperty("问题优化 0：否 1：是")
    private Integer qaOptimize;

    @ApiModelProperty("问题优化提示词")
    private String optimizePrompt;

    public String getTokenModel() {
        if (this.model.equals("gpt-4-1106-preview")) {
            return ChatCompletion.Model.GPT_4.getName();
        }
        return this.model;
    }
}
