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
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.NoValidGribException
import kotlin.math.ceil

/**
 * ### [Section 3: Bit-map section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/3/)
 *
 *     | Octets | # | Key                               | Type     | Content                                          |
 *     |--------|---|-----------------------------------|----------|--------------------------------------------------|
 *     | 1-3    | 3 | section4Length                    | unsigned | Length of section                                |
 *     | 4      | 1 | numberOfUnusedBitsAtEndOfSection3 | unsigned | Number of unused bits at end of Section 3        |
 *     | 5-6    | 2 | tableReference                    | unsigned | Table reference: If the octets contain zero, a   |
 *     |        |   |                                   |          | bit-map follows If the octets contain a number,  |
 *     |        |   |                                   |          | it refers to a predetermined bit-map provided by |
 *     |        |   |                                   |          | the centre                                       |
 *     | 7-nn   |   |                                   |          | The bit-map contiguous bits with a bit to data   |
 *     |        |   |                                   |          | point correspondence, ordered as defined in the  |
 *     |        |   |                                   |          | grid definition                                  |
 *
 * A class that represents the bitmap section (BMS) of a GRIB record. It
 * indicates grid points where no parameter value is defined.
 *
 * Constructs a <tt>GribRecordBMS</tt> object from a bit input stream.
 *
 * @param numberOfUnusedBitsAtEndOfSection3 (`4`)    Number of unused bits at end of Section 3
 * @param tableReference                    (`5-6`)  Table reference: If the octets contain zero, a bit-map follows If
 *                                                   the octets contain a number, it refers to a predetermined bit-map
 *                                                   provided by the centre
 * @param bitmap                            (`7-nn`) The bit-map contiguous bits with a bit to data point
 *                                                   correspondence, ordered as defined in the grid definition
 *
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author  Benjamin Stark
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib1RecordBMS(private val numberOfUnusedBitsAtEndOfSection3: Int,
					 private val tableReference: Int,
					 val bitmap: BooleanArray) : Grib1Section {
	companion object {
		private val BITMASK = intArrayOf(128, 64, 32, 16, 8, 4, 2, 1)

		internal fun readFromStream(gribInputStream: GribInputStream): Grib1RecordBMS {
			/* [1-3] Section Length */
			val length = Grib1Section.readFromStream(gribInputStream)

			// [4] Number of unused bits at end of Section 3
			val numberOfUnusedBitsAtEndOfSection3 = gribInputStream.readUINT(1)

			// [5-6] Table reference: If the octets contain zero, a bit-map follows If the octets contain a number,
			//       it refers to a predetermined bit-map provided by the centre
			val tableReference = gribInputStream.readUINT(2)
			if (tableReference != 0) throw NoValidGribException("BMS: No bit map defined here.")

			// [7-nn]
			val bytes = gribInputStream.read(length - 6)
			val bitmap = BooleanArray((length - 6) * 8 - numberOfUnusedBitsAtEndOfSection3)

			bitmap.indices.forEach { i -> // fill bit map
				bitmap[i] = bytes[i / 8].toInt() and BITMASK[i % 8] != 0
			}

			return Grib1RecordBMS(numberOfUnusedBitsAtEndOfSection3, tableReference, bitmap)
					.takeIf { it.length == length }
					?: throw NoValidGribException("BMS length mismatch")
		}
	}

	override val length: Int
		get() = 6 + ceil(bitmap.size.toDouble() / 8.0).toInt()

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream)  // [1-3] length
		outputStream.writeUInt(numberOfUnusedBitsAtEndOfSection3, bytes = 1)
		outputStream.writeUInt(tableReference, bytes = 2)
		//val bytes = ByteArray(ceil(bitmap.size / 8.0).toInt())
		//bitmap.map { if (it) 0x01 else 0x00 }.forEachIndexed { i, bit ->
		//	bytes[i / 8] = (bytes[i / 8].toInt() or bit shl (i % 8)).toByte()
		//}
		//outputStream.write(bytes)
		(bitmap + BooleanArray(numberOfUnusedBitsAtEndOfSection3)).toList()
				.map { if (it) 0x01 else 0x00 }
				.mapIndexed { i, bit -> bit shl (8 - i % 8 - 1) }
				.windowed(8, 8)
				.map { it.sum() }
				.map { it.toByte() }
				.toByteArray()
				.also { outputStream.write(it) }
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1RecordBMS
			&& length == other.length
			&& numberOfUnusedBitsAtEndOfSection3 == other.numberOfUnusedBitsAtEndOfSection3
			&& tableReference == other.tableReference
			&& bitmap.mapIndexed { i, b -> b to other.bitmap.getOrNull(i) }.all { (a, b) -> a == b }

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + numberOfUnusedBitsAtEndOfSection3
		result = 31 * result + tableReference
		result = 31 * result + bitmap.contentHashCode()
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 BMS section:",
			"\tBitmap length: ${bitmap.size}")
			.joinToString("\n")
}
