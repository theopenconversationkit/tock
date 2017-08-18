/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.shared

import org.jasypt.util.text.BasicTextEncryptor

/**
 *
 */
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

val encryptionEnabled: Boolean = propertyExists("tock_encrypt_pass")

fun encrypt(s: String): String {
    return textEncryptor.encrypt(s)
}

fun decrypt(s: String): String {
    return textEncryptor.decrypt(s)
}