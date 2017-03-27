package fr.vsct.tock.shared

import mu.KLogger

/**
 *
 */
fun KLogger.error(throwable: Throwable) = error(throwable.message ?: "", throwable)
