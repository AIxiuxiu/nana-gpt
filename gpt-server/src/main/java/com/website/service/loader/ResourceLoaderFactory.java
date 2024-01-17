package com.website.service.loader;

import org.springframework.stereotype.Component;

@Component
public class ResourceLoaderFactory {

    public ResourceLoader getLoaderByFileType(String fileType){
        if (FileType.isTextFile(fileType)){
            return new TextFileLoader();
        } else if (FileType.isWord(fileType)) {
            return new WordLoader();
        } else if (FileType.isPdf(fileType)) {
            return new PdfFileLoader();
        } else if (FileType.isMdFile(fileType)) {
            return new MarkDownFileLoader();
        } else if (FileType.isExcel(fileType)) {
            return new ExcelFileLoader();
        } else if (FileType.isCodeFile(fileType)) {
            return new CodeFileLoader();
        } else {
            return new TextFileLoader();
        }
    }
}
