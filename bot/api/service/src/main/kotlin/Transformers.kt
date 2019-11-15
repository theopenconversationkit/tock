/*
 * Copyright (C) 2017/2019 VSCT
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

package ai.tock.bot.api.service

import ai.tock.bot.api.model.RequestContext
import ai.tock.bot.api.model.UserRequest
import ai.tock.bot.api.model.context.Entity
import ai.tock.bot.api.model.context.UserData
import ai.tock.bot.api.model.message.user.Choice
import ai.tock.bot.api.model.message.user.Text
import ai.tock.bot.api.model.message.user.UserMessage
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.dialog.EntityValue
import ai.tock.bot.engine.user.UserPreferences

internal fun BotBus.toUserRequest(): UserRequest =
    UserRequest(
        intent?.wrappedIntent()?.name,
        entities
            .values
            .mapNotNull { it.value }
            .map { it.toEntity(this) },
        action.toApiMessage(),
        story.definition.id,
        step?.name,
        toRequestContext())

internal fun EntityValue.toEntity(bus: BotBus): Entity =
    Entity(
        entity.entityType.name,
        entity.role,
        content,
        value,
        evaluated,
        subEntities.map { it.toEntity(bus) },
        bus.hasActionEntity(entity.role)
    )

private fun Action.toApiMessage(): UserMessage =
    when (this) {
        is SendSentence -> stringText?.let { Text(it) } ?: error("no text in $this")
        is SendChoice -> Choice(toEncodedId())
        else -> error("unsupported action $this")
    }

private fun BotBus.toRequestContext(): RequestContext =
    RequestContext(
        botDefinition.namespace,
        userLocale,
        targetConnectorType,
        userInterfaceType,
        applicationId,
        userId,
        botId,
        userPreferences.toUserData()
    )

private fun UserPreferences.toUserData(): UserData =
    UserData(
        firstName,
        lastName,
        email,
        timezone,
        locale,
        picture,
        gender,
        test
    )
