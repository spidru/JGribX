package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.JGribX.setLoggingLevel
import mt.edu.um.cf2.jgribx.grib1.Grib1Level
import mt.edu.um.cf2.jgribx.grib1.Grib1Parameter
import mt.edu.um.cf2.jgribx.grib2.*
import org.apache.commons.cli.*
import org.apache.commons.cli.CommandLine
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintStream
import kotlin.system.exitProcess

/**
 * @author spidru
 */
object CommandLine {
	@JvmStatic
	fun main(args: Array<String>) {
		val usage = "JGribX [options]"
		val options = Options().apply {
			addOption(Option("c", "cutout", true, "Cutout a region")
					.apply { argName = "north,east,south,west" })
			addOption(Option("drs", "data-representation", true, "[GRIB2] Convert Data Representation Section to specified templates")
					.apply { argName = "template-num" })
			addOption(Option("h", "help", false, "Show this screen")
					.apply { argName = "file" })
			addOption(Option("i", "input", true, "Specify an input file")
					.apply { argName = "file" })
			addOption(Option("l", "loglevel", true, "Specify the logging level")
					.apply { argName = "level" })
			addOption(Option(null, "list-parameters", true, "List recognized parameters of a GRIB edition")
					.apply { argName = "grib-edition" })
			addOption(Option(null, "list-levels", true, "List recognized levels of a GRIB edition")
					.apply { argName = "grib-edition" })
			addOption(Option("o", "output", true, "Specify an outout file")
					.apply { argName = "file" })
			addOption(Option("p", "params", true, "A comma separated list of parameters to filter. The parameter can contain levels and values in the format: PARAM[:LEVEL[:VALUE]].")
					.apply { argName = "p1,p2,..." })
			addOption(Option("s", "summary", false, "Print a file summary"))
			addOption(Option("v", "version", false, "Show version information"))
		}

		val parser: CommandLineParser = DefaultParser()
		val formatter = HelpFormatter().apply { width = 120 }
		val cmd: CommandLine
		try {
			cmd = parser.parse(options, args)
		} catch (e: ParseException) {
			println(e.message)
			formatter.printHelp(usage, options)
			exitProcess(1)
		}

		if (cmd.options.isEmpty()) {
			System.err.println("No arguments specified")
			formatter.printHelp(usage, options)
			exitProcess(1)
		}

		if (cmd.hasOption("h")) {
			formatter.printHelp(usage, options)
			exitProcess(0)
		}

		if (cmd.hasOption("l")) {
			val level = cmd.getOptionValue("l").toInt()
			if (level in 1..5) {
				Logger.mode = Logger.LoggingMode.CONSOLE
				setLoggingLevel(level - 1)
			}
		}

		when {
			cmd.hasOption("list-parameters") -> when (cmd.getOptionValue("list-parameters").toInt()) {
				1 -> listOf(0, 128, 129).asSequence()
						.flatMap { ver -> (0..255).map { Grib1Parameter.getParameter(ver, it, 0) } }
						.mapNotNull { it?.let { Triple(it.code, it.description, it.units) } }
						.map { (c, d, u) -> "  %7s: %s [%s]".format(c, d, u.takeUnless { it.isBlank() } ?: "-") }
						.joinToString("\n", "List of parameters for GRIB 1:\n")
						.also { println(it) }
				2 -> {
					Grib2Parameter.loadDefaultParameters()
					ProductDiscipline.VALUES.values.forEach { discipline ->
						discipline.parameterCategories?.forEach { category ->
							println("List of parameters for GRIB 2 [dicipline: ${discipline.name}, category: ${category.name}]:")
							(0..255).mapNotNull { Grib2Parameter.getParameter(discipline, category.value, it) }
									.map { Triple(it.code, it.description, it.units) }
									.map { (c, d, u) ->
										"  %8s: %s [%s]".format(c, d, u.takeUnless { it.isBlank() } ?: "-")
									}
									.joinToString("\n")
									.also { println(it) }
							println()
						}
					}
				}
				else -> throw NoValidGribException("Invalid GRIB edition")
			}
			cmd.hasOption("list-levels") -> when (cmd.getOptionValue("list-levels").toInt()) {
				1 -> (0..255).asSequence()
						.map { Grib1Level.getLevel(it, 0) }
						.mapNotNull { it?.let { it.code to it.name } }
						.map { (code, name) -> "  %4s: %s".format(code, name) }
						.joinToString("\n", "List of levels for GRIB 1:\n")
						.also { println(it) }
				2 -> (0..255).asSequence()
						.map { Grib2Level.getLevel(it, 0f) }
						.mapNotNull { it?.let { it.code to it.name } }
						.map { (code, name) -> "  %4s: %s".format(code, name) }
						.joinToString("\n", "List of levels for GRIB 2:\n")
						.also { println(it) }
				else -> throw NoValidGribException("Invalid GRIB edition")
			}
			cmd.hasOption("i") -> {
				val inputFile = File(cmd.getOptionValue("i"))
				val filters = cmd
						.getOptionValue("p")
						?.split(",")
						?.map { it.toUpperCase() }
						?.map { it.split(":") }
						?.map { Triple(it[0], it.getOrNull(1), it.getOrNull(2)?.toIntOrNull()) }
				val drs = when (cmd.getOptionValue("drs")?.toInt()) {
					null -> null
					0 -> Grib2RecordDRS0::class
					2 -> Grib2RecordDRS2::class
					3 -> Grib2RecordDRS3::class
					else -> {
						System.err.println("Converting to Data Representation Template 0, 2 and 3 is supported")
						formatter.printHelp(usage, options)
						exitProcess(1)
					}
				}
				val cutOutRegion = cmd.getOptionValue("c")
						?.split(",")
						?.mapNotNull { it.toDoubleOrNull() }
						?.takeIf { it.size == 4 }
				val outputFile = cmd.getOptionValue("o")?.let { File(it) }

				try {
					val gribFile = GribFile(inputFile, colonSeparatedParameterLevelValueFilter(filters))

					if (drs != null) gribFile.records.filterIsInstance<Grib2Record>().forEach {
						it.convertDataRepresentationTo(drs)
					}

					cutOutRegion?.also { (north, east, south, west) -> gribFile.cutOut(north, east, south, west) }

					if (outputFile != null) GribOutputStream(outputFile.outputStream()).use { outputStream ->
						gribFile.writeTo(outputStream)
					}

					if (cmd.hasOption("s")) gribFile.getSummary(System.out) // Print out generic GRIB file info
				} catch (e: FileNotFoundException) {
					System.err.println("Cannot find file: ${inputFile.absolutePath}")
				} catch (e: IOException) {
					System.err.println("Could not open file: ${inputFile.absolutePath}")
				} catch (e: NotSupportedException) {
					System.err.println("GRIB file contains unsupported features: ${e}")
				} catch (e: NoValidGribException) {
					System.err.println("GRIB file is invalid: ${e}")
				}
			}
		}
		if (cmd.hasOption("v")) println("JGribX ${JGribX.VERSION}")
	}

