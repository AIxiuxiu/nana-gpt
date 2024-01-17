package com.website.service.loader;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;

import java.io.InputStream;

public class ExcelFileLoader implements ResourceLoader{

    @Override
    public String getContent(InputStream inputStream) {
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        return reader.readAsText(false);
    }

}
