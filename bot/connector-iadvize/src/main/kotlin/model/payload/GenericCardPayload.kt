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

package ai.tock.bot.connector.iadvize.model.payload

import ai.tock.bot.connector.iadvize.model.response.conversation.payload.genericjson.Image
import ai.tock.bot.connector.iadvize.model.response.conversation.payload.genericjson.Action

data class GenericCardPayload(val title: String? = null,
                              val text: String? = null,
                              val image: Image? = null,
                              val actions: List<Action> = mutableListOf())
    : Payload("card/content")
