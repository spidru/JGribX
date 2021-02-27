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

import mt.edu.um.cf2.jgribx.*
import java.util.*
import kotlin.math.roundToInt

/**
 * @author AVLAB-USER3
 */
class Grib2Record private constructor(indicatorSection: GribRecordIS,
									  private val identificationSection: Grib2RecordIDS,
									  private val gridDefinitionSections: List<Grib2RecordGDS>) : GribRecord(indicatorSection) {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream,
						   indicatorSection: GribRecordIS,
						   discipline: ProductDiscipline,
						   readEntire: Boolean = false): Grib2Record {
			var recordLength = indicatorSection.recordLength - indicatorSection.length
			var identificationSection: Grib2RecordIDS? = null
			var localUseSection: Grib2RecordLUS? = null
			var productDefinitionSection: Grib2RecordPDS? = null
			var gridDefinitionSection: Grib2RecordGDS? = null
			var dataRepresentationSection: Grib2RecordDRS? = null
			var bitmapSection: Grib2RecordBMS? = null

			val gridDefinitionSectionList = mutableListOf<Grib2RecordGDS>()

			while (recordLength > 4) {
				if (recordLength == 4L) break
				gribInputStream.mark(10)
				val sectionLength = gribInputStream.readUINT(4)
				if (sectionLength > recordLength) {
					Logger.error("Section appears to be larger than the remaining length in the record")
				}
				val section = gribInputStream.readUINT(1)
				gribInputStream.reset()
				gribInputStream.resetBitCounter()
				when (section) {
					1 -> identificationSection = Grib2RecordIDS.readFromStream(gribInputStream, readEntire)
					2 -> localUseSection = Grib2RecordLUS.readFromStream(gribInputStream, readEntire)
					3 -> gridDefinitionSection = Grib2RecordGDS.readFromStream(gribInputStream)
							.also { it.localUseSection = localUseSection }
							.also { gridDefinitionSectionList.add(it) }
					4 -> {
						if (identificationSection == null) throw NoValidGribException("Missing required IDS section")
						if (gridDefinitionSection == null) throw NoValidGribException("Missing required GDS section")
						productDefinitionSection = Grib2RecordPDS.readFromStream(
								gribInputStream,
								discipline,
								identificationSection.referenceTime)
								.also { gridDefinitionSection.productDefinitionSections.add(it) }
					}
					5 -> dataRepresentationSection = Grib2RecordDRS.readFromStream(gribInputStream)
					6 -> bitmapSection = Grib2RecordBMS.readFromStream(gribInputStream)
					7 -> {
						if (productDefinitionSection == null) throw NoValidGribException("Missing required PDS section")
						if (gridDefinitionSection == null) throw NoValidGribException("Missing required GDS section")
						if (dataRepresentationSection == null) throw NoValidGribException("Missing required DRS section")
						if (bitmapSection == null) throw NoValidGribException("Missing required BMS section")
						Grib2RecordDS.readFromStream(
								gribInputStream,
								gridDefinitionSection,
								dataRepresentationSection,
								bitmapSection)
								.also { productDefinitionSection.dataSections.add(it) }
					}
				}
				if (gribInputStream.byteCounter != sectionLength) Logger.error(
						"Length of Section ${section} does not match actual amount of bytes read")
				recordLength -= sectionLength.toLong()
			}
			if (identificationSection == null) throw NoValidGribException("Missing IDS section")
			if (gridDefinitionSectionList.isEmpty()) throw NoValidGribException("Missing GDS section")
			if (gridDefinitionSectionList.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSectionList.first().productDefinitionSections.isEmpty()) throw NoValidGribException("Missing PDS section")
			if (gridDefinitionSectionList.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return Grib2Record(indicatorSection, identificationSection, gridDefinitionSectionList)
		}
	}

	override val centreId: Int
		get() = identificationSection.centreId

	override val referenceTime: Calendar
		get() = identificationSection.referenceTime

	override val forecastTime: Calendar
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().forecastTime
		}

	override val levelCode: String
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().levelCode
		}

	override val levelDescription: String
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().levelDescription
		}

	override val levelIdentifier: String
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().levelIdentifier
		}

	override val levelValues: FloatArray
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first()
					.let { floatArrayOf(it.level1.value, it.level2?.value ?: Float.NaN) }
		}

	override val parameterCode: String
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().parameterAbbrev
		}

	override val parameterDescription: String
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().parameterDescription
		}

	override val processId: Int
		get() { // TODO The implementation should take into account the possibility of repeated sections
			if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS's")
			if (gridDefinitionSections.first().productDefinitionSections.size > 1) Logger.warning("Record contains multiple PDS's")
			return gridDefinitionSections.first().productDefinitionSections.first().processId
		}

	override fun getValue(latitude: Double, longitude: Double): Double {
		// TODO The implementation should take into account the possibility of repeated sections
		if (gridDefinitionSections.size > 1) Logger.warning("Record contains multiple GDS instances")
		val gds = gridDefinitionSections[0]
		if (gds !is Grib2RecordGDSLatLon) throw NotImplementedError("${gds::class.simpleName} not implemented")
		// double[] xcoords = gds.getGridXCoords();
		// double[] ycoords = gds.getGridYCoords();
		val j = ((latitude - gds.gridLatStart) / gds.gridDeltaY).roundToInt() // j = index_closest_latitude
		val i = ((longitude - gds.gridLonStart) / gds.gridDeltaX).roundToInt() // i = index_closest_longitude

		// double closest_latitude = ycoords[index_closest_latitude];
		// double closest_longitude = xcoords[index_closest_longitude];
		val scanMode = gds.scanMode
		if (scanMode.iDirectionEvenRowsOffset
				|| scanMode.iDirectionOddRowsOffset
				|| scanMode.jDirectionOffset
				|| !scanMode.rowsNiNjPoints
				|| scanMode.rowsZigzag) {
			Logger.error("Unsupported scan mode ${scanMode} found")
		}
		return if (scanMode.iDirectionConsecutive)
			gds.productDefinitionSections.first().dataSections.first().data[gds.gridNi * j + i].toDouble() else
			gds.productDefinitionSections.first().dataSections.first().data[gds.gridNj * i + j].toDouble()
	}

	fun writeTo(gribOutputStream: GribOutputStream) {
		indicatorSection.writeTo(gribOutputStream)
		identificationSection.writeTo(gribOutputStream)
		var lastLUS: Grib2RecordLUS? = null
		gridDefinitionSections.forEach { gds ->
			gds.localUseSection?.takeIf { it != lastLUS }?.also { lastLUS = it }?.writeTo(gribOutputStream)
			gds.writeTo(gribOutputStream)
			gds.productDefinitionSections.forEach { pds ->
				pds.writeTo(gribOutputStream)
				pds.dataSections.forEach { ds ->
					ds.drs.writeTo(gribOutputStream)
					ds.bms.writeTo(gribOutputStream)
					ds.writeTo(gribOutputStream)
				}
			}
		}
		GribRecordES(true).writeTo(gribOutputStream)
	}
}
