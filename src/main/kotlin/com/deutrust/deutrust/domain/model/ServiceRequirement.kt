package com.deutrust.deutrust.domain.model

import jakarta.persistence.*

enum class RequirementType {
    TEXT, DOCUMENT
}

@Entity
@Table(name = "service_requirement")
class ServiceRequirement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RequirementType,

    @Column(name = "title_de", nullable = false)
    var titleDe: String,

    @Column(name = "title_en", nullable = false)
    var titleEn: String,

    @Column(name = "description_de", columnDefinition = "TEXT")
    var descriptionDe: String? = null,

    @Column(name = "description_en", columnDefinition = "TEXT")
    var descriptionEn: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service
)
