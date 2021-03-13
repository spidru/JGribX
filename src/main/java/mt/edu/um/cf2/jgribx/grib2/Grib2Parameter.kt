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

import mt.edu.um.cf2.jgribx.Logger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Matcher
import java.util.regex.Pattern

class Grib2Parameter(private val discipline: ProductDiscipline,
					 private val category: ParameterCategory,
					 private val index: Int,
					 val code: String,
					 val description: String,
					 val units: String) {

	companion object {
		private val paramList = mutableListOf<Grib2Parameter>()
		var isDefaultLoaded = false
			private set

		fun loadDefaultParameters() {
			var filename: String
			Logger.debug("Number of product disciplines: " + ProductDiscipline.VALUES.size)
			for (discipline in ProductDiscipline.VALUES) {
				val categories = discipline.parameterCategories
				Logger.debug("Number of ${discipline} parameter categories: ${categories?.size}")
				if (categories != null) for (category in categories) {
					filename = "/$discipline-$category.txt"
					Logger.info("Resource path: ${filename}")
					val inputStream = Grib2Parameter::class.java.getResourceAsStream(filename)
					if (inputStream == null) {
						Logger.error("Cannot find ${filename}")
						continue
					}
					try {
						BufferedReader(InputStreamReader(inputStream)).use { reader ->
							val pattern = Pattern.compile("(\\d+)\\s*:\\s*(.*?)\\s*:\\s*(.*?)\\s*:\\s*(\\w*)")
							var m: Matcher
							var line: String? = reader.readLine()
							while (line != null) {
								m = pattern.matcher(line)
								if (m.find()) {
									val index = m.group(1).toInt()
									val paramDesc = m.group(2)
									val paramUnits = m.group(3)
									val paramName = m.group(4)
									paramList.add(Grib2Parameter(discipline,
											category,
											index,
											paramName,
											paramDesc,
											paramUnits))
								}
								line = reader.readLine()
							}
						}
					} catch (e: IOException) {
						Logger.error("Cannot read ${filename}")
					}
				}
			}
			isDefaultLoaded = true
		}

		fun getParameter(discipline: ProductDiscipline, category: Int, index: Int): Grib2Parameter? {
			for (parameter in paramList) {
				if (parameter.discipline == discipline &&
						parameter.category.value == category &&
						parameter.index == index) {
					return parameter
				}
			}
			return null
		}
	}
}
