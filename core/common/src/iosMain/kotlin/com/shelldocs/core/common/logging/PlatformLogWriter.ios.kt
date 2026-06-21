package com.shelldocs.core.common.logging

import platform.Foundation.NSLog

actual object PlatformLogWriter : LogWriter {
    override fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        NSLog("%s", "${level.name.padEnd(5)} [$tag] $message")
        throwable?.let { NSLog("%s", it.stackTraceToString()) }
    }
}
