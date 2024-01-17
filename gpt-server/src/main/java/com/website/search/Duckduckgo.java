package com.website.search;

import cn.hutool.core.util.URLUtil;
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
 * @create 2023/8/16 13:54
 */
@Slf4j
@Component
public class Duckduckgo implements SearchEngine {

    @Value("${http.proxy.host}")
    String proxyHost;

    @Value("${http.proxy.port}")
    int proxyPort;

    @Override
    public List<SearchResult> search(String query) {

        String queryUrl = "https://html.duckduckgo.com/html?q=" + URLUtil.encode(query.trim()) + "&kl=cn-zh";

        Document resultsPage;

        // Jsoup请求一直有问题。代理不通，中文错误
        // resultsPage = Jsoup.connect(queryUrl).proxy(chatGPTConfig.getProxy()).timeout(timeout).userAgent(userAgent).ignoreHttpErrors(true).get();

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
        Elements gs = resultsPage.getElementsByClass("web-result");
        for (Element g : gs) {
            Element hEl = g.getElementsByClass("result__title").first();
            String url = hEl.select("a").first().attr("href");
            String title = hEl.text();
            String snippet = g.getElementsByClass("result__snippet").text();
            searchResults.add(new SearchResult().setUrl(url).setTitle(title).setSnippet(snippet));
        }
        return searchResults;
    }

}
