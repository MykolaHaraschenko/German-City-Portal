package com.deutrust.deutrust.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "eudi_session")
class EudiSession(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "business_id", nullable = false)
    var businessId: String,

    @Column(nullable = false)
    var status: String = "PENDING", // PENDING, COMPLETED, EXPIRED, FAILED

    @Column(name = "requested_claims", columnDefinition = "TEXT", nullable = false)
    var requestedClaims: String, // JSON representing the DIF Presentation Definition claims

    @Column(nullable = false, unique = true)
    val nonce: String,

    @Column(nullable = false, unique = true)
    val state: String,

    @Column(name = "presentation_payload", columnDefinition = "TEXT")
    var presentationPayload: String? = null, // JSON containing the decrypted/verified claims from the wallet

    @Column(name = "optional_claims", columnDefinition = "TEXT")
    var optionalClaims: String? = null,

    @Column(name = "requested_documents", columnDefinition = "TEXT")
    var requestedDocuments: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
