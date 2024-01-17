package com.website.service.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * dataCenter请求类
 * @author ahl
 */
@Component
public class DataCenterHttp extends HttpService<BaseApiResponse>{

    @Autowired
    DataCenterHttp(@Value("${provider.base.url}") String prefixUrl) {
        this.prefixUrl = prefixUrl;
        this.responseType = BaseApiResponse.class;
    }

    public enum SourceEnum {
        kb, fms, idol, cninfo, goku, zb, deep, interact, cpp, sin, sbond, event, business, credit, finance, rsreport, cnsx;
    }

}
