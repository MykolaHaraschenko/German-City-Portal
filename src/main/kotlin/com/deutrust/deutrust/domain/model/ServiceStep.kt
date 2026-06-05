package com.deutrust.deutrust.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "service_step")
class ServiceStep(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "step_number", nullable = false)
    var stepNumber: Int,

    @Column(nullable = false)
    var type: String, // EUDI_SHARE, FORM, FILE_UPLOAD

    @Column(name = "title_de", nullable = false)
    var titleDe: String,

    @Column(name = "title_en", nullable = false)
    var titleEn: String,

    @Column(name = "config_json", columnDefinition = "TEXT", nullable = false)
    var configJson: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service
)
