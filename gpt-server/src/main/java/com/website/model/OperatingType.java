package com.website.model;

public enum OperatingType {
    /**
     * 操作类型
     */
    API("0", "接口统计"),
    LOGIN("1", "登陆");

    private String operatingType;
    private String description;

    OperatingType(String operatingType, String description) {
        this.operatingType = operatingType;
        this.description = description;
    }

    public String getOperatingType() {
        return operatingType;
    }

    public String getDescription() {
        return description;
    }
}
