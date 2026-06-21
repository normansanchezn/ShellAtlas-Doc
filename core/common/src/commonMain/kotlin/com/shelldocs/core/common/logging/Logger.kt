package com.shelldocs.core.common.logging

import com.shelldocs.core.common.logging.AppLogger.tag


/** Severity for a log line. Ordered so [AppLogger.minLevel] can filter cheaply. */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

/** Sink that receives every log line accepted by [AppLogger]. */
interface LogWriter {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

/** Platform console sink: Logcat on Android, NSLog on iOS, stdout elsewhere. */
expect object PlatformLogWriter : LogWriter

/**
 * Single entry point for app-wide logging. Reusable across every module
 * (DB, Ollama, Jira/Confluence/Azure, CRUD) — call [AppLogger.tag] once per
 * component and reuse the returned [TaggedLogger] instead of formatting
 * strings by hand at each call site.
 */
object AppLogger {

    var minLevel: LogLevel = LogLevel.DEBUG

    private val writers = mutableListOf<LogWriter>(PlatformLogWriter)

    /** Register an extra sink (e.g. a file or remote writer) on top of the platform console. */
    fun addWriter(writer: LogWriter) {
        writers += writer
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (level < minLevel) return
        writers.forEach { it.log(level, tag, message, throwable) }
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.WARN, tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.ERROR, tag, message, throwable)

    /** Scoped logger bound to [tag] — avoids repeating the tag at every call site. */
    fun tag(tag: String): TaggedLogger = TaggedLogger(tag)
}

/** Tag-bound facade returned by [AppLogger.tag]. */
class TaggedLogger(private val tag: String) {
    fun d(message: String) = AppLogger.d(tag, message)
    fun i(message: String) = AppLogger.i(tag, message)
    fun w(message: String, throwable: Throwable? = null) = AppLogger.w(tag, message, throwable)
    fun e(message: String, throwable: Throwable? = null) = AppLogger.e(tag, message, throwable)
}

/** Shared tag vocabulary so every module logs under the same set of categories. */
object LogTags {
    const val DATABASE = "ShellDocs/DB"
    const val OLLAMA = "ShellDocs/Ollama"
    const val INTEGRATION = "ShellDocs/Integration"
    const val NETWORK = "ShellDocs/Network"
    const val CRUD = "ShellDocs/CRUD"
    const val AUTH = "ShellDocs/Auth"
    const val STARTUP = "ShellDocs/Startup"
}
