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
package ai.tock.bot.test

import ai.tock.bot.connector.alexa.AlexaMessage
import ai.tock.bot.connector.alexa.alexaConnectorType
import ai.tock.bot.connector.ga.GAResponseConnectorMessage
import ai.tock.bot.connector.ga.gaConnectorType
import ai.tock.bot.connector.messenger.messengerConnectorType
import ai.tock.bot.connector.messenger.model.MessengerConnectorMessage
import ai.tock.bot.connector.slack.model.SlackConnectorMessage
import ai.tock.bot.connector.slack.slackConnectorType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import ai.tock.bot.connector.whatsapp.whatsAppConnectorType

/**
 * The Messenger message if any.
 */
fun BotBusMockLog.messenger(): MessengerConnectorMessage? = message(messengerConnectorType) as? MessengerConnectorMessage

/**
 * The Google Assistant message if any.
 */
fun BotBusMockLog.ga(): GAResponseConnectorMessage? = message(gaConnectorType) as? GAResponseConnectorMessage

/**
 * The Slack message if any.
 */
fun BotBusMockLog.slack(): SlackConnectorMessage? = message(slackConnectorType) as? SlackConnectorMessage

/**
 * The Alexa message if any.
 */
fun BotBusMockLog.alexa(): AlexaMessage? = message(alexaConnectorType) as? AlexaMessage

/**
 * The WhatsApp message if any.
 */
fun BotBusMockLog.whatsapp(): WhatsAppBotMessage? = message(whatsAppConnectorType) as? WhatsAppBotMessage
