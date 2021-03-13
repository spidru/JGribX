package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.TransformationException
import kotlin.math.ceil
import kotlin.math.log2
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

		internal fun convertFrom(ds: Grib2RecordDS<*>): Grib2RecordDS0 {
			if (ds is Grib2RecordDS0) return ds
			val drs = (ds.drs as? Grib2RecordDRS0)?.let { Grib2RecordDRS0(it) } // Re-instantiate
					?: throw TransformationException("Can't convert ${ds.drs::class.simpleName} to Grib2RecordDS0")
			return Grib2RecordDS0(ds.gds, drs, ds.bms, ds.data)
					.also { it.calculateDataRepresentationParameters() }
		}
	}

	override val length: Int
		get() = 5 + ceil(when (bms.indicator) {
			Grib2RecordBMS.Indicator.BITMAP_SPECIFIED,
			Grib2RecordBMS.Indicator.BITMAP_PREDEFINED -> bms.bitmap.filter { it }.size
			Grib2RecordBMS.Indicator.BITMAP_PREDETERMINED,
			Grib2RecordBMS.Indicator.BITMAP_NONE -> data.size
		}.toDouble() * drs.nBits.toDouble() / 8.0).toInt()

	private fun calculateDataRepresentationParameters() {
		// "D" Scaling
		var decimalScaleFactor = this.data
				.distinct()
				.map { "%f".format(it).split(".").getOrNull(1)?.length ?: 0 }
				.maxOrNull()
				?: 0

		var success = false
		do {
			val data = this.data.copyOf()
			data.forEachIndexed { i, value -> data[i] = value * 10.0.pow(decimalScaleFactor.toDouble()).toFloat() }

			// Reference value R search
			val refValue = data.minOrNull() ?: 0f
			if (ceil(log2(refValue)) / 8.0 > 4) {
				if (decimalScaleFactor > 0) decimalScaleFactor--
				continue
			}
			data.forEachIndexed { i, value -> data[i] = value - refValue }

			// "R" Scaling
			val binaryScaleFactor = 0

			val nBits = ceil(log2(data.maxOrNull()!!)).toInt()
			if (nBits > 8 * 4) {
				if (decimalScaleFactor > 0) decimalScaleFactor--
				continue
			}

			// Set Data Representation parameters
			drs.decimalScaleFactor = decimalScaleFactor
			drs.binaryScaleFactor = binaryScaleFactor
			drs.refValue = refValue
			drs.nBits = nBits
			success = true
			break
		} while (decimalScaleFactor > 0)
		if (!success) throw TransformationException("Cannot calculate Data Representation parameters")
	}

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number

		val decimalFactor = 10.0.pow(drs.decimalScaleFactor).toFloat()
		val binaryFactor = 2.0.pow(drs.binaryScaleFactor).toFloat()
		val data = filterBitmapFrom(data)
		repeat(gds.numberOfDataPoints) { i ->
			//    X   = (   Y    *     10^D      -       R     ) /     2^E
			val value = (data[i] * decimalFactor - drs.refValue) / binaryFactor
			outputStream.writeUBits(value.toInt(), drs.nBits)
		}
		outputStream.fillByte()
	}
}
