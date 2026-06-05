package com.deutrust.deutrust.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "issuer_session")
class IssuerSession(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "pre_authorized_code", nullable = false, unique = true)
    val preAuthorizedCode: String,

    @Column(name = "access_token", unique = true)
    var accessToken: String? = null,

    @Column(nullable = false)
    var status: String = "OFFER_CREATED", // OFFER_CREATED, TOKEN_ISSUED, CREDENTIAL_ISSUED, FAILED

    @Column(name = "credential_type", nullable = false)
    var credentialType: String,

    @Column(name = "credential_data", columnDefinition = "TEXT", nullable = false)
    var credentialData: String, // JSON representing the user claims to be embedded in SD-JWT

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
