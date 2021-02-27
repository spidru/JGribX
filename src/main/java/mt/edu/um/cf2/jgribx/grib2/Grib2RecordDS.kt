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
package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.NoValidGribException

/**
 * ### [Section 7: Data Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect7.shtml)
 *
 *    | Octet | # | Value                                                                                         |
 *    |-------|---|-----------------------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn                                                               |
 *    | 5     | 1 | Number of Section: 7                                                                          |
 *    | 6-nn  |   | Data in a format described by data Template 7.X, where X is the data  representation template |
 *    |       |   | number given in octets 10-11 of Section 5.                                                    |
 *
 * See [Table 7.0 Data Template Definition used in section 7](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table7-0.shtml)
 *
 * @param gds Reference to [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 * @param drs Reference to [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 * @param bms Reference to [Section 6: Bit Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 * @param data Decoded data
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib2RecordDS<DRS : Grib2RecordDRS> internal constructor(internal val gds: Grib2RecordGDS,
																		internal val drs: DRS,
																		internal val bms: Grib2RecordBMS,
																		val data: FloatArray) : Grib2Section {
	companion object {
		internal fun <DRS : Grib2RecordDRS> readFromStream(gribInputStream: GribInputStream,
														   gds: Grib2RecordGDS,
														   drs: DRS,
														   bms: Grib2RecordBMS): Grib2RecordDS<*> {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 7)

			return when (drs) {
				// Order is relevant since, e.g., DRS3 inherits DRS2 and DRS2 inherits DRS0!
				is Grib2RecordDRS3 -> Grib2RecordDS3.readFromStream(gribInputStream, gds, drs, bms)
						.also { it.length = length } // TODO Calculate length
				is Grib2RecordDRS2 -> Grib2RecordDS2.readFromStream(gribInputStream, gds, drs, bms)
						.also { it.length = length } // TODO Calculate length
				is Grib2RecordDRS0 -> Grib2RecordDS0.readFromStream(gribInputStream, gds, drs, bms)
				else -> throw TODO("Grib2RecordDS${drs::class.simpleName?.get(0)} not implemented!")
			}.takeIf { it.length == length } ?: throw NoValidGribException("DS length mismatch")
		}
	}

	override val number: Int = 7

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordDS<*>
			&& length == other.length
			&& number == other.number
			&& data.contentEquals(other.data)

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + data.contentHashCode()
		return result
	}
}
