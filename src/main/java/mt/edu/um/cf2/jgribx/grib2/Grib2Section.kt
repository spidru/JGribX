package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.api.GribSection


/**
 * ### Common section part
 *
 *    | Octet | # | Value                           |
 *    |-------|---|---------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn |
 *    | 5     | 1 | Number of Section               |
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface Grib2Section : GribSection {
	companion object {
		internal fun peekFromStream(gribInputStream: GribInputStream): Pair<Int, Int> {
			gribInputStream.mark(5)

			/* [1-4] Section Length */
			val length = gribInputStream.readUInt(4)

			/* [5] Section Number */
			val number = gribInputStream.readUInt(1)

			gribInputStream.reset()
			gribInputStream.resetBitCounter()
			return length to number
		}

		internal fun readFromStream(gribInputStream: GribInputStream, sectionNumber: Int): Int {
			/* [1-4] Section Length */
			val length = gribInputStream.readUInt(4)

			/* [5] Section Number */
			val number = gribInputStream.readUInt(1)
			if (number != sectionNumber) throw Exception("Incorrect section number ${number}")
			return length
		}
	}

	/** (`1-4`) Length of section in octets: `nn` */
	override val length: Int

	/** (`5`) Number of Section */
	val number: Int

	/** Writes section to target [output stream][GribOutputStream] */
	override fun writeTo(outputStream: GribOutputStream) {
		outputStream.writeUInt(length, bytes = 4) // [1-4] Section Length
		outputStream.writeUInt(number, bytes = 1) // [5] Section Number
	}
}
