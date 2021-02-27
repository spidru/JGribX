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
 * ### [Section 6: Bit-Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 *
 *    | Octet | # | Value                                                |
 *    |-------|---|------------------------------------------------------|
 *    | 1-4   |   | Length of the section in octets (nn)                 |
 *    | 5     |   | Number of the section (6)                            |
 *    | 6     |   | Bit-map indicator (See Table 6.0) (See note 1 below) |
 *    | 7-nn  |   | Bit-map                                              |
 *
 * @param indicatorValue (`6`) Bit-map indicator (see [Table 6.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table6-0.shtml))
 *                             If octet `6` is not zero, the length of this section is `6` and octets `7-nn` are not
 *                             present.
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordBMS internal constructor(private val indicatorValue: Int) : Grib2Section {
	/**
	 * ### [Table 6.0: Bit Map Indicator](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table6-0.shtml)
	 *
	 *    | Code  | Meaning                                                                              |
	 *    |-------|--------------------------------------------------------------------------------------|
	 *    | 0     | A bit map applies to this product and is specified in this section.                  |
	 *    | 1-253 | A bit map pre-determined by the orginating/generating center applies to this product |
	 *    |       | and is not specified in this section.                                                |
	 *    | 254   | A bit map previously defined in the same GRIB2 message applies to this product.      |
	 *    | 255   | A bit map does not apply to this product.                                            |
	 */
	internal enum class Indicator {
		/** A bit map applies to this product and is specified in this section. */
		BITMAP_SPECIFIED,

		/** A bit map pre-determined by the orginating/generating center applies to this product and is not specified
		 *  in this section. */
		BITMAP_PREDETERMINED,

		/** A bit map previously defined in the same GRIB2 message applies to this product. */
		BITMAP_PREDEFINED,

		/** A bit map does not apply to this product. */
		BITMAP_NONE
	}

	companion object {
		internal const val MISSING = 255

		internal fun readFromStream(gribInputStream: GribInputStream): Grib2RecordBMS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 6)

			/* [6] Bitmap indicator */
			val indicatorValue = gribInputStream.readUINT(1)
			if (indicatorValue != MISSING) throw TODO("BMS bitmap not yet supported (indicator: ${indicatorValue})")
			return Grib2RecordBMS(indicatorValue)
					.takeIf { it.length == length }
					?: throw NoValidGribException("BMS length mismatch")
		}
	}

	override val length: Int = 6 // TODO

	override val number: Int = 6

	internal val indicator: Indicator = when (indicatorValue) {
		0 -> Indicator.BITMAP_SPECIFIED
		in 1..253 -> Indicator.BITMAP_PREDETERMINED
		254 -> Indicator.BITMAP_PREDEFINED
		else -> Indicator.BITMAP_NONE
	}

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		outputStream.writeUInt(indicatorValue, bytes = 1)
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordBMS
			&& length == other.length
			&& number == other.number
			&& indicatorValue == other.indicatorValue

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + indicatorValue
		return result
	}
}
