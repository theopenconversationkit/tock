package fr.vsct.tock.bot.connector.ga.model.response

/**
 * @see https://developers.google.com/actions/reference/rest/Shared.Types/LineItemType
 */
enum class GALineItemType {
    UNSPECIFIED,
    REGULAR,
    TAX,
    DISCOUNT,
    GRATUITY,
    DELIVERY,
    SUBTOTAL,
    FEE
}