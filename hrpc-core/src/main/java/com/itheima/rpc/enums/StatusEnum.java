package com.itheima.rpc.enums;

import lombok.Getter;

@Getter
public enum StatusEnum {

    SUCCESS(200, "OK"),

    NOT_FOUND_SERVICE_PROVINDER(100001, "not found service provider");


    private Integer code;
    private String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
