/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.security

import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.property
import ai.tock.shared.propertyExists
import mu.KotlinLogging
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64
import org.jasypt.util.text.BasicTextEncryptor
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64 as JavaBase64

private val logger = KotlinLogging.logger {}

/**
 * Resolve the "tock_encrypt_pass" passphrase, falling back to a fixed dev value in dev environment,
 * or failing loudly otherwise.
 */
private fun encryptPassphrase(): String =
    property("tock_encrypt_pass", "").ifBlank {
        if (devEnvironment) "dev" else throw NoEncryptionPassException()
    }

private val textEncryptor: BasicTextEncryptor by lazy {
    BasicTextEncryptor().apply { setPassword(encryptPassphrase()) }
}

private const val SHA_256_ALGORITHM = "SHA-256"
private const val AES_ALGORITHM = "AES"

class AesGcmCipher(
    key: ByteArray,
    private val base64Encoder: JavaBase64.Encoder = JavaBase64.getEncoder(),
    private val base64Decoder: JavaBase64.Decoder = JavaBase64.getDecoder(),
) {
    companion object {
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val PASSPHRASE_HASH_ALGORITHM = SHA_256_ALGORITHM
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    }

    constructor(passphrase: String) : this(
        key =
            MessageDigest
                .getInstance(PASSPHRASE_HASH_ALGORITHM)
                .digest(passphrase.toByteArray(StandardCharsets.UTF_8)),
    )

    private val aesGcmKey: SecretKeySpec by lazy {
        SecretKeySpec(key, AES_ALGORITHM)
    }
    private val random = SecureRandom()

    fun encrypt(s: String): String {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { random.nextBytes(it) }
        val cipher =
            Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, aesGcmKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
        val encrypted = cipher.doFinal(s.toByteArray(StandardCharsets.UTF_8))
        return base64Encoder.encodeToString(iv + encrypted)
    }

    fun decryptOrNull(s: String): String? =
        try {
            decryptOrThrow(s)
        } catch (e: Exception) {
            logger.debug(e) { "AES-GCM decrypt failed" }
            null
        }

    fun decryptOrThrow(s: String): String {
        val data = base64Decoder.decode(s)
        require(data.size > GCM_IV_LENGTH_BYTES) { "Ciphertext too short: ${data.size} bytes" }
        val iv = data.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val cipherText = data.copyOfRange(GCM_IV_LENGTH_BYTES, data.size)
        val cipher =
            Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, aesGcmKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
        return String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
    }
}

val aesGcmCipher by lazy {
    AesGcmCipher(encryptPassphrase())
}

/**
 * Is encryption enabled?
 */
val encryptionEnabled: Boolean = propertyExists("tock_encrypt_pass")

/**
 * Hash with sha256.
 */
fun shaS256(s: String): String =
    String(
        Base64.encodeBase64Chunked(
            MessageDigest.getInstance(SHA_256_ALGORITHM).digest(s.toByteArray(StandardCharsets.UTF_8)),
        ),
    )

/**
 * The OID namespace identifier, as defined in RFC-4122
 */
private const val UUID_OID_NAMESPACE = "6ba7b812-9dad-11d1-80b4-00c04fd430c8"
private val oidNamespaceBytes = UUID_OID_NAMESPACE.toByteArray()

/**
 * Generates a UUID based on a sha256 hash of the given string.
 */
fun sha256Uuid(
    s: String,
    namespace: UUID? = null,
): UUID {
    val digest =
        MessageDigest
            .getInstance(SHA_256_ALGORITHM)
            .apply {
                update(namespace?.toString()?.toByteArray() ?: oidNamespaceBytes)
                update(s.toByteArray())
            }.digest()
    digest[6] = (digest[6].toInt() and 0x0f).toByte() // clear version
    digest[6] = (digest[6].toInt() or 0x80).toByte() // set to version 8
    digest[8] = (digest[8].toInt() and 0x3f).toByte() // clear variant
    digest[8] = (digest[8].toInt() or 0x80).toByte() // set to IETF variant
    return uuidFromBytes(digest)
}

private fun uuidFromBytes(data: ByteArray): UUID {
    require(data.size >= 16) {
        "data must be at least 16 bytes in length, was ${data.size}"
    }

    // Based on the private UUID(bytes[]) constructor
    var msb: Long = 0
    var lsb: Long = 0
    for (i in 0..7) msb = (msb shl 8) or (data[i].toInt() and 0xff).toLong()
    for (i in 8..15) lsb = (lsb shl 8) or (data[i].toInt() and 0xff).toLong()
    return UUID(msb, lsb)
}

/**
 * Encrypt a string and return the result.
 *
 * Consider using [encryptAesGcm] for security-sensitive purposes.
 */
fun encrypt(s: String): String = textEncryptor.encrypt(s)

/**
 * Decrypt a string and return the result.
 *
 * Consider using [decryptAesGcm] for security-sensitive purposes.
 */
fun decrypt(s: String): String =
    try {
        textEncryptor.decrypt(s)
    } catch (e: Exception) {
        logger.error(e)
        s
    }

/**
 * Encrypt a string with AES-256-GCM (authenticated encryption: tampering with the result is detected on decryption)
 * and return the result as a Base64 string (random IV + ciphertext + auth tag).
 *
 * @see decryptAesGcm
 */
fun encryptAesGcm(s: String): String = aesGcmCipher.encrypt(s)

/**
 * Decrypt a string encrypted with [encryptAesGcm].
 * Returns null if the input is malformed, was tampered with, or was encrypted/signed with a different key
 * (authentication tag verification failure).
 */
fun decryptAesGcm(s: String): String? = aesGcmCipher.decryptOrNull(s)

/**
 * Init encryption utilities.
 */
fun initEncryptor() {
    if (encryptionEnabled) {
        // warmup encryptor
        logger.info { "initialize encryptor..." }
        decrypt(encrypt("test"))
        check(decryptAesGcm(encryptAesGcm("test")) == "test") { "AES-GCM encryptor warmup failed" }
        logger.info { "encryptor initialized" }
    }
    TockObfuscatorService.loadObfuscators()
}
