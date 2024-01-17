package com.website.service.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http请求封装
 * @author ahl
 */
@Slf4j
public class HttpService<T> {
	/**
	 * 	url前缀
 	 */
	public String prefixUrl;

	public Class<T> responseType;

	@Autowired
	private RestTemplate restTemplate;


	/**
	 * 获取url
	 * @param url 请求地址
	 * @return 结果
	 */
	private String getUrl(String url) {
		if (prefixUrl != null && !this.prefixUrl.isEmpty()) {
			return this.prefixUrl + url;
		}
		return url;
	}


	//*********************************** 给定responseType ************************//

	/**
	 * GET请求调用方式
	 *
	 * @param url 请求URL
	 * @return T 响应对象封装类
	 */
	public T get(String url) {
		return get(url, this.getResponseType());
	}

	/**
	 * GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return T 响应对象封装类
	 */
	public T get(String url, Object... uriVariables) {
		return get(url, this.getResponseType(), uriVariables);
	}

	/**
	 * GET请求调用方式
	 *
	 * @param url 请求URL
	 * @return T 响应对象封装类
	 */
	public T get(String url, Map<String, String> params) {
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(
				params.entrySet().stream().collect(
						Collectors.toMap(Map.Entry::getKey, e -> Collections.singletonList(e.getValue()))));
		return get(url, this.getResponseType(), multiValueMap);
	}

