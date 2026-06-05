package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.Service
import com.deutrust.deutrust.domain.model.ServiceFaq
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceFaqRepository : JpaRepository<ServiceFaq, Long> {
    fun findByService(service: Service): List<ServiceFaq>
}
