package com.website.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.website.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "Qa对象", description = "")

@HeadRowHeight(28)
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 41)
@HeadFontStyle(fontHeightInPoints = 16)
@ContentFontStyle(fontHeightInPoints = 12)
public class Qa extends BaseEntity {

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
    @ExcelProperty("问题")
    @ColumnWidth(120)
    private String question;

    @ApiModelProperty("答案")
    @ExcelProperty("答案")
    @ColumnWidth(140)
    private String answer;

    @ApiModelProperty("0：未向量化，1：已向量化 2：向量化失败")
    @ExcelIgnore
    private Integer embedStatus;

    @ApiModelProperty("embedding向量化数据")
    @ExcelIgnore
    private String embedding;

    @ApiModelProperty("消耗token数量")
    @ExcelIgnore
    private Integer tokens;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    @ExcelIgnore
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ExcelIgnore
    private LocalDateTime updateTime;

}
