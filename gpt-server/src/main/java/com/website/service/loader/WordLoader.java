package com.website.service.loader;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class WordLoader implements ResourceLoader{
    @Override
    public String getContent(InputStream inputStream) {
        XWPFDocument document = null;
        try {
            document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String content = extractor.getText();
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
