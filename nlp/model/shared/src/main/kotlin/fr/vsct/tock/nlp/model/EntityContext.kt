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

package fr.vsct.tock.nlp.model

import fr.vsct.tock.nlp.core.BuildContext
import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import java.time.ZonedDateTime
import java.util.Locale

data class EntityContextKey(val applicationName: String?,
                            val intentName: String?,
                            val language: Locale,
                            val nlpEngineType: NlpEngineType,
                            val entityType: EntityType? = null,
                            val subEntities: Boolean = false) : ClassifierContextKey {
    override fun id(): String {
        return "$applicationName-$intentName-$language-${nlpEngineType.name}-${entityType?.name}-$subEntities"
    }
}

abstract class EntityContext(override val language: Locale,
                             override val engineType: NlpEngineType) : ClassifierContext<EntityContextKey> {

    override fun toString(): String {
        return key().toString()
    }

}


sealed class EntityCallContext(
        language: Locale,
        engineType: NlpEngineType,
        val referenceDate: ZonedDateTime) : EntityContext(language, engineType) {

}

class EntityCallContextForIntent(val applicationName: String,
                                 val intent: Intent,
                                 language: Locale,
                                 engineType: NlpEngineType,
                                 referenceDate: ZonedDateTime) : EntityCallContext(language, engineType, referenceDate) {

    constructor(context: CallContext, intent: Intent) : this(context.application.name, intent, context.language, context.engineType, context.referenceDate)

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, intent.name, language, engineType)
    }

}

class EntityCallContextForEntity(val entityType: EntityType,
                                 language: Locale,
                                 engineType: NlpEngineType,
                                 referenceDate: ZonedDateTime) : EntityCallContext(language, engineType, referenceDate) {

    constructor(context: CallContext, entityType: EntityType) : this(entityType, context.language, context.engineType, context.referenceDate)

    override fun key(): EntityContextKey {
        return EntityContextKey(null, null, language, engineType, entityType)
    }
}

class EntityCallContextForSubEntities(val entityType: EntityType,
                                      language: Locale,
                                      engineType: NlpEngineType,
                                      referenceDate: ZonedDateTime) : EntityCallContext(language, engineType, referenceDate) {
    override fun key(): EntityContextKey {
        return EntityContextKey(null, null, language, engineType, entityType, true)
    }
}


sealed class EntityBuildContext(
        language: Locale,
        engineType: NlpEngineType) : EntityContext(language, engineType) {

}

class EntityBuildContextForIntent(
        val applicationName: String,
        val intent: Intent,
        language: Locale,
        engineType: NlpEngineType) : EntityBuildContext(language, engineType) {

    constructor(context: BuildContext, intent: Intent) : this(context.application.name, intent, context.language, context.engineType)

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, intent.name, language, engineType)
    }

}

class EntityBuildContextForEntity(
        val entityType: EntityType,
        language: Locale,
        engineType: NlpEngineType) : EntityBuildContext(language, engineType) {
    override fun key(): EntityContextKey {
        return EntityContextKey(null, null, language, engineType, entityType)
    }
}

class EntityBuildContextForSubEntities(val entityType: EntityType,
                                       language: Locale,
                                       engineType: NlpEngineType) : EntityBuildContext(language, engineType) {
    override fun key(): EntityContextKey {
        return EntityContextKey(null, null, language, engineType, entityType, true)
    }
}
