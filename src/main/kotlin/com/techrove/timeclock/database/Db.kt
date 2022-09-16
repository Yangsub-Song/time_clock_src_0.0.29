package com.techrove.timeclock.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}


object Db {
    var isReady = false

    fun initialize() {
        Database.connect("jdbc:sqlite:./time_clock.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        transactionWithLock {
            //addLogger(StdOutSqlLogger)
            //SchemaUtils.drop(TimeSheets)
            SchemaUtils.create(TimeSheets)
        }
        isReady = true
    }

    fun delete() {
        transactionWithLock {
            SchemaUtils.drop(TimeSheets)
        }
    }
}

private val writeLock = ReentrantLock()

fun <T> transactionWithLock(db: Database? = null, statement: Transaction.() -> T): T =
    writeLock.withLock {
        transaction(db, statement)
    }
