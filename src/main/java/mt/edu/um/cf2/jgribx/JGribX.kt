/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * 
 * Licensed under MIT (https://github.com/spidru/JGribX/blob/master/LICENSE)
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx

object JGribX {
	/** Version of JGribX */
	const val VERSION = "0.4.1"

	fun setLoggingLevel(level: Int) {
		Logger.level = level
	}

	/** The path to the resource directory. */
	var resourcePath: String = "res/"
		set(value) {
			field = if (!value.endsWith("/")) "${value}/" else value
		}
}