package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.api.GribSection


/**
 * ### Common section part
 *
 *    | Octet | # | Value                           |
 *    |-------|---|---------------------------------|
 *    | 1-3   | 3 | Length of section in octets: nn |
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface Grib1Section : GribSection {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Int {
			/* [1-3] Section Length */
			return gribInputStream.readUINT(3)
		}
	}

	/** (`1-4`) Length of section in octets: `nn` */
	override val length: Int

	/** Writes section to target [output stream][GribOutputStream] */
	override fun writeTo(outputStream: GribOutputStream) {
		outputStream.writeUInt(length, bytes = 3) // [1-3] Section Length
	}
}
