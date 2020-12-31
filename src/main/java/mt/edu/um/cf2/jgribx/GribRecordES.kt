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

/**
 * @author AVLAB-USER3
 */
class GribRecordES(gribInputStream: GribInputStream) {
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
	}

	var isValid = false

	init {
		val octets = gribInputStream.read(4)
		val code = String(octets)
		isValid = if (code != "7777") {
			Logger.error("Record has not ended correctly.")
			false
		} else {
			true
		}
	}
}