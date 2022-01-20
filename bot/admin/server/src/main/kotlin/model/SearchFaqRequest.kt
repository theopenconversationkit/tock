/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin.model

import ai.tock.nlp.admin.model.PaginatedQuery
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.FaqQuery
import ai.tock.nlp.front.shared.config.FaqStatus
import org.litote.kmongo.Id

data class SearchFaqRequest(
    val tags: List<String>,
    val search: String?,
    val enabled: Boolean,
    val user :String?,
    val allButUser: String?,
//    val applicationId: Id<ApplicationDefinition>
) : PaginatedQuery()

{
    fun toFaqQuery(request: SearchFaqRequest, faqStatus: FaqStatus): FaqQuery {
        return FaqQuery(
            request.start,
            request.size,
            request.search,
            request.searchMark,
            request.tags,
            request.enabled,
            faqStatus,
            request.user,
            request.allButUser,
            request.applicationName,
            request.namespace,
        )
    }
}