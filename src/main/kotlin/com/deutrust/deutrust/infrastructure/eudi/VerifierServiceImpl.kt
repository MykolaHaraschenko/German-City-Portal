package com.deutrust.deutrust.infrastructure.eudi

import com.deutrust.deutrust.application.controller.VerifierController.DocumentRequest
import com.deutrust.deutrust.domain.model.EudiSession
import com.deutrust.deutrust.domain.service.VerifierService
import com.deutrust.deutrust.infrastructure.persistence.EudiSessionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class VerifierServiceImpl(
    private val eudiSessionRepository: EudiSessionRepository,
    private val eudiClient: EudiClient,
    private val objectMapper: ObjectMapper
) : VerifierService {

    @Transactional
    override fun createSession(
        businessId: String,
        requestedClaims: List<String>,
        optionalClaims: List<String>,
        documents: List<DocumentRequest>
    ): EudiSession {
        val nonce = UUID.randomUUID().toString()
        val state = UUID.randomUUID().toString()
        val claimsJson = objectMapper.writeValueAsString(requestedClaims)
        val optionalJson = objectMapper.writeValueAsString(optionalClaims)
        val documentsJson = if (documents.isNotEmpty()) objectMapper.writeValueAsString(documents) else null
        
        val session = EudiSession(
            businessId = businessId,
            requestedClaims = claimsJson,
            optionalClaims = optionalJson,
            requestedDocuments = documentsJson,
            nonce = nonce,
            state = state
        )
        return eudiSessionRepository.save(session)
    }

    override fun getSession(id: UUID): EudiSession? {
        return eudiSessionRepository.findById(id).orElse(null)
    }

    override fun getRequestObject(id: UUID, hostUrl: String): String {
        val session = getSession(id) ?: throw IllegalArgumentException("Session not found")
        if (session.businessId == "citizen-portal-mainpage") {
            return eudiClient.signAuthRequestObjectJwt(
                transactionId = session.id,
                redirectUri = hostUrl,
                nonce = session.nonce,
                state = session.state
            )
        }
        val claimsList = try {
            objectMapper.readValue(session.requestedClaims, List::class.java) as List<String>
        } catch (e: Exception) {
            listOf("given_name", "family_name", "date_of_birth")
        }
        val optionalList = try {
            if (session.optionalClaims != null) {
                objectMapper.readValue(session.optionalClaims, List::class.java) as List<String>
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val documentsList = try {
            if (!session.requestedDocuments.isNullOrBlank()) {
                val listType = objectMapper.typeFactory.constructCollectionType(List::class.java, DocumentRequest::class.java)
                objectMapper.readValue<List<DocumentRequest>>(session.requestedDocuments, listType)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return eudiClient.signRequestObjectJwt(
            transactionId = session.id,
            redirectUri = hostUrl,
            nonce = session.nonce,
            state = session.state,
            requestedClaims = claimsList,
            optionalClaims = optionalList,
            documents = documentsList
        )
    }

    @Transactional
    override fun handleDirectPost(id: UUID, vpToken: String, state: String): EudiSession {
        val session = getSession(id) ?: throw IllegalArgumentException("Session not found")
        println("=== EUDI handleDirectPost verification ===")
        println("Transaction ID: $id")
        println("Expected State: '${session.state}'")
        println("Received State: '$state'")

        if (session.state != state) {
            println("❌ State verification failed! Expected '${session.state}', but got '$state'")
            session.status = "FAILED"
            session.updatedAt = Instant.now()
            return eudiSessionRepository.save(session)
        }
        println("✅ State verification passed.")

        try {
            println("Verifying vpToken prefix: ${vpToken.take(150)}...")
            val verifiedClaims = eudiClient.verifySdJwt(vpToken)
            println("✅ vpToken verification passed! Claims: $verifiedClaims")
            session.presentationPayload = objectMapper.writeValueAsString(verifiedClaims)
            session.status = "COMPLETED"
        } catch (e: Exception) {
            println("❌ vpToken verification failed!")
            e.printStackTrace()
            session.status = "FAILED"
        }
        
        session.updatedAt = Instant.now()
        return eudiSessionRepository.save(session)
    }
}
