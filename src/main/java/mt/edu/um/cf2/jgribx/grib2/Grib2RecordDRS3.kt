package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream

/**
 * ### [5.3 Grid point data - complex packing](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp5-3.shtml)
 *
 *    | Octet | # | Value                                                                                             |
 *    |-------|---|---------------------------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn                                                                   |
 *    | 5     | 1 | Number of Section: 5                                                                              |
 *    | 6-9   | 4 | Number of data points where one or more values are specified in Section 7 when a bit map is       |
 *    |       |   | present, total number of data points when a bit map is absent                                     |
 *    | 10-11 | 2 | Data Representation Template Number (see code Table 5.0)                                          |
 *    | 12-15 | 4 | Reference value (R) (IEEE 32-bit floating-point value)                                            |
 *    | 16-17 | 2 | Binary Scale Factor (E)                                                                           |
 *    | 18-19 | 2 | Decimal Scale Factor (D)                                                                          |
 *    | 20    | 1 | Number of bits used for each packed value for simple packing, or for each group reference value   |
 *    |       |   | for complex packing or spatial differencing                                                       |
 *    | 21    | 1 | Type of original field values (see Code Table 5.1)                                                |
 *    | 22    | 1 | Group splitting method used (see Code Table 5.4)                                                  |
 *    | 23    | 1 | Missing value management used (see Code Table 5.5)                                                |
 *    | 24-27 | 4 | Primary missing value substitute                                                                  |
 *    | 28-31 | 4 | Secondary missing value substitute                                                                |
 *    | 32-35 | 4 | NG ― number of groups of data values into which field is split                                    |
 *    | 36    | 1 | Reference for group widths (see Note 12)                                                          |
 *    | 37    | 1 | Number of bits used for the group widths (after the reference value in octet 36 has been removed) |
 *    | 38-41 | 4 | Reference for group lengths (see Note 13)                                                         |
 *    | 42    | 1 | Length increment for the group lengths (see Note 14)                                              |
 *    | 43-46 | 4 | True length of last group                                                                         |
 *    | 47    | 1 | Number of bits used for the scaled group lengths (after subtraction of the reference value given  |
 *    |       |   | in octets 38-41 and division by the length                                                        |
 *    |       |   | increment given in octet 42)                                                                      |
 *    | 48    | 1 | Order of spatial difference (see Code Table 5.6)                                                  |
 *    | 49    | 1 | Number of octets required in the data section to specify extra descriptors needed for spatial     |
 *    |       |   | differencing (octets 6-ww in data template 7.3)                                                   |
 *
 *     Y = ( R + (X1 + X2) * 2^E ) / 10^D
 *
 * @param nDataPoints             (`6-9`)   Number of data points where one or more values are specified in
 *                                          [Section 7](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect7.shtml)
 *                                          when a bit map is present, total number of data points when a bit map is
 *                                          absent.
 * @param refValue                (`12-15`) Reference value (`R`) (IEEE 32-bit floating-point value)
 * @param binaryScaleFactor       (`16-17`) Binary scale factor (`E`)
 * @param decimalScaleFactor      (`18-19`) Decimal scale factor (`D`)
 * @param nBits                   (`20`)    Number of bits used for each packed value for simple packing, or for each
 *                                          group reference value for complex packing or spatial differencing
 * @param type                    (`21`)    Type of original field values
 *                                          [Table 5.1](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-1.shtml)
 * @param splitMethod             (`22`)    Group splitting method used
 *                                          [Table 5.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-4.shtml)
 * @param missingValueManagement  (`23`)    Missing value management used
 *                                          [Table 5.5](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-5.shtml)
 * @param missingValue            (`24-27`) Primary missing value substitute
 *                                (`28-31`) Secondary missing value substitute
 * @param nGroups                 (`32-35`) `NG` ― number of groups of data values into which field is split
 * @param refGroupWidths          (`36`)    Reference for group widths
 * @param groupWidthBits          (`37`)    Number of bits used for the group widths (after the reference value in
 *                                          octet 36 has been removed)
 * @param refGroupLengths         (`38-41`) Reference for group lengths
 * @param groupLengthIncrement    (`42`)    Length increment for the group lengths
 * @param lastGroupLength         (`43-46`) True length of last group
 * @param nBitsScaledGroupLengths (`47`)    Number of bits used for the scaled group lengths (after subtraction of the
 *                                          reference value given in octets 38-41 and division by the length increment
 *                                          given in octet `42`)
 * @param spatialDiffOrder        (`48`)    Order of spatial difference
 *                                          [Table 5.6](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table5-6.shtml)
 * @param spatialDescriptorOctets (`49`)    Number of octets required in the data section to specify extra descriptors
 *                                          needed for spatial differencing (octets `6-ww` in data
 *                                          [template 7.3](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp7-3.shtml))
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordDRS3 private constructor(nDataPoints: Int,
										  refValue: Float,
										  binaryScaleFactor: Int,
										  decimalScaleFactor: Int,
										  nBits: Int,
										  type: Int,

										  splitMethod: Int,
										  missingValueManagement: Int,
										  missingValue: FloatArray,
										  nGroups: Int,
										  refGroupWidths: Int,
										  groupWidthBits: Int,
										  refGroupLengths: Int,
										  groupLengthIncrement: Int,
										  lastGroupLength: Int,
										  nBitsScaledGroupLengths: Int,

										  internal val spatialDiffOrder: Int,
										  internal val spatialDescriptorOctets: Int) :
		Grib2RecordDRS2(nDataPoints, refValue, binaryScaleFactor, decimalScaleFactor, nBits,
				type, splitMethod, missingValueManagement, missingValue, nGroups, refGroupWidths, groupWidthBits,
				refGroupLengths, groupLengthIncrement, lastGroupLength, nBitsScaledGroupLengths) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream, nDataPoints: Int): Grib2RecordDRS3 {
			val drs2 = Grib2RecordDRS2.readFromStream(gribInputStream, nDataPoints)
			val spatialDiffOrder = gribInputStream.readUInt(1)
			val spatialDescriptorOctets = gribInputStream.readUInt(1)

			return Grib2RecordDRS3(drs2, spatialDiffOrder, spatialDescriptorOctets)
		}
	}

	private constructor(drs2: Grib2RecordDRS2, spatialDiffOrder: Int, spatialDescriptorOctets: Int) : this(
			drs2.nDataPoints, drs2.refValue, drs2.binaryScaleFactor, drs2.decimalScaleFactor, drs2.nBits, drs2.type,
			drs2.splitMethod, drs2.missingValueManagement, drs2.missingValue, drs2.nGroups, drs2.refGroupWidths,
			drs2.groupWidthBits, drs2.refGroupLengths, drs2.groupLengthIncrement, drs2.lastGroupLength,
			drs2.nBitsScaledGroupLengths, spatialDiffOrder, spatialDescriptorOctets)

	override val length: Int = 49

	override val templateNumber: Int = 3

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number, [6-11] DRS common stuff, [12-21] DRS0, [21-47] DRS2
		outputStream.writeUInt(spatialDiffOrder, bytes = 1) // [48]
		outputStream.writeUInt(spatialDescriptorOctets, bytes = 1) // [49]
	}

	override fun equals(other: Any?) = this === other
			|| super.equals(other)
			&& other is Grib2RecordDRS3
			&& spatialDiffOrder == other.spatialDiffOrder
			&& spatialDescriptorOctets == other.spatialDescriptorOctets

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + spatialDiffOrder
		result = 31 * result + spatialDescriptorOctets
		return result
	}
}
