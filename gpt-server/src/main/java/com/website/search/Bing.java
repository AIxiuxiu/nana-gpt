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
 * @create 2023/8/16 13:51
 */
@Slf4j
@Component
public class Bing implements SearchEngine {

    @Value("${http.proxy.host}")
    String proxyHost;

    @Value("${http.proxy.port}")
    int proxyPort;

    @Override
    public List<SearchResult> search(String query) {

        String queryUrl = "https://www.bing.com/search?q=" + URLUtil.encode(query.trim()) + "&FORM=BESBTB";

        Document resultsPage;
        String result = HttpRequest.get(queryUrl)
                .setHttpProxy(proxyHost, proxyPort)
                .timeout(timeout)
                .header(Header.USER_AGENT, userAgent)
                .execute().body();
        if (StringUtils.isNotEmpty(result)) {
            resultsPage = Jsoup.parse(result);
        } else {
            log.error("Could not search through bing.");
            return null;
        }

        List<SearchResult> searchResults = new ArrayList<>();
        Elements gs = resultsPage.select("ol#b_results li");
        for (Element g : gs) {
            Element hEl = g.select("h2").first();
            if (hEl == null || hEl.select("a").size() == 0) {
                continue;
            }
            String url = hEl.select("a").first().attr("href");
            String title = hEl.text();
            if (g.select("span.algoSlug_icon") != null) {
                g.select("span.algoSlug_icon").remove();
            }
            String snippet = g.select("div.b_caption").text();
            searchResults.add(new SearchResult().setUrl(url).setTitle(title).setSnippet(snippet));
        }
        return searchResults;
    }

}
