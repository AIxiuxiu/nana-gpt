package com.website.service.http;

public enum ResultCodeEnum {

    SUCCESS_CODE(0, "请求成功!"), ERROR_CODE_DEFAULT(100, "请求失败");

    private Integer type;

    private String message;

    private ResultCodeEnum(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
