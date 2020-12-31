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

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribRecord
import mt.edu.um.cf2.jgribx.GribRecordIS
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.grib2.Grib2RecordDS.Companion.readFromStream
import java.util.*
import kotlin.math.roundToInt

/**
 *
 * @author AVLAB-USER3
 */
class Grib2Record(indicatorSection: GribRecordIS,
				  private var identificationSection: Grib2RecordIDS,
				  protected var localUseSection: List<Grib2RecordLUS>,
				  var gridDefinitionSection: List<Grib2RecordGDS>,
				  private var productDefinitionSection: List<Grib2RecordPDS>,
				  private var dataRepresentationSection: List<Grib2RecordDRS>,
				  private var bitmapSection: List<Grib2RecordBMS>,
				  var dataSection: List<Grib2RecordDS>) : GribRecord(indicatorSection) {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream,
						   indicatorSection: GribRecordIS,
						   discipline: ProductDiscipline): Grib2Record {
			//val record = Grib2Record()
			var recordLength = indicatorSection.recordLength - indicatorSection.length
			var identificationSection: Grib2RecordIDS? = null
			var dataRepresentationSection: Grib2RecordDRS? = null
			var gridDefinitionSection: Grib2RecordGDS? = null
			var bitmapSection: Grib2RecordBMS? = null

			val gridDefinitionSectionList = mutableListOf<Grib2RecordGDS>()
			val productDefinitionSectionList = mutableListOf<Grib2RecordPDS>()
			val dataRepresentationSectionList = mutableListOf<Grib2RecordDRS>()
			val bitmapSectionList = mutableListOf<Grib2RecordBMS>()
			val dataSectionList = mutableListOf<Grib2RecordDS>()

			while (recordLength > 4) {
				if (recordLength == 4L) break
				gribInputStream.mark(10)
				val sectionLength = gribInputStream.readUINT(4)
				if (sectionLength > recordLength) {
					Logger.error("Section appears to be larger than the remaining length in the record.")
				}
				val section = gribInputStream.readUINT(1)
				gribInputStream.reset()
				gribInputStream.resetBitCounter()
				when (section) {
					1 -> identificationSection = Grib2RecordIDS.readFromStream(gribInputStream)
					2 -> gribInputStream.skip(sectionLength.toLong())
					3 -> gridDefinitionSection = Grib2RecordGDS.readFromStream(gribInputStream)
							.also { gridDefinitionSectionList.add(it) }
					4 -> if (identificationSection != null)
						Grib2RecordPDS(
								gribInputStream,
								discipline,
								identificationSection.referenceTime)
								.also { productDefinitionSectionList.add(it) } else
						throw NoValidGribException("Missing IDS section")
					5 -> dataRepresentationSection = Grib2RecordDRS.readFromStream(gribInputStream)
							?.also { dataRepresentationSectionList.add(it) }
					6 -> bitmapSection = Grib2RecordBMS.readFromStream(gribInputStream)
							?.also { bitmapSectionList.add(it) }
					7 -> if (dataRepresentationSection != null
							&& gridDefinitionSection != null
							&& bitmapSection != null)
						readFromStream(
								gribInputStream,
								dataRepresentationSection,
								gridDefinitionSection,
								bitmapSection)
								?.also { dataSectionList.add(it) }
					else -> throw NoValidGribException("Invalid section ${section} encountered")
				}
				if (gribInputStream.byteCounter != sectionLength) Logger.error(
						"Length of Section ${section} does not match actual amount of bytes read")
				recordLength -= sectionLength.toLong()
			}
			if (identificationSection == null) throw NoValidGribException("Missing IDS section")
			if (productDefinitionSectionList.isEmpty()) throw NoValidGribException("Missing PDS section")
			return Grib2Record(indicatorSection,
					identificationSection,
					emptyList(),
					gridDefinitionSectionList,
					productDefinitionSectionList,
					dataRepresentationSectionList,
					bitmapSectionList,
					dataSectionList)
		}
	}

	override val centreId: Int
		get() = identificationSection.centreId

	override val forecastTime: Calendar
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().forecastTime
		}

	override val levelCode: String
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().levelCode
		}

	override val levelDescription: String
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().levelDescription
		}

	override val levelIdentifier: String
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().levelIdentifier
		}

	override val levelValues: FloatArray
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().level?.values ?: floatArrayOf(0f, 0f)
		}

	override val parameterCode: String
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().parameterAbbrev
		}

	override val parameterDescription: String
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().parameterDescription
		}

	override val processId: Int
		get() {
			if (productDefinitionSection.size > 1) Logger.warning("Record contains multiple PDS's")
			return productDefinitionSection.first().processId
		}

	override val referenceTime: Calendar
		get() = identificationSection.referenceTime

	override fun getValue(latitude: Double, longitude: Double): Double {
		if (gridDefinitionSection.size > 1) Logger.warning("Record contains multiple GDS instances")
		val gds = gridDefinitionSection[0]
		// double[] xcoords = gds.getGridXCoords();
		// double[] ycoords = gds.getGridYCoords();
		val j = ((latitude - gds.gridLatStart) / gds.gridDeltaY).roundToInt() // j = index_closest_latitude
		val i = ((longitude - gds.gridLonStart) / gds.gridDeltaX).roundToInt() // i = index_closest_longitude

		// double closest_latitude = ycoords[index_closest_latitude];
		// double closest_longitude = xcoords[index_closest_longitude];
		val scanMode = gds.scanMode
		if (scanMode == null || scanMode.iDirectionEvenRowsOffset
				|| scanMode.iDirectionOddRowsOffset
				|| scanMode.jDirectionOffset
				|| !scanMode.rowsNiNjPoints
				|| scanMode.rowsZigzag) {
			Logger.error("Unsupported scan mode ${scanMode} found")
		}
		return if (scanMode?.iDirectionConsecutive == true)
			dataSection[0].data[gds.gridNi * j + i].toDouble() else dataSection[0].data[gds.gridNj * i + j].toDouble()
	}
}