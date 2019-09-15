package fr.vsct.tock.translator

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Locale

internal class DefaultDateTemplate(
    private val date: TemporalAccessor?,
    private val formatterProvider: DateTimeFormatterProvider) : DateTemplate {

    constructor(date: TemporalAccessor?, dateFormatter: DateTimeFormatter) :
        this(
            date,
            object : DateTimeFormatterProvider {
                override fun provide(locale: Locale): DateTimeFormatter = dateFormatter.withLocale(locale)
            })

    override fun format(locale: Locale): String {
        return date?.let {
            formatterProvider.provide(locale).format(it)
        } ?: ""
    }

    /**
     * To immediately format this date with the given locale.
     */
    internal fun formatTo(locale: Locale): TranslatedSequence = format(locale).raw

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultDateTemplate

        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        return date?.hashCode() ?: 0
    }

}