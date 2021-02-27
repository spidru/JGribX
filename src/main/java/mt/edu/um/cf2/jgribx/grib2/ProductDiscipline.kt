package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.grib2.ParameterCategory.Companion.getCategories

class ProductDiscipline private constructor(internal val value: Int, private val name: String = "") {
	companion object {
		val MISSING = 255
		val VALUES = mapOf(
				0 to "METEOROLOGICAL",
				1 to "HYDROLOGICAL",
				2 to "LAND_SURFACE",
				3 to "SATELLITE_REMOTE_SENSING",
				10 to "OCEANOGRAPHIC")
				.map { (k, v) -> ProductDiscipline(k, v) }
	}

	val parameterCategories = getCategories(value)

	override fun equals(other: Any?): Boolean = this === other
			|| other is ProductDiscipline
			&& value == other.value

	override fun hashCode(): Int = value

	override fun toString(): String = name
}
