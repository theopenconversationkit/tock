package fr.vsct.tock.bot.connector.slack.model


data class SlackMessageIn(val token: String,
                          val team_id: String,
                          val team_domain: String,
                          val channel_id: String,
                          val channel_name: String,
                          val timestamp: Number,
                          val user_id: String,
                          val user_name: String,
                          var text: String,
                          val trigger_word: String) : SlackConnectorMessage() {

    fun getRealMessage(): String {
        return this.text.replace("${this.trigger_word} ", "")
    }
}