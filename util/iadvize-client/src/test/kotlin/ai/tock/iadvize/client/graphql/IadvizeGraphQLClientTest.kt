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

package ai.tock.iadvize.client.graphql

import ai.tock.iadvize.client.DataNotFoundError
import ai.tock.iadvize.client.IadvizeApi
import ai.tock.iadvize.client.NotSuccessResponseError
import ai.tock.iadvize.client.graphql.models.GraphQLError
import ai.tock.iadvize.client.graphql.models.GraphQLErrorLocation
import ai.tock.iadvize.client.graphql.models.GraphQLResponse
import ai.tock.iadvize.client.graphql.models.customData.CustomDataResult
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRule
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRuleAvailability
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRuleChannelAvailability
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRuleResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class IadvizeGraphQLClientTest {
    private val distributionRule = ""
    private val routingRuleResponse: Response<GraphQLResponse<RoutingRuleResult>> = mockk(relaxed = true)
    private val routingRuleErrorResponse: Response<GraphQLResponse<RoutingRuleResult>> = mockk(relaxed = true)
    private val customDataResponse: Response<GraphQLResponse<CustomDataResult>> = mockk(relaxed = true)
    private val api: IadvizeApi = mockk(relaxed = true)
    private val client = IadvizeGraphQLClient()

    @BeforeEach
    fun setUp() {
        every { api.checkAvailability(any()).execute() } returns routingRuleResponse
        every { api.getCustomData(any()).execute() } returns customDataResponse

        every { routingRuleResponse.isSuccessful } returns true
        every { customDataResponse.isSuccessful } returns true

        every { routingRuleResponse.body() } returns
            GraphQLResponse(
                data =
                    RoutingRuleResult(
                        RoutingRule(
                            RoutingRuleAvailability(RoutingRuleChannelAvailability(true)),
                        ),
                    ),
            )

        every { routingRuleErrorResponse.body() } returns
            GraphQLResponse(
                data = RoutingRuleResult(null),
                errors =
                    listOf(
                        GraphQLError(
                            "error1",
                            listOf("path1"),
                            listOf(GraphQLErrorLocation(1, 1)),
                        ),
                        GraphQLError(
                            "error2",
                            listOf("path2"),
                            listOf(GraphQLErrorLocation(1, 1)),
                        ),
                    ),
            )

        client.iadvizeApi = api
    }

    @Test
    fun `check availability`() {
        val available = client.isAvailable(distributionRule)
        assertTrue(available)
    }

    @Test
    fun `check availability with an unsuccessful api response`() {
        every { api.checkAvailability(any()).execute() } returns routingRuleErrorResponse
        every { routingRuleErrorResponse.code() } returns 400
        val error =
            assertThrows(NotSuccessResponseError::class.java) {
                client.isAvailable(distributionRule)
            }

        val expectedMessage =
            """
            code : 400 
            errors:[
             GraphQLError{ message='error1', path=[path1], locations=[GraphQLErrorLocation{ line=1, column=1 }] },
             GraphQLError{ message='error2', path=[path2], locations=[GraphQLErrorLocation{ line=1, column=1 }] } 
            ]
            """.trimIndent()

        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun `check availability with a null api response`() {
        every { routingRuleResponse.body() } returns null
        assertThrows(DataNotFoundError::class.java) {
            client.isAvailable(distributionRule)
        }
    }
}
