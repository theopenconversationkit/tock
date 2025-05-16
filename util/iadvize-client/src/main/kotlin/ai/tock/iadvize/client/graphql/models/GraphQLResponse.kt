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

package ai.tock.iadvize.client.graphql.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Representation of a graphql response
 */
data class GraphQLResponse<T: Any> (
    @JsonProperty("data") val data: T,
    @JsonProperty("errors") val errors: List<GraphQLError>? = null
) {

    fun hasErrors(): Boolean = !errors.isNullOrEmpty()
    fun isSuccessful(): Boolean = !hasErrors()
}

data class GraphQLError(
    @JsonProperty("message")
    val message: String,
    @JsonProperty("path")
    val path: List<String>,
    @JsonProperty("locations")
    val locations: List<GraphQLErrorLocation>
) {
    override fun toString(): String {
        return "GraphQLError{ message='$message', path=$path, locations=$locations }"
    }
}

data class GraphQLErrorLocation(
    @JsonProperty("line")
    val line: Number,
    @JsonProperty("column")
    val column: Number
) {
    override fun toString(): String {
        return "GraphQLErrorLocation{ line=$line, column=$column }"
    }
}
