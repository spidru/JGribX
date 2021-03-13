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
import mt.edu.um.cf2.jgribx.api.GribSection
import mt.edu.um.cf2.jgribx.grib2.ProductDiscipline
import java.io.IOException

/**
 * ### Section 0: Indicator Section
 *
 * #### GRIB 1
 *    | Octet | # | Value                                                                   |
 *    |-------|---|-------------------------------------------------------------------------|
 *    | 1-4   | 4 | "GRIB" (coded according to the International Alphabet No. 5)            |
 *    | 5-7   | 3 | Total length of GRIB message in octets (including Section 0)            |
 *    | 8     | 1 | GRIB Edition Number (here 1)                                            |
 *
 * #### GRIB 2
 *    | Octet | # | Value                                                                   |
 *    |-------|---|-------------------------------------------------------------------------|
 *    | 1-4   | 4 | "GRIB" (coded according to the International Alphabet No. 5)            |
 *    | 5-6   | 2 | Reserved                                                                |
 *    | 7     | 1 | Discipline – GRIB Master Table Number (see Code Table 0.0)              |
 *    | 8     | 1 | GRIB Edition Number (here 2)                                            |
 *    | 9-16  | 8 | Total length of GRIB message in octets (including Section 0)            |
 *
 * A class that represents the indicator section (IS) of a GRIB record.
 *
 * 01/01/2001   Benjamin Stark          first version
 * 16/09/2002   Richard D. Gonzalez     modified to indicate support of GRIB edition 1 only
 * 24/05/2017   Andrew Spiteri          added support for GRIB2
 *
 * @param gribEdition Edition of GRIB specification used.
 * @param discipline Discipline – GRIB Master Table Number (see [Code Table 0.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table0-0.shtml))
 * @param recordLength Length in bytes of GRIB record.
 *
 * @author Benjamin Stark
 * @author Richard D. Gonzalez
 * @author Andrew Spiteri
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class GribRecordIS internal constructor(var gribEdition: Int,
										var discipline: ProductDiscipline?,
										val recordLength: Long) : GribSection {

	override val number: Int = 0

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
		fun readFromStream(gribInputStream: GribInputStream): GribRecordIS {
			val octets = ByteArray(16)
			gribInputStream.read(octets, 0, 8)
			val startCode = String(octets.copyOfRange(0, 4))
			if (startCode != "GRIB") {
				Logger.info("Start code not recognised: ${startCode}")
				throw NoValidGribException("Record does not have a valid GRIB header")
			}

			// check GRIB edition number
			val gribEdition = octets[7].toInt()
			val discipline: ProductDiscipline?
			val recordLength: Long
			when (gribEdition) {
				1 -> {
					discipline = null
					recordLength = bytesToLong(octets.copyOfRange(4, 7))
				}
				2 -> {
					discipline = ProductDiscipline.VALUES[octets[6].toInt()]
							?: TODO("Discipline ${octets[6].toInt()} not implemented")
					gribInputStream.read(octets, 8, 8)
					recordLength = bytesToLong(octets.copyOfRange(8, 16))
				}
				else -> throw NotSupportedException("GRIB edition ${gribEdition} is not yet supported")
			}

			return GribRecordIS(gribEdition, discipline, recordLength)
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

	/**
	 * Length in bytes of IS section. Section recordLength differs between GRIB editions 1 and 2. Currently only GRIB
	 * edition 1 supported - recordLength is 8 octets/bytes.
	 */
	override val length: Int
		get() = when (gribEdition) {
			1 -> 8
			2 -> 16
			else -> throw NotSupportedException("GRIB edition ${gribEdition} is not yet supported")
		}


	override fun writeTo(outputStream: GribOutputStream) {
		Logger.debug("Writing GRIB${gribEdition} Indicator Section (IS) - ${length} bytes (total: ${recordLength} bytes)")
		outputStream.write("GRIB".toByteArray())
		when (gribEdition) {
			1 -> {
				outputStream.writeLong(recordLength, bytes = 3) // [5-7] Record length
				outputStream.writeUInt(gribEdition, bytes = 1) // [8] GRIB Edition
			}
			2 -> {
				outputStream.write(byteArrayOf(0, 0)) // [5-6] Reserved
				outputStream.writeUInt(discipline?.value ?: ProductDiscipline.MISSING, bytes = 1) // [7] Discipline
				outputStream.writeUInt(gribEdition, bytes = 1) // [8] GRIB Edition
				outputStream.writeLong(recordLength, bytes = 8) // [9-16] Record length
			}
			else -> throw Exception("Unsupported GRIB edition ${gribEdition}")
		}
	}

	override fun equals(other: Any?) = this === other
			|| other is GribRecordIS
			&& length == other.length
			&& number == other.number
			&& gribEdition == other.gribEdition
			&& discipline == other.discipline
			&& recordLength == other.recordLength

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + gribEdition
		result = 31 * result + (discipline?.hashCode() ?: 0)
		result = 31 * result + recordLength.hashCode()
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB IS section:",
			"\tGrib edition ${gribEdition}",
			discipline?.let { "\tDiscipline: ${it}" },
			"\tLength: ${length}",
			"\tRecord length: ${recordLength} bytes")
			.joinToString("\n")
}
