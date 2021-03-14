package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribRecordIS
import mt.edu.um.cf2.jgribx.TransformationException
import mt.edu.um.cf2.jgribx.api.GribGridDefinitionSection
import mt.edu.um.cf2.jgribx.api.GribRecord
import java.util.*
import kotlin.reflect.KClass

/**
 * A GRIB2 Record containing a part of the GRIB2 message to enable repeated sections.
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2Record(override val indicator: GribRecordIS,
				  private val identification: Grib2RecordIDS,
				  override val productDefinition: Grib2RecordPDS,
				  data: Grib2RecordDS<*>) : GribRecord {

	override val gridDefinition: GribGridDefinitionSection
		get() = data.gds

	internal var data: Grib2RecordDS<*> = data
		private set

	override val centreId: Int
		get() = identification.centreId

	override val forecastTime: Calendar
		get() = productDefinition.forecastTime

	override val processId: Int
		get() = productDefinition.processId

	override val referenceTime: Calendar
		get() = identification.referenceTime

	override val values: FloatArray
		get() = data.values

	override fun getValue(sequence: Int): Float = data.getValue(sequence)

	override fun getValue(latitude: Double, longitude: Double): Float = data.getValue(latitude, longitude)

	fun <DRS : Grib2RecordDRS> convertDataRepresentationTo(type: KClass<DRS>) {
		val indexInPDS = productDefinition.dataSections.indexOf(data)
		if (indexInPDS == -1) throw TransformationException("DS not found in PDS")
		data = when (type) {
			Grib2RecordDRS0::class -> Grib2RecordDS0.convertFrom(data)
			else -> TODO("Conversion to ${type.simpleName} not implemented")
		}
		productDefinition.dataSections[indexInPDS] = data
	}


	override fun cutOut(north: Double, east: Double, south: Double, west: Double) = data
			.cutOut(north, east, south, west)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Grib2Record) return false

		if (indicator != other.indicator) return false
		if (identification != other.identification) return false
		if (productDefinition != other.productDefinition) return false
		if (data != other.data) return false
		if (gridDefinition != other.gridDefinition) return false

		return true
	}

	override fun hashCode(): Int {
		var result = indicator.hashCode()
		result = 31 * result + identification.hashCode()
		result = 31 * result + productDefinition.hashCode()
		result = 31 * result + data.hashCode()
		result = 31 * result + gridDefinition.hashCode()
		return result
	}
}
