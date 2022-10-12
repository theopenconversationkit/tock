/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin

import ai.tock.bot.admin.service.ScenarioGroupService
import ai.tock.bot.admin.service.ScenarioService
import ai.tock.bot.admin.service.ScenarioVersionService
import ai.tock.bot.admin.service.StoryService
import ai.tock.bot.admin.service.impl.ScenarioGroupServiceImpl
import ai.tock.bot.admin.service.impl.ScenarioServiceImpl
import ai.tock.bot.admin.service.impl.ScenarioVersionServiceImpl
import ai.tock.bot.admin.service.impl.StoryServiceImpl
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton


val botAdminServiceModule = Kodein.Module {
    bind<ScenarioGroupService>() with singleton { ScenarioGroupServiceImpl() }
    bind<ScenarioVersionService>() with singleton { ScenarioVersionServiceImpl() }
    bind<ScenarioService>() with singleton { ScenarioServiceImpl() }
    bind<StoryService>() with singleton { StoryServiceImpl() }
}
