package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
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
 */
class Grib2RecordDS0 private constructor(length: Int, data: FloatArray) : Grib2RecordDS(length, data) {
	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									gds: Grib2RecordGDS,
									drs: Grib2RecordDRS0,
									bms: Grib2RecordBMS): Grib2RecordDS0? {
			val length = gribInputStream.readUINT(4)
			val section = gribInputStream.readUINT(1)
			if (section != 7) {
				Logger.error("DS contains an incorrect section number ${section}!")
				return null
			}

			val decimalFactor = 10.0.pow(drs.decimalScaleFactor).toFloat()
			val binaryFactor = 2.0.pow(drs.binaryScaleFactor).toFloat()
			val data = FloatArray(gds.numberOfDataPoints)
			repeat(gds.numberOfDataPoints) { i ->
				val x = gribInputStream.readUBits(drs.nBits).toFloat()
				// Y    = (     R   + (X1 + X2) *  2^E    ) /    10^D
				data[i] = (drs.refValue + x * binaryFactor) / decimalFactor
			}
			gribInputStream.seekNextByte()

			return Grib2RecordDS0(length, data)
		}
	}
}
