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
/*
 * References: 
 *  http://nimbus.com.uy/weather/grads/grib_levels.html
 *  http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
 */
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.api.GribLevel

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 *
 * **2006-07-26 frv_peg: Added NCEP extention levels for use of GFS
 * files.**
 *
 * (level: 117, 211,212,213,222,223,232,233,242,243,244)
 *
 * Constructor. Creates a GribPDSLevel based on octets 10-12 of the PDS. Implements tables 3 and 3a.
 *
 * @see <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html">Table 3</a>
 *
 * @property id Index number from table 3 - can be used for comparison even if the description of the level changes
 * @property code Stores a short name of the level - same as the string "level" in the original GribRecordPDS
 *                implementation
 * @property name Name of the vertical coordinate/level
 * @property description Stores a descriptive name of the level GribRecordPDS implementation
 * @property units Stores the name of the level - same as the string "level" in the original GribRecordPDS implementation
 * @property value1 Value of PDS octet10 if separate from 11, otherwise value from octet10&11
 * @property value2 Value of PDS octet11
 * @property isNumeric True if a numeric values are used for this level (e.g. 1000 mb) False if level doesn't use values
 *                     (e.g. surface) Basically indicates whether you will be able to get a value for this level.
 * @property isSingleLayer Stores whether this is (usually) a vertical coordinate for a single layer (e.g. surface,
 *                         tropopause level) or multiple layers (e.g. hPa, m AGL) Aids in deciding whether to build 2D
 *                         or 3D grids from the data
 * @property isIncreasingUp Indicates whether the vertical coordinate increases with height. e.g. false for pressure
 *                          and sigma, true for height above ground or if unknown
 */
