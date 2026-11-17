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
package ai.tock.bot.connector.messenger.json.send

import ai.tock.bot.connector.messenger.model.send.Button
import ai.tock.bot.connector.messenger.model.send.Message
import ai.tock.bot.connector.messenger.model.send.Payload
import ai.tock.shared.jackson.Jackson2DelegateDeserializer

// The connector messages are stored in Mongo (Jackson 3 mapping) and read by the Jackson 2 deserializers of this package.

internal class MessageMongoDeserializer : Jackson2DelegateDeserializer<Message>(Message::class.java)

internal class ButtonMongoDeserializer : Jackson2DelegateDeserializer<Button>(Button::class.java)

internal class PayloadMongoDeserializer : Jackson2DelegateDeserializer<Payload>(Payload::class.java)
