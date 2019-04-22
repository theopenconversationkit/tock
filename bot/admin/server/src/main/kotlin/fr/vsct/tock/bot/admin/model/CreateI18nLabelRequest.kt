package fr.vsct.tock.bot.admin.model

import java.util.Locale

class CreateI18nLabelRequest(
        val label: String,
        val locale: Locale,
        val category: String
)