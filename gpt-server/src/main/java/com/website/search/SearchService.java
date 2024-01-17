package com.website.search;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author ahl
 * @desc
 * @create 2023/8/17 10:32
 */
@Slf4j
@Service
public class SearchService {

    @Autowired
    Duckduckgo duckduckgo;

    @Autowired
    Bing bing;

    @Autowired
    CnBing cnBing;

    @Autowired
    Google google;
    private SearchEngine getSearchEngine(String type) {
        if ("google".equals(type)) {
            return google;
        } else if ("bing".equals(type)) {
            return bing;
        } else if ("duckduckgo".equals(type)) {
            return duckduckgo;
        }
        return cnBing;
    }

    /**
     * 搜索列表
     * @param type
     * @param query
     * @return
     */
    public List<SearchResult> searchListByType(String type, String query) {
        SearchEngine searchEngine = getSearchEngine(type);
        return searchEngine.search(query);
    }

    /**
     * 搜索正文
     * @param type
     * @param query
     * @return
     */
    public List<SearchResult> searchContentByType(String type, String query) {
        List<SearchResult> searchResults = searchListByType(type, query);
        List<SearchResult> searchContents = new ArrayList<>();
        if (searchResults != null && searchResults.size() > 0) {
            List<CompletableFuture<SearchResult>> futures = new ArrayList<>();
            int limit = Math.min(searchResults.size(), 10);
            for (SearchResult searchResult: searchResults.subList(0, limit)) {
                CompletableFuture<SearchResult> searchFuture = CompletableFuture.supplyAsync(() -> {
                    return getDocByUrl(searchResult);
                });
                futures.add(searchFuture);
            }
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            try{
                //获取异步操作的结果，如果被阻塞，无法得到结果，那么最多等待1分钟之后退出
                combinedFuture.get(1, TimeUnit.MINUTES);
                for (CompletableFuture<SearchResult> future: futures) {
                    SearchResult result = future.get();
                    if (result != null) {
                        searchContents.add(result);
                    }
                }
            } catch (InterruptedException e) {
                log.error("获取搜索内容失败");
                return searchContents;
            } catch (ExecutionException e) {
                log.error("获取搜索内容线程在等待过程中被中断");
                return searchContents;
            } catch (TimeoutException e) {
                log.error("获取搜索内容在future对象完成之前已过期");
                return searchContents;
            }
        }
        return searchContents;
    }

    private SearchResult getDocByUrl(SearchResult searchResult) {
        try {
            if (StringUtils.isEmpty(searchResult.getUrl())) {
                return  null;
            }
            HttpResponse response = HttpRequest.get(searchResult.getUrl())
                    .timeout(30000)
                    .execute();
            String contentType = response.header(Header.CONTENT_TYPE);
            if (!contentType.contains("text/html")) {
                return null;
            }
            String result = response.body();
            if (StringUtils.isNotEmpty(result)) {
                Readability readability = new Readability(result);
                String content = readability.getArticleContent(false);
                if (StringUtils.isNotEmpty(content)) {
                    content = reviseText(content);
                    //内容特别少的不要了
                    if (content.length() > 64){
                        searchResult.setContent(content);
                        return searchResult;
                    }
                }
            }
        } catch (Exception e) {
            log.error("爬取文章失败" + e.getMessage());
        }
        return null;
    }

    private  String reviseText(String text)
    {
        if (text.startsWith("\n"))
        {
            text = text.replaceFirst("\n", "");
        }
        if (text.startsWith(" "))
        {
            text = text.replaceFirst(" +", "");
        }

        text = text.replaceAll("\\*", " * ");

        text = text.replaceAll("\\,+", ",");
        text = text.replaceAll("\\,\\.", ".");
        text = text.replaceAll("\\r\\n- ?\\r\\n", "- ");
        text = text.replaceAll("\\n- ?\\n", "- ");
        text = text.replaceAll("\\n-", "\n- ");

        text = text.replaceAll("\\n +", "\n");
        text = text.replaceAll("\\n+", "\n");
        text = text.replaceAll(" +", " ");

        text = text.trim();
        return text;
    }
}
