/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class User(
    val id: String,
    @JsonProperty("created_timestamp") val created: Long,
    val name: String,
    @JsonProperty("screen_name") val screenName: String,
    val protected: Boolean,
    val verified: Boolean,
    @JsonProperty("followers_count") val followersCount: Int,
    @JsonProperty("friends_count") val friendsCount: Int,
    @JsonProperty("statuses_count") val statusesCount: Int,
    @JsonProperty("profile_image_url") val profileImageUrl: String,
    @JsonProperty("profile_image_url_https") val profileImageUrlHttps: String
)