package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.api.GribProductDefinitionSection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Floats encoding/decoding results in loss of precision, here we define the max precision we can work with.
 * This is than used when comparing two floats to determine whether they are equal.
 */
internal const val FLOAT_PRECISION = 0.000015

val DEFAULT_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'", Locale.ROOT)
		.apply { timeZone = TimeZone.getTimeZone("UTC") }

val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
		.apply { timeZone = TimeZone.getTimeZone("UTC") }

internal fun Double.formatDegrees(hemisphere: Pair<Char, Char>, degDigits: Int): String {
	val symbol = if (this >= 0) hemisphere.first else hemisphere.second
	val degrees = abs(this).toInt()
	val minutes = ((abs(this) - degrees) * 60).toInt()
	val seconds = (((abs(this) - degrees) * 60 - minutes) * 60).toInt()
	return "%0${degDigits}d°%02d'%02d\"%s".format(degrees, minutes, seconds, symbol)
}

internal fun GribProductDefinitionSection.log(messageIndex: Int, recordIndex: Int) = Logger
		.info("GRIB[msg=%02d, rec=%02d, ref=%s]: %s - %s @ %s".format(messageIndex, recordIndex,
				ISO_DATE_FORMAT.format(referenceTime.time),
				ISO_DATE_FORMAT.format(forecastTime.time),
				parameter.let { "${it.description} [${it.code}]" },
				level.let { "${it.identifier} (${it.description})" }))

/**
 * Creates a filter out of a list of parameter, level, value combinations.
 *
 * @param filters a list of parameter, level, value combinations.
 */
fun colonSeparatedParameterLevelValueFilter(filters: List<Triple<String, String?, Int?>>?):
		(GribProductDefinitionSection) -> Boolean = { pds ->
	filters == null || filters.any { (param, level, value) ->
		pds.parameter.code.equals(param, ignoreCase = true)
				&& (level == null || pds.level.code.equals(level, ignoreCase = true))
				&& (value == null || pds.level.value.toInt() == value)
	}
}
