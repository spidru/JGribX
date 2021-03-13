package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.api.GribProductDefinitionSection
import mt.edu.um.cf2.jgribx.api.GribRecord
import java.text.SimpleDateFormat
import java.util.*

/**
 * Floats encoding/decoding results in loss of precision, here we define the max precision we can work with.
 * This is than used when comparing two floats to determine whether they are equal.
 */
internal const val FLOAT_PRECISION = 0.000015

val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
		.apply { timeZone = TimeZone.getTimeZone("UTC") }

internal fun GribRecord.log(messageIndex: Int, recordIndex: Int) = productDefinition
		.log(messageIndex, recordIndex, referenceTime)

internal fun GribProductDefinitionSection.log(messageIndex: Int, recordIndex: Int, referenceTime: Calendar) = Logger
		.info("GRIB[msg=%02d, rec=%02d, ref=%s]: %s - %s @ %s".format(messageIndex, recordIndex,
				ISO_DATE_FORMAT.format(referenceTime.time),
				ISO_DATE_FORMAT.format(forecastTime.time),
				parameter.let { "${it.description} [${it.code}]" },
				level.let { "${it?.identifier} (${it?.description})" }))
