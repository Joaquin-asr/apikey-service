package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;

public class InvalidApiKeyException extends ApiKeyValidationException {

    public InvalidApiKeyException() {
        super(ApiKeyConstants.MSG_INVALID_KEY, ApiKeyConstants.CODE_INVALID_KEY);
    }
}
