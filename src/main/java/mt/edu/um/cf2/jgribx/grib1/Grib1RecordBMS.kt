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

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.NoValidGribException

/**
 * A class that represents the bitmap section (BMS) of a GRIB record. It
 * indicates grid points where no parameter value is defined.
 *
 * Constructs a <tt>GribRecordBMS</tt> object from a bit input stream.
 *
 * @param gribInputStream bit input stream with BMS content
 *
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author  Benjamin Stark
 * @version 1.0
 */
class Grib1RecordBMS(gribInputStream: GribInputStream) {
	/** Length in bytes of this section. */
	var length: Int
		protected set

	/** The bit map. */
	var bitmap: BooleanArray
		protected set

	init {
		val bitmask = intArrayOf(128, 64, 32, 16, 8, 4, 2, 1)
		var octets = ByteArray(3)
		gribInputStream.read(octets)

		// octets 1-3 (length of section)
		length = Bytes2Number.bytesToUint(octets.copyOfRange(0, 3))

		// read rest of section
		octets = ByteArray(length - 3)
		gribInputStream.read(octets)
		val nBitsUnused = Bytes2Number.bytesToUint(octets[0])

		// octets 5-6
		if (octets[1].toInt() != 0 || octets[2].toInt() != 0) throw NoValidGribException("GribRecordBMS: No bit map defined here.")

		// create new bit map, octet 4 contains number of unused bits at the end
		bitmap = BooleanArray((length - 6) * 8 - nBitsUnused)

		// fill bit map
		for (i in bitmap.indices) bitmap[i] = octets[i / 8 + 3].toInt() and bitmask[i % 8] != 0
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 BMS section:",
			"\tBitmap length: ${bitmap.size}")
			.joinToString("\n")
}
