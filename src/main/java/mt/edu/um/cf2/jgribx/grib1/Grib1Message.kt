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
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.*
import mt.edu.um.cf2.jgribx.api.GribMessage
import mt.edu.um.cf2.jgribx.api.GribRecord
import java.io.IOException
import java.util.*

/**
 * A class representing a single GRIB message (and record). A GRIB message consists of five sections:
 *
 * - Indicator Section (IS),
 * - Product Definition Section (PDS),
 * - Grid Definition Section (GDS),
 * - Bit Map Section (BMS), and
 * - Binary Data Section (BDS).
 *
 * For GRIB1 a grib message contains exactly one GRIB records and therefore this class implements both
 *
 * @property productDefinition The product definition section (PDS).
 * @property gridDefinition The grid definition section (GDS).
 * @property bitmap The bitmap section (BMS).
 * @property binaryData The binary data section (BDS).
 *
 * @author Benjamin Stark
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib1Message private constructor(override val indicator: GribRecordIS,
									   override val productDefinition: Grib1RecordPDS,
									   override var gridDefinition: Grib1RecordGDS,
									   private var bitmap: Grib1RecordBMS?,
									   private var binaryData: Grib1RecordBDS) :
		GribRecord, GribMessage {

	companion object {
		/**
		 * Constructs a <tt>GribRecord</tt> object from a bit input stream.
		 *
		 * @param gribInputStream bit input stream with GRIB record content
		 * @param indicatorSection
		 * @return
		 *
		 * @throws IOException           if stream can not be opened etc.
		 * @throws NotSupportedException
		 * @throws NoValidGribException  if stream contains no valid GRIB file
		 */
		internal fun readFromStream(gribInputStream: GribInputStream,
									indicatorSection: GribRecordIS,
									messageIndex: Int,
									parameterFilter: (String) -> Boolean): Grib1Message {
			gribInputStream.resetBitCounter()
			val productDefinitionSection = Grib1RecordPDS.readFromStream(gribInputStream)
			if (gribInputStream.byteCounter != productDefinitionSection.length)
				throw NoValidGribException("Incorrect PDS length")
			productDefinitionSection.log(messageIndex, 0, productDefinitionSection.referenceTime)

			if (!parameterFilter(productDefinitionSection.parameter.code)) {
				throw SkipException("Parameter: ${productDefinitionSection.parameter.code} filtered out")
			}

			val gridDefinitionSection = if (productDefinitionSection.gdsExists) {
				gribInputStream.resetBitCounter()
				Grib1RecordGDS.readFromStream(gribInputStream).also {
					if (gribInputStream.byteCounter != it.length) throw NoValidGribException("Incorrect GDS length")
				}
			} else throw NoValidGribException("GribRecord: No GDS included.")

			val bitmapSection: Grib1RecordBMS? = if (productDefinitionSection.bmsExists) {
				gribInputStream.resetBitCounter()
				Grib1RecordBMS.readFromStream(gribInputStream).also { // read Bitmap Section
					if (gribInputStream.byteCounter != it.length) throw NoValidGribException("Incorrect BMS length")
				}
			} else null

			gribInputStream.resetBitCounter()
			val binaryDataSection = Grib1RecordBDS.readFromStream(
					gribInputStream,
					productDefinitionSection,
					gridDefinitionSection,
					bitmapSection)
			if (gribInputStream.byteCounter != binaryDataSection.length)
				throw NoValidGribException("Incorrect BDS length")

			return Grib1Message(
					indicatorSection,
					productDefinitionSection,
					gridDefinitionSection,
					bitmapSection,
					binaryDataSection)
		}
	}

	override val records: List<GribRecord>
		get() = listOf(this)

	override val centreId: Int
		get() = productDefinition.centre

	override val forecastTime: Calendar
		get() = productDefinition.forecastTime

	/** Get the byte recordLength of this GRIB record. */
	val length: Long
		get() = indicator.messageLength


	override val processId: Int
		get() = productDefinition.processId

	/** Get grid coordinates in longitude/latitude */
	val gridCoords: Array<DoubleArray>
		get() = gridDefinition.coords

	/** Get data/parameter values as an array of float. */
	override val values: FloatArray
		get() {
			if (!binaryData.isConstant) return binaryData.values
			val gridSize: Int = gridDefinition.cols * gridDefinition.rows
			val values = FloatArray(gridSize)
			val ref: Float = binaryData.referenceValue
			for (i in 0 until gridSize) values[i] = ref
			return values
		}

	override val referenceTime: Calendar
		get() = productDefinition.referenceTime

	override fun getValue(sequence: Int): Float = binaryData.getValue(sequence)

	override fun getValue(latitude: Double, longitude: Double): Float = binaryData.getValue(latitude, longitude)

	override fun toString(): String = listOfNotNull(
			"GRIB1 record:",
			indicator.toString().prependIndent("\t"),
			productDefinition.toString().prependIndent("\t"),
			gridDefinition.toString().prependIndent("\t"),
			bitmap?.toString()?.prependIndent("\t"),
			binaryData.toString().prependIndent("\t"))
			.joinToString("\n")

	override fun writeTo(gribOutputStream: GribOutputStream) {
		indicator.writeTo(gribOutputStream)
		productDefinition.writeTo(gribOutputStream)
		gridDefinition.writeTo(gribOutputStream)
		bitmap?.writeTo(gribOutputStream)
		binaryData.writeTo(gribOutputStream)
		GribRecordES(true).writeTo(gribOutputStream)
	}

	override fun cutOut(north: Double, east: Double, south: Double, west: Double) =
			TODO("Not yet implemented")
}
