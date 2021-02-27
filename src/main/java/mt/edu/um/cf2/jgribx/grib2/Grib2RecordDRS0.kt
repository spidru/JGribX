package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream

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
 * @param length             (`1-4`)  Length of the section in octets (nn)
 * @param nDataPoints        (`6-9`)  Number of data points where one or more values are specified in Section 7 when a bit
 *                                    map is present, total number of data points when a bit map is absent.
 * @param templateNumber     (`10-11`) Data representation template number
 *                                     [Table 5.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-0.shtml)
 * @param refValue           (`12-15`) Reference value (R) (IEEE 32-bit floating-point value)
 * @param binaryScaleFactor  (`16-17`) Binary scale factor (E)
 * @param decimalScaleFactor (`18-19`) Decimal scale factor (D)
 * @param nBits              (`20`)    Number of bits used for each packed value for simple packing, or for each group
 *                                     reference value for complex packing or spatial differencing
 * @param type               (`21`)    Type of original field values
 *                                     [Table 5.1](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-1.shtml)
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
open class Grib2RecordDRS0 protected constructor(length: Int,
												 nDataPoints: Int,
												 templateNumber: Int,
												 internal val refValue: Float,
												 internal val binaryScaleFactor: Int,
												 internal val decimalScaleFactor: Int,
												 internal val nBits: Int,
												 internal val type: Int) :
		Grib2RecordDRS(length, nDataPoints, templateNumber) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									length: Int,
									nDataPoints: Int,
									templateNumber: Int): Grib2RecordDRS0 {

			/* [12-15] Reference value (R) (IEEE 32-bit floating-point value) */
			val refValue = gribInputStream.readFloat(4, Bytes2Number.FLOAT_IEEE754)

			/* [16-17] Binary Scale Factor (E) */
			val binaryScaleFactor = gribInputStream.readINT(2, Bytes2Number.INT_SM)

			/* [18-19] Decimal Scale Factor (D) */
			val decimalScaleFactor = gribInputStream.readINT(2, Bytes2Number.INT_SM)

			/* [20] Number of bits used for each packed value for simple packing */
			val nBits = gribInputStream.readUINT(1)

			/* [21] Type of original field values */
			val type = gribInputStream.readUINT(1)

			return Grib2RecordDRS0(length, nDataPoints, templateNumber, refValue, binaryScaleFactor,
					decimalScaleFactor, nBits, type)
		}
	}
}