	public <T> T get(String url, Class<T> responseType, MultiValueMap<String, String> params) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getUrl(url));
		URI uri = builder.queryParams(params).build().encode().toUri();
		return restTemplate.getForObject(uri, responseType);
	}

	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T get(String url, Map<String, String> headers, Map<String, ?> uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return get(url, httpHeaders, this.getResponseType(), uriVariables);
	}

	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T get(String url, Map<String, String> headers, Object... uriVariables) {
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		return get(url, HttpMethod.GET, requestEntity, this.getResponseType(), uriVariables);
	}

	/*********************************** 无 responseType ************************/

	/**
	 * GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param responseType 返回对象类型
	 * @return T 响应对象封装类
	 */
	public <T> T get(String url, Class<T> responseType) {
		return restTemplate.getForObject(getUrl(url), responseType);
	}

	/**
	 * GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return T 响应对象封装类
	 */
	public <T> T get(String url, Class<T> responseType, Object... uriVariables) {
		return restTemplate.getForObject(getUrl(url), responseType, uriVariables);
	}

	/**
	 * GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return T 响应对象封装类
	 */
	public <T> T get(String url, Class<T> responseType, Map<String, ?> uriVariables) {
		return restTemplate.getForObject(getUrl(url), responseType, uriVariables);
	}


	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return T 响应对象封装类
	 */
	public <T> T get(String url, Map<String, String> headers, Class<T> responseType, Object... uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return get(url, httpHeaders, responseType, uriVariables);
	}

	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T get(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) {
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		return exchange(url, HttpMethod.GET, requestEntity, responseType, uriVariables);
	}

	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T get(String url, Map<String, String> headers, Class<T> responseType, Map<String, ?> uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return get(url, httpHeaders, responseType, uriVariables);
	}

	/**
	 * 带请求头的GET请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T get(String url, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables) {
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		return exchange(url, HttpMethod.GET, requestEntity, responseType, uriVariables);
	}

	// ----------------------------------POST-------------------------------------------------------

	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, Object requestBody) {
		return post(url, requestBody, this.getResponseType());
	}

	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, Object requestBody, Object... uriVariables) {
		return post(url, requestBody, this.getResponseType(), uriVariables);
	}


	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, Object requestBody, Map<String, ?> uriVariables) {
		return post(url, requestBody, this.getResponseType(), uriVariables);
	}


	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, HttpHeaders headers, Object requestBody, Object... uriVariables) {
		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestBody, headers);
		return post(url, requestEntity, this.getResponseType(), uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, Map<String, String> headers, Object requestBody, Map<String, ?> uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return post(url, httpHeaders, requestBody, this.getResponseType(), uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, HttpHeaders headers, Object requestBody, Map<String, ?> uriVariables) {
		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestBody, headers);
		return post(url, requestEntity, this.getResponseType(), uriVariables);
	}

	/**
	 * 自定义请求头和请求体的POST请求调用方式
	 *
	 * @param url           请求URL
	 * @param requestEntity 请求头和请求体封装对象
	 * @param uriVariables  URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, HttpEntity<?> requestEntity, Object... uriVariables) {
		return post(url, HttpMethod.POST, requestEntity, this.getResponseType(), uriVariables);
	}

	/**
	 * 自定义请求头和请求体的POST请求调用方式
	 *
	 * @param url           请求URL
	 * @param requestEntity 请求头和请求体封装对象
	 * @param uriVariables  URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public T post(String url, HttpEntity<?> requestEntity, Map<String, ?> uriVariables) {
		return post(url, HttpMethod.POST, requestEntity, this.getResponseType(), uriVariables);
	}

	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, Object requestBody, Class<T> responseType) {
		return restTemplate.postForObject(getUrl(url), requestBody, responseType);
	}

	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
		return restTemplate.postForObject(getUrl(url), requestBody, responseType, uriVariables);
	}

	/**
	 * POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
		return restTemplate.postForObject(getUrl(url), requestBody, responseType, uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return post(url, httpHeaders, requestBody, responseType, uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestBody, headers);
		return post(url, requestEntity, responseType, uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAll(headers);
		return post(url, httpHeaders, requestBody, responseType, uriVariables);
	}

	/**
	 * 带请求头的POST请求调用方式
	 *
	 * @param url          请求URL
	 * @param headers      请求头参数
	 * @param requestBody  请求参数体
	 * @param responseType 返回对象类型
	 * @param uriVariables URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestBody, headers);
		return post(url, requestEntity, responseType, uriVariables);
	}

	/**
	 * 自定义请求头和请求体的POST请求调用方式
	 *
	 * @param url           请求URL
	 * @param requestEntity 请求头和请求体封装对象
	 * @param responseType  返回对象类型
	 * @param uriVariables  URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
		return restTemplate.exchange(getUrl(url), HttpMethod.POST, requestEntity, responseType, uriVariables).getBody();
	}

	/**
	 * 自定义请求头和请求体的POST请求调用方式
	 *
	 * @param url           请求URL
	 * @param requestEntity 请求头和请求体封装对象
	 * @param responseType  返回对象类型
	 * @param uriVariables  URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T post(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
		return restTemplate.exchange(getUrl(url), HttpMethod.POST, requestEntity, responseType, uriVariables).getBody();
	}

	// ----------------------------------通用方法-------------------------------------------------------

	/**
	 * 通用调用方式
	 *
	 * @param url           请求URL
	 * @param method        请求方法类型
	 * @param requestEntity 请求头和请求体封装对象
	 * @param responseType  返回对象类型
	 * @param uriVariables  URL中的变量，按顺序依次对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
		return restTemplate.exchange(getUrl(url), method, requestEntity, responseType, uriVariables).getBody();
	}

	/**
	 * 通用调用方式
	 *
	 * @param url           请求URL
	 * @param method        请求方法类型
	 * @param requestEntity 请求头和请求体封装对象
	 * @param responseType  返回对象类型
	 * @param uriVariables  URL中的变量，与Map中的key对应
	 * @return ResponseEntity 响应对象封装类
	 */
	public <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
		return restTemplate.exchange(getUrl(url), method, requestEntity, responseType, uriVariables).getBody();
	}

	public void setPrefixUrl(String prefixUrl) {
		this.prefixUrl = prefixUrl;
	}

	public <T> void setResponseType(Class responseType) {
		this.responseType = responseType;
	}

	public Class<T> getResponseType() {
		return responseType;
	}
}
