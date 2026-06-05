package com.deutrust.deutrust.domain.service

import com.deutrust.deutrust.application.controller.VerifierController.DocumentRequest
import com.deutrust.deutrust.domain.model.EudiSession
import java.util.UUID

interface VerifierService {
    fun createSession(
        businessId: String,
        requestedClaims: List<String>,
        optionalClaims: List<String> = emptyList(),
        documents: List<DocumentRequest> = emptyList()
    ): EudiSession
    fun getSession(id: UUID): EudiSession?
    fun getRequestObject(id: UUID, hostUrl: String): String
    fun handleDirectPost(id: UUID, vpToken: String, state: String): EudiSession
}
