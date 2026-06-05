package com.deutrust.deutrust.infrastructure.eudi

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.ECDSASigner
import eu.europa.ec.eudi.sdjwt.HashAlgorithm
import eu.europa.ec.eudi.sdjwt.NimbusSdJwtOps
import eu.europa.ec.eudi.sdjwt.SdJwtFactory
import eu.europa.ec.eudi.sdjwt.dsl.values.sdJwt
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

@Service
class IssuerCryptoService(
    private val objectMapper: ObjectMapper
) {

    private fun loadPrivateKey(): ECPrivateKey? {
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
            kf.generatePrivate(spec) as ECPrivateKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Signs the credential data using SD-JWT format.
     */
    fun signSdJwtCredential(
        credentialType: String,
        credentialData: Map<String, Any>,
        issuerUri: String,
        subject: String,
        jwk: Map<String, Any>? = null,
        formatType: String = "vc+sd-jwt"
    ): String = runBlocking {
        val privateKey = loadPrivateKey()
            ?: throw IllegalStateException("Cannot issue credential: private key not found or invalid.")

        val signer = ECDSASigner(privateKey)

        // 1. Build the SdJwtObject with selectively disclosable claims
        val sdJwtObj = sdJwt {
            claim("iss", issuerUri)
            claim("sub", subject)
            claim("iat", System.currentTimeMillis() / 1000)
            claim("exp", System.currentTimeMillis() / 1000 + 31536000) // 1 year
            claim("vct", credentialType)

            if (jwk != null) {
                val jwkJson = kotlinx.serialization.json.buildJsonObject {
                    jwk.forEach { (k, v) ->
                        put(k, kotlinx.serialization.json.JsonPrimitive(v.toString()))
                    }
                }
                val cnfJson = kotlinx.serialization.json.buildJsonObject {
                    put("jwk", jwkJson)
                }
                claim("cnf", cnfJson)
            }

            // Make every entry in credentialData selectively disclosable
            credentialData.forEach { (key, value) ->
                when (value) {
                    is String -> sdClaim(key, value)
                    is Number -> sdClaim(key, value)
                    is Boolean -> sdClaim(key, value)
                    else -> {
                        // For complex objects, just store them as strings or handle them manually.
                        // Here we just serialize to string to keep it simple.
                        sdClaim(key, objectMapper.writeValueAsString(value))
                    }
                }
            }
        }

        // 2. Issue the SD-JWT
        val factory = SdJwtFactory.Default
        val issuer = NimbusSdJwtOps.issuer(factory, signer, JWSAlgorithm.ES256) {
            type(com.nimbusds.jose.JOSEObjectType(formatType))
            
            // Inject issuer certificate so the wallet can verify the signature
            val certFile = File("keys/certificate.pem")
            if (certFile.exists()) {
                val certPem = certFile.readText()
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replace("\\s".toRegex(), "")
                x509CertChain(listOf(com.nimbusds.jose.util.Base64(certPem)))
            }
            keyID("test-key-1")
        }
        
        val result = issuer.issue(sdJwtObj)
        val sdJwt = result.getOrThrow()
        
        // 3. Serialize to standard format (Header.Payload.Signature~Disclosure1~Disclosure2~...)
        val jwtString = sdJwt.jwt.serialize()
        val disclosuresString = sdJwt.disclosures.joinToString("~") { it.value }
        if (disclosuresString.isEmpty()) {
            "${jwtString}~"
        } else {
            "${jwtString}~${disclosuresString}~"
        }
    }
}
