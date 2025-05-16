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

package ai.tock.bot.xray

import ai.tock.shared.jackson.addSerializer
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
object XrayConfiguration {

    private const val DEFAULT_XRAY_URL = "please set xray url"
    internal val xrayUrl: String = property("tock_bot_test_xray_url", DEFAULT_XRAY_URL)

    @Volatile
    private var configured: Boolean = false

    fun isXrayAvailable(): Boolean {
        return DEFAULT_XRAY_URL != xrayUrl
    }

    fun configure() {
        if (!configured) {
            configured = true
            mapper.registerModule(
                SimpleModule()
                    .addSerializer(
                        OffsetDateTime::class,
                        object : OffsetDateTimeSerializer(
                            INSTANCE,
                            true,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZ")
                        ) {
                        }
                    )
            )
        }
    }
}
