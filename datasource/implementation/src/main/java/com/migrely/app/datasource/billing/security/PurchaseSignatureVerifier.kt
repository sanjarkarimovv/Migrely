package com.migrely.app.datasource.billing.security

import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

private const val KEY_ALGORITHM = "RSA"
private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

/**
 * Checks Google Play's signature on a purchase with the app's licence public key, the base64 key
 * Play Console shows under Monetization setup (BRD D107). Without a key every signature is accepted
 * when [acceptWithoutKey], as in debug builds, and refused otherwise, so a release built without
 * its key never unlocks anything.
 */
class PurchaseSignatureVerifier(
    private val base64PublicKey: String,
    private val acceptWithoutKey: Boolean
) {
    private val publicKey: PublicKey? by lazy {
        try {
            KeyFactory.getInstance(KEY_ALGORITHM)
                .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey)))
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun isValid(signedData: String, signature: String): Boolean {
        if (base64PublicKey.isBlank()) return acceptWithoutKey
        val key = publicKey ?: return false
        return try {
            Signature.getInstance(SIGNATURE_ALGORITHM).run {
                initVerify(key)
                update(signedData.toByteArray(Charsets.UTF_8))
                verify(Base64.getDecoder().decode(signature))
            }
        } catch (_: GeneralSecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
