package com.website.service.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BaseApiResponse implements Serializable {

    private static final long serialVersionUID = -2487074053808281342L;

    private Integer code;

    private String message;

    private List<Object> data;

    private int total;

    private Map<String, Object> map;

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
