package com.klodit.almizan.util

import android.util.Base64
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object E2EECryptoManager {

    /**
     * 1. Generates an AES-256 key and encrypts the JSON financial payload.
     * Packages the IV and ciphertext into a JSON envelope mimicking the web client.
     */
    fun encryptFinancialOffer(jsonContent: String, cacheDir: File): File {
        // Generate AES-256 Key
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, SecureRandom())
        val secretKey: SecretKey = keyGen.generateKey()

        // Generate 12-byte IV for GCM
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        // Encrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(jsonContent.toByteArray(Charsets.UTF_8))

        // Create the envelope exactly like Next.js `buildFinancialCiphertextFile`
        val jsonEnvelope = JSONObject().apply {
            put("alg", "AES-256-GCM")
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        }

        // Save to temp file
        val tempFile = File.createTempFile("offre-financiere", ".enc", cacheDir)
        FileOutputStream(tempFile).use { fos ->
            fos.write(jsonEnvelope.toString().toByteArray(Charsets.UTF_8))
        }
        
        return tempFile
    }

    /**
     * 2. Computes the SHA-256 Hash of a File (Used for both Tech and Fin files)
     */
    fun computeSha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    data class EcdsaProof(val signatureBase64: String, val publicKeyPem: String)

    /**
     * 3. Generates an ECDSA P-384 KeyPair, signs the hash, and exports the Public Key to PEM.
     */
    fun generateEcdsaSignature(hashHex: String): EcdsaProof {
        // Generate EC P-384 KeyPair
        val keyPairGen = KeyPairGenerator.getInstance("EC")
        keyPairGen.initialize(ECGenParameterSpec("secp384r1"), SecureRandom())
        val keyPair = keyPairGen.generateKeyPair()

        // Sign the Hash
        val signatureAlg = Signature.getInstance("SHA384withECDSA")
        signatureAlg.initSign(keyPair.private)
        signatureAlg.update(hashHex.toByteArray(Charsets.UTF_8))
        val signatureBytes = signatureAlg.sign()

        // Export Public Key to PEM
        val pubKeyBytes = keyPair.public.encoded
        val pubKeyBase64 = Base64.encodeToString(pubKeyBytes, Base64.NO_WRAP)
        val pemLines = pubKeyBase64.chunked(64).joinToString("\n")
        val pem = "-----BEGIN PUBLIC KEY-----\n$pemLines\n-----END PUBLIC KEY-----"

        return EcdsaProof(
            signatureBase64 = Base64.encodeToString(signatureBytes, Base64.NO_WRAP),
            publicKeyPem = pem
        )
    }
}