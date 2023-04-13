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

package security.auth.cas

import ai.tock.shared.vertx.vertx
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.pac4j.vertx.auth.Pac4jUser
import kotlin.test.assertEquals

internal class SampleCASAuthProviderTest {

    @DisplayName("Ability to read userId attribute from principal")
    @Test
    fun `Ability to read userId attribute from principal`() {
        //GIVEN
        val cas = SampleCASAuthProvider(vertx)

        val profileMap = HashMap<String, UserProfile>()
        profileMap["CasClient"] = CommonProfile(true)
        profileMap["CasClient"]!!.id = "123"
        profileMap["CasClient"]!!.addAttribute("userId", "123")

        val user = Pac4jUser()
        user.setUserProfiles(profileMap)

        //WHEN
        val id = cas.readCasLogin(user)

        //THEN
        assertEquals(id, "123")
    }

}
