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
package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.Bytes2Number.bytesToLong
import mt.edu.um.cf2.jgribx.grib2.ProductDiscipline
import java.io.IOException

/**
 * A class that represents the indicator section (IS) of a GRIB record.
 *
 * 01/01/2001   Benjamin Stark          first version
 * 16/09/2002   Richard D. Gonzalez     modified to indicate support of GRIB edition 1 only
 * 24/05/2017   Andrew Spiteri          added support for GRIB2
 *
 * @property gribEdition Edition of GRIB specification used.
 * @property length Length in bytes of IS section.
 *                  Section recordLength differs between GRIB editions 1 and 2
 *                  Currently only GRIB edition 1 supported - recordLength is 8 octets/bytes.
 * @property recordLength Length in bytes of GRIB record.
 */
class GribRecordIS(var gribEdition: Int,
				   val length: Int,
				   val recordLength: Long) {
	companion object {
		/**
		 * Constructs a [GribRecordIS] object from a buffered input stream.
		 *
		 * @param gribInputStream [GribInputStream] containing IS content
		 * @return
		 * @throws NotSupportedException
		 * @throws mt.edu.um.cf2.jgribx.NoValidGribException
		 * @throws IOException           if stream can not be opened etc.
		 */
		fun readFromStream(gribInputStream: GribInputStream): Pair<GribRecordIS, ProductDiscipline?> {
			val octets = ByteArray(16)
			gribInputStream.read(octets, 0, 8)
			val startCode = String(octets.copyOfRange(0, 4))
			if (startCode != "GRIB") {
				Logger.info("Start code not recognised: ${startCode}")
				throw NoValidGribException("Record does not have a valid GRIB header")
			}

			// check GRIB edition number
			val gribEdition = octets[7].toInt()
			val length: Int
			val discipline: ProductDiscipline?
			val recordLength: Long
			when (gribEdition) {
				1 -> {
					length = 8
					discipline = null
					recordLength = bytesToLong(octets.copyOfRange(4, 7))
				}
				2 -> {
					length = 16
					discipline = ProductDiscipline.VALUES.getOrNull(octets[6].toInt())
					gribInputStream.read(octets, 8, 8)
					recordLength = bytesToLong(octets.copyOfRange(8, 16))
				}
				else -> throw NotSupportedException("GRIB edition ${gribEdition} is not yet supported")
			}

			return GribRecordIS(gribEdition, length, recordLength) to discipline
		}

		/**
		 * Seeks the location of the next IS header.
		 * If a valid header is found, the input stream is repositioned to the position just before the found header.
		 *
		 * @param gribInputStream GRIB input stream to read from
		 * @throws IOException
		 */
		fun seekNext(gribInputStream: GribInputStream) {
			val code = ByteArray(4)
			var bytesSkipped = 0
			while (gribInputStream.available() > 0) {
				gribInputStream.mark(4)
				gribInputStream.read(code)
				if (code.contentEquals("GRIB".toByteArray())) {
					gribInputStream.reset()
					break
				}
				gribInputStream.reset()
				gribInputStream.read(1) // skip 1 byte
				bytesSkipped++
			}
			if (bytesSkipped > 0) {
				Logger.warning("Extra ${bytesSkipped} bytes were found between end of last record and start of next record")
			}
		}
	}

	override fun toString(): String = listOfNotNull(
			"GRIB IS section:",
			"\tGrib edition ${gribEdition}",
			"\tLength: ${recordLength} bytes")
			.joinToString("\n")
}
