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

import ai.tock.nlp.bgem3.Bgem3AwsClient
import ai.tock.nlp.bgem3.Bgem3Configuration
import software.amazon.awssdk.regions.Region
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

class Bgem3AwsClientTest {

    @Test
    fun test1() {
        val config = Bgem3Configuration(Region.EU_WEST_3,"test","application/json","sa-voyageurs-dev")
        val client = Bgem3AwsClient(config)
        val response = client.parse(Bgem3AwsClient.ParseRequest("{\n" +
                "\"inputs\"\n" +
                ":\n" +
                "\"I like you so much\"\n" +
                "}"))
                assertEquals(response.intent?.label,"POSITIVE")
        assertTrue { response.intent?.score!! > 0.99 }
    }
}
