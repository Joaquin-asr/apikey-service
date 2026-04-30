package com.example.apikeyservice.exception;

import com.example.apikeyservice.constants.ApiKeyConstants;

public class CooldownActiveException extends ApiKeyValidationException {

    public CooldownActiveException() {
        super(ApiKeyConstants.MSG_COOLDOWN_ACTIVE, ApiKeyConstants.CODE_COOLDOWN_ACTIVE);
    }
}
