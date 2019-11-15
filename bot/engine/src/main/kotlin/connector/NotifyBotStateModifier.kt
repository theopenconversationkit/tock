package ai.tock.bot.connector

/**
 * Manage bot state and notifications.
 */
enum class NotifyBotStateModifier {
    /**
     * If the bot is disabled for the user, don't send the notification.
     */
    KEEP_CURRENT_STATE,
    /**
     * Send the notification even for disabled bot, but don't reactivate the bot for the user if it is disabled.
     */
    ACTIVATE_ONLY_FOR_THIS_NOTIFICATION,
    /**
     * Send the notification and reactivate the bot for the user.
     */
    REACTIVATE
}