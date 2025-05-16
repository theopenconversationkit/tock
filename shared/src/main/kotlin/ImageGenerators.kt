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

package ai.tock.shared

import io.vertx.core.MultiMap
import mu.KotlinLogging
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.util.XMLResourceDescriptor
import org.w3c.dom.Document
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.ByteArrayOutputStream
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Generates an image from a svg template.
 */
class ImageGenerator<T : Any>(
    private val svgToPngConverter: SvgToPngConverter,
    private val svgGenerator: SvgGenerator<T>
) {

    fun generate(params: T, format: ImageFormat): ByteArray {
        val document = svgGenerator.generate(params)

        return when (format) {
            ImageFormat.PNG -> svgToPngConverter.documentToPngByteArray(document)
            ImageFormat.SVG -> documentToByteArray(document)
        }
    }

    private fun documentToByteArray(document: Document): ByteArray {
        val transformer = TransformerFactory.newInstance().newTransformer()
        val outputStream = ByteArrayOutputStream()

        transformer.transform(DOMSource(document), StreamResult(outputStream))

        return outputStream.toByteArray()
    }
}

/**
 * Convert a svg image to png image.
 */
class SvgToPngConverter {
    fun documentToPngByteArray(doc: Document): ByteArray {
        val inputSvgImage = TranscoderInput(doc)
        val pngOstream = ByteArrayOutputStream()
        val outputPngImage = TranscoderOutput(pngOstream)
        val pngConverter = PNGTranscoder()
        pngConverter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 500.0f)
        pngConverter.transcode(inputSvgImage, outputPngImage)
        pngOstream.flush()
        pngOstream.close()
        return pngOstream.toByteArray()
    }
}

/**
 * Supported generation format.
 */
enum class ImageFormat(val contentType: String) {
    SVG("image/svg+xml"), PNG("image/png");

    companion object {
        fun findByCode(code: String): ImageFormat? {
            return values().firstOrNull { format -> format.name.equals(code, ignoreCase = true) }
        }
    }
}

/**
 * Generates an image from a svg file.
 */
abstract class SvgGenerator<T : Any>(resourceName: String, resourcePath: String = "/generation/") {

    private val template = "$resourcePath$resourceName.svg"

    fun generate(params: T): Document {
        val doc: Document = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createDocument(
            resource(template).toString(),
            resourceAsStream(template)
        )
        applyParamsToDocument(doc, params)

        return doc
    }

    abstract fun applyParamsToDocument(doc: Document, params: T)
}

/**
 * Provides a data instance from specified parameters.
 */
interface ImageParametersExtractor<T : Any> {

    /**
     * Returns the parameters data instance.
     * If null is returned, it means the parameters can not be extracted ([ai.tock.shared.vertx.ImageGeneratorHandler] returns 404).
     */
    fun extract(params: MultiMap): T?
}

/**
 * Load and register fonts.
 */
object FontLoader {
    private val logger = KotlinLogging.logger {}

    fun register(fontFilesNames: List<String>, fontFilesPath: String = "/generation/fonts/") {
        try {
            fontFilesNames.map { createFont(fontFilesPath, it) }.forEach {
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerAndLogFont(it)
            }
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private fun GraphicsEnvironment.registerAndLogFont(font: Font) {
        if (!registerFont(font)) {
            logger.error("font $font not loaded")
        } else {
            logger.info("font $font loaded")
        }
    }

    private fun createFont(path: String, file: String): Font {
        return Font.createFont(
            Font.TRUETYPE_FONT,
            resourceAsStream(path + file)
        )
    }
}
