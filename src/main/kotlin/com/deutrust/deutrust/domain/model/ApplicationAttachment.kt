package com.deutrust.deutrust.domain.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "application_attachment")
class ApplicationAttachment(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "file_name", nullable = false)
    var fileName: String,

    @Column(name = "content_type", nullable = false)
    var contentType: String,

    @Column(name = "file_path", nullable = false)
    var filePath: String,

    @Column(name = "attachment_key", nullable = false)
    var attachmentKey: String, // e.g. "landlord_confirmation"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: ServiceApplication
)
