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

import mt.edu.um.cf2.jgribx.api.GribLevel

/**
 * ### [Code Table 4.5: Fixed surface types and units](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-5.shtml)
 *
 * See also [weather level on nimbus.com.uy](http://nimbus.com.uy/weather/grads/grib_levels.html)
 *
 * @param id Level ID/index (code figure)
 * @param code A unique codename for the level
 * @param name A generic name for the level
 * @param value Parameter value
 * @param units Value units
 * @param description Describes the level together with any specified values
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2Level private constructor(override val id: Int,
									 override val code: String,
									 override val name: String,
									 override val value: Float,
									 private val units: String = "",
									 description: String? = null) : GribLevel {

	companion object {
		fun getLevel(type: Int, value: Float): Grib2Level? = when (type) {
			//0 -> Grib2Level(type, "RES", "reserved")
			1 -> Grib2Level(type, "SFC", "ground or water surface", value)
			2 -> Grib2Level(type, "CBL", "cloud base level", value)
			3 -> Grib2Level(type, "CTL", "level of cloud tops", value)
			4 -> Grib2Level(type, "0DEG", "level of 0 degC isotherm", value)
			5 -> Grib2Level(type, "ADCL", "level of adiabatic condensation lifted from the surface", value)
			6 -> Grib2Level(type, "MWSL", "maximum wind level", value)
			7 -> Grib2Level(type, "TRO", "tropopause", value)
			8 -> Grib2Level(type, "NTAT", "nominal top of the atmosphere", value)
			9 -> Grib2Level(type, "SEAB", "sea bottom", value)
			10 -> Grib2Level(type, "EATM", "entire atmosphere", value)
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
			20 -> Grib2Level(type, "TMPL", "isothermal level", value, units = "K",
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
			100 -> Grib2Level(type, "ISBL", "isobaric surface", value, units = "Pa",
					description = "pressure at ${(value / 100).toInt()}hPa")
			101 -> Grib2Level(type, "MSL", "mean sea level", value)
			102 -> Grib2Level(type, "GPML", "specified altitude above MSL", value, units = "m",
					description = "${value}m above MSL")
			103 -> Grib2Level(type, "TGL", "specified height level above ground", value, units = "m",
					description = "${value}m above ground")
			104 -> Grib2Level(type, "SIGM", "sigma level", value,
					description = "${value} sigma level")
			105 -> Grib2Level(type, "HYBL", "Hybrid level", value,
					description = "${value} hybrid level(s)")
			106 -> Grib2Level(type, "DBLL", "depth below land surface", value, units = "m")
			//107 -> Grib2Level(type, "", "isentropic (theta) level")
			108 -> Grib2Level(type, "SPDL", "level at specified pressure difference from ground to level",
					value, units = "hPa",
					description = "${value}hPa pressure difference from ground")
			109 -> Grib2Level(type, "PVL", "potential vorticity surface", value, units = "K m2 kg-1 s-1")
			//110 -> Grib2Level(type, "", "Reserved")
			111 -> Grib2Level(type, "ETAL", "eta Level", value)
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
			200 -> Grib2Level(type, "EATM", "entire atmosphere (considered as a single layer)", value)
			201 -> Grib2Level(type, "EOCN", "entire ocean (considered as a single layer)", value)
			204 -> Grib2Level(type, "HTFL", "highest tropospheric freezing level", value)
			//206 -> Grib2Level(type, "", "grid scale cloud bottom level")
			//207 -> Grib2Level(type, "", "grid scale cloud top level")
			209 -> Grib2Level(type, "BCBL", "boundary layer cloud bottom level", value)
			210 -> Grib2Level(type, "BCTL", "boundary layer cloud top level", value)
			211 -> Grib2Level(type, "BCY", "boundary layer cloud layer", value)
			212 -> Grib2Level(type, "LCBL", "low cloud bottom level", value)
			213 -> Grib2Level(type, "LCTL", "low cloud top level", value)
			214 -> Grib2Level(type, "LCY", "low cloud layer", value)
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
			220 -> Grib2Level(type, "PBL", "planetary boundary layer", value)
			222 -> Grib2Level(type, "MCBL", "Middle cloud bottom level", value)
			223 -> Grib2Level(type, "MCTL", "Middle cloud top level", value)
			224 -> Grib2Level(type, "MCY", "Middle cloud layer", value)
			232 -> Grib2Level(type, "HCBL", "High cloud bottom level", value)
			233 -> Grib2Level(type, "HCTL", "High cloud top level", value)
			234 -> Grib2Level(type, "HCY", "High cloud layer", value)
			242 -> Grib2Level(type, "CCBL", "Convective cloud bottom level", value)
			243 -> Grib2Level(type, "CCTL", "Convective cloud top level", value)
			244 -> Grib2Level(type, "CCY", "Convective cloud layer", value)
			else -> null
		}
	}

	override val description: String = description ?: name

	override fun equals(other: Any?) = this === other
			|| other is Grib2Level
			&& id == other.id
			&& value == other.value

	override fun hashCode() = 31 * id + value.hashCode()
}
