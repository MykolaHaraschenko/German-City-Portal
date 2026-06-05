package com.deutrust.deutrust.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "issuer")
data class IssuerProperties(
    val documentTypes: List<DocumentTypeConfig> = emptyList()
)

data class DocumentTypeConfig(
    val id: String,
    val displayName: String,
    val backgroundColor: String = "#1d1d1f",
    val textColor: String = "#FFFFFF",
    val logoUri: String? = null,
    val logoAltText: String? = null,
    val claims: List<ClaimConfig> = emptyList()
)

data class ClaimConfig(
    val name: String,
    val required: Boolean = false
)
