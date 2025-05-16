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

package ai.tock.bot.connector.ga.model.response

import ai.tock.shared.jackson.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 *
 */
class GAOptionValueSpecTest {

    @Test
    fun testSerializationAndDeserialization() {
        val option = GAOptionValueSpec(
            carouselSelect =
            GACarouselSelect(
                listOf(
                    GACarouselItem(
                        GAOptionInfo(
                            "key1",
                            listOf("synonym1")
                        ),
                        "titre1",
                        "description1",
                        GAImage(
                            "https://aaa.com/test.png",
                            "Image with text"
                        )
                    ),
                    GACarouselItem(
                        GAOptionInfo(
                            "key2",
                            listOf("synonym2")
                        ),
                        "titre2",
                        "description2",
                        GAImage(
                            "https://aaa.com/test2.png",
                            "Image with text"
                        )
                    )
                )
            )
        )
        val s = mapper.writeValueAsString(option)
        assertEquals(option, mapper.readValue<GAInputValueData>(s))
    }
}
