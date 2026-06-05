package com.deutrust.deutrust.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "service_faq")
class ServiceFaq(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "question_de", nullable = false)
    var questionDe: String,

    @Column(name = "question_en", nullable = false)
    var questionEn: String,

    @Column(name = "answer_de", columnDefinition = "TEXT", nullable = false)
    var answerDe: String,

    @Column(name = "answer_en", columnDefinition = "TEXT", nullable = false)
    var answerEn: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service
)
