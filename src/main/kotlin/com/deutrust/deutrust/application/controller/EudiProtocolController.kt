package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.domain.service.VerifierService
import com.deutrust.deutrust.infrastructure.eudi.EudiClient
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@CrossOrigin
@RestController
@RequestMapping("/api/v1")
class EudiProtocolController(
    private val verifierService: VerifierService,
    private val eudiClient: EudiClient,
    @Value("\${eudi.public-url:}") private val publicUrl: String
) {

    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

    // ==========================================
    // 1. OPENID4VP (Presentation / Verifier Flow)
    // ==========================================

    /**
     * Resolves the signed request object (JWS) for the EUDI Wallet scanning a verification QR code.
     * Returned with standard OID4VP content type 'application/oauth-authz-req+jwt'.
     */
    @GetMapping(
        value = ["/openid4vp/requests/{transactionId}"],
        produces = ["application/oauth-authz-req+jwt"]
    )
    fun getRequestObject(
        @PathVariable transactionId: UUID,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val hostUrl = getBaseUrl(request)
        return try {
            val signedJwt = verifierService.getRequestObject(transactionId, hostUrl)
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/oauth-authz-req+jwt"))
                .body(signedJwt)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Standard HTTPS QR Bridge page. This renders a webpage that native smartphone cameras
     * scan. When opened, it instantly triggers a client-side redirect using the custom 
     * openid4vp:// protocol scheme, opening the EUDI Wallet natively on the mobile device.
     */
    @GetMapping(
        value = ["/openid4vp/qr-bridge/{transactionId}"],
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun qrBridge(
        @PathVariable transactionId: UUID,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val hostUrl = getBaseUrl(request)
        verifierService.getSession(transactionId) ?: return ResponseEntity.notFound().build()
        val clientId = eudiClient.getClientId(hostUrl)
        val requestUri = "$hostUrl/api/v1/openid4vp/requests/$transactionId"
        val encodedClientId = java.net.URLEncoder.encode(clientId, java.nio.charset.StandardCharsets.UTF_8.toString())
        val encodedRequestUri = java.net.URLEncoder.encode(requestUri, java.nio.charset.StandardCharsets.UTF_8.toString())
        val deepLink = "openid4vp://?client_id=$encodedClientId&request_uri=$encodedRequestUri"

        val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>EUDI Wallet Bridge</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #f5f5f7 0%, #e8e8ed 100%);
                        color: #1d1d1f;
                    }
                    .card {
                        text-align: center;
                        padding: 40px;
                        background: rgba(255, 255, 255, 0.9);
                        backdrop-filter: blur(20px);
                        border-radius: 24px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.06);
                        max-width: 90%;
                        width: 380px;
                    }
                    h2 {
                        margin-top: 0;
                        font-size: 20px;
                        font-weight: 600;
                    }
                    p {
                        color: #86868b;
                        font-size: 14px;
                        margin-bottom: 30px;
                    }
                    .btn {
                        display: inline-block;
                        width: 100%;
                        box-sizing: border-box;
                        padding: 14px 24px;
                        background-color: #0071e3;
                        color: white;
                        text-decoration: none;
                        border-radius: 980px;
                        font-weight: 600;
                        font-size: 16px;
                        transition: background-color 0.2s ease;
                    }
                    .btn:hover {
                        background-color: #0077ed;
                    }
                    .loader {
                        border: 3px solid #f3f3f3;
                        border-top: 3px solid #0071e3;
                        border-radius: 50%;
                        width: 24px;
                        height: 24px;
                        animation: spin 1s linear infinite;
                        margin: 20px auto;
                    }
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                </style>
                <script>
                    window.onload = function() {
                        setTimeout(function() {
                            window.location.href = "$deepLink";
                        }, 800);
                    };
                </script>
            </head>
            <body>
                <div class="card">
                    <div class="loader"></div>
                    <h2>Redirecting to EUDI Wallet</h2>
                    <p>We are opening the EUDI Digital Identity Wallet app on your device to share requested details securely.</p>
                    <a class="btn" href="$deepLink">Open EUDI Wallet</a>
                </div>
            </body>
            </html>
        """.trimIndent()

        return ResponseEntity.ok(html)
    }

    /**
     * EUDI Wallet direct posts the Verifiable Presentation (VP) to this endpoint.
     * Supports both form-url-encoded and JSON representations of OID4VP direct_post.
     */
    @PostMapping(
        "/openid4vp/response/{transactionId}",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun receiveDirectPostForm(
        @PathVariable transactionId: UUID,
        @RequestParam("vp_token", required = false) vpToken: String?,
        @RequestParam("state", required = false) state: String?,
        @RequestParam("response", required = false) responseJwt: String?
    ): ResponseEntity<Map<String, String>> {
        var finalVpToken = extractVpTokenString(vpToken)
        var finalState = state

        if (responseJwt != null) {
            try {
                val parts = responseJwt.split(".")
                val clearTextJws = if (parts.size == 5) {
                    eudiClient.decryptJwe(responseJwt)
                } else {
                    responseJwt
                }

                val trimmedJws = clearTextJws.trim()
                val jarmPayload = if (trimmedJws.startsWith("{")) {
                    // Plain JSON response payload (not wrapped in a JWS)
                    objectMapper.readValue(trimmedJws, Map::class.java)
                } else {
                    // Signed JWS payload
                    val jwsParts = trimmedJws.split(".")
                    if (jwsParts.size >= 2) {
                        val decodedBytes = java.util.Base64.getUrlDecoder().decode(jwsParts[1])
                        val payloadJson = String(decodedBytes)
                        objectMapper.readValue(payloadJson, Map::class.java)
                    } else {
                        null
                    }
                }

                if (jarmPayload != null) {
                    finalVpToken = extractVpTokenString(jarmPayload["vp_token"])
                    finalState = jarmPayload["state"]?.toString()
                }
            } catch (e: Exception) {
                println("❌ JARM processing error in receiveDirectPostForm!")
                e.printStackTrace()
            }
        }

        if (finalVpToken == null || finalState == null) {
            println("❌ Validation failed: finalVpToken is null or finalState is null")
            return ResponseEntity.badRequest().build()
        }

        verifierService.handleDirectPost(transactionId, finalVpToken, finalState)
        return ResponseEntity.ok(mapOf("status" to "accepted"))
    }

    @PostMapping(
        "/openid4vp/response/{transactionId}",
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun receiveDirectPostJson(
        @PathVariable transactionId: UUID,
        @RequestBody payload: Map<String, Any>
    ): ResponseEntity<Map<String, String>> {
        var vpToken = extractVpTokenString(payload["vp_token"])
        var state = payload["state"]?.toString()
        val responseJwt = payload["response"]?.toString()

        if (responseJwt != null) {
            try {
                val parts = responseJwt.split(".")
                val clearTextJws = if (parts.size == 5) {
                    eudiClient.decryptJwe(responseJwt)
                } else {
                    responseJwt
                }

                val trimmedJws = clearTextJws.trim()
                val jarmPayload = if (trimmedJws.startsWith("{")) {
                    // Plain JSON response payload (not wrapped in a JWS)
                    objectMapper.readValue(trimmedJws, Map::class.java)
                } else {
                    // Signed JWS payload
                    val jwsParts = trimmedJws.split(".")
                    if (jwsParts.size >= 2) {
                        val decodedBytes = java.util.Base64.getUrlDecoder().decode(jwsParts[1])
                        val payloadJson = String(decodedBytes)
                        objectMapper.readValue(payloadJson, Map::class.java)
                    } else {
                        null
                    }
                }

                if (jarmPayload != null) {
                    vpToken = extractVpTokenString(jarmPayload["vp_token"])
                    state = jarmPayload["state"]?.toString()
                }
            } catch (e: Exception) {
                println("❌ JARM processing error in receiveDirectPostJson!")
                e.printStackTrace()
            }
        }

        if (vpToken == null || state == null) {
            println("❌ Validation failed: vpToken is null or state is null")
            return ResponseEntity.badRequest().build()
        }

        verifierService.handleDirectPost(transactionId, vpToken, state)
        return ResponseEntity.ok(mapOf("status" to "accepted"))
    }

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

    private fun extractVpTokenString(vpTokenObj: Any?): String? {
        if (vpTokenObj == null) return null
        if (vpTokenObj is String) {
            val trimmed = vpTokenObj.trim()
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                try {
                    val parsed = objectMapper.readValue(trimmed, Any::class.java)
                    return extractVpTokenString(parsed)
                } catch (e: Exception) {
                    // fall through and treat as raw string if JSON parsing fails
                }
            }
            return trimmed
        }
        if (vpTokenObj is Map<*, *>) {
            val keysToTry = listOf("verifiableCredential", "verifiable_credential", "vp_token", "vp", "credential")
            for (key in keysToTry) {
                val match = vpTokenObj[key] ?: vpTokenObj[key.lowercase()]
                if (match != null) {
                    val nested = extractVpTokenString(match)
                    if (nested != null && nested.contains(".")) return nested
                }
            }
            for (value in vpTokenObj.values) {
                if (value is String) {
                    val nested = extractVpTokenString(value)
                    if (nested != null && nested.contains(".")) return nested
                }
                if (value is List<*>) {
                    val first = value.firstOrNull()
                    if (first != null) {
                        val nested = extractVpTokenString(first)
                        if (nested != null && nested.contains(".")) return nested
                    }
                }
                if (value is Map<*, *>) {
                    val nested = extractVpTokenString(value)
                    if (nested != null && nested.contains(".")) return nested
                }
            }
        }
        if (vpTokenObj is List<*>) {
            val first = vpTokenObj.firstOrNull()
            if (first != null) return extractVpTokenString(first)
        }
        return vpTokenObj.toString()
    }
}
