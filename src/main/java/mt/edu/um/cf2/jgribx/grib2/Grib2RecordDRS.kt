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
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.NoValidGribException

/**
 * ### [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
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
 * @param nDataPoints (`6-9`) Number of data points where one or more values are specified in Section 7 when a bit
 *                               map is present, total number of data points when a bit map is absent.
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib2RecordDRS protected constructor(internal val nDataPoints: Int) : Grib2Section {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream): Grib2RecordDRS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 5)

			/* [6-9] Number of data points */
			val nDataPoints = gribInputStream.readUINT(4)

			/* [10-11] Data representation template number */
			val templateNumber = gribInputStream.readUINT(2)
			return when (templateNumber) {
				0 -> Grib2RecordDRS0.readFromStream(gribInputStream, nDataPoints)
				2 -> Grib2RecordDRS2.readFromStream(gribInputStream, nDataPoints)
				3 -> Grib2RecordDRS3.readFromStream(gribInputStream, nDataPoints)
				else -> throw TODO("Data Representation type ${templateNumber} not supported yet")
			}.takeIf { it.length == length } ?: throw NoValidGribException("DRS length mismatch")
		}
	}

	override val number: Int = 5

	/**
	 * (`10-11`) Data representation template number
	 *           [Table 5.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-0.shtml)
	 */
	protected abstract val templateNumber: Int

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		outputStream.writeUInt(nDataPoints, bytes = 4)
		outputStream.writeUInt(templateNumber, bytes = 2)
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordDRS
			&& length == other.length
			&& number == other.number
			&& nDataPoints == other.nDataPoints
			&& templateNumber == other.templateNumber

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + nDataPoints
		result = 31 * result + templateNumber
		return result
	}
}
