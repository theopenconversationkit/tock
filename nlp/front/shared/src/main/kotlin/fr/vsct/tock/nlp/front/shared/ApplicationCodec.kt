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

package fr.vsct.tock.nlp.front.shared

import fr.vsct.tock.nlp.front.shared.codec.ApplicationDump
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import fr.vsct.tock.nlp.front.shared.codec.ApplicationImportReport
import fr.vsct.tock.nlp.front.shared.codec.DumpType

/**
 * To import and export applications, intents, entities and sentences.
 */
interface ApplicationCodec {

    /**
     * Export application dump.
     */
    fun export(applicationId: String, dumpType: DumpType): ApplicationDump

    /**
     * Analyse the dump and present options.
     */
    fun prepareImport(dump: ApplicationDump): ApplicationImportConfiguration

    /**
     * Import the dump.
     */
    fun import(
            namespace: String,
            dump: ApplicationDump,
            configuration: ApplicationImportConfiguration = ApplicationImportConfiguration()): ApplicationImportReport
}