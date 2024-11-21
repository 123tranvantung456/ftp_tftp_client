package com.java.client.ftp.enums;

import lombok.Getter;

@Getter
public enum ResponseCode {
    OK(200),
    SYSTEM_STATUS(211),
    DIRECTORY_STATUS(212),
    FILE_STATUS(213),
    SERVICE_READY(220),
    SERVICE_CLOSING(221),
    USER_LOGGED_IN(230),
    NEED_PASSWORD(331),
    NOT_LOGGED_IN(530),
    SYNTAX_ERROR(500),
    NOT_SUPPORTED(501),
    NOT_IMPLEMENTED(502),
    BAD_SEQUENCE(503),
    SERVICE_NOT_AVAILABLE(421),
    DISCONNECTED(421),
    FILE_CONFLICT(550),
    FILE_ALREADY_EXISTS(553),
    OPERATION_OK(257),
    USER_NOT_FOUND(430),
    CANNOT_CHANGE_DIRECTORY(550),
    CANNOT_OPEN_DATA_CONNECTION(425),
    ACTION_NOT_TAKEN(450),
    ALREADY_EXISTS(553),
    FILE_COMPLETED_TRANSFER(226),
    FILE_STARTING_TRANSFER(150),
    REQUEST_TIMEOUT(522),
    USER_EXIT_ACKNOWLEDGED(229);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public static ResponseCode fromCode(int code) {
        for (ResponseCode response : values()) {
            if (response.getCode() == code) {
                return response;
            }
        }
        return null;
    }
}