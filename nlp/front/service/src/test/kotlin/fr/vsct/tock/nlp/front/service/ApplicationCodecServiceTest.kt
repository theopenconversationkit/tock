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

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.shared.injector
import org.junit.Test
import kotlin.test.assertFalse

/**
 *
 */
class ApplicationCodecServiceTest {

    fun inject(applicationConfiguration: ApplicationConfiguration) {
        injector.inject(Kodein {
            import(Kodein.Module {
                bind<ApplicationConfiguration>() with provider { applicationConfiguration }
            })
        })
    }

    @Test
    fun importExistingAppShouldNotCreateApp() {
        val app = ApplicationDefinition("test", "namespace", _id = "id")
        val mock: ApplicationConfiguration = mock() {
            on { getApplicationByNamespaceAndName(any(), any()) } doReturn app
        }
        inject(mock)
        val dump = ApplicationDump(app)

        val report = ApplicationCodecService.import("namespace", dump)
        assertFalse(report.modified)
    }
}