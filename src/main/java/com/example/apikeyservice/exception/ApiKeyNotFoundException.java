package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;

public class ApiKeyNotFoundException extends ApiKeyValidationException {

    public ApiKeyNotFoundException() {
        super(ApiKeyConstants.MSG_NOT_FOUND, ApiKeyConstants.CODE_NOT_FOUND);
    }
}
