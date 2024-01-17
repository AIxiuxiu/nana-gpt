package com.website.search;

/**
 * @author ahl
 * @desc
 * @create 2023/8/16 13:41
 */

import java.util.List;

public interface SearchEngine {

    int timeout = 30000;

    String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko)Chrome/56.0.2924.87 Safari/537.36" ;

    List<SearchResult> search(String query);

}
