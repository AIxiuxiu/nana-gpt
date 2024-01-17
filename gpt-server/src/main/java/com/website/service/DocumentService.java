package com.website.service;

import com.website.entity.Document;
import com.website.service.base.BaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
public interface DocumentService extends BaseService<Document> {

    /**
     *  上传文件，并对文件进行处理
     * @param file
     */
    List<String> getFileContent(MultipartFile file, String kbId);

}
