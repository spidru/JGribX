/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * Adapted from JGRIB: http://jgrib.sourceforge.net/
 * 
 * Licensed under MIT: https://github.com/spidru/JGribX/blob/master/LICENSE
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

object Logger {
	enum class LoggingMode {
		OFF, CONSOLE, LOCAL, REMOTE
	}

	/** Designates severe error events that typically lead to application abort.  */
	const val FATAL = 0

	/** Designates error events that might still allow the application to continue running.  */
	const val ERROR = 1

	/** Designates potentially problematic events.  */
	const val WARNING = 2

	/** Designates informational messages that highlight the progress of the application.  */
	const val INFO = 3

	/** Designates fine-grained informational events that are mostly useful for debugging.  */
	const val DEBUG = 4

	/** Designates finer-grained informational events that are useful for tracing a problem.  */
	const val TRACE = 5

	private var pw: PrintWriter? = null
	var mode: LoggingMode? = null
	var level = 0

	fun print(message: String, lvl: Int) {
		if (lvl > level) return
		val formattedMessage = when (lvl) {
			FATAL -> "FATAL: ${message}"
			ERROR -> "ERROR: ${message}"
			WARNING -> "WARNING: ${message}"
			INFO -> "INFO: ${message}"
			DEBUG -> "DEBUG: ${message}"
			TRACE -> "TRACE: ${message}"
			else -> message
		}
		when (mode) {
			LoggingMode.OFF -> {
			}
			LoggingMode.CONSOLE -> print(formattedMessage)
			LoggingMode.LOCAL -> {
				pw = (pw ?: openLogFile(File("log.txt")))
						?.also { it.print(formattedMessage) }
			}
			LoggingMode.REMOTE -> throw UnsupportedOperationException("The current version of Logger does not yet support remote logging")
		}
	}

	fun debug(message: String?, throwable: Throwable? = null) = println(message, throwable, DEBUG)

	fun info(message: String?, throwable: Throwable? = null) = println(message, throwable, INFO)

	fun warning(message: String?, throwable: Throwable? = null) = println(message, throwable, WARNING)

	fun error(message: String?, throwable: Throwable? = null) = println(message, throwable, ERROR)

	private fun println(message: String?, throwable: Throwable?, lvl: Int) {
		print("${message ?: ""}\n", lvl)
		throwable?.printStackTrace(if (lvl < 2) System.err else System.out)
	}

	fun flush() {
		pw?.flush()
	}

	private fun openLogFile(file: File): PrintWriter? {
		if (!file.exists()) try {
			file.createNewFile()
			println("New log file created")
		} catch (ioe: IOException) {
			println("Cannot create log file") // FIXME always writing to stdout
		}
		return try {
			PrintWriter(BufferedWriter(FileWriter(file, true)))
		} catch (ioe: IOException) {
			// TODO Add exception handler
			null
		}
	}
}