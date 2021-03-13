package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import kotlin.math.pow

/**
 * ### [7.0 Grid point data - simple packing](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp7-0.shtml)
 *
 *    | Octet | # | Value                                                                  |
 *    |-------|---|------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn                                        |
 *    | 5     | 1 | Number of Section: 7                                                   |
 *    | 6-nn  |   | Binary data values - binary string, with each (scaled) data value (X2) |
 *
 *     Y = ( R + (X1 + X2) * 2^E ) / 10^D
 *   where `X1 = 0`, `X2 = X` (the value)
 *
 * @param gds Reference to [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 * @param drs Reference to [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 * @param bms Reference to [Section 6: Bit Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 * @param data Decoded data
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordDS0 internal constructor(gds: Grib2RecordGDS,
										  drs: Grib2RecordDRS0,
										  bms: Grib2RecordBMS,
										  data: FloatArray) :
		Grib2RecordDS<Grib2RecordDRS0>(gds, drs, bms, data) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									gds: Grib2RecordGDS,
									drs: Grib2RecordDRS0,
									bms: Grib2RecordBMS): Grib2RecordDS0 {
			val decimalFactor = 10.0.pow(drs.decimalScaleFactor).toFloat()
			val binaryFactor = 2.0.pow(drs.binaryScaleFactor).toFloat()
			val data = FloatArray(gds.numberOfDataPoints)
			repeat(gds.numberOfDataPoints) { i ->
				val x = gribInputStream.readUBits(drs.nBits).toFloat()
				// Y    = (     R   + (X1 + X2) *  2^E    ) /    10^D
				data[i] = (drs.refValue + x * binaryFactor) / decimalFactor
			}
			gribInputStream.seekNextByte()

			return Grib2RecordDS0(gds, drs, bms, data)
		}
	}

	override val length: Int
		get() = 5 + data.size

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number

		val decimalFactor = 10.0.pow(drs.decimalScaleFactor).toFloat()
		val binaryFactor = 2.0.pow(drs.binaryScaleFactor).toFloat()
		repeat(gds.numberOfDataPoints) { i ->
			//    X   = (   Y    *     10^D      -       R     ) /     2^E
			val value = (data[i] * decimalFactor - drs.refValue) / binaryFactor
			outputStream.writeUBits(value.toInt(), drs.nBits)
		}
		outputStream.fillByte()
	}
}
