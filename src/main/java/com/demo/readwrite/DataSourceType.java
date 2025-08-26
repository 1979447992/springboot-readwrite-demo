package com.demo.readwrite;

public enum DataSourceType {
    MASTER("master"),
    SLAVE("slave");
    
    private String value;
    
    DataSourceType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}