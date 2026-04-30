package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;

public class ApiKeyExpiredException extends ApiKeyValidationException {

    public ApiKeyExpiredException() {
        super(ApiKeyConstants.MSG_EXPIRED, ApiKeyConstants.CODE_EXPIRED);
    }
}
