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

const val ROUTING_RULE_QUERY_NAME = "RoutingRule"
const val ROUTING_RULE_QUERY = """
     query $ROUTING_RULE_QUERY_NAME(${'$'}id: UUID!) {
         routingRule(id: ${'$'}id) {
            availability {
               chat {
                 isAvailable
               }
            }
        }
     }
"""

const val CUSTOM_DATA_QUERY_NAME = "VisitorConversationCustomData"
const val CUSTOM_DATA_QUERY = """
    query $CUSTOM_DATA_QUERY_NAME(${'$'}conversationId: UUID!) {
        visitorConversationCustomData(conversationId: ${'$'}conversationId) {
            customData {
             ... on VisitorConversationCustomDataEntryString {
                 label
                 key,
                 stringValue
                }
            }
        }          
    }
"""
