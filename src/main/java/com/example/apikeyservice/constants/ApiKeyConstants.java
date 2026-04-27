package com.example.apikeyservice.constants;

public final class ApiKeyConstants {

    private ApiKeyConstants() {}

    // Operation codes
    public static final String CODE_CREATED              = "APIKEY_CREATED";
    public static final String CODE_ALREADY_ACTIVE       = "APIKEY_ALREADY_ACTIVE";
    public static final String CODE_EXPIRED_RENEWED      = "APIKEY_EXPIRED_RENEWED";
    public static final String CODE_INVALID_CLIENT       = "INVALID_CLIENT_ID";
    public static final String CODE_MANUAL_RENEWED       = "APIKEY_MANUAL_RENEWED";
    public static final String CODE_COOLDOWN_ACTIVE      = "RENEW_COOLDOWN_ACTIVE";
    public static final String CODE_MISSING_PARAMS       = "MISSING_PARAMETERS";
    public static final String CODE_INVALID_RENEWED_ID   = "INVALID_RENEWED_APIKEY_ID";
    public static final String CODE_VALID                = "APIKEY_VALID";
    public static final String CODE_NOT_FOUND            = "APIKEY_NOT_FOUND";
    public static final String CODE_EXPIRED              = "APIKEY_EXPIRED";
    public static final String CODE_INVALID_KEY          = "APIKEY_INVALID";

    // Messages
    public static final String MSG_CREATED               = "Nuevo ApiKey generado exitosamente";
    public static final String MSG_ALREADY_ACTIVE        = "Ya tienes un ApiKey activo";
    public static final String MSG_EXPIRED_RENEWED       = "ApiKey expirado. Nuevo ApiKey generado correctamente";
    public static final String MSG_INVALID_CLIENT        = "ClientID invalido";
    public static final String MSG_MANUAL_RENEWED        = "ApiKey renovado correctamente";
    public static final String MSG_COOLDOWN_ACTIVE       = "No puede renovar todavia. Espere a que finalice el periodo de cooldown";
    public static final String MSG_MISSING_PARAMS        = "Debe proporcionar el/los headers requeridos";
    public static final String MSG_INVALID_RENEWED_ID    = "RenewedApikeyID invalido";
    public static final String MSG_VALID                 = "El ApiKey es valido";
    public static final String MSG_NOT_FOUND             = "No se encontro un ApiKey activo para este cliente";
    public static final String MSG_EXPIRED               = "El ApiKey ha expirado";
    public static final String MSG_INVALID_KEY           = "El ApiKey no es valido";
}
