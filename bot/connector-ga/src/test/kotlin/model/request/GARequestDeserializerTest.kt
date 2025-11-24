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

package ai.tock.bot.connector.ga.model.request

import ai.tock.shared.jackson.mapper
import ai.tock.shared.resource
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 */
class GARequestDeserializerTest {
    @Test
    fun testGARequestDeserializer() {
        val json = resource("/request.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

    @Test
    fun testGARequest2Deserializer() {
        val json = resource("/request2.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

    @Test
    fun testGARequest3Deserializer() {
        val json = resource("/request3.json")
        val request: GARequest = mapper.readValue(json)
        assertNotNull(request)
    }

    @Test
    fun testGARequestWithLocationDeserializer() {
        val json = resource("/request_with_location.json")
        val request: GARequest = mapper.readValue(json)
        assertEquals(
            GARequest(
                user = GAUser(profile = null, accessToken = null, permissions = null, locale = "en-US", userVerificationStatus = GAUserVerificationStatus.UNKNOWN),
                device =
                    GADevice(
                        location =
                            GALocation(
                                coordinates =
                                    GALatLng(
                                        latitude = 37.4219806,
                                        longitude = -122.0841979,
                                    ),
                                formattedAddress = null,
                                zipCode = null,
                                city = null,
                                postalAddress = null,
                                name = null,
                                phoneNumber = null,
                                notes = null,
                            ),
                    ),
                surface =
                    GASurface(
                        capabilities =
                            listOf(
                                GACapability(name = "actions.capability.AUDIO_OUTPUT"),
                                GACapability(name = "actions.capability.SCREEN_OUTPUT"),
                            ),
                    ),
                conversation =
                    GAConversation(
                        conversationId = "1505388822587",
                        type = GAConversationType.NEW,
                        conversationToken = null,
                    ),
                inputs =
                    listOf(
                        GAInput(
                            rawInputs = listOf(GARawInput(createTime = null, inputType = GAInputType.VOICE, query = "yes")),
                            intent = "actions.intent.PERMISSION",
                            arguments =
                                listOf(
                                    GAArgument(
                                        name = "PERMISSION",
                                        rawText = null,
                                        boolValue = null,
                                        textValue = "true",
                                        datetimeValue = null,
                                        extension = null,
                                    ),
                                ),
                        ),
                    ),
                isInSandbox = true,
                availableSurfaces = emptyList(),
            ),
            request,
        )
    }

    @Test
    fun testGARequestWithNewSurfaceDeserializer() {
        val json = resource("/request_with_new_surface.json")
        val request: GARequest = mapper.readValue(json)
        assertEquals(
            GARequest(
                user = GAUser(profile = null, accessToken = null, permissions = null, locale = "en-US"),
                device = GADevice(),
                surface =
                    GASurface(
                        capabilities =
                            listOf(
                                GACapability(name = "actions.capability.SCREEN_OUTPUT"),
                                GACapability(name = "actions.capability.AUDIO_OUTPUT"),
                                GACapability(name = "actions.capability.WEB_BROWSER"),
                            ),
                    ),
                conversation =
                    GAConversation(
                        conversationId = "1505388822587",
                        type = GAConversationType.NEW,
                        conversationToken = null,
                    ),
                inputs =
                    listOf(
                        GAInput(
                            rawInputs = listOf(),
                            intent = "new_surface_intent",
                            arguments =
                                listOf(
                                    GAArgument(
                                        name = "NEW_SURFACE",
                                        rawText = null,
                                        boolValue = null,
                                        textValue = null,
                                        datetimeValue = null,
                                        extension = GANewSurfaceValue(GANewSurfaceStatus.OK),
                                    ),
                                ),
                        ),
                    ),
                isInSandbox = true,
                availableSurfaces =
                    listOf(
                        GASurface(
                            listOf(
                                GACapability(name = "actions.capability.SCREEN_OUTPUT"),
                                GACapability(name = "actions.capability.AUDIO_OUTPUT"),
                                GACapability(name = "actions.capability.WEB_BROWSER"),
                            ),
                        ),
                    ),
            ),
            request,
        )
    }
}
