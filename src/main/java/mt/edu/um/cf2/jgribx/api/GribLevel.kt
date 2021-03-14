package mt.edu.um.cf2.jgribx.api

/**
 * Unified GRIB level API
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribLevel {
	val id: Int
	val code: String
	val name: String
	val description: String
	val value: Float

	/** Returns a unique ID for the given combination code-value combination. */
	val identifier: String
		get() = if (java.lang.Float.isNaN(value)) code else "${code}:${if (value % 1 == 0f) value.toInt() else value}"
}
