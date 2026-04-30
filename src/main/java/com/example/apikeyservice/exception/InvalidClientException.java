package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;

public class InvalidClientException extends ApiKeyValidationException {

    public InvalidClientException() {
        super(ApiKeyConstants.MSG_INVALID_CLIENT, ApiKeyConstants.CODE_INVALID_CLIENT);
    }
}
