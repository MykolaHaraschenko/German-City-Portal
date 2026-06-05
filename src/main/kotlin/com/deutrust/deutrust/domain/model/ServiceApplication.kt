package com.deutrust.deutrust.domain.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "service_application")
class ServiceApplication(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service,

    @Column(nullable = false)
    var status: String = "PENDING", // PENDING, APPROVED, REJECTED

    @Column(name = "applicant_name")
    var applicantName: String? = null,

    @Column(name = "responses_json", columnDefinition = "TEXT", nullable = false)
    var responsesJson: String, // Combined JSON of responses for all steps

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: Instant = Instant.now()
)
