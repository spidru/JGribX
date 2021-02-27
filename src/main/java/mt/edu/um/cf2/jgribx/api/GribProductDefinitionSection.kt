package mt.edu.um.cf2.jgribx.api

import java.util.*

/**
 * GRIBx Product Definitions Section (PDS) common interface
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribProductDefinitionSection {
	val parameter: GribParameter
	val level: GribLevel?
	val forecastTime: Calendar
}
