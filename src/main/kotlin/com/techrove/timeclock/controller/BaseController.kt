package com.techrove.timeclock.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import tornadofx.Controller

private val logger = KotlinLogging.logger("BaseController")

abstract class BaseController(private val dispatcher: CoroutineDispatcher = Dispatchers.JavaFx) :
    Controller(), CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext get() = dispatcher + job

    fun start() {
        job = Job()
    }

    open fun stop() {
        cancel()
    }

    suspend fun runApi(block: suspend () -> Unit): Boolean? {
        return try {
            block()
            true
        } catch (e: Exception) {
            logger.error { e.message }
            null
        }
    }
}