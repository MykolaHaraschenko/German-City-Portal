package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.Service
import com.deutrust.deutrust.domain.model.ServiceRequirement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRequirementRepository : JpaRepository<ServiceRequirement, Long> {
    fun findByService(service: Service): List<ServiceRequirement>
}