class Grib1Level(override val id: Int,
				 override val code: String,
				 override val name: String,
				 description: String? = null,
				 private val units: String = "",
				 private val value1: Float = Float.NaN,
				 private val value2: Float = Float.NaN,
				 private val isNumeric: Boolean = false,
				 private val isSingleLayer: Boolean = true,
				 private val isIncreasingUp: Boolean = true) : GribLevel, Comparable<Grib1Level> {

	companion object {
		fun getLevel(levelType: Int, levelData: Int): Grib1Level? {
			val v1 = levelData and 0xFF00 shr 8
			val v2 = levelData and 0xFF
			return when (levelType) {
				1 -> Grib1Level(levelType, "SFC", "ground or water surface")
				2 -> Grib1Level(levelType, "CBL", "cloud base level")
				3 -> Grib1Level(levelType, "CTL", "level of cloud tops")
				4 -> Grib1Level(levelType, "0DEG", "level of 0 degC isotherm")
				5 -> Grib1Level(levelType, "ADCL", "level of adiabatic condensation lifted from the surface")
				6 -> Grib1Level(levelType, "MWSL", "maximum wind level")
				7 -> Grib1Level(levelType, "TRO", "tropopause")
				8 -> Grib1Level(levelType, "NTAT", "nominal top of atmosphere")
				9 -> Grib1Level(levelType, "SEAB", "sea bottom")
				20 -> Grib1Level(levelType, "TMPL", "isothermal level",
						value1 = levelData.toFloat(),
						units = "K",
						isNumeric = true,
						description = "Isothermal level at ${levelData / 100}K")
				100 -> Grib1Level(levelType, "ISBL", "isobaric level",
						value1 = levelData.toFloat(),
						units = "hPa",
						isNumeric = true,
						isIncreasingUp = false,
						isSingleLayer = false,
						description = "pressure at ${levelData}hPa")
				101 -> Grib1Level(levelType, "ISBY", "layer between two isobaric levels",
						value1 = ((levelData and 0xFF00 shr 8) * 10).toFloat(), // convert from kPa to hPa - who uses kPa???
						value2 = (v2 * 10).toFloat(),
						units = "hPa",
						description = "layer between ${(levelData and 0xFF00 shr 8) * 10} and ${v2 * 10} hPa")
				102 -> Grib1Level(levelType, "MSL", "mean sea level")
				103 -> Grib1Level(levelType, "GPML", "specified altitude above MSL",
						value1 = levelData.toFloat(),
						units = "m",
						isNumeric = true,
						isSingleLayer = false,
						description = "$levelData m above mean sea level")
				104 -> Grib1Level(levelType, "GPMY", "layer between two specified altitudes above MSL",
						value1 = (v1 * 100).toFloat(), // convert hm to m,
						value2 = (v2 * 100).toFloat(),
						units = "m",
						description = "Layer between ${v1} and ${v2}m above mean sea level")
				105 -> Grib1Level(levelType, "TGL", "specified height level above ground",
						value1 = levelData.toFloat(),
						units = "m",
						isNumeric = true,
						isSingleLayer = false,
						description = "$levelData m above ground")
				106 -> Grib1Level(levelType, "HTGY", "layer between two specified height levels above ground",
						value1 = (v1 * 100).toFloat(), // convert hm to m,
						value2 = (v2 * 100).toFloat(),
						units = "m",
						isNumeric = true,
						description = "Layer between ${v1 * 100} and ${v2 * 100} m above ground")
				107 -> Grib1Level(levelType, "SIGL", "Sigma level",
						value1 = levelData / 10000.0f,
						units = "sigma",
						isNumeric = true,
						isSingleLayer = false,
						isIncreasingUp = false,
						description = "sigma = $levelData")
				108 -> Grib1Level(levelType, "SIGY", "Layer between two sigma layers",
						value1 = v1 / 100.0f,
						value2 = v2 / 100.0f,
						isNumeric = true,
						description = "Layer between sigma levels ${v1 / 100.0f} and ${v2 / 100.0f}")
				109 -> Grib1Level(levelType, "HYBL", "hybrid level",
						value1 = levelData.toFloat(),
						isNumeric = true,
						description = "hybrid level $levelData")
				110 -> Grib1Level(levelType, "HYBY", "Layer between two hybrid levels",
						value1 = v1.toFloat(),
						value2 = v2.toFloat(),
						isNumeric = true,
						description = "Layer between hybrid levels $v1 and $v2")
				111 -> Grib1Level(levelType, "DBLL", "Depth below land surface",
						value1 = levelData.toFloat(),
						units = "cm",
						isNumeric = true,
						description = "${levelData}cm below land surface")
				112 -> Grib1Level(levelType, "DBLY", "Layer between two levels below land surface",
						value1 = v1.toFloat(),
						value2 = v2.toFloat(),
						units = "cm",
						isNumeric = true,
						description = "Layer between $v1 and ${v2}cm below land surface")
				113 -> Grib1Level(levelType, "THEL", "Isentropic (theta) level",
						value1 = levelData.toFloat(),
						units = "K",
						isNumeric = true,
						isSingleLayer = false,
						description = "$levelData K")
				114 -> Grib1Level(levelType, "THEY", "Layer between two isentropic layers",
						value1 = (v1 + 475).toFloat(),
						value2 = (v2 + 475).toFloat(),
						units = "K",
						isNumeric = true,
						description = "Layer between ${v1 + 475} and ${v2 + 475} K")
				115 -> Grib1Level(levelType, "SPDL", "Pressure difference",
						units = "hPa",
						value1 = levelData.toFloat(),
						description = "Level at ${levelData}hPa from ground")
				116 -> Grib1Level(levelType, "SPDY", "Layer between pressure differences from ground to levels",
						value1 = v1.toFloat(),
						value2 = v2.toFloat(),
						units = "hPa",
						isNumeric = true,
						description = "Layer between pressure differences from ground: ${v1} and ${v2}K")
				117 -> Grib1Level(levelType, "PVL", "potential vorticity surface",
						value1 = levelData.toFloat(),
						units = "10^-6 km^2/kgs",
						isNumeric = false)
				119 -> Grib1Level(levelType, "ETAL", "ETA level",
						value1 = levelData.toFloat())
				125 -> Grib1Level(levelType, "HGLH", "Height above ground (high precision)",
						value1 = levelData.toFloat(),
						units = "cm",
						isNumeric = true,
						isSingleLayer = false,
						description = "${levelData}cm above ground")
				160 -> Grib1Level(levelType, "DBSL", "Depth below sea level",
						value1 = levelData.toFloat(),
						units = "m",
						isNumeric = true,
						description = "$levelData m below sea level")
				200 -> Grib1Level(levelType, "EATM", "entire atmosphere layer")
				201 -> Grib1Level(levelType, "EOCN", "entire ocean layer")
				204 -> Grib1Level(levelType, "HTFL", "Highest tropospheric freezing level")
				211 -> Grib1Level(levelType, "BCY", "Boundary layer cloud layer", isNumeric = false)
				212 -> Grib1Level(levelType, "LCBL", "Low cloud bottom level", isNumeric = false)
				213 -> Grib1Level(levelType, "LCTL", "Low cloud top level", isNumeric = false)
				214 -> Grib1Level(levelType, "LCY", "Low Cloud Layer")
				222 -> Grib1Level(levelType, "MCBL", "Middle cloud bottom level", isNumeric = false)
				223 -> Grib1Level(levelType, "MCTL", "Middle cloud top level", isNumeric = false)
				224 -> Grib1Level(levelType, "MCY", "Middle Cloud Layer")
				232 -> Grib1Level(levelType, "HCBL", "High cloud bottom level", isNumeric = false)
				233 -> Grib1Level(levelType, "HCTL", "High cloud top level", isNumeric = false)
				234 -> Grib1Level(levelType, "HCY", "High Cloud Layer")
				242 -> Grib1Level(levelType, "CCBL", "Convective cloud bottom level", isNumeric = false)
				243 -> Grib1Level(levelType, "CCTL", "Convective cloud top level", isNumeric = false)
				244 -> Grib1Level(levelType, "CCY", "Convective cloud layer", isNumeric = false)
				else -> null
			}
		}
	}

	/** true if negative z-value */
	val isDepth: Boolean
		get() = id == 111 || id == 160

	override val description: String = description ?: name

	override val value: Float
		get() = value1

	/**
	 * rdg - added this method to be used in a comparator for sorting while
	 * extracting records.
	 *
	 * TODO JK: Should this be part of the default API (not consistent with GRIB2), other solutions:
	 *   - Define comparators outside this class (utility) - different comparators for one class possible
	 *   - Leave definition of custom comparators to users of this library (remove it here)
	 *   - Implement [Comparable] interface across the library (e.g. as a part of
	 *     [GribLevel][mt.edu.um.cf2.jgribx.api.GribLevel]).
	 *
	 * @param other
	 *
	 * @return - -1 if level is "less than" this, 0 if equal, 1 if level is
	 * "greater than" this.
	 */
	override fun compareTo(other: Grib1Level): Int = Comparator
			.comparingInt(Grib1Level::id)
			.thenComparing(Grib1Level::value1)
			.thenComparing(Grib1Level::value2)
			.compare(this, other)

	override fun equals(other: Any?) = this === other
			|| other is Grib1Level
			&& id == other.id
			&& value1 == other.value1
			&& value2 == other.value2

	override fun hashCode(): Int {
		var result = id
		result = 31 * result + value1.hashCode()
		result = 31 * result + value2.hashCode()
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 Level description:",
			"\tIndex: ${id}",
			"\tCode: ${code}",
			"\tName: ${name}",
			"\tDescription: ${description}",
			"\tValue1: ${value1}",
			"\tValue2: ${value2}",
			"\tUnits: ${units}",
			"\tIncreasing up: ${isIncreasingUp}",
			"\tSingle layer: ${isSingleLayer}")
			.joinToString("\n")
}
