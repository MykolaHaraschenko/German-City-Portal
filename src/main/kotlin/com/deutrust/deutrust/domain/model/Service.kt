package com.deutrust.deutrust.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "service")
class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name_de", nullable = false)
    var nameDe: String,

    @Column(name = "name_en", nullable = false)
    var nameEn: String,

    @Column(nullable = false)
    var link: String,

    @Column(name = "description_de", columnDefinition = "TEXT", nullable = false)
    var descriptionDe: String = "",

    @Column(name = "description_en", columnDefinition = "TEXT", nullable = false)
    var descriptionEn: String = "",

    @Column(name = "cost_de", nullable = false)
    var costDe: String = "",

    @Column(name = "cost_en", nullable = false)
    var costEn: String = "",

    @Column(name = "processing_time_de", nullable = false)
    var processingTimeDe: String = "",

    @Column(name = "processing_time_en", nullable = false)
    var processingTimeEn: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @Column(name = "is_top", nullable = false)
    var isTop: Boolean = false
)
