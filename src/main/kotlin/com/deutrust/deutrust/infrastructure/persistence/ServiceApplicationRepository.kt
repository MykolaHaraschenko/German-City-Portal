package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.ServiceApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ServiceApplicationRepository : JpaRepository<ServiceApplication, UUID> {
    fun findByApplicantNameOrderBySubmittedAtDesc(applicantName: String): List<ServiceApplication>
    fun findAllByOrderBySubmittedAtDesc(): List<ServiceApplication>
}
