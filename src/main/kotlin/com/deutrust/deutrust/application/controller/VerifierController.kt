package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.domain.model.EudiSession
import com.deutrust.deutrust.domain.service.VerifierService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@CrossOrigin
@RestController
@RequestMapping("/api/v1/sessions")
class VerifierController(
    private val verifierService: VerifierService,
    @Value("\${eudi.public-url:}") private val publicUrl: String
) {

    data class DocumentRequest(
        val vct: String,
        val format: String = "dc+sd-jwt",
        val requestedClaims: List<String>,
        val optionalClaims: List<String> = emptyList(),
        val required: Boolean = true
    )

    data class CreateSessionRequest(
        val businessId: String,
        val requestedClaims: List<String>? = emptyList(),
        val optionalClaims: List<String>? = emptyList(),
        val documents: List<DocumentRequest>? = emptyList()
    )

    data class SessionResponse(
        val transactionId: UUID,
        val businessId: String,
        val status: String,
        val qrCodePayload: String, // The deep link for the EUDI Wallet
        val claims: String?
    )

    @PostMapping
    fun createSession(
        @RequestBody request: CreateSessionRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<SessionResponse> {
        val session = verifierService.createSession(
            request.businessId,
            request.requestedClaims ?: emptyList(),
            request.optionalClaims ?: emptyList(),
            request.documents ?: emptyList()
        )
        
        // Form the EUDI compliant OpenID4VP HTTPS QR Bridge Link
        val hostUrl = getBaseUrl(servletRequest)
        val bridgeLink = "$hostUrl/api/v1/openid4vp/qr-bridge/${session.id}"

        return ResponseEntity.ok(
            SessionResponse(
                transactionId = session.id,
                businessId = session.businessId,
                status = session.status,
                qrCodePayload = bridgeLink,
                claims = session.presentationPayload
            )
        )
    }

    @GetMapping("/{id}/status")
    fun getSessionStatus(
        @PathVariable id: UUID,
        servletRequest: HttpServletRequest
    ): ResponseEntity<SessionResponse> {
        val session = verifierService.getSession(id) ?: return ResponseEntity.notFound().build()
        val hostUrl = getBaseUrl(servletRequest)
        val bridgeLink = "$hostUrl/api/v1/openid4vp/qr-bridge/${session.id}"

        return ResponseEntity.ok(
            SessionResponse(
                transactionId = session.id,
                businessId = session.businessId,
                status = session.status,
                qrCodePayload = bridgeLink,
                claims = session.presentationPayload
            )
        )
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
}
