package ai.tock.bot.connector.teams.messages

internal object MarkdownHelper {

    fun activeLink(answer: String?): String? {
        val words = answer?.split(" ") ?: return null
        val wordsWithActiveLink = mutableListOf<String>()
        words.forEach { word ->
            if (word.startsWith("http://") || word.startsWith("https://")) {
                wordsWithActiveLink.add("[$word]($word)")
            } else {
                wordsWithActiveLink.add(word)
            }
        }
        return wordsWithActiveLink.joinToString(" ")
    }
}