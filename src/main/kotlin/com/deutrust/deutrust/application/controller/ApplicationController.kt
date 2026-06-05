package com.deutrust.deutrust.application.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.deutrust.deutrust.domain.model.ApplicationAttachment
import com.deutrust.deutrust.domain.model.ServiceApplication
import com.deutrust.deutrust.infrastructure.persistence.ServiceApplicationRepository
import com.deutrust.deutrust.infrastructure.persistence.ApplicationAttachmentRepository
import com.deutrust.deutrust.infrastructure.persistence.ServiceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartHttpServletRequest
import java.util.UUID

@CrossOrigin
@RestController
@RequestMapping("/api/v1/services")
class ApplicationController(
    private val serviceRepository: ServiceRepository,
    private val applicationRepository: ServiceApplicationRepository,
    private val attachmentRepository: ApplicationAttachmentRepository,
    private val objectMapper: ObjectMapper
) {

    data class SubmitResponse(
        val applicationId: UUID,
        val status: String
    )

    @PostMapping("/{slug}/apply")
    fun applyForService(
        @PathVariable slug: String,
        @RequestParam("responses") responsesJson: String,
        @RequestParam(value = "username", required = false) username: String?,
        request: MultipartHttpServletRequest
    ): ResponseEntity<SubmitResponse> {
        val serviceLink = "/services/$slug"
        val service = serviceRepository.findByLink(serviceLink)
            ?: return ResponseEntity.notFound().build()

        var applicantName: String? = null
        try {
            val root = objectMapper.readTree(responsesJson)
            val rootUsername = root.path("username").asText()
            if (!rootUsername.isNullOrBlank()) {
                applicantName = rootUsername
            }
            
            if (applicantName.isNullOrBlank()) {
                val eudiClaimsNode = root.path("eudiClaims")
                val fullName = eudiClaimsNode.path("full_name").asText()
                if (!fullName.isNullOrBlank()) {
                    applicantName = fullName
                } else {
                    val givenName = eudiClaimsNode.path("given_name").asText()
                    val familyName = eudiClaimsNode.path("family_name").asText()
                    if (!givenName.isNullOrBlank() && !familyName.isNullOrBlank()) {
                        applicantName = "$givenName $familyName"
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors and leave as null
        }

        if (applicantName.isNullOrBlank() && !username.isNullOrBlank()) {
            applicantName = username
        }

        // Create and Save the ServiceApplication
        val application = applicationRepository.save(
            ServiceApplication(
                service = service,
                responsesJson = responsesJson,
                status = "APPROVED",
                applicantName = applicantName
            )
        )

        val uploadsDir = "uploads/${application.id}"
        java.io.File(uploadsDir).mkdirs()

        // Generate Mock Decision Notice PDF
        val decisionPdfName = "Bescheid_${service.nameDe.replace(" ", "_")}.pdf"
        val decisionPdfPath = "$uploadsDir/$decisionPdfName"
        val pdfContent = """
            %PDF-1.4
            1 0 obj
            << /Type /Catalog /Pages 2 0 R >>
            endobj
            2 0 obj
            << /Type /Pages /Kids [3 0 R] /Count 1 >>
            endobj
            3 0 obj
            << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R >>
            endobj
            4 0 obj
            << /Length 200 >>
            stream
            BT
            /F1 14 Tf
            72 750 Td
            (FREISTAAT SACHSEN - BESCHEID) Tj
            /F1 10 Tf
            0 -40 Td
            (Ihre Dienstleistung: ${service.nameDe}) Tj
            0 -20 Td
            (Antrags-ID: ${application.id}) Tj
            0 -20 Td
            (Status: GENEHMIGT / APPROVED) Tj
            0 -20 Td
            (Herzlichen Glueckwunsch! Ihr Antrag wurde erfolgreich genehmigt.) Tj
            ET
            endstream
            endobj
            xref
            0 5
            0000000000 65535 f
            0000000009 00000 n
            0000000058 00000 n
            0000000115 00000 n
            0000000210 00000 n
            trailer
            << /Size 5 /Root 1 0 R >>
            startxref
            420
            %%EOF
        """.trimIndent()
        java.nio.file.Files.write(java.nio.file.Paths.get(decisionPdfPath), pdfContent.toByteArray(Charsets.UTF_8))

        // Save mock decision attachment
        attachmentRepository.save(
            ApplicationAttachment(
                fileName = decisionPdfName,
                contentType = "application/pdf",
                filePath = decisionPdfPath,
                attachmentKey = "decision_notice",
                application = application
            )
        )

        // Process File Attachments dynamically from Multipart Request
        request.fileMap.forEach { (key, file) ->
            if (!file.isEmpty) {
                val originalName = file.originalFilename ?: "uploaded_file"
                val filePath = "$uploadsDir/$originalName"
                java.nio.file.Files.write(java.nio.file.Paths.get(filePath), file.bytes)
                
                attachmentRepository.save(
                    ApplicationAttachment(
                        fileName = originalName,
                        contentType = file.contentType ?: "application/octet-stream",
                        filePath = filePath,
                        attachmentKey = key,
                        application = application
                    )
                )
            }
        }

        return ResponseEntity.ok(
            SubmitResponse(
                applicationId = application.id,
                status = application.status
            )
        )
    }
}
