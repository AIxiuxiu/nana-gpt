package com.website.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文档
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "文档上传", description = "文档上传参数")
public class UploadDocVo {

    @ApiModelProperty(value = "文件数组", required = true)
    MultipartFile[] files;

    @ApiModelProperty(value = "知识库ID")
    String kbId;

    @ApiModelProperty(value = "文档名称")
    String docName;

    @ApiModelProperty(value = "标签")
    String tags;
}


