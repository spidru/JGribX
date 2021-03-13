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
import mt.edu.um.cf2.jgribx.Logger

/**
 * [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 *
 *    | Octet | # | Value                                                                                        |
 *    |-------|---|----------------------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of the section in octets (nn)                                                         |
 *    | 5     | 1 | Number of the section (5)                                                                    |
 *    | 6-9   | 4 | Number of data points where one or more values are specified in Section 7 when a bit map is  |
 *    |       |   | present, total number of data points when a bit map is absent.                               |
 *    | 10-11 | 2 | Data representation template number (See Table 5.0)                                          |
 *    | 12-nn |   | Data representation template (See Template 5.X, where X is the number given in octets 10-11) |
 *
 * @param length         (`1-4`) Length of the section in octets (nn)
 * @param nDataPoints    (`6-9`) Number of data points where one or more values are specified in Section 7 when a bit
 *                               map is present, total number of data points when a bit map is absent.
 * @param templateNumber (`10-11`) Data representation template number [Table 5.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-0.shtml)
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib2RecordDRS protected constructor(internal val length: Int,
													internal val nDataPoints: Int,
													internal val templateNumber: Int) {

	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Grib2RecordDRS? {
			/*  [1-4] Length of the section in octets (nn) */
			val length = gribInputStream.readUINT(4)

			/* [5] Number of the section (5) */
			val section = gribInputStream.readUINT(1)
			if (section != 5) {
				Logger.error("DRS contains an incorrect section number ${section}!")
				return null
			}

			/* [6-9] Number of data points */
			val nDataPoints = gribInputStream.readUINT(4)

			/* [10-11] Data representation template number */
			val templateNumber = gribInputStream.readUINT(2)
			return when (templateNumber) {
				0 -> Grib2RecordDRS0.readFromStream(gribInputStream, length, nDataPoints, templateNumber)
				2 -> Grib2RecordDRS2.readFromStream(gribInputStream, length, nDataPoints, templateNumber)
				3 -> Grib2RecordDRS3.readFromStream(gribInputStream, length, nDataPoints, templateNumber)
				else -> TODO("Data Representation type ${templateNumber} not supported yet")
			}
		}
	}
}
