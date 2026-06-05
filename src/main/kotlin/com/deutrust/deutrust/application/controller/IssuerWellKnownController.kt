package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.infrastructure.eudi.DocumentTypeRegistry
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class IssuerWellKnownController(
    @Value("\${eudi.public-url:}") private val publicUrl: String,
    private val documentTypeRegistry: DocumentTypeRegistry
) {

    private fun getBaseUrl(request: HttpServletRequest): String {
        if (!publicUrl.isNullOrBlank()) {
            return publicUrl.trim().removeSuffix("/")
        }
        val scheme = request.scheme
        val serverName = request.serverName
        val serverPort = request.serverPort
        return if (serverPort == 80 || serverPort == 443) {
            "$scheme://$serverName"
        } else {
            "$scheme://$serverName:$serverPort"
        }
    }

    @GetMapping("/.well-known/openid-credential-issuer")
    fun getCredentialIssuerMetadata(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val hostUrl = getBaseUrl(request)

        // Auto-generated from DocumentTypeRegistry (reads from application.yml)
        val credentialConfigurations = documentTypeRegistry.toCredentialConfigurations()

        val metadata = mapOf(
            "credential_issuer" to hostUrl,
            "credential_endpoint" to "$hostUrl/api/v1/issuer/credential",
            "batch_credential_endpoint" to "$hostUrl/api/v1/issuer/batch_credential",
            "authorization_servers" to listOf(hostUrl),
            "authorization_server" to hostUrl,
            "jwks" to mapOf(
                "keys" to listOf(
                    mapOf(
                        "kty" to "EC",
                        "crv" to "P-256",
                        "x" to "tNeo02Cv_BlKk6OY4MP8gdbIgsKHU_fYULP0Wzyr0cY",
                        "y" to "TwMTUBxK0cqPPiM4fFQ-5viuj_jXLNi1dG4Xmg6KVfU",
                        "use" to "sig",
                        "alg" to "ES256",
                        "kid" to "test-key-1"
                    )
                )
            ),
            "credential_configurations_supported" to credentialConfigurations
        )

        return ResponseEntity.ok(metadata)
    }

    @GetMapping("/.well-known/oauth-authorization-server")
    fun getAuthorizationServerMetadata(request: HttpServletRequest): ResponseEntity<Map<String, Any>> {
        val hostUrl = getBaseUrl(request)

        val metadata = mapOf(
            "issuer" to hostUrl,
            "token_endpoint" to "$hostUrl/api/v1/issuer/token",
            "grant_types_supported" to listOf(
                "urn:ietf:params:oauth:grant-type:pre-authorized_code"
            ),
            "response_types_supported" to listOf("vp_token"),
            "token_endpoint_auth_methods_supported" to listOf("none"),
            "dpop_signing_alg_values_supported" to listOf("ES256")
        )

        return ResponseEntity.ok(metadata)
    }
}
