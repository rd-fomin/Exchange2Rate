package com.exchange.model;

public class UserValue {
    private String charCode;
    private String value;

    public UserValue() {}

    public String getCharCode() {
        return charCode;
    }

    public UserValue setCharCode(String charCode) {
        this.charCode = charCode;
        return this;
    }

    public String getValue() {
        return value;
    }

    public UserValue setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "UserValue{" +
                "charCode='" + charCode + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

}
