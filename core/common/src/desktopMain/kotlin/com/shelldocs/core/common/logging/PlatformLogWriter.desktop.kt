package com.shelldocs.core.common.logging

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual object PlatformLogWriter : LogWriter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val line = "${Clock.System.now()} ${level.name.padEnd(5)} [$tag] $message"
        println(line)
        throwable?.printStackTrace()
    }
}
