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
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import mu.KotlinLogging
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64
import org.jasypt.util.text.BasicTextEncryptor

private val logger = KotlinLogging.logger {}

private val textEncryptor: BasicTextEncryptor by lazy {
    BasicTextEncryptor()
        .apply {
            property("tock_encrypt_pass", "").apply {
                if (isBlank()) {
                    if (devEnvironment) {
                        setPassword("dev")
                    } else {
                        throw NoEncryptionPassException()
                    }
                } else {
                    setPassword(this)
                }
            }
        }
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
            MessageDigest.getInstance("SHA-256").digest(s.toByteArray(StandardCharsets.UTF_8))
        )
    )

/**
 * The OID namespace identifier, as defined in RFC-4122
 */
private const val UUID_OID_NAMESPACE = "6ba7b812-9dad-11d1-80b4-00c04fd430c8"
private val oidNamespaceBytes = UUID_OID_NAMESPACE.toByteArray()

/**
 * Generates a UUID based on a sha256 hash of the given string.
 */
fun sha256Uuid(s: String, namespace: UUID? = null): UUID {
    val digest = MessageDigest.getInstance("SHA-256").apply {
        update(namespace?.toString()?.toByteArray() ?: oidNamespaceBytes)
        update(s.toByteArray())
    }.digest()
    digest[6] = (digest[6].toInt() and 0x0f).toByte() /* clear version        */
    digest[6] = (digest[6].toInt() or  0x80).toByte() /* set to version 8     */
    digest[8] = (digest[8].toInt() and 0x3f).toByte() /* clear variant        */
    digest[8] = (digest[8].toInt() or  0x80).toByte() /* set to IETF variant  */
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
 */
fun encrypt(s: String): String {
    return textEncryptor.encrypt(s)
}

/**
 * Decrypt a string and return the result.
 */
fun decrypt(s: String): String {
    return try {
        textEncryptor.decrypt(s)
    } catch (e: Exception) {
        logger.error(e)
        s
    }
}

/**
 * Init encryption utilities.
 */
fun initEncryptor() {
    if (encryptionEnabled) {
        // warmup encryptor
        logger.info { "initialize encryptor..." }
        decrypt(encrypt("test"))
        logger.info { "encryptor initialized" }
    }
    TockObfuscatorService.loadObfuscators()
}
