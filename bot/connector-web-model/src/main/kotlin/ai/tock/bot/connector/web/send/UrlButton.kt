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

package ai.tock.bot.connector.web.send

import ai.tock.bot.connector.web.HrefTargetType
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("url_button")
data class UrlButton(
    val title: String,
    val url: String,
    val imageUrl: String? = null,
    val target: String? = HrefTargetType._blank.name,
    val style: String? = ButtonStyle.primary.name,
    val windowFeatures: String? = null,
) : Button(ButtonType.web_url)
