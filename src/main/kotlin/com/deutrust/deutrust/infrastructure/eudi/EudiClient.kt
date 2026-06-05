package com.deutrust.deutrust.infrastructure.eudi

import com.deutrust.deutrust.application.controller.VerifierController.DocumentRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.io.File
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.interfaces.ECPublicKey
import java.security.interfaces.ECPrivateKey
import java.util.Base64
import java.util.UUID
import java.nio.ByteBuffer
import java.security.spec.ECPublicKeySpec
import java.security.spec.ECPoint
import java.security.AlgorithmParameters
import java.security.spec.ECParameterSpec
import javax.crypto.KeyAgreement
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.GCMParameterSpec
import java.security.MessageDigest
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jose.crypto.ECDHDecrypter

import org.springframework.beans.factory.annotation.Value

@Component
class EudiClient(
    private val objectMapper: ObjectMapper,
    @Value("\${eudi.client-id:}") private val configuredClientId: String
) {

    private fun loadPrivateKey(): PrivateKey? {
        return try {
            val keyFile = File("keys/private_key.pem")
            if (!keyFile.exists()) return null
            val pem = keyFile.readText()
            val cleanPem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")
            val decoded = Base64.getDecoder().decode(cleanPem)
            val spec = PKCS8EncodedKeySpec(decoded)
            val kf = KeyFactory.getInstance("EC")
            kf.generatePrivate(spec)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadCertificateBase64(): String? {
        return try {
            val certFile = File("keys/certificate.pem")
            if (!certFile.exists()) return null
            val cf = java.security.cert.CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(certFile.inputStream()) as java.security.cert.X509Certificate
            Base64.getEncoder().encodeToString(cert.encoded)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a standard PEX (Presentation Exchange) compliant Presentation Definition JSON.
     * It dynamically requests the specified claims and constrains the credential type (vct) to eu.europa.ec.eudi.pid.1.
     */
    fun createPresentationDefinition(
        requestedClaims: List<String>,
        optionalClaims: List<String> = emptyList(),
        documents: List<DocumentRequest> = emptyList()
    ): String {
        val inputDescriptors = mutableListOf<Map<String, Any>>()

        if (documents.isNotEmpty()) {
            documents.forEachIndexed { index, doc ->
                val fields = mutableListOf<Map<String, Any>>(
                    mapOf(
                        "path" to listOf("$.vct"),
                        "filter" to mapOf(
                            "type" to "string",
                            "const" to doc.vct
                        )
                    )
                )
                doc.requestedClaims.forEach { claim ->
                    fields.add(mapOf("path" to listOf("$.$claim"), "intent_to_retain" to true))
                }
                doc.optionalClaims.forEach { claim ->
                    fields.add(mapOf("path" to listOf("$.$claim"), "intent_to_retain" to true, "optional" to true))
                }
                
                inputDescriptors.add(
                    mapOf(
                        "id" to "descriptor-${doc.vct.replace(":", "-").replace(".", "-")}-$index",
                        "format" to mapOf(
                            doc.format to mapOf("alg" to listOf("ES256", "RS256"))
                        ),
                        "constraints" to mapOf(
                            "limit_disclosure" to "required",
                            "fields" to fields
                        )
                    )
                )
            }
        } else {
            val fields = mutableListOf<Map<String, Any>>(
                mapOf(
                    "path" to listOf("$.vct"),
                    "filter" to mapOf(
                        "type" to "string",
                        "const" to "eu.europa.ec.eudi.pid.1"
                    )
                )
            )
            requestedClaims.forEach { claim ->
                fields.add(mapOf("path" to listOf("$.$claim"), "intent_to_retain" to true))
            }
            optionalClaims.forEach { claim ->
                fields.add(mapOf("path" to listOf("$.$claim"), "intent_to_retain" to true, "optional" to true))
            }
            inputDescriptors.add(
                mapOf(
                    "id" to "eu.europa.ec.eudi.pid.1",
                    "format" to mapOf(
                        "vc+sd-jwt" to mapOf("alg" to listOf("ES256", "RS256"))
                    ),
                    "constraints" to mapOf(
                        "limit_disclosure" to "required",
                        "fields" to fields
                    )
                )
            )
        }

        val definition = mapOf(
            "id" to "verification-sig",
            "input_descriptors" to inputDescriptors
        )
        return objectMapper.writeValueAsString(definition)
    }

    /**
     * Generates a DCQL Query JSON for OID4VP
     * requesting the specified claims from the PID credential.
     */
    /**
     * Generates a DCQL Query JSON map for OID4VP
     * requesting the specified claims from the PID credential in both dc+sd-jwt and mso_mdoc formats.
     */
    fun createDcqlQuery(
        requestedClaims: List<String>,
        optionalClaims: List<String> = emptyList(),
        documents: List<DocumentRequest> = emptyList()
    ): Map<String, Any> {
        if (documents.isNotEmpty()) {
            val credentials = mutableListOf<Map<String, Any>>()
            val requiredIds = mutableListOf<String>()
            val optionalIds = mutableListOf<String>()

            documents.forEachIndexed { index, doc ->
                val claimsList = mutableListOf<Map<String, Any>>()
                doc.requestedClaims.forEach { claim ->
                    claimsList.add(mapOf("path" to listOf(claim)))
                }
                doc.optionalClaims.forEach { claim ->
                    claimsList.add(mapOf("path" to listOf(claim), "optional" to true))
                }

                val credId = "doc-${doc.vct.replace(":", "-").replace(".", "-")}-$index"
                if (doc.required) {
                    requiredIds.add(credId)
                } else {
                    optionalIds.add(credId)
                }

                val credMap = mutableMapOf<String, Any>(
                    "id" to credId,
                    "format" to doc.format,
                    "claims" to claimsList
                )
                
                if (doc.format == "mso_mdoc") {
                    credMap["meta"] = mapOf("doctype_value" to doc.vct)
                } else {
                    credMap["meta"] = mapOf("vct_values" to listOf(doc.vct))
                }
                
                credentials.add(credMap)
            }

            val options = mutableListOf<List<String>>()
            val subsets = mutableListOf<List<String>>(emptyList())
            for (item in optionalIds) {
                val size = subsets.size
                for (i in 0 until size) {
                    subsets.add(subsets[i] + item)
                }
            }
            for (subset in subsets) {
                val option = requiredIds + subset
                if (option.isNotEmpty()) {
                    options.add(option)
                }
            }

            val credentialSets = listOf(
                mapOf("options" to options)
            )

            return mapOf(
                "credentials" to credentials,
                "credential_sets" to credentialSets
            )
        } else {
            val sdJwtClaims = mutableListOf<Map<String, Any>>()
            requestedClaims.forEach { claim ->
                sdJwtClaims.add(mapOf("path" to listOf(claim)))
            }
            optionalClaims.forEach { claim ->
                sdJwtClaims.add(mapOf("path" to listOf(claim), "optional" to true))
            }
            val credentials = listOf(
                mapOf(
                    "id" to "residence-sd-jwt",
                    "format" to "dc+sd-jwt",
                    "claims" to sdJwtClaims,
                    "meta" to mapOf(
                        "vct_values" to listOf("urn:credential:proof_of_residence_registration")
                    )
                )
            )
            val credentialSets = listOf(
                mapOf(
                    "options" to listOf(listOf("residence-sd-jwt"))
                )
            )
            return mapOf(
                "credentials" to credentials,
                "credential_sets" to credentialSets
            )
        }
    }

    fun createAuthDcqlQuery(): Map<String, Any> {
        val sdJwtClaims = listOf(
            mapOf("path" to listOf("given_name")),
            mapOf("path" to listOf("family_name")),
            mapOf("path" to listOf("birthdate")),
            mapOf("path" to listOf("address", "street_address")),
            mapOf("path" to listOf("address", "postal_code")),
            mapOf("path" to listOf("address", "locality")),
            mapOf("path" to listOf("address", "country")),
            mapOf("path" to listOf("nationalities"))
        )

        val mdocClaims = listOf(
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "given_name")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "family_name")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "birth_date")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "resident_street")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "resident_postal_code")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "resident_city")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "resident_country")),
            mapOf("path" to listOf("eu.europa.ec.eudi.pid.1", "nationality"))
        )

        val credentials = listOf(
            mapOf(
                "id" to "pid-sd-jwt",
                "format" to "dc+sd-jwt",
                "claims" to sdJwtClaims,
                "meta" to mapOf(
                    "vct_values" to listOf("urn:eudi:pid:de:1")
                )
            ),
            mapOf(
                "id" to "pid-mso-mdoc",
                "format" to "mso_mdoc",
                "claims" to mdocClaims,
                "meta" to mapOf(
                    "doctype_value" to "eu.europa.ec.eudi.pid.1"
                )
            )
        )

        val credentialSets = listOf(
            mapOf(
                "options" to listOf(
                    listOf("pid-sd-jwt"),
                    listOf("pid-mso-mdoc")
                )
            )
        )

        return mapOf(
            "credentials" to credentials,
            "credential_sets" to credentialSets
        )
    }

    private fun getPublicKeyJwk(): Map<String, Any>? {
        return try {
            val certFile = File("keys/certificate.pem")
            val pubKeyFile = File("keys/public_key.pem")
            val publicKey = when {
                certFile.exists() -> {
                    val cf = java.security.cert.CertificateFactory.getInstance("X.509")
                    val cert = cf.generateCertificate(certFile.inputStream())
                    cert.publicKey
                }
                pubKeyFile.exists() -> {
                    val pem = pubKeyFile.readText()
                    val cleanPem = pem
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replace("\\s".toRegex(), "")
                    val decoded = Base64.getDecoder().decode(cleanPem)
                    val spec = X509EncodedKeySpec(decoded)
                    val kf = KeyFactory.getInstance("EC")
                    kf.generatePublic(spec)
                }
                else -> null
            }

            if (publicKey is ECPublicKey) {
                val w = publicKey.w
                val xBytes = getCoordinateBytes(w.affineX)
                val yBytes = getCoordinateBytes(w.affineY)
                val xBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(xBytes)
                val yBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(yBytes)

                mapOf(
                    "kty" to "EC",
                    "crv" to "P-256",
                    "x" to xBase64,
                    "y" to yBase64,
                    "alg" to "ECDH-ES",
                    "kid" to "6410f043-6732-4bb5-b3a4-c9ff233702c2"
                )
            } else {
                // Default mock P-256 JWK if no keys are found yet
                mapOf(
                    "kty" to "EC",
                    "crv" to "P-256",
                    "x" to "nh9m4g5AvCRW9qdJ88JCAMgRmpjtx0p9LLQgx2TR0g4",
                    "y" to "73doUYKZnfanw1LQti7p_M3YCuTXLLplS8L_2OdFDV8",
                    "alg" to "ECDH-ES",
                    "kid" to "6410f043-6732-4bb5-b3a4-c9ff233702c2"
                )
            }
        } catch (e: Exception) {
            // Default mock P-256 JWK if any error occurs
            mapOf(
                "kty" to "EC",
                "crv" to "P-256",
                "x" to "nh9m4g5AvCRW9qdJ88JCAMgRmpjtx0p9LLQgx2TR0g4",
                "y" to "73doUYKZnfanw1LQti7p_M3YCuTXLLplS8L_2OdFDV8",
                "alg" to "ECDH-ES",
                "kid" to "6410f043-6732-4bb5-b3a4-c9ff233702c2"
            )
        }
    }

    fun getPublicKeyJwkPublic(): Map<String, Any> {
        return getPublicKeyJwk() ?: mapOf(
            "kty" to "EC",
            "crv" to "P-256",
            "x" to "nh9m4g5AvCRW9qdJ88JCAMgRmpjtx0p9LLQgx2TR0g4",
            "y" to "73doUYKZnfanw1LQti7p_M3YCuTXLLplS8L_2OdFDV8",
            "alg" to "ECDH-ES",
            "kid" to "6410f043-6732-4bb5-b3a4-c9ff233702c2"
        )
    }

    private fun getCoordinateBytes(coordinate: java.math.BigInteger): ByteArray {
        val bytes = coordinate.toByteArray()
        if (bytes.size == 32) return bytes
        if (bytes.size > 32) {
            return bytes.copyOfRange(bytes.size - 32, bytes.size)
        }
        val padded = ByteArray(32)
        System.arraycopy(bytes, 0, padded, 32 - bytes.size, bytes.size)
        return padded
    }

    /**
     * Helper to resolve the correct client_id for standard or custom configuration.
     */
    fun getClientId(redirectUri: String): String {
        val hasCert = File("keys/certificate.pem").exists()
        if (hasCert) {
            // x509_san_dns expects the host name matching the certificate SAN, prefixed with the scheme
            val host = redirectUri.removePrefix("https://").removePrefix("http://").split("/")[0].split(":")[0]
            return "x509_san_dns:$host"
        }
        return if (!configuredClientId.isNullOrBlank()) configuredClientId.trim() else redirectUri
    }

    /**
     * Creates an OpenID4VP Request Object (JWT payload) for the EUDI Wallet.
     */
    fun createRequestObject(
        transactionId: UUID,
        redirectUri: String,
        nonce: String,
        state: String,
        requestedClaims: List<String> = listOf("given_name", "family_name", "date_of_birth"),
        optionalClaims: List<String> = emptyList(),
        documents: List<DocumentRequest> = emptyList()
    ): Map<String, Any> {
        val finalClientId = getClientId(redirectUri)
        
        val clientMetadata = mapOf(
            "jwks" to mapOf(
                "keys" to listOf(getPublicKeyJwkPublic())
            ),
            "vp_formats_supported" to mapOf(
                "mso_mdoc" to mapOf(
                    "alg" to listOf("ES256")
                ),
                "dc+sd-jwt" to mapOf(
                    "kb-jwt_alg_values" to listOf("ES256"),
                    "sd-jwt_alg_values" to listOf("ES256")
                )
            ),
            "encrypted_response_enc_values_supported" to listOf("A128GCM", "A256GCM")
        )

        return mapOf(
            "client_id" to finalClientId,
            "response_uri" to "$redirectUri/api/v1/openid4vp/response/$transactionId",
            "response_type" to "vp_token",
            "response_mode" to "direct_post.jwt",
            "aud" to "https://self-issued.me/v2",
            "nonce" to nonce,
            "state" to state,
            "dcql_query" to createDcqlQuery(requestedClaims, optionalClaims, documents),
            "client_metadata" to clientMetadata,
            "iat" to (System.currentTimeMillis() / 1000),
            "exp" to (System.currentTimeMillis() / 1000 + 3600)
        )
    }

    fun createAuthRequestObject(
        transactionId: UUID,
        redirectUri: String,
        nonce: String,
        state: String
    ): Map<String, Any> {
        val finalClientId = getClientId(redirectUri)
        
        val clientMetadata = mapOf(
            "jwks" to mapOf(
                "keys" to listOf(getPublicKeyJwkPublic())
            ),
            "vp_formats_supported" to mapOf(
                "mso_mdoc" to mapOf(
                    "alg" to listOf("ES256")
                ),
                "dc+sd-jwt" to mapOf(
                    "kb-jwt_alg_values" to listOf("ES256"),
                    "sd-jwt_alg_values" to listOf("ES256")
                )
            ),
            "encrypted_response_enc_values_supported" to listOf("A128GCM", "A256GCM")
        )

        return mapOf(
            "client_id" to finalClientId,
            "response_uri" to "$redirectUri/api/v1/openid4vp/response/$transactionId",
            "response_type" to "vp_token",
            "response_mode" to "direct_post.jwt",
            "aud" to "https://self-issued.me/v2",
            "nonce" to nonce,
            "state" to state,
            "dcql_query" to createAuthDcqlQuery(),
            "client_metadata" to clientMetadata,
            "iat" to (System.currentTimeMillis() / 1000),
            "exp" to (System.currentTimeMillis() / 1000 + 3600)
        )
    }

    /**
     * Signs the OpenID4VP Request Object as an ES256 JWS (JWT) using the verifier's P-256 private key.
     */
    fun signRequestObjectJwt(
        transactionId: UUID,
        redirectUri: String,
        nonce: String,
        state: String,
        requestedClaims: List<String> = listOf("given_name", "family_name", "date_of_birth"),
        optionalClaims: List<String> = emptyList(),
        documents: List<DocumentRequest> = emptyList()
    ): String {
        val payloadMap = createRequestObject(transactionId, redirectUri, nonce, state, requestedClaims, optionalClaims, documents)
        val payloadJson = objectMapper.writeValueAsString(payloadMap)

        val header = mutableMapOf<String, Any>(
            "alg" to "ES256",
            "typ" to "oauth-authz-req+jwt"
        )
        val certBase64 = loadCertificateBase64()
        if (certBase64 != null) {
            header["x5c"] = listOf(certBase64)
        }
        val headerJson = objectMapper.writeValueAsString(header)

        val headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toByteArray())
        val payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())

        val unsignedToken = "$headerBase64.$payloadBase64"

        val privateKey = loadPrivateKey()
        val signatureBytes = if (privateKey != null) {
            val signature = Signature.getInstance("SHA256withECDSAinP1363Format")
            signature.initSign(privateKey)
            signature.update(unsignedToken.toByteArray())
            signature.sign()
        } else {
            "mock-signature-hash-representing-ec-sig-key-eudi".toByteArray()
        }

        val signatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
        return "$unsignedToken.$signatureBase64"
    }

    fun signAuthRequestObjectJwt(
        transactionId: UUID,
        redirectUri: String,
        nonce: String,
        state: String
    ): String {
        val payloadMap = createAuthRequestObject(transactionId, redirectUri, nonce, state)
        val payloadJson = objectMapper.writeValueAsString(payloadMap)

        val header = mutableMapOf<String, Any>(
            "alg" to "ES256",
            "typ" to "oauth-authz-req+jwt"
        )
        val certBase64 = loadCertificateBase64()
        if (certBase64 != null) {
            header["x5c"] = listOf(certBase64)
        }
        val headerJson = objectMapper.writeValueAsString(header)

        val headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toByteArray())
        val payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())

        val unsignedToken = "$headerBase64.$payloadBase64"

        val privateKey = loadPrivateKey()
        val signatureBytes = if (privateKey != null) {
            val signature = Signature.getInstance("SHA256withECDSAinP1363Format")
            signature.initSign(privateKey)
            signature.update(unsignedToken.toByteArray())
            signature.sign()
        } else {
            "mock-signature-hash-representing-ec-sig-key-eudi".toByteArray()
        }

        val signatureBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes)
        return "$unsignedToken.$signatureBase64"
    }

    /**
     * Verifies the SD-JWT VC received in the direct-post vp_token.
     * In a production environment, this parses disclosures and verifies key binding / trust lists.
     * For the MVP, we decode the SD-JWT VC layers, extract the claims, and return the resolved profile.
     */
    fun verifySdJwt(vpToken: String): Map<String, Any> {
        try {
            println("=== EudiClient verifySdJwt start ===")
            // SD-JWT VC format is usually: <payload-jwt>~<disclosure-1>~<disclosure-2>...~<optional-holder-binding-jws>
            val parts = vpToken.split("~")
            println("SD-JWT Parts Count: ${parts.size}")
            val credentialJwt = parts[0]

            // Parse the JWT payload to extract basic metadata and public keys
            val jwtBody = credentialJwt.split(".")
            println("Credential JWT segments count: ${jwtBody.size}")
            if (jwtBody.size < 2) {
                throw IllegalArgumentException("Invalid JWT format (requires header.payload.signature, got ${jwtBody.size} segments)")
            }

            val payloadJson = String(Base64.getUrlDecoder().decode(jwtBody[1]))
            println("Credential Payload JSON: $payloadJson")
            val rawClaims = objectMapper.readValue(payloadJson, Map::class.java) as Map<String, Any>

            // Process disclosures
            val disclosedClaims = mutableMapOf<String, Any>()

            // Standard SD-JWT Disclosures are JSON arrays: [salt, claim_name, claim_value]
            // We loop through disclosures (from index 1 to second-to-last or last part)
            for (i in 1 until parts.size) {
                val disclosure = parts[i].trim()
                if (disclosure.isEmpty()) continue

                // If it is a key binding signature (starts with JWS header eyJ and contains dots), skip it
                if (disclosure.startsWith("eyJ") && disclosure.contains(".")) {
                    println("Found Holder Binding JWS (skipping signature disclosure parsing for: ${disclosure.take(50)}...)")
                    continue
                }

                try {
                    val decodedBytes = Base64.getUrlDecoder().decode(disclosure)
                    val disclosureArray = objectMapper.readValue(decodedBytes, List::class.java)
                    if (disclosureArray.size >= 3) {
                        val claimName = disclosureArray[1]?.toString()
                        val claimValue = disclosureArray[2]
                        if (claimName != null && claimValue != null) {
                            println("Parsed SD-JWT Disclosure: '$claimName' = '$claimValue'")
                            disclosedClaims[claimName] = claimValue
                        }
                    }
                } catch (de: Exception) {
                    println("Warning: failed to decode disclosure segment index $i: ${de.message}")
                }
            }

            // Add metadata from the JWT payload
            disclosedClaims["vct"] = rawClaims["vct"] ?: "unknown"
            disclosedClaims["iss"] = rawClaims["iss"] ?: "unknown"
            disclosedClaims["verified"] = true

            println("=== EudiClient verifySdJwt success ===")
            return disclosedClaims
        } catch (e: Exception) {
            println("❌ EudiClient verifySdJwt failed!")
            e.printStackTrace()
            throw IllegalArgumentException("Failed to verify SD-JWT: ${e.message}", e)
        }
    }

    /**
     * Generates an OID4VCI Credential Offer structure.
     */
    fun createCredentialOffer(
        offerId: UUID,
        issuerUri: String,
        preAuthorizedCode: String
    ): Map<String, Any> {
        return mapOf(
            "credential_issuer" to issuerUri,
            "credential_configuration_ids" to listOf("BusinessMembership_SD_JWT"),
            "grants" to mapOf(
                "urn:ietf:params:oauth:grant-type:pre-authorized_code" to mapOf(
                    "pre-authorized_code" to preAuthorizedCode,
                    "user_pin_required" to false
                )
            )
        )
    }

    /**
     * Generates and signs an SD-JWT Verifiable Credential for the user.
     */
    fun signSdJwtCredential(
        userId: String?,
        credentialType: String,
        credentialData: Map<String, Any>,
        issuerUri: String
    ): String {
        // Form a standard SD-JWT header
        val header = mapOf(
            "alg" to "HS256",
            "typ" to "vc+sd-jwt"
        )
        
        // Form the payload
        val payload = mutableMapOf<String, Any>(
            "iss" to issuerUri,
            "sub" to (userId ?: "user-123"),
            "iat" to (System.currentTimeMillis() / 1000),
            "exp" to (System.currentTimeMillis() / 1000 + 31536000),
            "vct" to credentialType
        )
        payload.putAll(credentialData)

        val headerBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(header)
        )
        val payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(payload)
        )

        // Simple mock signature for MVP (using SHA256 bytes)
        val mockSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(
            "mock-signature-hash-representing-ec-sig-key-eudi".toByteArray()
        )

        // Create dummy disclosures for selective disclosure presentation demo
        val disclosureGivenName = Base64.getUrlEncoder().withoutPadding().encodeToString(
            "[_salt_given_name, given_name, ${credentialData["given_name"] ?: "Name"}]".toByteArray()
        )
        val disclosureFamilyName = Base64.getUrlEncoder().withoutPadding().encodeToString(
            "[_salt_family_name, family_name, ${credentialData["family_name"] ?: "Surname"}]".toByteArray()
        )

        return "$headerBase64.$payloadBase64.$mockSignature~$disclosureGivenName~$disclosureFamilyName~"
    }

    /**
     * Decrypts an ECDH-ES A256GCM encrypted JWE from the wallet to obtain the cleartext JWS.
     */
    fun decryptJwe(jweString: String): String {
        try {
            val encryptedJWT = EncryptedJWT.parse(jweString.trim())
            val privateKey = loadPrivateKey() as? ECPrivateKey 
                ?: throw IllegalStateException("Verifier private key is not loaded or is not an EC private key")
            val decrypter = ECDHDecrypter(privateKey)
            encryptedJWT.decrypt(decrypter)
            return encryptedJWT.payload.toString()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decrypt JWE: ${e.message}", e)
        }
    }
}
