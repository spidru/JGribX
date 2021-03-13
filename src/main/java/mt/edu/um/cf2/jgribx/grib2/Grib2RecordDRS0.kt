package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream

/**
 * ### [5.0 Grid point data - simple packing](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp5-0.shtml)
 *
 *    | Octet | # | Value                                                                                           |
 *    |-------|---|-------------------------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn                                                                 |
 *    | 5     | 1 | Number of Section: 5                                                                            |
 *    | 6-9   | 4 | Number of data points where one or more values are specified in  Section 7 when a bit map is    |
 *    |       |   | present, total number of data points when a  bit map is absent                                  |
 *    | 10-11 | 2 | Data Representation Template Number (see code Table 5.0)                                        |
 *    | 12-15 | 4 | Reference value (R) (IEEE 32-bit floating-point value)                                          |
 *    | 16-17 | 2 | Binary Scale Factor (E)                                                                         |
 *    | 18-19 | 2 | Decimal Scale Factor (D)                                                                        |
 *    | 20    | 1 | Number of bits used for each packed value for simple packing, or for each group reference value |
 *    |       |   | for complex packing or spatial differencing                                                     |
 *    | 21    | 1 | Type of original field values (see Code Table 5.1)                                              |
 *
 *     Y = ( R + (X1 + X2) * 2^E ) / 10^D
 *   where `X1 = 0`, `X2 = X` (the value)
 *
 * @param nDataPoints        (`6-9`)   Number of data points where one or more values are specified in
 *                                     [Section 7](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect7.shtml)
 *                                     when a bit map is present, total number of data points when a bit map is absent.
 * @param refValue           (`12-15`) Reference value (`R`) (IEEE 32-bit floating-point value)
 * @param binaryScaleFactor  (`16-17`) Binary scale factor (`E`)
 * @param decimalScaleFactor (`18-19`) Decimal scale factor (`D`)
 * @param nBits              (`20`)    Number of bits used for each packed value for simple packing, or for each group
 *                                     reference value for complex packing or spatial differencing
 * @param type               (`21`)    Type of original field values
 *                                     [Table 5.1](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-1.shtml)
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
open class Grib2RecordDRS0 internal constructor(nDataPoints: Int,
												internal val refValue: Float,
												internal val binaryScaleFactor: Int,
												internal val decimalScaleFactor: Int,
												internal val nBits: Int,
												internal val type: Int) :
		Grib2RecordDRS(nDataPoints) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream, nDataPoints: Int): Grib2RecordDRS0 {

			/* [12-15] Reference value (R) (IEEE 32-bit floating-point value) */
			val refValue = gribInputStream.readFloatIEEE754(4)

			/* [16-17] Binary Scale Factor (E) */
			val binaryScaleFactor = gribInputStream.readSMInt(2)

			/* [18-19] Decimal Scale Factor (D) */
			val decimalScaleFactor = gribInputStream.readSMInt(2)

			/* [20] Number of bits used for each packed value for simple packing */
			val nBits = gribInputStream.readUInt(1)

			/* [21] Type of original field values */
			val type = gribInputStream.readUInt(1)

			return Grib2RecordDRS0(nDataPoints, refValue, binaryScaleFactor, decimalScaleFactor, nBits, type)
		}
	}

	override val length: Int = 21

	override val templateNumber: Int = 0

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number, [6-11] DRS common stuff
		outputStream.writeFloatIEEE754(refValue, bytes = 4) // [12-15]
		outputStream.writeSMInt(binaryScaleFactor, bytes = 2) // [16-17]
		outputStream.writeSMInt(decimalScaleFactor, bytes = 2) // [18-19]
		outputStream.writeUInt(nBits, bytes = 1) // [20]
		outputStream.writeUInt(type, bytes = 1) // [21]
	}

	override fun equals(other: Any?) = this === other
			|| super.equals(other)
			&& other is Grib2RecordDRS0
			&& refValue == other.refValue
			&& binaryScaleFactor == other.binaryScaleFactor
			&& decimalScaleFactor == other.decimalScaleFactor
			&& nBits == other.nBits
			&& type == other.type

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + refValue.hashCode()
		result = 31 * result + binaryScaleFactor
		result = 31 * result + decimalScaleFactor
		result = 31 * result + nBits
		result = 31 * result + type
		return result
	}
}
