package ai.tock.bot.engine.dialog

import ai.tock.bot.engine.BotBus
import ai.tock.shared.Loader

@Volatile
private var internalRestartListeners: List<RestartListener>? = null

internal val restartListeners: List<RestartListener>
    get() =
        internalRestartListeners
            ?: Loader.loadServices<RestartListener>().also { internalRestartListeners = it }

/**
 * Called when a user restarts the conversation (see [ai.tock.bot.engine.nlp.BuiltInKeywordListener.restartKeyword]).
 *
 * Implementations are loaded with the [java.util.ServiceLoader]: declare them in
 * `META-INF/services/ai.tock.bot.engine.dialog.RestartListener`.
 *
 * Use it to reset the custom state of the bot (for instance, data stored outside the dialog). Do not send messages:
 * the answer to the user is sent by the restart handler.
 */
interface RestartListener {
    /**
     * Called after a new [Dialog] has been started for the user.
     * [previousDialog] is the dialog of the conversation that has just been closed.
     */
    fun onRestart(
        bus: BotBus,
        previousDialog: Dialog,
    )
}
