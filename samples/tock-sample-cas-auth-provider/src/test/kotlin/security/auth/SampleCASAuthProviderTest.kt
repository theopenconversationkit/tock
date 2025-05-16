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

package security.auth

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.pac4j.core.profile.CommonProfile
import org.pac4j.vertx.auth.Pac4jUser

internal class SampleCASAuthProviderTest {

    @DisplayName("Ability to read login from principal")
    @Test
    fun `Ability to read login from principal`() {
        //GIVEN
        val cas = SampleCASAuthProvider(mockk(relaxed = true))

        val profileMap = HashMap<String, CommonProfile>()
        profileMap["CasClient"] = CommonProfile()
        profileMap["CasClient"]!!.id = "123"

        val user = Pac4jUser()
        user.setUserProfiles(profileMap)

        //WHEN
        val id = cas.readCasLogin(user)

        //THEN
        assertEquals(id, "123")
    }

}
