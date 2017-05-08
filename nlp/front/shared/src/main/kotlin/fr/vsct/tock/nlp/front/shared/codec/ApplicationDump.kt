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

package fr.vsct.tock.nlp.front.shared.codec

import fr.vsct.tock.nlp.front.shared.codec.DumpType.full
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant

/**
 * An application dump.
 */
data class ApplicationDump(val application: ApplicationDefinition,
                           val entityTypes: List<EntityTypeDefinition> = emptyList(),
                           val intents: List<IntentDefinition> = emptyList(),
                           val sentences: List<ClassifiedSentence> = emptyList(),
                           val dumpType: DumpType = full,
                           val timestamp: Instant = Instant.now()) {

}