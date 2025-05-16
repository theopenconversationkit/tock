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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.PredefinedValue
import ai.tock.nlp.front.shared.build.ModelBuild
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration
import ai.tock.nlp.front.shared.namespace.NamespaceSharingConfiguration
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.nlp.front.shared.test.EntityTestError
import ai.tock.nlp.front.shared.test.IntentTestError
import ai.tock.nlp.front.shared.test.TestBuild
import ai.tock.nlp.front.shared.user.UserNamespace
import org.litote.jackson.data.JacksonDataRegistry
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
        PredefinedValue::class,
        DictionaryData::class,
        UserNamespace::class,
        NamespaceConfiguration::class,
    ]
)
@JacksonDataRegistry(
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
        PredefinedValue::class,
        DictionaryData::class,
        UserActionLog::class,
        UserNamespace::class,
        NamespaceConfiguration::class,
    ]
)
internal object NlpDataRegistry
