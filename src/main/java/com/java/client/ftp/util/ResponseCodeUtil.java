package com.java.client.ftp.util;

import com.java.client.ftp.enums.ResponseCode;

public class ResponseCodeUtil {
    public static ResponseCode getResponseCode(String response) {
        try {
            int code = Integer.parseInt(response.split(" ")[0]);
            return ResponseCode.fromCode(code);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid response format: " + response, e);
        }
    }
}
