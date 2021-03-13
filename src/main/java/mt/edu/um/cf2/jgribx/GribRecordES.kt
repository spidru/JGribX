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

import mt.edu.um.cf2.jgribx.api.GribSection

/**
 * @author AVLAB-USER3
 */
class GribRecordES internal constructor(internal val isValid: Boolean) : GribSection {
	companion object {
		fun seekNext(gribInputStream: GribInputStream) {
			var nBytes = 0
			var countBytes = 0
			val code = IntArray(4)
			while (gribInputStream.available() > 0) {
				code[countBytes] = gribInputStream.readUINT(1)
				nBytes++

				if (code[0].toChar() == '7'
						&& code[1].toChar() == '7'
						&& code[2].toChar() == '7'
						&& code[3].toChar() == '7') break

				countBytes++
				if (countBytes == 4) countBytes = 0
			}
			Logger.info("Skipped ${nBytes} bytes to end of record")
		}

		internal fun readFromStream(gribInputStream: GribInputStream): GribRecordES {
			val octets = gribInputStream.read(4)
			val code = String(octets)
			val isValid = if (code != "7777") {
				Logger.error("Record has not ended correctly.")
				false
			} else {
				true
			}
			return GribRecordES(isValid)
		}
	}

	override val length: Int = 4

	override val number: Int = 8

	override fun writeTo(outputStream: GribOutputStream) {
		Logger.debug("Writing GRIB End Section (ES) - 4 bytes")
		outputStream.write("7777".toByteArray())
	}

	override fun equals(other: Any?) = this === other
			|| other is GribRecordES
			&& length == other.length
			&& number == other.number
			&& isValid == other.isValid

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + isValid.hashCode()
		return result
	}
}
