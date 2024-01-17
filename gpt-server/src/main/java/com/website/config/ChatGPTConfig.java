package com.website.config;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ahl
 * @desc chatGPT配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "chatgpt")
public class ChatGPTConfig {

    /**
     * socks 或 http 代理
     */
    private String proxyType;

    private Proxy.Type getProxyType() {
        Proxy.Type type = Proxy.Type.SOCKS;
        if (proxyType.equals("http") || proxyType.equals("HTTP")) {
            type = Proxy.Type.HTTP;
        }
        return type;
    }

    private String proxyHost;
    private int proxyPost;

    /**
     * chatGPT key
     */
    private List<String> apiKeys;
    /**
     * 反向代理的openApi地址
     */
    private String apiHost;

    public Proxy getProxy() {
        Proxy proxy = null;
        if (StringUtils.isNotEmpty(getProxyHost())) {
            proxy = new Proxy(getProxyType(), new InetSocketAddress(getProxyHost(), getProxyPost()));
        }
        return proxy;
    }

    @Bean
    public OpenAiClient openAiClient() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient okHttpClient;
        Proxy proxy = getProxy();
        okHttpClient = new OkHttpClient
                .Builder()
                .proxy(proxy)
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();


        return OpenAiClient
                .builder()
                .apiHost(getApiHost())
                .apiKey(getApiKeys())
                //自定义key使用策略 默认随机策略
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .build();
    }

    @Bean
    public OpenAiStreamClient openAiStreamClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //!!!!!!测试或者发布到服务器千万不要配置Level == BODY!!!!
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient okHttpClient;
        Proxy proxy = getProxy();

        okHttpClient = new OkHttpClient
                .Builder()
                .proxy(proxy)
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();

        return OpenAiStreamClient
                .builder()
                .apiHost(getApiHost())
                .apiKey(getApiKeys())
                //自定义key使用策略 默认随机策略
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .build();
    }
}
