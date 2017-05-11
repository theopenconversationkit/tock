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

package fr.vsct.tock.nlp.front.service

import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class ParserServiceTest {

    @Test
    fun formatQuery_shouldRemoveAllTabsAndCarriage() {
        val parsed = ParserService.formatQuery("a \r d\n \td")
        assertEquals("a  d d", parsed)
        println("a  d d")
    }

    @Test
    fun formatQuery_shouldTrim() {
        val parsed = ParserService.formatQuery(" a \r d\n \t ")
        assertEquals("a  d", parsed)
    }
}