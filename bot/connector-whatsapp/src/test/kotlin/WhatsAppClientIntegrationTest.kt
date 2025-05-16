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

package ai.tock.bot.connector.whatsapp

import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.stt.STT
import ai.tock.stt.google.googleSTTModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.mockk
import org.junit.jupiter.api.Test

/**
 *
 */
class WhatsAppClientIntegrationTest {

    @Test
    fun `translate voice`() {
        val client = WhatsAppClient(
            property("url", "none"),
            "admin",
            property("password", "password")
        )
        val bytes = client.getMedia("9b1b92f8-4ba9-4132-8e5b-872483453614")!!
        val injector = KodeinInjector().apply {
            inject(
                Kodein {
                    import(
                        Kodein.Module {
                            bind<STT>() with provider { mockk<STT>() }
                        }
                    )
                    import(googleSTTModule, true)
                }
            )
        }
        val translate = injector.provide<STT>().parse(bytes)
        println(translate)
    }
}
