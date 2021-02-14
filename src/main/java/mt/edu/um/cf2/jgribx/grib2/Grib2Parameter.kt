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
							val pattern = Pattern.compile("([\\d-]+)\\s*:\\s*(.*?)\\s*:\\s*(.*?)\\s*:\\s*(\\w*)")
							var m: Matcher
							var line: String? = reader.readLine()
							while (line != null) {
								m = pattern.matcher(line)
								if (m.find()) {
									val indices = m.group(1)
											.split('-')
											.mapNotNull { it.toIntOrNull() }
											.let { if (it.size == 2) it[0]..it[1] else it[0]..it[0] }
									val description = m.group(2)
									val units = m.group(3)
									val name = m.group(4)
									if (!arrayOf("RESERVED", "MISSING").contains(name)) {
										val params = indices
												.map { Grib2Parameter(discipline, category, it, name, description, units) }
										//.onEach { Logger.debug("Param: ${it}") }
										paramList.addAll(params)
									}
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

	override fun toString() = "Grib2Parameter(discipline=${discipline}, category=${category}, index=${index}," +
			" code='${code}', description='${description}', units='${units}')"
}
