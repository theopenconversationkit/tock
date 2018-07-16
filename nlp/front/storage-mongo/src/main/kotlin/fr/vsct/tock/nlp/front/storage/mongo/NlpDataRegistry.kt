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

package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.core.PredefinedValue
import fr.vsct.tock.nlp.front.shared.build.ModelBuild
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.Classification
import fr.vsct.tock.nlp.front.shared.config.ClassifiedEntity
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.QueryContext
import fr.vsct.tock.nlp.front.shared.test.EntityTestError
import fr.vsct.tock.nlp.front.shared.test.IntentTestError
import fr.vsct.tock.nlp.front.shared.test.TestBuild
import org.litote.kmongo.DataRegistry

/**
 *
 */
@DataRegistry(
    [
        EntityTestError::class,
        ModelBuild::class,
        ModelBuildTrigger::class,
        ApplicationDefinition::class,
        Classification::class,
        ClassifiedEntity::class,
        EntityDefinition::class,
        EntityTypeDefinition::class,
        IntentDefinition::class,
        ParseQuery::class,
        ParseResult::class,
        QueryContext::class,
        IntentTestError::class,
        TestBuild::class,
        PredefinedValue::class
    ]
)
internal object NlpDataRegistry