package com.deutrust.deutrust.infrastructure.eudi

import com.deutrust.deutrust.domain.model.IssuerSession
import com.deutrust.deutrust.domain.service.IssuerService
import com.deutrust.deutrust.infrastructure.persistence.IssuerSessionRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

@Service
class IssuerServiceImpl(
    private val sessionRepository: IssuerSessionRepository,
    private val cryptoService: IssuerCryptoService,
    private val objectMapper: ObjectMapper
) : IssuerService {

    private val secureRandom = SecureRandom()

    private fun generateRandomToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @Transactional
    override fun createOffer(credentialType: String, credentialData: Map<String, Any>): IssuerSession {
        val preAuthCode = generateRandomToken()
        val session = IssuerSession(
            preAuthorizedCode = preAuthCode,
            credentialType = credentialType,
            credentialData = objectMapper.writeValueAsString(credentialData)
        )
        return sessionRepository.save(session)
    }

    @Transactional
    override fun exchangeToken(preAuthorizedCode: String): String {
        val session = sessionRepository.findByPreAuthorizedCode(preAuthorizedCode)
            ?: throw IllegalArgumentException("Invalid or expired pre-authorized code")

        if (session.status != "OFFER_CREATED") {
            throw IllegalStateException("Session is not in OFFER_CREATED state")
        }

        val accessToken = generateRandomToken()
        session.accessToken = accessToken
        session.status = "TOKEN_ISSUED"
        sessionRepository.save(session)

        return accessToken
    }

    @Transactional
    override fun issueCredential(accessToken: String, issuerUri: String, jwk: Map<String, Any>?, formatType: String): String {
        val session = sessionRepository.findByAccessToken(accessToken)
            ?: throw IllegalArgumentException("Invalid access token")

        if (session.status != "TOKEN_ISSUED") {
            throw IllegalStateException("Session is not in TOKEN_ISSUED state")
        }

        val credentialDataMap: Map<String, Any> = objectMapper.readValue(
            session.credentialData,
            object : TypeReference<Map<String, Any>>() {}
        )

        // Generate SD-JWT
        val sdJwt = cryptoService.signSdJwtCredential(
            credentialType = session.credentialType,
            credentialData = credentialDataMap,
            issuerUri = issuerUri,
            subject = UUID.randomUUID().toString(), // In a real system, you might link to an actual user ID
            jwk = jwk,
            formatType = formatType
        )

        session.status = "CREDENTIAL_ISSUED"
        sessionRepository.save(session)

        return sdJwt
    }
}
