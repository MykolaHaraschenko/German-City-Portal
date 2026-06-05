package com.deutrust.deutrust.domain.service

import com.deutrust.deutrust.domain.model.IssuerSession
import java.util.UUID

interface IssuerService {
    /**
     * Initiates the issuance process for a specific user and credential type.
     * Returns the pre-authorized code that will be embedded in the Credential Offer.
     */
    fun createOffer(credentialType: String, credentialData: Map<String, Any>): IssuerSession

    /**
     * Exchanges a pre-authorized code for an access token.
     */
    fun exchangeToken(preAuthorizedCode: String): String

    /**
     * Issues the actual SD-JWT credential using the access token.
     * Returns the serialized SD-JWT string.
     */
    fun issueCredential(accessToken: String, issuerUri: String, jwk: Map<String, Any>? = null, formatType: String = "vc+sd-jwt"): String
}
