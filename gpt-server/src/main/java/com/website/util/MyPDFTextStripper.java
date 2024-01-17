package com.website.util;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

/**
 * @author ahl
 * @desc
 * @create 2023/6/19 16:11
 */
public class MyPDFTextStripper extends PDFTextStripper  {

        public MyPDFTextStripper() throws IOException {
            super();
        }

        public List<List<TextPosition>> myGetCharactersByArticle() {
            return charactersByArticle;
        }
}
