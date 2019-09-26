/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.engine.config

import fr.vsct.tock.bot.connector.media.MediaFileDescriptor
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.TockBotBus
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.file
import fr.vsct.tock.shared.cache.getFromCache
import fr.vsct.tock.shared.cache.putInCache
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.vertx.blocking
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.litote.kmongo.toId
import java.util.UUID

/**
 * To manage uploaded files.
 */
object UploadedFilesService {

    private const val UPLOADED_TYPE = "_uploaded"
    private val basePath = property("tock_bot_serve_files_path", "/f/")
    private val imagesTypes = setOf("png", "jpg", "jpeg", "svg", "gif")
    private val audioTypes = setOf("ogg", "mp3", "oga")
    private val videoTypes = setOf("ogv", "mp4")

    fun attachmentType(url: String): AttachmentType =
        if (url.length > 2) {
            url.substring(url.length - 3).let { suffix ->
                when {
                    imagesTypes.contains(suffix) -> AttachmentType.image
                    audioTypes.contains(suffix) -> AttachmentType.audio
                    videoTypes.contains(suffix) -> AttachmentType.video
                    else -> file
                }
            }
        } else {
            file
        }

    fun uploadFile(namespace: String, fileName: String, bytes: ByteArray): MediaFileDescriptor? {
        val id = (namespace + UUID.randomUUID().toString()).toLowerCase()
        val name = fileName.trim().toLowerCase()
        val lastDot = name.lastIndexOf(".")
        if (lastDot == -1 || lastDot == name.length - 1) {
            return null
        }

        val suffix = name.substring(lastDot + 1)
        val fileId = "$id.$suffix"
        putInCache(fileId.toId(), UPLOADED_TYPE, bytes)
        return MediaFileDescriptor(suffix, fileName, id)
    }

    fun downloadFile(context: RoutingContext, id: String, suffix: String) {
        downloadFile(context, "$id.$suffix")
    }

    internal fun botFilePath(bus: BotBus, id: String, suffix: String): String =
        (bus as? TockBotBus)?.connector?.getBaseUrl() + basePath + id + "." + suffix

    fun getFileContentFromUrl(url: String): ByteArray? =
        url.run {
            val start = url.lastIndexOf('/')
            if (start == -1) {
                null
            } else {
                getFromCache(url.substring(start+1).toId(), UPLOADED_TYPE)
            }
        }


    private fun downloadFile(context: RoutingContext, id: String) {
        val bytes: ByteArray? = getFromCache(id.toId(), UPLOADED_TYPE)
        if (bytes != null) {
            context.response().putHeader("Content-Type", guessContentType(id))
                .end(Buffer.buffer(bytes))
        } else {
            context.response().setStatusCode(404).end()
        }
    }

    fun guessContentType(fileName: String): String =
        fileName.toLowerCase().let { id ->
            when {
                id.endsWith(".png") -> "image/png"
                id.endsWith(".jpg") || id.endsWith(".jpeg") -> "image/jpeg"
                id.endsWith(".gif") -> "image/gif"
                id.endsWith(".svg") -> "image/svg+xml"
                id.endsWith(".ogg") || id.endsWith(".oga") -> "audio/ogg"
                id.endsWith(".ogv") -> "video/ogg"
                id.endsWith(".mp3") -> "audio/mpeg"
                id.endsWith(".mp4") -> "video/mp4"
                id.endsWith(".pdf") -> "application/pdf"
                id.endsWith(".zip") -> "application/zip"
                else -> "application/octet-stream"
            }
        }

    internal fun configure(): (Router) -> Unit {
        return { router ->
            router.get("$basePath*").blocking { context ->
                val id = context.request().uri().substring(basePath.length).toLowerCase()
                downloadFile(context, id)
            }
        }
    }
}