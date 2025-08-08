package ai.tock.bot.connector.googlechat

import org.commonmark.node.*
import org.commonmark.parser.Parser

/**
 * Converts standard Markdown to Google Chat format.
 *
 * Supports:
 * - Headings (converted to bold text)
 * - Formatted text (bold, italic, inline code)
 * - Lists (all converted to simple bullet points)
 * - Links (simple text extraction)
 * - Code blocks
 * - Paragraphs and line breaks
 */
object GoogleChatMarkdown {

    private val parser: Parser = Parser.builder().build()

    fun toGoogleChat(markdown: String): String {
        if (markdown.isBlank()) return ""

        val document = parser.parse(markdown)
        val converter = GoogleChatConverter()
        document.accept(converter)

        return converter.result()
    }

    /**
     * Visitor that converts CommonMark AST to Google Chat format.
     */
    private class GoogleChatConverter : AbstractVisitor() {
        private val output = StringBuilder()
        private var isInListItem = false

        fun result(): String = output.toString().trimEnd()

        override fun visit(heading: Heading) {
            output.append('*')
            visitChildren(heading)
            output.append('*')

            // H1 has more spacing than other heading levels
            if (heading.level == 1) {
                output.append("\n\n")
            } else {
                output.append('\n')
            }
        }

        override fun visit(paragraph: Paragraph) {
            visitChildren(paragraph)

            // In a list, only add a single line break
            if (isInListItem) {
                output.append('\n')
            } else {
                output.append("\n\n")
            }
        }

        override fun visit(bulletList: BulletList) {
            visitChildren(bulletList)
            output.append('\n')
        }

        override fun visit(orderedList: OrderedList) {
            visitChildren(orderedList)
            output.append('\n')
        }

        override fun visit(listItem: ListItem) {
            output.append("* ")

            val wasInListItem = isInListItem
            isInListItem = true
            visitChildren(listItem)
            isInListItem = wasInListItem
        }

        override fun visit(fencedCodeBlock: FencedCodeBlock) {
            output.append("```")
            output.append(fencedCodeBlock.literal.trimEnd())
            output.append("```\n\n")
        }

        override fun visit(indentedCodeBlock: IndentedCodeBlock) {
            output.append("```")
            output.append(indentedCodeBlock.literal.trimEnd())
            output.append("```\n\n")
        }

        override fun visit(strongEmphasis: StrongEmphasis) {
            output.append('*')
            visitChildren(strongEmphasis)
            output.append('*')
        }

        override fun visit(emphasis: Emphasis) {
            output.append('_')
            visitChildren(emphasis)
            output.append('_')
        }

        override fun visit(code: Code) {
            output.append('`')
            output.append(code.literal)
            output.append('`')
        }

        override fun visit(link: Link) {
            val linkText = extractLinkText(link)

            output.append('<')
                .append(link.destination)
                .append('|')
                .append(linkText.ifBlank { link.destination })
                .append('>')
        }

        override fun visit(text: Text) {
            output.append(text.literal)
        }

        override fun visit(softLineBreak: SoftLineBreak) {
            output.append('\n')
        }

        override fun visit(hardLineBreak: HardLineBreak) {
            output.append('\n')
        }

        private fun extractLinkText(link: Link): String {
            val textBuilder = StringBuilder()

            link.accept(object : AbstractVisitor() {
                override fun visit(text: Text) {
                    textBuilder.append(text.literal)
                }
            })

            return textBuilder.toString()
        }
    }
}