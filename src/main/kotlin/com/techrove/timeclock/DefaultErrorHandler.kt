package com.techrove.timeclock

import mu.KotlinLogging
import org.sqlite.date.ExceptionUtils
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger("Exception")

/**
 * Custom UncaughtExceptionHandler
 */
class DefaultErrorHandler : Thread.UncaughtExceptionHandler {

    class ErrorEvent(val thread: Thread, val error: Throwable) {
        internal var consumed = false
        fun consume() {
            consumed = true
        }
    }

    companion object {
        // By default, all error messages are shown. Override to decide if certain errors should be handled another way.
        // Call consume to avoid error dialog.
        var filter: (ErrorEvent) -> Unit = {
            // restart app for uncaught exception
            exitProcess(0)
        }
    }

    override fun uncaughtException(t: Thread, error: Throwable) {
        logger.error(error) { "Uncaught error $error" }

        if (isCycle(error)) {
            logger.info { "Detected cycle handling error, aborting. $error" }
        } else {
            val event = ErrorEvent(t, error)
            filter(event)

            if (!event.consumed) {
                event.consume()
            }
        }

    }

    private fun isCycle(error: Throwable) = error.stackTrace.any {
        it.className.startsWith("${javaClass.name}\$uncaughtException$")
    }
}
