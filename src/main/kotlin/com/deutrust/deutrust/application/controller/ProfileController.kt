package com.deutrust.deutrust.application.controller

import com.deutrust.deutrust.domain.model.ServiceApplication
import com.deutrust.deutrust.infrastructure.persistence.ServiceApplicationRepository
import com.deutrust.deutrust.infrastructure.persistence.ApplicationAttachmentRepository
import com.deutrust.deutrust.domain.service.IssuerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.UUID

@CrossOrigin
@RestController
@RequestMapping("/api/v1/profile")
class ProfileController(
    private val applicationRepository: ServiceApplicationRepository,
    private val attachmentRepository: ApplicationAttachmentRepository,
    private val issuerService: IssuerService,
    private val objectMapper: ObjectMapper,
    @Value("\${eudi.public-url:}") private val publicUrl: String
) {

    data class AttachmentDto(
        val id: UUID,
        val fileName: String
    )

    data class ApplicationDto(
        val id: UUID,
        val serviceNameDe: String,
        val serviceNameEn: String,
        val status: String,
        val submittedAt: Instant,
        val attachments: List<AttachmentDto>
    )

    @GetMapping("/applications")
    fun getProfileApplications(@RequestParam(required = false) username: String?): ResponseEntity<List<ApplicationDto>> {
        val applications = if (!username.isNullOrBlank()) {
            applicationRepository.findByApplicantNameOrderBySubmittedAtDesc(username)
        } else {
            applicationRepository.findAllByOrderBySubmittedAtDesc()
        }

        val dtoList = applications.map { app ->
            val attachments = attachmentRepository.findByApplication(app)
                .filter { it.attachmentKey == "decision_notice" }
                .map { att ->
                    AttachmentDto(id = att.id, fileName = att.fileName)
                }

            ApplicationDto(
                id = app.id,
                serviceNameDe = app.service.nameDe,
                serviceNameEn = app.service.nameEn,
                status = app.status,
                submittedAt = app.submittedAt,
                attachments = attachments
            )
        }

        return ResponseEntity.ok(dtoList)
    }

    @GetMapping("/attachments/{id}")
    fun downloadAttachment(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val attachment = attachmentRepository.findById(id)
            .orElseThrow { NoSuchElementException("Attachment not found") }

        val file = java.io.File(attachment.filePath)
        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${attachment.fileName}\"")
            .contentType(MediaType.parseMediaType(attachment.contentType))
            .body(file.readBytes())
    }

    private fun getBaseUrl(request: HttpServletRequest): String {
        if (!publicUrl.isNullOrBlank()) {
            return publicUrl.trim().removeSuffix("/")
        }
        val scheme = request.scheme
        val serverName = request.serverName
        val serverPort = request.serverPort
        return if (serverPort == 80 || serverPort == 443) {
            "$scheme://$serverName"
        } else {
            "$scheme://$serverName:$serverPort"
        }
    }

    data class OfferResponse(
        val sessionId: UUID,
        val offerUri: String
    )

    @PostMapping("/applications/{id}/issue-offer")
    fun createOfferForApplication(
        @PathVariable id: UUID,
        servletRequest: HttpServletRequest
    ): ResponseEntity<OfferResponse> {
        val application = applicationRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
            
        if (application.status != "APPROVED") {
            return ResponseEntity.badRequest().build()
        }

        // Determine credentialType and map claims based on the service
        val slug = application.service.link.removePrefix("/services/")
        
        val claimsMap = mutableMapOf<String, Any>()
        
        var formInputsNode: com.fasterxml.jackson.databind.JsonNode? = null
        try {
            val responsesTree = objectMapper.readTree(application.responsesJson)
            formInputsNode = responsesTree.path("formInputs")
        } catch (e: Exception) {}
        
        val credentialType = when (slug) {
            "wohnsitz-anmelden", "wohnsitz-ummelden" -> {
                val street = formInputsNode?.path("street")?.asText() ?: ""
                val postalCode = formInputsNode?.path("postalCode")?.asText() ?: ""
                val city = formInputsNode?.path("city")?.asText() ?: ""
                val moveInDate = formInputsNode?.path("moveInDate")?.asText() ?: ""
                
                claimsMap["full_name"] = application.applicantName ?: "Erika Mustermann"
                claimsMap["residential_address"] = "$street, $postalCode $city"
                claimsMap["date_of_registration"] = moveInDate
                claimsMap["registration_authority"] = "Freistaat Sachsen Meldebehörde"
                "urn:credential:proof_of_residence_registration"
            }
            "kfz-zulassung", "fuehrerschein" -> {
                claimsMap["holder_name"] = application.applicantName ?: "Erika Mustermann"
                claimsMap["vehicle_plate"] = formInputsNode?.path("vehiclePlate")?.asText()?.ifBlank { "DD-XY-1234" } ?: "DD-XY-1234"
                claimsMap["parking_zone"] = formInputsNode?.path("parkingZone")?.asText()?.ifBlank { "Zone Dresden Mitte" } ?: "Zone Dresden Mitte"
                claimsMap["valid_from"] = formInputsNode?.path("moveInDate")?.asText()?.ifBlank { "2026-06-05" } ?: "2026-06-05"
                claimsMap["valid_until"] = "2028-06-05"
                "urn:credential:parking_permit"
            }
            else -> {
                claimsMap["taxpayer_name"] = application.applicantName ?: "Erika Mustermann"
                claimsMap["tax_id"] = "DE123456789"
                claimsMap["tax_year"] = "2025"
                claimsMap["clearance_status"] = "CLEARED"
                claimsMap["issued_by"] = "Finanzamt Dresden"
                "urn:credential:tax_clearance_certificate"
            }
        }

        val session = issuerService.createOffer(credentialType, claimsMap)
        val hostUrl = getBaseUrl(servletRequest)

        val offerEndpoint = "$hostUrl/api/v1/issuer/offers/${session.id}"
        val encodedOfferUri = java.net.URLEncoder.encode(offerEndpoint, "UTF-8")
        val offerUri = "openid-credential-offer://?credential_offer_uri=$encodedOfferUri"

        return ResponseEntity.ok(OfferResponse(session.id, offerUri))
    }
}
