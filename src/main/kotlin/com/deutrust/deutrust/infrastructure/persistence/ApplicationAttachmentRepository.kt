package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.ApplicationAttachment
import com.deutrust.deutrust.domain.model.ServiceApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ApplicationAttachmentRepository : JpaRepository<ApplicationAttachment, UUID> {
    fun findByApplication(application: ServiceApplication): List<ApplicationAttachment>
}
