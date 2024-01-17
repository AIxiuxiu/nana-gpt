package com.website.search;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ahl
 * @desc
 * @create 2023/8/16 13:42
 */
@Slf4j
@Component
public class Google implements SearchEngine {

    @Value("${http.proxy.host}")
    String proxyHost;

    @Value("${http.proxy.port}")
    int proxyPort;

    @Override
    public List<SearchResult> search(String query) {
        query = URLEncodeUtil.encodeQuery(query.trim().replaceAll("\\s", "+"));

        String queryUrl = "https://www.google.com/search?q=" + query.trim() + "&num=10&lr=lang_zh-CN";

        Document resultsPage;
        String result = HttpRequest.get(queryUrl)
                .setHttpProxy(proxyHost, proxyPort)
                .timeout(timeout)
                .header(Header.USER_AGENT, userAgent)
                .execute().body();
        if (StringUtils.isNotEmpty(result)) {
            resultsPage = Jsoup.parse(result);
        } else {
            log.error("Could not search through duckduckgo.");
            return null;
        }
        List<SearchResult> searchResults = new ArrayList<>();
        Elements titles = resultsPage.select("a[href] h3");
        for (Element titleEl : titles) {
            String title = titleEl.text();
            if (titleEl.parent() == null || titleEl.parent().parent() == null || titleEl.parent().parent().parent() == null || titleEl.parent().parent().parent().parent() == null) {
                continue;
            }
            Element parent = titleEl.parent().parent().parent().parent();
            String urlStr = parent.select("a[href]").attr("href");
            UrlBuilder builder = UrlBuilder.ofHttp(urlStr, CharsetUtil.CHARSET_UTF_8);
            String url = builder.getQuery().get("url").toString();
            String snippet = parent.nextElementSibling().text();
            searchResults.add(new SearchResult().setUrl(url).setTitle(title).setSnippet(snippet));
        }
        return searchResults;
    }

}
