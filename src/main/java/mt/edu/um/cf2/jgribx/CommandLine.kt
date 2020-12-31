package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.JGribX.setLoggingLevel
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.system.exitProcess

/**
 * @author spidru
 */
object CommandLine {
	@JvmStatic
	fun main(args: Array<String>) {
		val options = Options()

		/* Version Information */
		val version = Option("v", "version", false, "Show version information")
		version.isRequired = false
		options.addOption(version)

		/* File Information */
		val inputFile = Option("i", "input", true, "Specify an input file")
		inputFile.isRequired = false
		options.addOption(inputFile)

		/* Logging Level */
		val logLevel = Option("l", "loglevel", true, "Specify the logging level")
		logLevel.isRequired = false
		options.addOption(logLevel)
		val parser: CommandLineParser = DefaultParser()
		val formatter = HelpFormatter()
		val cmd: CommandLine
		try {
			cmd = parser.parse(options, args)
		} catch (e: ParseException) {
			println(e.message)
			formatter.printHelp("JGribX", options)
			exitProcess(1)
		}

		/* If no arguments have been specified, display help */
		if (cmd.options.isEmpty()) {
			System.err.println("No arguments specified")
			formatter.printHelp("JGribX", options)
			exitProcess(1)
		}

		/* Must be first option processed due to logging */
		if (cmd.hasOption("l")) {
			val level = cmd.getOptionValue("l").toInt()
			if (level in 1..5) {
				Logger.mode = Logger.LoggingMode.CONSOLE
				setLoggingLevel(level - 1)
			}
		}
		if (cmd.hasOption("i")) {
			val inputFilePath = cmd.getOptionValue("i")
			try {
				val gribFile = GribFile(inputFilePath)

				// Print out generic GRIB file info
				gribFile.getSummary(System.out)
				val params: List<String?> = gribFile.parameterCodes
				println("Parameters:")
				for (param in params) {
					print("$param ")
				}
			} catch (e: FileNotFoundException) {
				System.err.println("Cannot find file: $inputFilePath")
			} catch (e: IOException) {
				System.err.println("Could not open file: $inputFilePath")
			} catch (e: NotSupportedException) {
				System.err.println("GRIB file contains unsupported features: $e")
			} catch (e: NoValidGribException) {
				System.err.println("GRIB file is invalid: $e")
			}
		}
		if (cmd.hasOption("v")) println("JGribX " + JGribX.VERSION)
	}
}
