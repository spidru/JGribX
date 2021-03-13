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
import mt.edu.um.cf2.jgribx.api.GribMessage
import kotlin.reflect.KClass

/**
 * GRIB2 Message implementation based on "Guide to the WMO Table Driven Code Form Used for the Representation and
 * Exchange of Regularly Spaced Data In Binary Form: FM 92 GRIB Edition 2" documentation
 *
 * A GRIB2 message contains following sections:
 *
 *     - Section 0: Indicator Section
 *     - Section 1: Identification Section
 *     - Section 2: Local Use Section (optional)                                  |
 *     - Section 3: Grid Definition Section                       |               |
 *     - Section 4: Product Definition Section    |               |               |
 *     - Section 5: Data Representation Section   | (repeated)    | (repeated)    | (repeated)
 *     - Section 6: Bit-Map Section               |               |               |
 *     - Section 7: Data Section                  |               |               |
 *     - Section 8: End Section
 *
 * The GRIB2 regulations state that (A) Sequences of GRIB2 sections 2 to 7, 3 to 7, or 4 to 7 may be repeated within
 * a single GRIB2 message, (B) All sections within such repeated sequences must be present and shall appear in the
 * numerical order noted above, and (C) Unrepeated sections remain in effect until redefined.
 *
 * This results into possibly multiple [GRIB2 records][Grib2Record] per one [GRIB2 message][Grib2Message] and therefore
 * the implementations are split (in contrast to GRIB1 where one [GRIB1 message][mt.edu.um.cf2.jgribx.grib1.Grib1Message]
 * contains exactly one [GRIB record][mt.edu.um.cf2.jgribx.api.GribRecord] only)
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2Message(private val indicatorSection: GribRecordIS,
				   private val identificationSection: Grib2RecordIDS,
				   private val gridDefinitionSections: List<Grib2RecordGDS>,
				   override val records: List<Grib2Record>) : GribMessage {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream,
						   indicatorSection: GribRecordIS,
						   discipline: ProductDiscipline,
						   messageIndex: Int,
						   parameterFilter: (String) -> Boolean,
						   readEntire: Boolean = false): Grib2Message {
			var messageLength = indicatorSection.messageLength - indicatorSection.length
			var identificationSection: Grib2RecordIDS? = null
			var localUseSection: Grib2RecordLUS? = null
			var productDefinitionSection: Grib2RecordPDS? = null
			var gridDefinitionSection: Grib2RecordGDS? = null
			var dataRepresentationSection: Grib2RecordDRS? = null
			var bitmapSection: Grib2RecordBMS? = null
			var bitmapSectionSelected: Grib2RecordBMS? = null

			val gridDefinitionSectionList = mutableListOf<Grib2RecordGDS>()
			val records = mutableListOf<Grib2Record>()
			var recordCount = 0

			while (messageLength > 4) {
				if (messageLength == 4L) break

				val (sectionLength, sectionNumber) = Grib2Section.peekFromStream(gribInputStream)
				if (sectionNumber == 1) recordCount++
				if (sectionLength > messageLength) {
					Logger.error("Section appears to be larger than the remaining length in the GRIB message")
				}

				val byteCounter = gribInputStream.createByteCounter(length = sectionLength.toLong())
				try {
					when (sectionNumber) {
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
							productDefinitionSection.log(messageIndex, records.size, identificationSection.referenceTime)
							if (!parameterFilter(productDefinitionSection.parameter.code)) {
								throw SkipException("Parameter filter applied")
							}
						}
						5 -> dataRepresentationSection = Grib2RecordDRS.readFromStream(gribInputStream)
						6 -> bitmapSectionSelected = Grib2RecordBMS.readFromStream(gribInputStream).let {
							when (it.indicator) {
								Grib2RecordBMS.Indicator.BITMAP_SPECIFIED -> {
									bitmapSection = it
									it
								}
								Grib2RecordBMS.Indicator.BITMAP_PREDETERMINED -> it
								Grib2RecordBMS.Indicator.BITMAP_PREDEFINED -> {
									it.bitmap = bitmapSection?.bitmap?.clone()
											?: throw NoValidGribException("No previous BMS defined")
									it
								}
								Grib2RecordBMS.Indicator.BITMAP_NONE -> it
							}
						}
						7 -> {
							if (identificationSection == null) throw NoValidGribException("Missing required IDS section")
							if (productDefinitionSection == null) throw NoValidGribException("Missing required PDS section")
							if (gridDefinitionSection == null) throw NoValidGribException("Missing required GDS section")
							if (dataRepresentationSection == null) throw NoValidGribException("Missing required DRS section")
							if (bitmapSectionSelected == null) throw NoValidGribException("Missing required BMS section")
							val dataSection = Grib2RecordDS.readFromStream(
									gribInputStream,
									gridDefinitionSection,
									dataRepresentationSection,
									bitmapSectionSelected)
									.also { productDefinitionSection.dataSections.add(it) }
							Grib2Record(indicatorSection, identificationSection, productDefinitionSection, dataSection)
									.also { records.add(it) }
						}
					}
					if (gribInputStream.byteCounter != sectionLength) Logger.error(
							"Length of Section ${sectionNumber} (${sectionLength}B)" +
									" does not match actual amount of bytes read (${gribInputStream.byteCounter}B)")
					messageLength -= sectionLength.toLong()
				} catch (e: Throwable) {
					Logger.warning("Skipping GRIB message ${messageIndex} record ${recordCount} (${e.message})", e)
					messageLength -= sectionLength
					gribInputStream.skip(sectionLength - byteCounter.read)
					gribInputStream.destroyByteCounter(byteCounter)
					for (i in ((sectionNumber + 1)..7)) {
						val (length, number) = Grib2Section.peekFromStream(gribInputStream)
						if (length > messageLength) {
							Logger.error("Section appears to be larger than the remaining length in the GRIB message")
							break
						}
						Logger.debug("Skipping section ${number} of ${length} B")
						gribInputStream.skip(length.toLong())
						messageLength -= length.toLong()
						if (messageLength == 4L) break
					}
				}
			}
			if (identificationSection == null) throw NoValidGribException("Missing IDS section")
			if (records.isEmpty()) throw NoValidGribException("No valid records out of ${recordCount} in GRIB2 message")
			if (gridDefinitionSectionList.isEmpty()) throw NoValidGribException("Missing GDS section")
			if (gridDefinitionSectionList.first().productDefinitionSections.isEmpty()) throw NoValidGribException("Missing PDS section")
			return Grib2Message(indicatorSection, identificationSection, gridDefinitionSectionList, records)
		}
	}

	fun <DRS : Grib2RecordDRS> convertDataRepresentationTo(type: KClass<DRS>) = records
			.forEach { it.convertDataRepresentationTo(type) }

	override fun writeTo(gribOutputStream: GribOutputStream) {
		indicatorSection.messageLength = calculateMessageLength()
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

	private fun calculateMessageLength(): Long {
		var lastLUS: Grib2RecordLUS? = null
		return indicatorSection.length.toLong() +
				identificationSection.length.toLong() +
				gridDefinitionSections.map { gds ->
					(gds.localUseSection?.takeIf { it != lastLUS }?.also { lastLUS = it }?.length?.toLong() ?: 0) +
							gds.length.toLong() +
							gds.productDefinitionSections
									.flatMap { pds -> listOf(pds) + pds.dataSections.flatMap { ds -> listOf(ds.drs, ds.bms, ds) } }
									.map { it.length.toLong() }
									.sum()
				}.sum() +
				GribRecordES(true).length.toLong()
	}
}
