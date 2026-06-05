package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.Category
import com.deutrust.deutrust.domain.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<Service, Long> {
    fun findByIsTopTrue(): List<Service>
    
    fun findByNameDeContainingIgnoreCaseOrNameEnContainingIgnoreCase(
        nameDe: String,
        nameEn: String
    ): List<Service>

    fun findByCategory(category: Category): List<Service>

    fun findByLink(link: String): Service?
}
