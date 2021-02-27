package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import kotlin.math.pow

/**
 * ### [7.2 Grid point data - Complex Packing](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp7-2.shtml)
 *
 *    | Octet     | # | Value                                                                                         |
 *    |-----------|---|-----------------------------------------------------------------------------------------------|
 *    | 1-4       | 4 | Length of section in octets: nn                                                               |
 *    | 5         | 1 | Number of Section: 7                                                                          |
 *    | 6-xx      |   | NG group reference values (X1 in the decoding formula), each of which is encoded using the    |
 *    |           |   | number of bits specified in octet 20 of data representation template 5.0. Bits set to zero    |
 *    |           |   | shall be appended as necessary to ensure this sequence of numbers ends on an octet boundary   |
 *    | [xx+1]-yy |   | NG group widths, each of which is encoded using the number of bits specified in octet 37 of   |
 *    |           |   | data representation template 5.2. Bits set to zero shall be appended as necessary to ensure   |
 *    |           |   | this sequence of numbers ends on an octet boundary                                            |
 *    | [yy+1]-zz |   | NG scaled group lengths, each of which is encoded using the number of bits specified in octet |
 *    |           |   | 47 of data representation template 5.2. Bits set to zero shall be appended as necessary to    |
 *    |           |   | ensure this sequence of numbers ends on an octet boundary (see Note 5)                        |
 *    | [zz+1]-nn |   | Packed values (X2 in the decoding formula), where each value is a deviation from its          |
 *    |           |   | respective group reference value                                                              |
 *
 *     Y = ( R + (X1 + X2) * 2^E ) / 10^D
 *
 * @param gds Reference to [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 * @param drs Reference to [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 * @param bms Reference to [Section 6: Bit Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 * @param data Decoded data
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
open class Grib2RecordDS2 private constructor(gds: Grib2RecordGDS,
											  drs: Grib2RecordDRS2,
											  bms: Grib2RecordBMS,
											  data: FloatArray) :
		Grib2RecordDS<Grib2RecordDRS2>(gds, drs, bms, data) {

	companion object {
		/**
		 * (`6-xx`) NG group reference values (`X1` in the decoding formula), each of which is encoded using the number
		 *          of bits specified in octet `20` of data representation
		 *          [template 5.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp5-0.shtml).
		 */
		internal fun readGroupReferenceValuesX1(gribInputStream: GribInputStream, drs: Grib2RecordDRS2): FloatArray {
			val valueX1 = FloatArray(drs.nGroups)
			if (drs.nBits != 0) { // Octet: 20
				repeat(drs.nGroups) { i ->
					valueX1[i] = gribInputStream.readUBits(drs.nBits).toFloat()
				}
				// Bits set to zero shall be appended as necessary to ensure this sequence of numbers ends on an
				// octet boundary
				gribInputStream.seekNextByte()
			}
			return valueX1
		}

		/**
		 * (`[xx+1]-yy`) `NG` group widths, each of which is encoded using the number of bits specified in octet `37`
		 *               of data representation [template 5.2](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp5-2.shtml).
		 */
		internal fun readGroupWidths(gribInputStream: GribInputStream, drs: Grib2RecordDRS2): IntArray {
			val groupWidths = IntArray(drs.nGroups) // Initialised to zeroes
			if (drs.groupWidthBits != 0) { // Octet: 37
				repeat(drs.nGroups) { i ->
					groupWidths[i] = drs.refGroupWidths + gribInputStream.readUBits(drs.groupWidthBits).toInt()
				}
				// Bits set to zero shall be appended as necessary to ensure this sequence of numbers ends on an
				// octet boundary
				gribInputStream.seekNextByte()
			}
			return groupWidths
		}

		/**
		 * (`[yy+1]-zz`) `NG` scaled group lengths, each of which is encoded using the number of bits specified in
		 *               octet `47` of data representation
		 *               [template 5.2](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp5-2.shtml).
		 *
		 * **Group length is defined as the number of values in a group.**
		 *
		 * **Note 5:** The essence of the complex packing method is to subdivide a field of values into NG groups,
		 * where the values in each group have similar sizes. In this procedure, it is necessary to retain enough
		 * information to recover the group lengths upon decoding. The NG group lengths for any given field can be
		 * described by
		 *
		 *         Ln = ref + Kn x len_inc, n = 1..NG
		 *
		 * where [ref][Grib2RecordDRS2.refGroupLengths] is given by [octets `38-41`][Grib2RecordDRS2.refGroupLengths]
		 * and [len_inc][Grib2RecordDRS2.groupLengthIncrement] by [octet `42`][Grib2RecordDRS2.groupLengthIncrement].
		 * The [NG][Grib2RecordDRS2.nGroups] values of K (the scaled group lengths) are stored in the data section,
		 * each with the number of bits specified by [octet `47`][Grib2RecordDRS2.nBitsScaledGroupLengths]. Since the
		 * [last group][Grib2RecordDRS2.lastGroupLength] is a special case which may not be able to be specified by
		 * this relationship, the length of the last group is stored in [octets 43-46][Grib2RecordDRS2.lastGroupLength].
		 *
		 * Relevant [Data Representation Section][Grib2RecordDRS2] octets:
		 * * `38-41`: [Grib2RecordDRS2.refGroupLengths] (`ref`)
		 * * `   42`: [Grib2RecordDRS2.groupLengthIncrement] (`len_inc`)
		 * * `43-46`: [Grib2RecordDRS2.lastGroupLength]
		 *
		 * @see Grib2RecordDRS2.refGroupLengths
		 * @see Grib2RecordDRS2.groupLengthIncrement
		 * @see Grib2RecordDRS2.lastGroupLength
		 */
		internal fun readScaledGroupLengths(gribInputStream: GribInputStream, drs: Grib2RecordDRS2): IntArray {
			val scaledGroupLengths = IntArray(drs.nGroups)
			if (drs.nBitsScaledGroupLengths != 0) { // Octet: 47
				repeat(drs.nGroups) { i ->
					scaledGroupLengths[i] = gribInputStream.readUBits(drs.nBitsScaledGroupLengths).toInt()
				}
				// Bits set to zero shall be appended as necessary to ensure this sequence of numbers ends on an
				// octet boundary
				gribInputStream.seekNextByte()
			}

			//var totalLength = 0
			repeat(drs.nGroups - 1) { i ->
				//        Ln          =        ref          +  Kn                   x         len_inc,        n = 1..NG
				scaledGroupLengths[i] = drs.refGroupLengths + scaledGroupLengths[i] * drs.groupLengthIncrement
				//4totalLength += scaledGroupLengths[i]
			}
			//totalLength += drs.lastGroupLength
			scaledGroupLengths[drs.nGroups - 1] = drs.lastGroupLength
			//if (totalLength != drs.nDataPoints) throw Exception("nPoints != drs.nPoints: ${totalLength} != ${drs.nDataPoints}")
			return scaledGroupLengths
		}

		/**
		 * (`[zz+1]-nn`) Packed values (`X2` in the decoding formula), where each value is a deviation from its
		 *               respective group reference value
		 */
		internal fun readPackedValues(gribInputStream: GribInputStream,
									  drs: Grib2RecordDRS2,
									  gds: Grib2RecordGDS,
									  valueX1: FloatArray,
									  groupWidth: IntArray,
									  groupLength: IntArray): Triple<FloatArray, BooleanArray?, Int> {
			// Get X2 values and calculate the results Y using the formula:
			//  Y = (R + (X1 + X2) * (2^E)) / (10^D)
			val valueX = FloatArray(gds.numberOfDataPoints) // X1 + X2
			var count = 0
			var dataSize = 0
			var dataBitMap: BooleanArray? = null
			gribInputStream.seekNextByte()
			when (drs.missingValueManagement) {
				0 -> { // No explicit missing values included within the data values
					repeat(drs.nGroups) { i ->
						if (groupWidth[i] != 0) repeat(groupLength[i]) {
							valueX[count++] = valueX1[i] + gribInputStream.readUBits(groupWidth[i]).toFloat()
						} else repeat(groupLength[i]) {
							valueX[count++] = valueX1[i] // X1 + 0 (X2 = 0)
						}
					}
				}
				1,     // Primary missing values included within the data values
				2 -> { // Primary and secondary missing values included within the data values
					dataBitMap = BooleanArray(gds.numberOfDataPoints)
					repeat(drs.nGroups) { i ->
						if (groupWidth[i] != 0) {
							val primaryMissingValue = 2.0.pow(groupWidth[i].toDouble()).toInt() - 1
							val secondaryMissingValue = primaryMissingValue - 1
							repeat(groupLength[i]) {
								valueX[count] = gribInputStream.readUBits(groupWidth[i]).toFloat()
								if (valueX[count] == primaryMissingValue.toFloat()
										|| drs.missingValueManagement == 2
										&& valueX[count] == secondaryMissingValue.toFloat()) {
									dataBitMap[count] = false
								} else {
									dataBitMap[count] = true
									valueX[dataSize++] = valueX[count] + valueX1[i]
								}
								count++
							}
						} else {
							val primaryMissingValue = 2.0.pow(drs.nBits.toDouble()).toFloat()
							val secondaryMissingValue = primaryMissingValue - 1
							if (valueX1[i] == primaryMissingValue) repeat(groupLength[i]) {
								dataBitMap[count++] = false
							} else if (drs.missingValueManagement == 2
									&& valueX1[i] == secondaryMissingValue) repeat(groupLength[i]) {
								dataBitMap[count++] = false
							} else repeat(groupLength[i]) {
								dataBitMap[count] = true
								valueX[dataSize++] = valueX1[i]
								count++
							}
						}
					}
				}
			}
			gribInputStream.seekNextByte()
			return Triple(valueX, dataBitMap, dataSize)
		}

		internal fun calculateData(drs: Grib2RecordDRS2,
								   gds: Grib2RecordGDS,
								   valuesX: FloatArray,
								   dataBitMap: BooleanArray?): FloatArray {
			val decimalFactor = 10.0.pow(drs.decimalScaleFactor).toFloat()
			val binaryFactor = 2.0.pow(drs.binaryScaleFactor).toFloat()
			val data = FloatArray(gds.numberOfDataPoints)
			if (drs.missingValueManagement == 0) {
				for (i in valuesX.indices) {
					// Y    = (     R       + (X1 + X2)  *     2^E     ) /    10^D
					data[i] = (drs.refValue + valuesX[i] * binaryFactor) / decimalFactor
				}
			} else if (dataBitMap != null) { // (drs.missingValueManagement == 1 || drs.missingValueManagement == 2)
				var c = 0
				for (i in data.indices) {
					if (dataBitMap[i]) {
						// Y    = (     R       +  (X1 + X2)   *     2^E     ) /    10^D
						data[i] = (drs.refValue + valuesX[c++] * binaryFactor) / decimalFactor
					} else {
						data[i] = drs.missingValue[0] // FIXME
					}
				}
			}
			return data
		}

		internal fun readFromStream(gribInputStream: GribInputStream,
									gds: Grib2RecordGDS,
									drs: Grib2RecordDRS2,
									bms: Grib2RecordBMS): Grib2RecordDS2 {
			// [ 6 - xx] NG group reference values (X1 in the decoding formula), each of which is encoded using the
			//           number of bits specified in octet 20 of data representation template 5.0.
			val valueX1 = readGroupReferenceValuesX1(gribInputStream, drs)

			// [[xx+1] - yy] NG group widths, each of which is encoded using the number of bits specified in
			//               octet 37 of data representation template 5.2.
			val groupWidth = readGroupWidths(gribInputStream, drs)

			// test
			//if (drs.missingValueManagement != 0) throw NotSupportedException("Missing Value Management is not supported")

			// [[yy+1] - zz] NG scaled group lengths, each of which is encoded using the number of bits specified in
			//               octet 47 of data representation template 5.2.
			val groupLength = try {
				readScaledGroupLengths(gribInputStream, drs)
			} catch (e: Exception) { // TODO ???
				val data = FloatArray(drs.nDataPoints)
				repeat(drs.nDataPoints) { i ->
					data[i] = drs.missingValue[0] // FIXME
				}
				return Grib2RecordDS2(gds, drs, bms, data)
			}

			// [[zz+1] - nn] Packed values (X2 in the decoding formula), where each value is a deviation from its
			//               respective group reference value
			val (valueX, dataBitMap, _) = readPackedValues(gribInputStream, drs, gds, valueX1, groupWidth, groupLength)

			val data = calculateData(drs, gds, valueX, dataBitMap)

			// bit map is used
			if (bms.indicator != Grib2RecordBMS.Indicator.BITMAP_NONE) {
				Logger.error("Bitmap not supported yet")
				//val idx = 0
				//val tmp = FloatArray(gds.numberOfDataPoints)
				//for (i in 0 until gds.numberOfDataPoints) {
				//	if ((bitmap[i/8] & ))
				//}
			}

			return Grib2RecordDS2(gds, drs, bms, data)
		}
	}

	override var length: Int = 0 // TODO Calculate length

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		TODO("Writing Grid point data - Complex Packing is not supported yet")
	}
}
