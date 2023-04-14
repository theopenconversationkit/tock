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

package ai.tock.bot.admin.verticle

import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.shared.exception.admin.AdminException
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.vertx.toRequestHandler
import io.vertx.core.http.HttpMethod


class FileVerticle : ChildVerticle<AdminException>{

    override fun configure(parent: WebVerticle<AdminException>) {
        with(parent) {

            blockingUploadBinaryPost("/file", TockUserRole.botUser, handler = toRequestHandler { context, (fileName, bytes) ->
                val file = UploadedFilesService.uploadFile(context.organization, fileName, bytes)
                    ?: WebVerticle.badRequest("file must have an extension (ie file.png)")
                file
            })

            blocking(
                HttpMethod.GET, "/file/:id.:suffix", TockUserRole.botUser,
                handler = toRequestHandler { context ->
                    val id = context.path("id")
                    if (!id.startsWith(context.organization)) {
                        WebVerticle.unauthorized()
                    } else {
                        UploadedFilesService.downloadFile(context, id, context.path("suffix"))
                    }
                })
        }
    }

}