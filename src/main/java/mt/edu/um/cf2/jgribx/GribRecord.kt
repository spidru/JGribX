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

import mt.edu.um.cf2.jgribx.grib1.Grib1Record
import mt.edu.um.cf2.jgribx.grib2.Grib2Record
import java.util.*

abstract class GribRecord(val indicatorSection: GribRecordIS) {
	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream): GribRecord {
			val (indicatorSection, discipline) = GribRecordIS.readFromStream(gribInputStream)
			val record = when (indicatorSection.gribEdition) {
				1 -> Grib1Record.readFromStream(gribInputStream, indicatorSection)
				2 -> if (discipline != null) Grib2Record
						.readFromStream(gribInputStream, indicatorSection, discipline) else
					throw NoValidGribException("Missing discipline for GRIB2 edition")
				else -> throw NoValidGribException("Unsupported GRIB edition ${indicatorSection.gribEdition}")
			}
			val es = GribRecordES(gribInputStream)
			if (!es.isValid) throw NoValidGribException("Grib End Section is invalid")
			return record
		}
	}

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