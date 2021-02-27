package mt.edu.um.cf2.jgribx.api

import mt.edu.um.cf2.jgribx.*
import mt.edu.um.cf2.jgribx.grib1.Grib1Message
import mt.edu.um.cf2.jgribx.grib2.Grib2Message

/**
 * GRIB message interface representing a GRIBx message as per documentation.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribMessage {
	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream): GribMessage {
			// There may be data on the beginning of the file, e.g. when using get_gfs.pl script
			// (https://www.cpc.ncep.noaa.gov/products/wesley/get_gfs.html)
			GribRecordIS.seekNext(gribInputStream)

			val indicatorSection = GribRecordIS.readFromStream(gribInputStream)

			// In case of an error skip the whole message to speed up reading (GribRecordIS is slow)
			gribInputStream.createByteCounter(
					initial = indicatorSection.length.toLong(),
					length = indicatorSection.recordLength).use {
				val message = when (indicatorSection.gribEdition) {
					1 -> Grib1Message.readFromStream(gribInputStream, indicatorSection)
					2 -> indicatorSection.discipline?.let { discipline ->
						Grib2Message.readFromStream(gribInputStream, indicatorSection, discipline)
					} ?: throw NoValidGribException("Missing discipline for GRIB2 edition")
					else -> throw NoValidGribException("Unsupported GRIB edition ${indicatorSection.gribEdition}")
				}
				val es = GribRecordES.readFromStream(gribInputStream)
				if (!es.isValid) throw NoValidGribException("Grib End Section is invalid")
				return message
			}
		}
	}

	/** List of GRIB records */
	val records: List<GribRecord>

	fun writeTo(gribOutputStream: GribOutputStream)
}
