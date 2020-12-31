package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.grib2.ParameterCategory.Companion.getCategories

class ProductDiscipline private constructor(private val value: Int, private val name: String = "") {
	companion object {
		val VALUES = mapOf(
				0 to "METEOROLOGICAL",
				1 to "HYDROLOGICAL",
				2 to "LAND_SURFACE",
				3 to "SATELLITE_REMOTE_SENSING",
				10 to "OCEANOGRAPHIC")
				.map { (k, v) -> ProductDiscipline(k, v) }
	}

	val parameterCategories = getCategories(value)

	override fun equals(other: Any?): Boolean = other != null && other is ProductDiscipline && value == other.value

	override fun hashCode(): Int {
		var result = value
		result = 31 * result + name.hashCode()
		result = 31 * result + (parameterCategories?.hashCode() ?: 0)
		return result
	}

	override fun toString(): String = name
}
