package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.domain.model.Category
import com.deutrust.deutrust.domain.model.Service
import com.deutrust.deutrust.domain.model.ServiceRequirement
import com.deutrust.deutrust.domain.model.ServiceFaq
import com.deutrust.deutrust.domain.model.ServiceStep
import com.deutrust.deutrust.infrastructure.persistence.CategoryRepository
import com.deutrust.deutrust.infrastructure.persistence.ServiceRepository
import com.deutrust.deutrust.infrastructure.persistence.ServiceRequirementRepository
import com.deutrust.deutrust.infrastructure.persistence.ServiceFaqRepository
import com.deutrust.deutrust.infrastructure.persistence.ServiceStepRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(
    private val categoryRepository: CategoryRepository,
    private val serviceRepository: ServiceRepository,
    private val requirementRepository: ServiceRequirementRepository,
    private val faqRepository: ServiceFaqRepository,
    private val stepRepository: ServiceStepRepository
) {

    data class CategoryDto(
        val id: Long,
        val nameDe: String,
        val nameEn: String,
        val link: String,
        val icon: String
    )

    data class ServiceDto(
        val id: Long,
        val nameDe: String,
        val nameEn: String,
        val link: String,
        val descriptionDe: String,
        val descriptionEn: String
    )

    data class CategoryDetailsDto(
        val nameDe: String,
        val nameEn: String,
        val descriptionDe: String,
        val descriptionEn: String,
        val services: List<ServiceDto>
    )

    data class RequirementDto(
        val id: Long,
        val type: String,
        val titleDe: String,
        val titleEn: String,
        val descriptionDe: String?,
        val descriptionEn: String?
    )

    data class FaqDto(
        val id: Long,
        val questionDe: String,
        val questionEn: String,
        val answerDe: String,
        val answerEn: String
    )

    data class StepDto(
        val id: Long,
        val stepNumber: Int,
        val type: String,
        val titleDe: String,
        val titleEn: String,
        val configJson: String
    )

    data class ServiceDetailsDto(
        val nameDe: String,
        val nameEn: String,
        val descriptionDe: String,
        val descriptionEn: String,
        val costDe: String,
        val costEn: String,
        val processingTimeDe: String,
        val processingTimeEn: String,
        val requirements: List<RequirementDto>,
        val faqs: List<FaqDto>,
        val steps: List<StepDto>
    )

    @GetMapping
    fun getCategories(): ResponseEntity<List<CategoryDto>> {
        val categories = categoryRepository.findAll().map {
            CategoryDto(
                id = it.id ?: 0L,
                nameDe = it.nameDe,
                nameEn = it.nameEn,
                link = it.link,
                icon = it.icon
            )
        }
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/top")
    fun getTopServices(): ResponseEntity<List<ServiceDto>> {
        val topServices = serviceRepository.findByIsTopTrue().map {
            ServiceDto(
                id = it.id ?: 0L,
                nameDe = it.nameDe,
                nameEn = it.nameEn,
                link = it.link,
                descriptionDe = it.descriptionDe,
                descriptionEn = it.descriptionEn
            )
        }
        return ResponseEntity.ok(topServices)
    }

    @GetMapping("/search")
    fun searchServices(@RequestParam query: String): ResponseEntity<List<ServiceDto>> {
        if (query.isBlank()) {
            return ResponseEntity.ok(emptyList())
        }
        val results = serviceRepository.findByNameDeContainingIgnoreCaseOrNameEnContainingIgnoreCase(query, query)
            .map {
                ServiceDto(
                    id = it.id ?: 0L,
                    nameDe = it.nameDe,
                    nameEn = it.nameEn,
                    link = it.link,
                    descriptionDe = it.descriptionDe,
                    descriptionEn = it.descriptionEn
                )
            }
        return ResponseEntity.ok(results)
    }

    @GetMapping("/categories/{slug}")
    fun getCategoryDetails(@PathVariable slug: String): ResponseEntity<CategoryDetailsDto> {
        val categoryLink = "/categories/$slug"
        val category = categoryRepository.findByLink(categoryLink) 
            ?: return ResponseEntity.notFound().build()
        
        val services = serviceRepository.findByCategory(category).map {
            ServiceDto(
                id = it.id ?: 0L,
                nameDe = it.nameDe,
                nameEn = it.nameEn,
                link = it.link,
                descriptionDe = it.descriptionDe,
                descriptionEn = it.descriptionEn
            )
        }
        
        return ResponseEntity.ok(
            CategoryDetailsDto(
                nameDe = category.nameDe,
                nameEn = category.nameEn,
                descriptionDe = category.descriptionDe,
                descriptionEn = category.descriptionEn,
                services = services
            )
        )
    }

    @GetMapping("/services/{slug}")
    fun getServiceDetails(@PathVariable slug: String): ResponseEntity<ServiceDetailsDto> {
        val serviceLink = "/services/$slug"
        val service = serviceRepository.findByLink(serviceLink)
            ?: return ResponseEntity.notFound().build()

        val requirements = requirementRepository.findByService(service).map {
            RequirementDto(
                id = it.id ?: 0L,
                type = it.type.name,
                titleDe = it.titleDe,
                titleEn = it.titleEn,
                descriptionDe = it.descriptionDe,
                descriptionEn = it.descriptionEn
            )
        }

        val faqs = faqRepository.findByService(service).map {
            FaqDto(
                id = it.id ?: 0L,
                questionDe = it.questionDe,
                questionEn = it.questionEn,
                answerDe = it.answerDe,
                answerEn = it.answerEn
            )
        }

        val steps = stepRepository.findByServiceOrderByStepNumberAsc(service).map {
            StepDto(
                id = it.id ?: 0L,
                stepNumber = it.stepNumber,
                type = it.type,
                titleDe = it.titleDe,
                titleEn = it.titleEn,
                configJson = it.configJson
            )
        }

        return ResponseEntity.ok(
            ServiceDetailsDto(
                nameDe = service.nameDe,
                nameEn = service.nameEn,
                descriptionDe = service.descriptionDe,
                descriptionEn = service.descriptionEn,
                costDe = service.costDe,
                costEn = service.costEn,
                processingTimeDe = service.processingTimeDe,
                processingTimeEn = service.processingTimeEn,
                requirements = requirements,
                faqs = faqs,
                steps = steps
            )
        )
    }
}
