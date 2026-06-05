package com.deutrust.deutrust.infrastructure.eudi

import com.deutrust.deutrust.config.DocumentTypeConfig
import com.deutrust.deutrust.config.IssuerProperties
import org.springframework.stereotype.Component

/**
 * Central registry of all document types the issuer can issue.
 * Reads from YAML configuration (application.yml).
 * Used by IssuerWellKnownController to auto-generate /.well-known metadata
 * and by IssuerService for credential type validation.
 */
@Component
class DocumentTypeRegistry(
    private val issuerProperties: IssuerProperties
) {

    fun getAll(): List<DocumentTypeConfig> = issuerProperties.documentTypes

    fun get(id: String): DocumentTypeConfig? =
        issuerProperties.documentTypes.find { it.id == id }

    fun exists(id: String): Boolean =
        issuerProperties.documentTypes.any { it.id == id }

    /**
     * Generates the credential_configurations_supported map for
     * /.well-known/openid-credential-issuer metadata endpoint.
     * Each registered document type becomes a credential configuration entry.
     */
    fun toCredentialConfigurations(): Map<String, Any> {
        return issuerProperties.documentTypes.associate { docType ->
            docType.id to buildConfigurationEntry(docType)
        }
    }

    private fun buildConfigurationEntry(docType: DocumentTypeConfig): Map<String, Any> {
        val config = mutableMapOf<String, Any>(
            "format" to "dc+sd-jwt",
            "vct" to docType.id,
            "scope" to docType.id,
            "cryptographic_binding_methods_supported" to listOf("jwk", "x5c"),
            "credential_signing_alg_values_supported" to listOf("ES256", "ES384", "RS256"),
            "proof_types_supported" to mapOf(
                "jwt" to mapOf("proof_signing_alg_values_supported" to listOf("ES256"))
            ),
            "proof_types" to mapOf(
                "jwt" to mapOf("proof_signing_alg_values_supported" to listOf("ES256"))
            )
        )

        val display = mutableMapOf<String, Any>(
            "name" to docType.displayName,
            "locale" to "en-US",
            "background_color" to docType.backgroundColor,
            "text_color" to docType.textColor
        )

        if (docType.logoUri != null) {
            val logo = mutableMapOf<String, String>("uri" to docType.logoUri)
            if (docType.logoAltText != null) {
                logo["alt_text"] = docType.logoAltText
            }
            display["logo"] = logo
        }

        config["display"] = listOf(display)
        return config
    }
}
