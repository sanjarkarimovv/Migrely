package com.migrely.app.datasource.billing.security

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val PURCHASE_JSON = """{"productId":"migrely_lifetime","purchaseToken":"token"}"""

class PurchaseSignatureVerifierTest {
    private val playKeys: KeyPair = keyPair()
    private val otherKeys: KeyPair = keyPair()

    private val classUnderTest = PurchaseSignatureVerifier(
        Base64.getEncoder().encodeToString(playKeys.public.encoded),
        acceptWithoutKey = false
    )

    @Test
    fun `Given Play's signature when checking then it is valid`() {
        assertTrue(classUnderTest.isValid(PURCHASE_JSON, sign(PURCHASE_JSON, playKeys)))
    }

    @Test
    fun `Given changed data when checking then it is not valid`() {
        val signature = sign(PURCHASE_JSON, playKeys)

        assertFalse(classUnderTest.isValid(PURCHASE_JSON.replace("lifetime", "free"), signature))
    }

    @Test
    fun `Given a signature by another key when checking then it is not valid`() {
        assertFalse(classUnderTest.isValid(PURCHASE_JSON, sign(PURCHASE_JSON, otherKeys)))
    }

    @Test
    fun `Given a signature that is not base64 when checking then it is not valid`() {
        assertFalse(classUnderTest.isValid(PURCHASE_JSON, "not base64 !"))
    }

    @Test
    fun `Given a broken key when checking then nothing is valid`() {
        val broken = PurchaseSignatureVerifier("bm90IGEga2V5", acceptWithoutKey = true)

        assertFalse(broken.isValid(PURCHASE_JSON, sign(PURCHASE_JSON, playKeys)))
    }

    @Test
    fun `Given no key in a debug build when checking then everything is accepted`() {
        assertTrue(
            PurchaseSignatureVerifier("", acceptWithoutKey = true).isValid(PURCHASE_JSON, "")
        )
    }

    @Test
    fun `Given no key in a release build when checking then nothing is accepted`() {
        val release = PurchaseSignatureVerifier("", acceptWithoutKey = false)

        assertFalse(release.isValid(PURCHASE_JSON, sign(PURCHASE_JSON, playKeys)))
    }

    private fun keyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun sign(data: String, keys: KeyPair): String =
        Signature.getInstance("SHA1withRSA").run {
            initSign(keys.private)
            update(data.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(sign())
        }
}
