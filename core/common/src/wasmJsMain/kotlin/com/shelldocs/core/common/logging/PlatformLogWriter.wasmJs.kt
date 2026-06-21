package com.shelldocs.core.common.logging

actual object PlatformLogWriter : LogWriter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val line = "${level.name.padEnd(5)} [$tag] $message"
        when (level) {
            LogLevel.ERROR -> console.error(line)
            LogLevel.WARN -> console.warn(line)
            else -> console.log(line)
        }
        throwable?.let { console.error(it.stackTraceToString()) }
    }
}

private external object console {
    fun log(message: String)
    fun warn(message: String)
    fun error(message: String)
}
