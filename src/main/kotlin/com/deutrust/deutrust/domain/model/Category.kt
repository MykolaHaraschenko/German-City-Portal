package com.deutrust.deutrust.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "category")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name_de", nullable = false)
    var nameDe: String,

    @Column(name = "name_en", nullable = false)
    var nameEn: String,

    @Column(nullable = false)
    var link: String,

    @Column(nullable = false)
    var icon: String,

    @Column(name = "description_de", columnDefinition = "TEXT", nullable = false)
    var descriptionDe: String = "",

    @Column(name = "description_en", columnDefinition = "TEXT", nullable = false)
    var descriptionEn: String = ""
)
