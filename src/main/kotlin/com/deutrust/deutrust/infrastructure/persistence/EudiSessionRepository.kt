package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.EudiSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EudiSessionRepository : JpaRepository<EudiSession, UUID> {
    fun findByNonce(nonce: String): EudiSession?
    fun findByState(state: String): EudiSession?
}
