package mt.edu.um.cf2.jgribx.api

/**
 * Unified GRIB parameter API
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribParameter {
	val id: Int
	val code: String
	val description: String
}
