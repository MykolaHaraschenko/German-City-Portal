package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.domain.service.IssuerService
import com.deutrust.deutrust.infrastructure.persistence.IssuerSessionRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@CrossOrigin
@RestController
@RequestMapping("/api/v1/issuer")
class IssuerController(
    private val issuerService: IssuerService,
    private val sessionRepository: IssuerSessionRepository,
    @Value("\${eudi.public-url:}") private val publicUrl: String
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

    data class CreateOfferRequest(
        val credentialType: String = "urn:credential:proof_of_residence_registration",
        val claims: Map<String, Any>
    )

    data class OfferResponse(
        val sessionId: UUID,
        val offerUri: String
    )

    // 1. Create Offer Endpoint (Internal/Frontend usage)
    @PostMapping("/offers")
    fun createOffer(
        @RequestBody request: CreateOfferRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<OfferResponse> {
        val session = issuerService.createOffer(request.credentialType, request.claims)
        val hostUrl = getBaseUrl(servletRequest)

        // Instead of passing the entire JSON in the URI (which might fail validation or be too large for the wallet),
        // we use credential_offer_uri to let the wallet download the JSON.
        val offerEndpoint = "$hostUrl/api/v1/issuer/offers/${session.id}"
        val encodedOfferUri = java.net.URLEncoder.encode(offerEndpoint, "UTF-8")

        val offerUri = "openid-credential-offer://?credential_offer_uri=$encodedOfferUri"

        return ResponseEntity.ok(OfferResponse(session.id, offerUri))
    }

    // 1.5. GET Offer JSON Endpoint (Wallet usage)
    @GetMapping("/offers/{sessionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOfferJson(
        @PathVariable sessionId: UUID,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        val session = sessionRepository.findById(sessionId).orElse(null)
            ?: return ResponseEntity.notFound().build()
            
        val hostUrl = getBaseUrl(servletRequest)

        val credentialOffer = mapOf(
            "credential_issuer" to hostUrl,
            "credential_configuration_ids" to listOf(session.credentialType),
            "credentials" to listOf(session.credentialType), // For draft 11 compatibility
            "grants" to mapOf(
                "urn:ietf:params:oauth:grant-type:pre-authorized_code" to mapOf(
                    "pre-authorized_code" to session.preAuthorizedCode,
                    "user_pin_required" to false
                )
            )
        )

        return ResponseEntity.ok(credentialOffer)
    }

    // 2. Token Endpoint (Wallet usage)
    @PostMapping(
        "/token",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun tokenEndpoint(
        @RequestParam("grant_type", required = false) grantType: String?,
        @RequestParam("pre-authorized_code", required = false) preAuthCode: String?
    ): ResponseEntity<Any> {
        if (grantType != "urn:ietf:params:oauth:grant-type:pre-authorized_code" || preAuthCode.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "invalid_request"))
        }

        return try {
            val accessToken = issuerService.exchangeToken(preAuthCode)
            val response = mapOf(
                "access_token" to accessToken,
                "token_type" to "bearer",
                "expires_in" to 3600, // 1 hour
                "c_nonce" to UUID.randomUUID().toString(),
                "c_nonce_expires_in" to 86400
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "invalid_grant", "error_description" to e.message))
        }
    }

    // 3. Credential Endpoint (Wallet usage)
    @PostMapping(
        "/credential",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun credentialEndpoint(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody payload: Map<String, Any>,
        servletRequest: HttpServletRequest
    ): ResponseEntity<Any> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val accessToken = authHeader.removePrefix("Bearer ").trim()

        return try {
            val hostUrl = getBaseUrl(servletRequest)
            
            // Extract JWK from proofs
            var jwk: Map<String, Any>? = null
            val proofs = payload["proofs"] as? Map<*, *>
            val jwtList = proofs?.get("jwt") as? List<*>
            val jwtString = jwtList?.firstOrNull() as? String
            
            if (jwtString != null) {
                // JWT format is Header.Payload.Signature
                val parts = jwtString.split(".")
                if (parts.size == 3) {
                    val headerJson = String(java.util.Base64.getUrlDecoder().decode(parts[0]))
                    val headerMap = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().readValue(headerJson, Map::class.java)
                    @Suppress("UNCHECKED_CAST")
                    jwk = headerMap["jwk"] as? Map<String, Any>
                }
            }

            // Determine format based on requested configuration ID (fallback to dc+sd-jwt)
            val reqConfigId = payload["credential_configuration_id"] as? String
            val formatType = if (reqConfigId != null && reqConfigId.endsWith("_vc")) "vc+sd-jwt" else "dc+sd-jwt"

            // Generate the actual SD-JWT credential using our crypto service
            val sdJwt = issuerService.issueCredential(accessToken, hostUrl, jwk, formatType)

            val response = mapOf(
                "credentials" to listOf(
                    mapOf("credential" to sdJwt)
                ),
                "c_nonce" to java.util.UUID.randomUUID().toString(),
                "c_nonce_expires_in" to 86400
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "invalid_request", "error_description" to e.message))
        }
    }
}