	/**
	 * Prints out a summary of the GRIB file.
	 * @param out
	 */
	private fun GribFile.getSummary(out: PrintStream = System.out) {
		val centreIds = centreIDs
		val processIds = processIDs
		val refDates = referenceTimes
		val forecastDates = forecastTimes

		// Print out generic GRIB file info
		out.println("---------------------------------------")
		out.println("Reading file: ${filename}")
		out.println("GRIB Edition: ${edition}")
		out.println("Messages successfully read: ${messages.size} of ${messages.size + messagesSkippedCount}")
		out.println("Records successfully read: ${recordCount}")
		out.println("---------------------------------------")

		// Print out originating centre info
		out.print("Weather Centre(s): ")
		for (i in centreIds.indices) {
			out.print("${centreIds[i]} [${GribCodes.getCentreName(centreIds[i])}]")
			if (i != centreIds.size - 1) out.print(",")
		}
		out.println()

		// Print out generating process info
		out.print("Model(s): ")
		for (i in processIds.indices) {
			out.print("${processIds[i]} [${GribCodes.getProcessName(processIds[i])}]")
			if (i != processIds.size - 1) out.print(",")
		}
		out.println()

		// Get reference time
		out.println("Reference Time: ")
		for (date in refDates) {
			out.println("\t${DEFAULT_DATE_FORMAT.format(date.time)}")
		}

		// Get forecast times
		out.println("Forecast Time(s) - ${forecastDates.size} dates: ")
		for (date in forecastDates) {
			out.println("\t${DEFAULT_DATE_FORMAT.format(date.time)}")
		}

		println("Available data:")
		records.distinctBy { "${it.parameter.code}/${it.level?.identifier}" }.forEach {
			println("\t${it.parameter.description} [${it.parameter.code}]: ${it.level?.description} (${it.level?.identifier})")
		}

		println("Grid: ")
		records.asSequence()
				.map { it.gridDefinition }
				.map { "${it.cols}x${it.rows}=${it.cols * it.rows} points: ${it.deltaX}°x${it.deltaY}°" }
				.distinct()
				.forEach { println("\t${it}") }
		println("Area: ")
		records.asSequence()
				.map { it.gridDefinition }
				.map { it.xCoords to it.yCoords }
				.map { (x, y) -> listOfNotNull(x.minOrNull(), y.minOrNull(), x.maxOrNull(), y.maxOrNull()) }
				.filter { it.size == 4 }
				.map { (west, south, east, north) ->
					listOf(west.formatDegrees('E' to 'W', 3),
							south.formatDegrees('N' to 'S', 2),
							east.formatDegrees('E' to 'W', 3),
							north.formatDegrees('N' to 'S', 2))
				}
				.map { (west, south, east, north) -> "${south} ${west} -> ${north} ${east}" }
				.distinct().toList()
				.forEach { println("\t${it}") }
	}
}
