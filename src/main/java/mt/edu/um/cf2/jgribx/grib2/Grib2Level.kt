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
package mt.edu.um.cf2.jgribx.grib2

/** Constructor for an instance of [Grib2Level] matching the specified type and value */
class Grib2Level(
		var index: Int,
		var code: String, // provides a unique codename for the level
		var name: String,  // provides a generic name for the level
		var units: String = "",
		internal var value1: Float = 0f,
		internal var value2: Float = 0f,
		description: String? = null) { // describes the level together with any specified values

	companion object {
		fun getLevel(type: Int, value: Float): Grib2Level? = when (type) {
			1 -> Grib2Level(type, "SFC", "ground or water surface")
			2 -> Grib2Level(type, "CBL", "cloud base level")
			3 -> Grib2Level(type, "CTL", "level of cloud tops")
			4 -> Grib2Level(type, "0DEG", "level of 0 degC isotherm")
			5 -> Grib2Level(type, "ADCL", "level of adiabatic condensation lifted from the surface")
			6 -> Grib2Level(type, "MWSL", "maximum wind level")
			7 -> Grib2Level(type, "TRO", "tropopause")
			100 -> Grib2Level(type, "ISBL", "isobaric surface",
					units = "hPa",
					value1 = value / 100,
					description = "pressure at ${(value / 100).toInt()}hPa")
			101 -> Grib2Level(type, "MSL", "mean sea level")
			102 -> Grib2Level(type, "GPML", "specified altitude above MSL",
					units = "m",
					value1 = value,
					description = "${value}m above MSL")
			103 -> Grib2Level(type, "TGL", "Specified height level above ground",
					units = "m",
					value1 = value,
					description = "${value}m above ground")
			105 -> Grib2Level(type, "HYBL", "Hybrid level",
					value1 = value,
					description = "${value} hybrid level(s)")
			106 -> Grib2Level(type, "DBLL", "depth below land surface",
					units = "m")
			108 -> Grib2Level(type, "SPDL", "level at specified pressure difference from ground to level",
					units = "hPa",
					value1 = value,
					description = "${value}hPa pressure difference from ground")
			200 -> Grib2Level(type, "EATM", "entire atmosphere (considered as a single layer)")
			204 -> Grib2Level(type, "HTFL", "highest tropospheric freezing level")
			220 -> Grib2Level(type, "PBL", "planetary boundary layer")
			else -> null
		}
	}

	val description: String = description ?: name

	val levelIdentifier: String
		get() = "$code:$value1"

	val values: FloatArray
		get() = floatArrayOf(value1, value2)
}
