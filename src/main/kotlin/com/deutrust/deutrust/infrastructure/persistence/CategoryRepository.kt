package com.deutrust.deutrust.infrastructure.persistence

import com.deutrust.deutrust.domain.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByLink(link: String): Category?
}
