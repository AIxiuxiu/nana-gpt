package com.website.service.loader;

import java.io.InputStream;

/**
 * 资源载入
 */
public interface ResourceLoader {
    String getContent(InputStream inputStream);
}
