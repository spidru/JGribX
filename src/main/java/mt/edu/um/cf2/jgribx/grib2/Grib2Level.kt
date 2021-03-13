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

/**
 * Constructor for an instance of [Grib2Level] matching the specified type and value
 *
 * - [https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-5.shtml]
 * - [http://nimbus.com.uy/weather/grads/grib_levels.html]
 */
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
			//0 -> Grib2Level(type, "RES", "reserved")
			1 -> Grib2Level(type, "SFC", "ground or water surface")
			2 -> Grib2Level(type, "CBL", "cloud base level")
			3 -> Grib2Level(type, "CTL", "level of cloud tops")
			4 -> Grib2Level(type, "0DEG", "level of 0 degC isotherm")
			5 -> Grib2Level(type, "ADCL", "level of adiabatic condensation lifted from the surface")
			6 -> Grib2Level(type, "MWSL", "maximum wind level")
			7 -> Grib2Level(type, "TRO", "tropopause")
			8 -> Grib2Level(type, "NTAT", "nominal top of the atmosphere")
			9 -> Grib2Level(type, "SEAB", "sea bottom")
			10 -> Grib2Level(type, "EATM", "entire atmosphere")
			//11 -> Grib2Level(type, "", "cumulonimbus Base (CB)",
			//		units = "m",
			//		value1 = value)
			//12 -> Grib2Level(type, "", "cumulonimbus Top (CT)",
			//		units = "m",
			//		value1 = value)
			//13 -> Grib2Level(type, "", "lowest level where vertically integrated cloud cover exceeds the specified percentage (cloud base for a given percentage cloud cover)",
			//		units = "%",
			//		value1 = value)
			//14 -> Grib2Level(type, "", "level of free convection (LFC)")
			//15  -> Grib2Level(type, "", "convection condensation level (CCL)")
			//16 -> Grib2Level(type, "", "level of neutral buoyancy or equilibrium (LNB)")
			//in 17..19 -> Grib2Level(type, "", "reserved")
			20 -> Grib2Level(type, "TMPL", "isothermal level",
					units = "K",
					value1 = value,
					description = "${value}K isoterm")
			//21 -> Grib2Level(type, "", "lowest level where mass density exceeds the specified value (base for a given threshold of mass density)",
			//		units = "kg m-3",
			//		value1 = value)
			//22 -> Grib2Level(type, "", "highest level where mass density exceeds the specified value (top for a given threshold of mass density)",
			//		units = "kg m-3",
			//		value1 = value)
			//23 -> Grib2Level(type, "", "lowest level where air concentration exceeds the specified value (base for a given threshold of air concentration",
			//		units = "Bq m-3",
			//		value1 = value)
			//24 -> Grib2Level(type, "", "highest level where air concentration exceeds the specified value (top for a given threshold of air concentration)",
			//		units = "Bq m-3",
			//		value1 = value)
			//25 -> Grib2Level(type, "", "highest level where radar reflectivity exceeds the specified value (echo top for a given threshold of reflectivity)",
			//		units = "dBZ",
			//		value1 = value)
			//in 26..99 -> Grib2Level(type, "", "Reserved")
			100 -> Grib2Level(type, "ISBL", "isobaric surface",
					units = "hPa",
					value1 = value / 100,
					description = "pressure at ${(value / 100).toInt()}hPa")
			101 -> Grib2Level(type, "MSL", "mean sea level")
			102 -> Grib2Level(type, "GPML", "specified altitude above MSL",
					units = "m",
					value1 = value,
					description = "${value}m above MSL")
			103 -> Grib2Level(type, "TGL", "specified height level above ground",
					units = "m",
					value1 = value,
					description = "${value}m above ground")
			104 -> Grib2Level(type, "SIGM", "sigma level",
					value1 = value,
					description = "${value} sigma level")
			105 -> Grib2Level(type, "HYBL", "Hybrid level",
					value1 = value,
					description = "${value} hybrid level(s)")
			106 -> Grib2Level(type, "DBLL", "depth below land surface",
					units = "m")
			//107 -> Grib2Level(type, "", "isentropic (theta) level")
			108 -> Grib2Level(type, "SPDL", "level at specified pressure difference from ground to level",
					units = "hPa",
					value1 = value,
					description = "${value}hPa pressure difference from ground")
			109 -> Grib2Level(type, "PVL", "potential vorticity surface",
					units = "K m2 kg-1 s-1",
					value1 = value)
			//110 -> Grib2Level(type, "", "Reserved")
			111 -> Grib2Level(type, "ETAL", "eta Level")
			//112 -> Grib2Level(type, "", "Reserved")
			//113 -> Grib2Level(type, "", "logarithmic hybrid level")
			//114 -> Grib2Level(type, "", "snow Level",
			//		units = "Numeric",
			//		value1 = value)
			//115 -> Grib2Level(type, "", "sigma height level") // see Note 4
			//116 -> Grib2Level(type, "", "Reserved")
			//117 -> Grib2Level(type, "", "mixed layer depth",
			//		units = "m",
			//		value1 = value)
			//118 -> Grib2Level(type, "", "hybrid height level")
			//119 -> Grib2Level(type, "", "hybrid pressure level")
			//in 120..149 -> Grib2Level(type, "", "Reserved")
			//150 -> Grib2Level(type, "", "generalized vertical height coordinate") // see Note 4
			//151 -> Grib2Level(type, "", "soil level", // see Note 5
			//		units = "Numeric",
			//		value1 = value)
			//in 152..159 -> Grib2Level(type, "", "Reserved")
			//160 -> Grib2Level(type, "", "depth below sea level",
			//		units = "m",
			//		value1 = value)
			//161 -> Grib2Level(type, "", "depth below water surface",
			//		units = "m",
			//		value1 = value)
			//162 -> Grib2Level(type, "", "lake or river bottom")
			//163 -> Grib2Level(type, "", "bottom of sediment layer")
			//164 -> Grib2Level(type, "", "bottom of thermally active sediment layer")
			//165 -> Grib2Level(type, "", "bottom of sediment layer penetrated by thermal wave")
			//166 -> Grib2Level(type, "", "mixing layer")
			//167 -> Grib2Level(type, "", "bottom of root zone")
			//168 -> Grib2Level(type, "", "ocean model level",
			//		units = "Numeric",
			//		value1 = value)
			//169 -> Grib2Level(type, "", "ocean level defined by water density (sigma-theta) difference from near-surface to level", // see Note 7
			//		units = "kg m-3",
			//		value1 = value)
			//170 -> Grib2Level(type, "", "ocean level defined by water potential temperature difference from near-surface to level", // see Note 7
			//		units = "K",
			//		value1 = value)
			//in 171..173 -> Grib2Level(type, "", "Reserved")
			//174 -> Grib2Level(type, "", "top surface of ice on sea, lake or river")
			//175 -> Grib2Level(type, "", "top surface of ice, under snow, on sea, lake or river")
			//176  -> Grib2Level(type, "", "bottom surface (underside) ice on sea, lake or river")
			//177 -> Grib2Level(type, "", "deep soil (of indefinite depth)")
			//178 -> Grib2Level(type, "", "reserved")
			//179  -> Grib2Level(type, "", "top surface of glacier ice and inland ice")
			//180 -> Grib2Level(type, "", "deep inland or glacier ice (of indefinite depth)")
			//181 -> Grib2Level(type, "", "grid tile land fraction as a model surface")
			//182 -> Grib2Level(type, "", "grid tile water fraction as a model surface")
			//183 -> Grib2Level(type, "", "grid tile ice fraction on sea, lake or river as a model surface")
			//184  -> Grib2Level(type, "", "grid tile glacier ice and inland ice fraction as a model surface")
			//in 185..191 -> Grib2Level(type, "", "Reserved")
			//in 192..254 -> Grib2Level(type, "", "Reserved for Local Use")
			200 -> Grib2Level(type, "EATM", "entire atmosphere (considered as a single layer)")
			201 -> Grib2Level(type, "EOCN", "entire ocean (considered as a single layer)")
			204 -> Grib2Level(type, "HTFL", "highest tropospheric freezing level")
			//206 -> Grib2Level(type, "", "grid scale cloud bottom level")
			//207 -> Grib2Level(type, "", "grid scale cloud top level")
			209 -> Grib2Level(type, "BCBL", "boundary layer cloud bottom level")
			210 -> Grib2Level(type, "BCTL", "boundary layer cloud top level")
			211 -> Grib2Level(type, "BCY", "boundary layer cloud layer")
			212 -> Grib2Level(type, "LCBL", "low cloud bottom level")
			213 -> Grib2Level(type, "LCTL", "low cloud top level")
			214 -> Grib2Level(type, "LCY", "low cloud layer")
			//215 -> Grib2Level(type, "", "cloud ceiling")
			//216 -> Grib2Level(type, "", "effective layer top level",
			//		units = "m",
			//		value1 = value)
			//217 -> Grib2Level(type, "", "effective layer bottom level",
			//		units = "m",
			//		value1 = value)
			//218 -> Grib2Level(type, "", "effective layer",
			//		units = "m",
			//		value1 = value)
			220 -> Grib2Level(type, "PBL", "planetary boundary layer")
			222 -> Grib2Level(type, "MCBL", "Middle cloud bottom level")
			223 -> Grib2Level(type, "MCTL", "Middle cloud top level")
			224 -> Grib2Level(type, "MCY", "Middle cloud layer")
			232 -> Grib2Level(type, "HCBL", "High cloud bottom level")
			233 -> Grib2Level(type, "HCTL", "High cloud top level")
			234 -> Grib2Level(type, "HCY", "High cloud layer")
			242 -> Grib2Level(type, "CCBL", "Convective cloud bottom level")
			243 -> Grib2Level(type, "CCTL", "Convective cloud top level")
			244 -> Grib2Level(type, "CCY", "Convective cloud layer")
			else -> null
		}
	}

	val description: String = description ?: name

	val levelIdentifier: String
		get() = "$code:$value1"

	val values: FloatArray
		get() = floatArrayOf(value1, value2)
}
