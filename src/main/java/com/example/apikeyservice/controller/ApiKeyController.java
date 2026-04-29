package com.example.apikeyservice.controller;

import com.example.apikeyservice.dto.ApiKeyResponseDto;
import com.example.apikeyservice.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apikeys")
@Tag(name = "API Keys", description = "Generación, validación y renovación de API Keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/generate")
    @Operation(
        summary = "Generar o recuperar API Key activo",
        description = "Retorna el API Key activo del cliente. Si no existe o expiró, genera uno nuevo automáticamente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "API Key retornado o creado",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Client ID inválido o ausente",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class)))
    })
    public ResponseEntity<ApiKeyResponseDto> generate(
            @Parameter(description = "Identificador del cliente", example = "CLIENT-001")
            @RequestHeader(value = "X-Client-Id", required = false) String clientId) {
        return ResponseEntity.ok(apiKeyService.createOrGetApiKey(clientId));
    }

    @PostMapping("/renew")
    @Operation(
        summary = "Renovar API Key manualmente",
        description = "Fuerza la renovación del API Key activo. Sujeto a cooldown configurable (default: 24h)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "API Key renovado exitosamente",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Client ID o Renew ID inválido",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class))),
        @ApiResponse(responseCode = "429", description = "Cooldown activo — no se puede renovar aún",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class)))
    })
    public ResponseEntity<ApiKeyResponseDto> renewApiKey(
            @Parameter(description = "Identificador del cliente", example = "CLIENT-001")
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            @Parameter(description = "Clave secreta de renovación", example = "RENEW-SECRET-KEY-001")
            @RequestHeader(value = "X-Renewed-ApiKey-Id", required = false) String renewedApiKeyId) {
        return ResponseEntity.ok(apiKeyService.forceRenewApiKey(renewedApiKeyId, clientId));
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Validar API Key",
        description = "Verifica que el API Key proporcionado esté activo y no haya expirado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "API Key válido",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "API Key inválido, expirado o no encontrado",
            content = @Content(schema = @Schema(implementation = ApiKeyResponseDto.class)))
    })
    public ResponseEntity<ApiKeyResponseDto> validateApiKey(
            @Parameter(description = "Identificador del cliente", example = "CLIENT-001")
            @RequestHeader(value = "X-Client-Id", required = false) String clientId,
            @Parameter(description = "API Key a validar")
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey) {
        return ResponseEntity.ok(apiKeyService.validateApiKey(clientId, apiKey));
    }
}
