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
 * GRIB End Section
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class GribRecordES internal constructor(internal val isValid: Boolean) : GribSection {
	companion object {
		private val SEQUENCE = "7777".toByteArray()

		internal fun readFromStream(gribInputStream: GribInputStream): GribRecordES {
			val code = gribInputStream.read(4)
			val isValid = if (!code.contentEquals(SEQUENCE)) {
				Logger.error("Record has not ended correctly.")
				false
			} else true

			return GribRecordES(isValid)
		}

		fun seekNext(gribInputStream: GribInputStream) {
			var bytesSkipped = 0
			while (gribInputStream.available() > 0) {
				if (gribInputStream.peek(4).contentEquals(SEQUENCE)) break
				gribInputStream.read(1) // skip 1 byte
				bytesSkipped++
			}
			if (bytesSkipped > 0) Logger.info("Skipped ${bytesSkipped} bytes to end of record")
		}
	}

	override val length: Int = 4

	override fun writeTo(outputStream: GribOutputStream) {
		Logger.debug("Writing GRIB End Section (ES) - 4 bytes")
		outputStream.write(SEQUENCE)
	}

	override fun equals(other: Any?) = this === other
			|| other is GribRecordES
			&& length == other.length
			&& isValid == other.isValid

	override fun hashCode() = 31 * length + isValid.hashCode()
}
