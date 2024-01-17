package com.website.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.website.entity.Document;
import com.website.entity.KbSetting;
import com.website.mapper.DocumentMapper;
import com.website.service.DocumentService;
import com.website.service.KbSettingService;
import com.website.service.loader.ResourceLoader;
import com.website.service.loader.ResourceLoaderFactory;
import com.website.util.RecursiveCharacterTextSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ahl
 * @since 2023-06-12
 */
@Service
@Slf4j
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    @Autowired
    private KbSettingService kbSettingService;

    @Autowired
    private ResourceLoaderFactory resourceLoaderFactory;


    @Override
    public List<String> getFileContent(MultipartFile file, String kbId) {
        try {
            String fileName = file.getOriginalFilename();
            ResourceLoader resourceLoader = resourceLoaderFactory.getLoaderByFileType(fileName.substring(fileName.lastIndexOf(".") + 1));
            String content = resourceLoader.getContent(file.getInputStream());
            assert content != null;
            content = content.replaceAll(" +", " ");

            int chunkSize = 1000;
            int chunkOverlap = 200;
            KbSetting kbSetting = kbSettingService.getSettingByKbId(kbId);
            if (kbSetting != null) {
                chunkSize = kbSetting.getSplitterChunkSize();
                chunkOverlap = kbSetting.getSplitterChunkOverlap();
            }
            RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter(null, chunkSize, chunkOverlap);
            return textSplitter.splitText(content);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
