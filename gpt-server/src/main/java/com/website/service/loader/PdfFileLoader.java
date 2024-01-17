package com.website.service.loader;

import cn.hutool.core.util.NumberUtil;
import com.website.util.MyPDFTextStripper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PdfFileLoader implements ResourceLoader{
    @Override
    public String getContent(InputStream inputStream) {
        StringBuilder textInfo = new StringBuilder();
        try {
            PDDocument pd = PDDocument.load(inputStream);
            ObjectExtractor oe = new ObjectExtractor(pd);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            int pageSize = pd.getNumberOfPages();
            // 一页一页读取
            for (int i = 0; i < pageSize; i++) {
                System.out.println("第" + (i + 1) + "页");
                Page page = oe.extract(i + 1);
                List<Map<String, Object>> tableTempList = new ArrayList<>();

                //抽取出所有的表格内容
                List<Table> table = sea.extract(page);
                List<double[]> loc = new ArrayList<>();
                for (Table table1 : table) {
                    Map<String, Object> tableInfo = new HashMap<>();
                    if (table1.getRows().size() > 0) {
                        loc.add(new double[]{table1.y, table1.y + table1.height});
                        tableInfo.put("title", "");
                        tableInfo.put("other", "");
                        tableInfo.put("header", table1.getRows().get(0).stream().map(RectangularTextContainer::getText).map(o -> o.replace("\r", "")).collect(Collectors.joining(" | ")));
                        StringBuilder tableRows = new StringBuilder();
                        for (List<RectangularTextContainer> row : table1.getRows()) {
                            tableRows.append((row.stream().map(RectangularTextContainer::getText).map(o -> o.replace("\r", "")).collect(Collectors.joining(" | ")) + "\n").toString());
                        }
                        tableInfo.put("table", tableRows);
                        tableTempList.add(tableInfo);
                    }
                }
                //抽取出所有的文本内容
                MyPDFTextStripper stripper = new MyPDFTextStripper();
                //设置按顺序输出
                stripper.setSortByPosition(true);
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                stripper.setParagraphStart("\t");
                stripper.setParagraphEnd(" ");
                stripper.getText(pd);
                double lastY = 0;
                List<List<TextPosition>> charactersByArticle = stripper.myGetCharactersByArticle();
                StringBuilder pagraph = new StringBuilder();
                int tableIndex = 0;
                boolean inTable = false;
                outer:
                for (int m = 0; m < charactersByArticle.get(0).size(); m++) {
                    TextPosition word = charactersByArticle.get(0).get(m);
                    float startY = word.getY();
                    // float endY = word.getEndY();
                    if (lastY != startY) {
                        if (inTable && tableIndex < loc.size() && tableIndex < tableTempList.size()) {
                            if(loc.get(tableIndex)[1] > startY)  {
                                continue;
                            } else {
                                inTable = false;
                                Map tableInfo = tableTempList.get(tableIndex);
                                String tableStr =  tableInfo.get("header") + "\n" + tableInfo.get("table");
                                textInfo.append(tableStr + "\n");
                            }
                        }

                        // 如果文本的y在表格的上下边框的内部，则不输出继续遍历文本
                        for (int j = tableIndex; j < loc.size(); j++) {
                            double[] l = loc.get(j);
                            if (l[0] <= startY && l[1] >= startY) {
                                inTable = true;
                                tableIndex = j;
                                // 添加段落
                                String pagraphStr = pagraph.toString().trim();
                                if (StringUtils.isNotEmpty(pagraphStr) && pagraphStr.length() > 10 && !NumberUtil.isNumber(pagraphStr) && !pagraphStr.contains(".........")  && !pagraphStr.contains("········")) {
                                    textInfo.append(pagraphStr);
                                    textInfo.append("\n");
                                }
                                pagraph = new StringBuilder();
                                continue outer;
                            }
                        }
                    }


                    if (!word.getUnicode().equals(" ")) {
                        pagraph.append(word.getUnicode());
                    } else {
                        if (lastY == startY) {
                            //同一行继续添加
                            pagraph.append(word.getUnicode());
                        } else {
                            String pagraphStr = pagraph.toString().trim();
                            if (StringUtils.isNotEmpty(pagraphStr) && pagraphStr.length() > 10 && !NumberUtil.isNumber(pagraphStr) && !pagraphStr.contains(".........")  && !pagraphStr.contains("········")) {
                                textInfo.append(pagraphStr);
                                textInfo.append("\n");
                            }
                            pagraph = new StringBuilder();
                            lastY = startY;
                        }
                    }
                }

                String pagraphStr = pagraph.toString().trim();
                if (StringUtils.isNotEmpty(pagraphStr) && pagraphStr.length() > 10 && !NumberUtil.isNumber(pagraphStr) && !pagraphStr.contains(".........") && !pagraphStr.contains("········")) {
                    textInfo.append(pagraphStr);
                    textInfo.append("\n");
                }
            }
            pd.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        return textInfo.toString();
    }
}
