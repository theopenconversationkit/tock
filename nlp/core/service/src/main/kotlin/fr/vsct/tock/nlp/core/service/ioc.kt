/*
 * Copyright (C) 2017 VSCT
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

package fr.vsct.tock.nlp.core.service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.core.service.entity.EntityCore
import fr.vsct.tock.nlp.core.service.entity.EntityCoreService
import fr.vsct.tock.nlp.core.service.entity.EntityMerge
import fr.vsct.tock.nlp.core.service.entity.EntityMergeService

val coreModule = Kodein.Module {
    bind<NlpCore>() with provider { NlpCoreService }

    //internal bindings
    bind<EntityCore>() with provider { EntityCoreService }
    bind<EntityMerge>() with provider { EntityMergeService }
}