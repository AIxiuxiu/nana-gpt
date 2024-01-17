package com.website.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author ahl
 * @since 2023-07-30
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("interact_question")
@ApiModel(value = "InteractQuestion对象", description = "")
@HeadRowHeight(28)
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 41)
@HeadFontStyle(fontHeightInPoints = 16)
@ContentFontStyle(fontHeightInPoints = 12)
public class InteractQuestion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

    @ApiModelProperty("关联内容ID")
    @ExcelIgnore
    private Long contentId;

    @ApiModelProperty("用户ID")
    @ExcelIgnore
    private Long userId;

    @ApiModelProperty("问题")
    @ExcelProperty("提问内容")
    @ColumnWidth(140)
    private String question;

    @ApiModelProperty("消耗token数量")
    @ExcelIgnore
    private Integer tokens;

    @ApiModelProperty("创建时间")
    @ExcelIgnore
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @ExcelIgnore
    private LocalDateTime updateTime;

}
