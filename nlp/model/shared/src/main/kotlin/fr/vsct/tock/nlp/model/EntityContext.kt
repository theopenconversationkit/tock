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
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.quality.TestContext
import fr.vsct.tock.nlp.core.sample.SampleExpression
import java.time.ZonedDateTime
import java.util.Locale

data class EntityContextKey(
    override val applicationName: String,
    val intentName: String?,
    val language: Locale,
    override val engineType: NlpEngineType,
    val entityType: EntityType? = null,
    val subEntities: Boolean = false
) : ClassifierContextKey {
    override fun id(): String {
        return "$applicationName-$intentName-$language-${engineType.name}-${entityType?.name}-$subEntities"
    }
}

abstract class EntityContext(
    override val language: Locale,
    override val engineType: NlpEngineType,
    override val applicationName: String
) : ClassifierContext<EntityContextKey> {

    override fun toString(): String {
        return key().toString()
    }

}


sealed class EntityCallContext(
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String,
    val referenceDate: ZonedDateTime
) : EntityContext(language, engineType, applicationName) {

}

class EntityCallContextForIntent(
    val intent: Intent,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String,
    referenceDate: ZonedDateTime
) : EntityCallContext(language, engineType, applicationName, referenceDate) {

    constructor(context: CallContext, intent: Intent) : this(
        intent,
        context.language,
        context.engineType,
        context.application.name,
        context.evaluationContext.referenceDate
    )

    constructor(context: TestContext, intent: Intent) : this(context.callContext, intent)

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, intent.name, language, engineType)
    }

}

class EntityCallContextForEntity(
    val entityType: EntityType,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String,
    referenceDate: ZonedDateTime
) : EntityCallContext(language, engineType, applicationName, referenceDate) {

    constructor(context: CallContext, entity: Entity) :
            this(
                entity.entityType,
                context.language,
                context.engineType,
                context.application.name,
                context.evaluationContext.referenceDateForEntity(entity)
            )

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, null, language, engineType, entityType)
    }
}

class EntityCallContextForSubEntities(
    val entityType: EntityType,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String,
    referenceDate: ZonedDateTime
) : EntityCallContext(language, engineType, applicationName, referenceDate) {

    constructor(entityType: EntityType, context: EntityCallContext) : this(
        entityType, context.language, context.engineType, context.applicationName, context.referenceDate
    )

    override fun key(): EntityContextKey {
        return EntityContextKey(
            applicationName,
            null,
            language,
            engineType,
            entityType,
            true
        )
    }
}


sealed class EntityBuildContext(
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String
) : EntityContext(language, engineType, applicationName) {

    /**
     * Returns only expression valid for this context
     */
    abstract fun selectValid(expressions: List<SampleExpression>): List<SampleExpression>
}

class EntityBuildContextForIntent(
    val intent: Intent,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String
) : EntityBuildContext(language, engineType, applicationName) {

    constructor(context: BuildContext, intent: Intent) : this(
        intent,
        context.language,
        context.engineType,
        context.application.name
    )

    constructor(context: CallContext, intent: Intent) : this(
        intent,
        context.language,
        context.engineType,
        context.application.name
    )

    constructor(context: TestContext, intent: Intent) : this(context.callContext, intent)

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, intent.name, language, engineType)
    }

    override fun selectValid(expressions: List<SampleExpression>): List<SampleExpression> {
        val result = expressions.filter { it.intent == intent }
        //returns empty list if no expression contains at least one expression
        return if (result.any { it.entities.isNotEmpty() }) result else emptyList()
    }
}

class EntityBuildContextForEntity(
    val entityType: EntityType,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String
) : EntityBuildContext(language, engineType, applicationName) {
    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, null, language, engineType, entityType)
    }

    override fun selectValid(expressions: List<SampleExpression>): List<SampleExpression> {
        return expressions
            .asSequence()
            .filter { it.containsEntityType(entityType) }
            .map { it.copy(entities = it.entities.filter { e -> e.isType(entityType) }) }
            .toList()
    }
}

class EntityBuildContextForSubEntities(
    val entityType: EntityType,
    language: Locale,
    engineType: NlpEngineType,
    applicationName: String
) : EntityBuildContext(language, engineType, applicationName) {

    constructor(context: BuildContext, entityType: EntityType)
            : this(entityType, context.language, context.engineType, context.application.name)

    override fun key(): EntityContextKey {
        return EntityContextKey(applicationName, null, language, engineType, entityType, true)
    }

    override fun selectValid(expressions: List<SampleExpression>): List<SampleExpression> {
        return expressions
            .filter { it.containsEntityType(entityType) }
            .flatMap { sample ->
                sample
                    .entities
                    .asSequence()
                    .filter { it.isType(entityType) }
                    .map {
                        SampleExpression(
                            it.textValue(sample.text),
                            sample.intent,
                            it.subEntities,
                            sample.context
                        )
                    }
                    .toList()
            }
    }
}
