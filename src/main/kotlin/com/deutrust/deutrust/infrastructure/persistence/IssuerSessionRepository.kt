package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.IssuerSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IssuerSessionRepository : JpaRepository<IssuerSession, UUID> {
    fun findByPreAuthorizedCode(preAuthorizedCode: String): IssuerSession?
    fun findByAccessToken(accessToken: String): IssuerSession?
}
