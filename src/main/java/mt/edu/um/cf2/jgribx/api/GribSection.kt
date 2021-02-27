package mt.edu.um.cf2.jgribx.api

import mt.edu.um.cf2.jgribx.GribOutputStream


/**
 * ### Common section part
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribSection {
	/** Length of section in octets: `nn` */
	val length: Int

	/** Writes section to target [output stream][GribOutputStream] */
	fun writeTo(outputStream: GribOutputStream)
}
