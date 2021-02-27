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

import java.util.*

/**
 * A GRIB Record is a representation of part of a [GRIB message][mt.edu.um.cf2.jgribx.api.GribMessage] containing one
 * dataset. Different GRIB editions support different number of such records per
 * [GRIB message][mt.edu.um.cf2.jgribx.api.GribMessage]. Currently GRIB1 supports exactly one records per message while
 * GRIB2 supports multiple records per message due to possible repetitions of sections 2 to 7, 3 to 7, or 4 to 7.
 *
 * @param indicatorSection [Section 0: Indicator Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect0.shtml)
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class GribRecord(val indicatorSection: GribRecordIS) {
	/** Returns the ID corresponding to the originating centre. */
	abstract val centreId: Int
	abstract val forecastTime: Calendar
	abstract val levelCode: String
	abstract val levelDescription: String

	/** Returns the unique ID for the level code and value combination. */
	abstract val levelIdentifier: String
	abstract val levelValues: FloatArray
	abstract val parameterCode: String
	abstract val parameterDescription: String

	/** Returns the ID corresponding to the generating process. */
	abstract val processId: Int
	abstract val referenceTime: Calendar
	abstract fun getValue(latitude: Double, longitude: Double): Double
}
