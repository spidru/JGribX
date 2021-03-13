package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.Logger

/**
 * [Parameter category by product discipline](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-1.shtml)
 */
abstract class ParameterCategory private constructor(internal val value: Int, private val name: String) {
	class Meteorological private constructor(categoryId: Int, name: String) : ParameterCategory(categoryId, name) {
		companion object {
			val CATEGORIES: List<ParameterCategory> = mapOf(
					0 to "TEMPERATURE",
					1 to "MOISTURE",
					2 to "MOMENTUM",
					3 to "MASS",
					6 to "CLOUD",
					7 to "THERMODYNAMIC_STABILITY_INDICES",
					14 to "TRACE_GASES")
					.entries
					.onEach { (k, _) -> Logger.debug("Adding Meteorological category: ${k}") }
					.map { (k, v) -> Meteorological(k, v) }
		}
	}

	class Hydrological private constructor(categoryId: Int, name: String) : ParameterCategory(categoryId, name) {
		companion object {
			val CATEGORIES: List<ParameterCategory> = mapOf(
					0 to "BASIC",
					1 to "PROBABILITIES")
					.entries
					.map { (k, v) -> Hydrological(k, v) }
		}
	}

	class LandSurface private constructor(categoryId: Int, name: String) : ParameterCategory(categoryId, name) {
		companion object {
			val CATEGORIES: List<ParameterCategory> = mapOf(
					0 to "VEGETATION_BIOMASS",
					1 to "AGRICULTURAL_AQUACULTURAL",
					2 to "TRANSPORTATION",
					3 to "SOIL",
					4 to "FIRE_WEATHER",
					5 to "LAND_SURFACE")
					.entries
					.map { (k, v) -> LandSurface(k, v) }
		}
	}

	class SatelliteRemoteSensing private constructor(categoryId: Int, name: String) :
			ParameterCategory(categoryId, name) {
		companion object {
			protected val ENTRIES = mapOf(
					0 to "IMAGE_FORMAT",
					1 to "QUANTITATIVE")

			val CATEGORIES: List<ParameterCategory> = ENTRIES.entries.map { (k, v) -> SatelliteRemoteSensing(k, v) }
		}
	}

	class Oceanographic private constructor(categoryId: Int, name: String) : ParameterCategory(categoryId, name) {
		companion object {
			val CATEGORIES: List<ParameterCategory> = mapOf(
					0 to "WAVES",
					1 to "CURRENTS",
					2 to "ICE")
					.entries
					.map { (k, v) -> Oceanographic(k, v) }
		}
	}

	companion object {
		fun getCategories(discipline: Int): List<ParameterCategory>? = when (discipline) {
			0 -> Meteorological.CATEGORIES
			1 -> Hydrological.CATEGORIES
			2 -> LandSurface.CATEGORIES
			3 -> SatelliteRemoteSensing.CATEGORIES
			10 -> Oceanographic.CATEGORIES
			else -> null
		}
	}

	override fun equals(other: Any?): Boolean = this === other
			|| other is ParameterCategory
			&& value == other.value
			&& name == other.name

	override fun hashCode(): Int = 31 * value + name.hashCode()

	override fun toString(): String = name
}
