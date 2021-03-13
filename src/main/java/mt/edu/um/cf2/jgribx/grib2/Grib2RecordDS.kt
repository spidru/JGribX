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
 * [Table 7.0 Data Template Definition used in section 7](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table7-0.shtml)
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
open class Grib2RecordDS protected constructor(internal val length: Int, val data: FloatArray) {
	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									gds: Grib2RecordGDS,
									drs: Grib2RecordDRS,
									bms: Grib2RecordBMS): Grib2RecordDS? = when (drs) {
			// Order is relevant since, e.g., DRS3 inherits DRS2 and DRS2 inherits DRS0!
			is Grib2RecordDRS3 -> Grib2RecordDS3.readFromStream(gribInputStream, gds, drs, bms)
			is Grib2RecordDRS2 -> Grib2RecordDS2.readFromStream(gribInputStream, gds, drs, bms)
			is Grib2RecordDRS0 -> Grib2RecordDS0.readFromStream(gribInputStream, gds, drs, bms)
			else -> TODO("Grib2RecordDS${drs::class.simpleName?.last()} not implemented!")
		}
	}
}
