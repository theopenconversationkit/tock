/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.security

import ai.tock.shared.devEnvironment
import ai.tock.shared.error
import ai.tock.shared.property
import ai.tock.shared.propertyExists
import mu.KotlinLogging
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
                        error("no tock_encrypt_pass set")
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
        //warmup encryptor
        logger.info { "initialize encryptor..." }
        decrypt(encrypt("test"))
        logger.info { "encryptor initialized" }
    }
    TockObfuscatorService.loadObfuscators()
}