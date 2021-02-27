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
package mt.edu.um.cf2.jgribx.api

import mt.edu.um.cf2.jgribx.GribRecordIS
import java.util.*

/**
 * A GRIB Record is a representation of part of a [GRIB message][mt.edu.um.cf2.jgribx.api.GribMessage] containing one
 * dataset. Different GRIB editions support different number of such records per
 * [GRIB message][mt.edu.um.cf2.jgribx.api.GribMessage]. Currently GRIB1 supports exactly one records per message while
 * GRIB2 supports multiple records per message due to possible repetitions of sections 2 to 7, 3 to 7, or 4 to 7.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribRecord {
	/** [Section 0: Indicator Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect0.shtml) */
	val indicatorSection: GribRecordIS
	val gridDefinitionSection: GribGridDefinitionSection

	/** Returns the ID corresponding to the originating centre. */
	val centreId: Int
	val forecastTime: Calendar
	val levelCode: String
	val levelDescription: String

	/** Returns the unique ID for the level code and value combination. */
	val levelIdentifier: String
	val levelValues: FloatArray
	val parameterCode: String
	val parameterDescription: String

	/** Returns the ID corresponding to the generating process. */
	val processId: Int
	val referenceTime: Calendar

	val values: FloatArray

	fun getValue(sequence: Int): Float

	fun getValue(latitude: Double, longitude: Double): Float

	/**
	 * Cut a bounding box from grid. The defined bounding box must be inside the grid.
	 *
	 * @param north Northern latitude of the cutout
	 * @param east Eastern longitude of the cutout
	 * @param south Southern latitude of the cutout
	 * @param west Western longitude of the cutout
	 */
	fun cutOut(north: Double, east: Double, south: Double, west: Double)
}
