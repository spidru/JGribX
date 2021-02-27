package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger

/**
 * ### [7.3 Grid point data - Complex Packing and Spatial Differencing](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp7-3.shtml)
 *
 *    | Octet     | # | Value                                                                                         |
 *    |-----------|---|-----------------------------------------------------------------------------------------------|
 *    | 1 - 4     | 4 | Length of section in octets: nn                                                               |
 *    | 5         | 1 | Number of Section: 7                                                                          |
 *    | 6-ww      |   | First value(s) of original (undifferenced) scaled data values, followed by the overall        |
 *    |           |   | minimum of the differences. The number of values stored is 1 greater than the order of        |
 *    |           |   | differentiation, and the field width is  described in octet 49 of Data Representation         |
 *    |           |   | Template 5.3                                                                                  |
 *    | [ww+1]-xx |   | NG group reference values (X1 in the decoding formula), each of which is encoded using the    |
 *    |           |   | number of bits specified in octet 20 of Data Representation Template 5.0. Bits set to zero    |
 *    |           |   | shall be appended where necessary to ensure this sequence of numbers ends on an octet         |
 *    |           |   | boundary.                                                                                     |
 *    | [xx+1]-yy |   | NG group widths, each of which is encoded using the number of bits specified in octet 37 of   |
 *    |           |   | Data Representation Template 5.2. Bits set to zero shall be appended as necessary to ensure   |
 *    |           |   | this sequence of numbers ends on an octet boundary.                                           |
 *    | [yy+1]-zz |   | NG scaled group lengths, each of which is encoded using the number of bits specified in octet |
 *    |           |   | 47 of Data Representation Template 5.2. Bits set to zero shall be appended as necessary to    |
 *    |           |   | ensure this sequence of numbers ends on an octet boundary. (see Note 14 of Data               |
 *    |           |   | Representation Template 5.2)                                                                  |
 *    | [zz+1]-nn |   | Packed values (X2 in the decoding formula), where each value is a deviation from its          |
 *    |           |   | respective group reference value.                                                             |
 *
 *     Y = ( R + (X1 + X2) * 2^E ) / 10^D
 *
 * @param gds Reference to [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 * @param drs Reference to [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 * @param bms Reference to [Section 6: Bit Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 * @param data Decoded data
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordDS3 private constructor(gds: Grib2RecordGDS,
										 drs: Grib2RecordDRS3,
										 bms: Grib2RecordBMS,
										 data: FloatArray) :
		Grib2RecordDS<Grib2RecordDRS3>(gds, drs, bms, data) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									gds: Grib2RecordGDS,
									drs: Grib2RecordDRS3,
									bms: Grib2RecordBMS): Grib2RecordDS3 {
			if (drs.nGroups == 0) Logger.error("Zero groups not supported yet")

			val ival1: Int
			var ival2 = 0
			val minsd: Int
			if (drs.spatialDescriptorOctets > 0) {
				// first order spatial differencing g1 and gMin
				ival1 = gribInputStream.readSMInt(drs.spatialDescriptorOctets)
				if (drs.spatialDiffOrder == 2) { // second order spatial differencing h1, h2, hMin
					ival2 = gribInputStream.readSMInt(drs.spatialDescriptorOctets)
				}
				minsd = gribInputStream.readSMInt(drs.spatialDescriptorOctets)
			} else {
				// TODO raise exception
				val data = FloatArray(gds.numberOfDataPoints)
				for (i in data.indices) {
					data[i] = drs.missingValue[0] // FIXME
				}
				return Grib2RecordDS3(gds, drs, bms, data)
			}

			// [[ww+1] - xx] NG group reference values (X1 in the decoding formula), each of which is encoded using the
			//               number of bits specified in octet 20 of data representation template 5.0.
			val valueX1 = Grib2RecordDS2.readGroupReferenceValuesX1(gribInputStream, drs)

			// [[xx+1] - yy] NG group widths, each of which is encoded using the number of bits specified in
			//               octet 37 of data representation template 5.2.
			val groupWidth = Grib2RecordDS2.readGroupWidths(gribInputStream, drs)

			// test
			//if (drs.missingValueManagement != 0) throw NotSupportedException("Missing Value Management is not supported")

			// [[yy+1] - zz] NG scaled group lengths, each of which is encoded using the number of bits specified in
			//               octet 47 of data representation template 5.2.
			val groupLength = try {
				Grib2RecordDS2.readScaledGroupLengths(gribInputStream, drs)
			} catch (e: Exception) { // TODO ???
				val data = FloatArray(drs.nDataPoints)
				repeat(drs.nDataPoints) { i ->
					data[i] = drs.missingValue[0] // FIXME
				}
				return Grib2RecordDS3(gds, drs, bms, data)
			}

			// [[zz+1] - nn] Packed values (X2 in the decoding formula), where each value is a deviation from its
			//               respective group reference value
			val (valueX, dataBitMap, dataSize) = Grib2RecordDS2.readPackedValues(gribInputStream, drs, gds, valueX1, groupWidth, groupLength)

			when (drs.spatialDiffOrder) { // Code Table 5.6: Order of Spatial Differencing
				1 -> { // First-order spatial differencing
					// For first order spatial differencing, an initial field of values f is replaced by a new field of
					// values g, where
					//         g1 = f1, g2 = f2 – f1, ..., gn = fn – fn-1
					valueX[0] = ival1.toFloat() // g1/f1
					val count = if (drs.missingValueManagement == 0) gds.numberOfDataPoints else dataSize
					for (i in 1 until count) {
						valueX[i] += minsd.toFloat()
						valueX[i] = valueX[i] + valueX[i - 1]
					}
				}
				2 -> { // Second-order spatial differencing
					// For second order spatial differencing, the field of values g is itself replaced by a new field of
					// values h, where
					//         h1 = f1, h2 = f2, h3 = g3 – g2, ..., hn = gn – gn-1
					valueX[0] = ival1.toFloat() // h1/f1
					valueX[1] = ival2.toFloat() // h2/f2
					val count = if (drs.missingValueManagement == 0) gds.numberOfDataPoints else dataSize
					for (i in 2 until count) {
						valueX[i] += minsd.toFloat()
						valueX[i] = valueX[i] + 2 * valueX[i - 1] - valueX[i - 2]
					}
				}
				else -> {
					// FIXME not specified
				}
			}

			val data = Grib2RecordDS2.calculateData(drs, gds, valueX, dataBitMap)

			// bit map is used
			if (bms.indicator != Grib2RecordBMS.Indicator.BITMAP_NONE) {
				Logger.error("Bitmap not supported yet")
				//val idx = 0
				//val tmp = FloatArray(gds.numberOfDataPoints)
				//for (i in 0 until gds.numberOfDataPoints) {
				//	if ((bitmap[i/8] & ))
				//}
			}
			return Grib2RecordDS3(gds, drs, bms, data)
		}
	}

	override var length: Int = 0 // TODO Calculate length

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		TODO("Writing Grid point data - Complex Packing and Spatial Differencing is not supported yet")
	}
}
