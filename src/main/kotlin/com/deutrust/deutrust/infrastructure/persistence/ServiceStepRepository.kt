package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.Service
import com.deutrust.deutrust.domain.model.ServiceStep
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceStepRepository : JpaRepository<ServiceStep, Long> {
    fun findByServiceOrderByStepNumberAsc(service: Service): List<ServiceStep>
}
