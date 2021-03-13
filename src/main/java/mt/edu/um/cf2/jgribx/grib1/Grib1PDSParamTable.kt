/**
 * ===============================================================================
 * $Id: GribPDSParamTable.java,v 1.10 2006/07/25 13:46:00 frv_peg Exp $
 * ===============================================================================
 * JGRIB library
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Authors:
 * See AUTHORS file
 * ===============================================================================
 */
/**
 * GribPDSParamTable.java  1.0  08/01/2002
 *
 * Newly created, based on GribTables class.  Moved Parameter Table specific
 * functionality to this class.
 * Performs operations related to loading parameter tables stored in files.
 * Removed the embedded table as this limited functionality and
 * made dynamic changes impossible.
 * Through a lookup table (see readParameterTableLookup) all of the supported
 * Parameter Tables are known.  An actual table is not loaded until a parameter
 * from that center/subcenter/table is loaded.
 *
 * For now, the lookup table name is hard coded to ".\\tables\\tablelookup.lst"
 *
 * rdg - Still need to finish implementing SubCenters
 *
 * @Author: Capt Richard D. Gonzalez
 */
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.io.*
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 *
 * @property filename Stores the name of the file containing this table - not opened unless required for lookup.
 * @property centerId Identification of center e.g. 88 for Oslo
 * @property subCenterId Identification of center defined sub-center - not fully implemented yet
 * @property tableNumber Identification of parameter table version number
 * @property parameters Parameters - stores array of GribPDSParameter classes
 */
class Grib1PDSParamTable(protected var filename: String? = null,
						 protected var centerId: Int = 0,
						 protected var subCenterId: Int = 0,
						 protected var tableNumber: Int = 0,
						 protected var parameters: Array<Grib1Parameter>? = null) {

	companion object {
		/**
		 * System property variable to search, or set, when using user supplied gribtab directories, and or a stand
		 * alone gribtab file
		 *
		 * !! Important: Remember to format this as an URL !!
		 */
		const val PROPERTY_GRIBTABURL = "GribTabURL"

		/** Name of directory to search in, when reading buildin parameter tables, ie stored in jgrib.jar */
		private const val TABLE_DIRECTORY = "tables"

		/** Name of file to read, when searching for parameter tables stored in a directory */
		private const val TABLE_LIST = "tablelookup.lst"

		/** There is 256 entries in a parameter table due to the nature of a byte */
		private const val NPARAMETERS = 256

		/** List of parameter tables */
		var tables: ArrayList<*>? = null

		/**
		 * Added by Richard D. Gonzalez static Array with parameter tables used by the GRIB file (should only be one,
		 * but not actually limited to that - this allows GRIB files to be read that have more than one center's
		 * information in it)
		 */
		protected val paramTables: Array<Grib1PDSParamTable>? = null

		/** Used to store names of files */
		protected val fileTabMap: MutableMap<Any, Any> = HashMap<Any, Any>()

		/**
		 * Load default tables from jar file (class path)
		 *
		 * Reads in the list of tables available and stores them.  Does not actually
		 * open the parameter tables files, nor store the list of parameters, but
		 * just stores the file names of the parameter tables.
		 * Parameters for a table are read in when the table is requested (in the
		 * getParameterTable method).
		 * Currently hardcoded the file name to "tablelookup".  May change to command
		 * line later, but would rather minimize command line inputs.
		 *
		 * Added by Tor C.Bekkvik
		 * todo add method for appending more GRIBtables later
		 * todo comments
		 * todo repeated gribtables in tablelookup : load only 1 copy !
		 * todo keep mapping info; keep destination table center,table etc
		 * @param aTables
		 * @throws IOException
		 */
		private fun initFromJAR(aTables: ArrayList<Grib1PDSParamTable>) {
			val cl = Grib1PDSParamTable::class.java.classLoader
			val baseUrl = cl.getResource(TABLE_DIRECTORY) ?: return
			Logger.debug("JGRIB: Buildin gribtab url = ${baseUrl.toExternalForm()}")
			readTableEntries(baseUrl.toExternalForm(), aTables)
		}

		/**
		 * Initiate default tables
		 * added by Tor C.Bekkvik
		 * @param tables
		 */
		private fun initDefaultTableEntries(tables: ArrayList<Grib1PDSParamTable>) {
			val defaulttableNcepReanal2 = arrayOf(arrayOf("var0", "undefined", "undefined"),
					arrayOf("pres", "Pressure", "Pa"),
					arrayOf("prmsl", "Pressure reduced to MSL", "Pa"),
					arrayOf("ptend", "Pressure tendency", "Pa/s"),
					arrayOf("var4", "undefined", "undefined"),
					arrayOf("var5", "undefined", "undefined"),
					arrayOf("gp", "Geopotential", "m^2/s^2"),
					arrayOf("hgt", "Geopotential height", "gpm"),
					arrayOf("dist", "Geometric height", "m"),
					arrayOf("hstdv", "Std dev of height", "m"),
					arrayOf("hvar", "Varianance of height", "m^2"),
					arrayOf("tmp", "Temperature", "K"),
					arrayOf("vtmp", "Virtual temperature", "K"),
					arrayOf("pot", "Potential temperature", "K"),
					arrayOf("epot", "Pseudo-adiabatic pot. temperature", "K"),
					arrayOf("tmax", "Max. temperature", "K"),
					arrayOf("tmin", "Min. temperature", "K"),
					arrayOf("dpt", "Dew point temperature", "K"),
					arrayOf("depr", "Dew point depression", "K"),
					arrayOf("lapr", "Lapse rate", "K/m"),
					arrayOf("visib", "Visibility", "m"),
					arrayOf("rdsp1", "Radar spectra (1)", ""),
					arrayOf("rdsp2", "Radar spectra (2)", ""),
					arrayOf("rdsp3", "Radar spectra (3)", ""),
					arrayOf("var24", "undefined", "undefined"),
					arrayOf("tmpa", "Temperature anomaly", "K"),
					arrayOf("presa", "Pressure anomaly", "Pa"),
					arrayOf("gpa", "Geopotential height anomaly", "gpm"),
					arrayOf("wvsp1", "Wave spectra (1)", ""),
					arrayOf("wvsp2", "Wave spectra (2)", ""),
					arrayOf("wvsp3", "Wave spectra (3)", ""),
					arrayOf("wdir", "Wind direction", "deg"),
					arrayOf("wind", "Wind speed", "m/s"),
					arrayOf("ugrd", "u wind", "m/s"),
					arrayOf("vgrd", "v wind", "m/s"),
					arrayOf("strm", "Stream function", "m^2/s"),
					arrayOf("vpot", "Velocity potential", "m^2/s"),
					arrayOf("mntsf", "Montgomery stream function", "m^2/s^2"),
					arrayOf("sgcvv", "Sigma coord. vertical velocity", "/s"),
					arrayOf("vvel", "Pressure vertical velocity", "Pa/s"),
					arrayOf("dzdt", "Geometric vertical velocity", "m/s"),
					arrayOf("absv", "Absolute vorticity", "/s"),
					arrayOf("absd", "Absolute divergence", "/s"),
					arrayOf("relv", "Relative vorticity", "/s"),
					arrayOf("reld", "Relative divergence", "/s"),
					arrayOf("vucsh", "Vertical u shear", "/s"),
					arrayOf("vvcsh", "Vertical v shear", "/s"),
					arrayOf("dirc", "Direction of current", "deg"),
					arrayOf("spc", "Speed of current", "m/s"),
					arrayOf("uogrd", "u of current", "m/s"),
					arrayOf("vogrd", "v of current", "m/s"),
					arrayOf("spfh", "Specific humidity", "kg/kg"),
					arrayOf("rh", "Relative humidity", "%"),
					arrayOf("mixr", "Humidity mixing ratio", "kg/kg"),
					arrayOf("pwat", "Precipitable water", "kg/m^2"),
					arrayOf("vapp", "Vapor pressure", "Pa"),
					arrayOf("satd", "Saturation deficit", "Pa"),
					arrayOf("evp", "Evaporation", "kg/m^2"),
					arrayOf("cice", "Cloud Ice", "kg/m^2"),
					arrayOf("prate", "Precipitation rate", "kg/m^2/s"),
					arrayOf("tstm", "Thunderstorm probability", "%"),
					arrayOf("apcp", "Total precipitation", "kg/m^2"),
					arrayOf("ncpcp", "Large scale precipitation", "kg/m^2"),
					arrayOf("acpcp", "Convective precipitation", "kg/m^2"),
					arrayOf("srweq", "Snowfall rate water equiv.", "kg/m^2/s"),
					arrayOf("weasd", "Accum. snow", "kg/m^2"),
					arrayOf("snod", "Snow depth", "m"),
					arrayOf("mixht", "Mixed layer depth", "m"),
					arrayOf("tthdp", "Transient thermocline depth", "m"),
					arrayOf("mthd", "Main thermocline depth", "m"),
					arrayOf("mtha", "Main thermocline anomaly", "m"),
					arrayOf("tcdc", "Total cloud cover", "%"),
					arrayOf("cdcon", "Convective cloud cover", "%"),
					arrayOf("lcdc", "Low level cloud cover", "%"),
					arrayOf("mcdc", "Mid level cloud cover", "%"),
					arrayOf("hcdc", "High level cloud cover", "%"),
					arrayOf("cwat", "Cloud water", "kg/m^2"),
					arrayOf("var77", "undefined", "undefined"),
					arrayOf("snoc", "Convective snow", "kg/m^2"),
					arrayOf("snol", "Large scale snow", "kg/m^2"),
					arrayOf("wtmp", "Water temperature", "K"),
					arrayOf("land", "Land cover (land=1;sea=0)", "fraction"),
					arrayOf("dslm", "Deviation of sea level from mean", "m"),
					arrayOf("sfcr", "Surface roughness", "m"),
					arrayOf("albdo", "Albedo", "%"),
					arrayOf("tsoil", "Soil temperature", "K"),
					arrayOf("soilm", "Soil moisture content", "kg/m^2"),
					arrayOf("veg", "Vegetation", "%"),
					arrayOf("salty", "Salinity", "kg/kg"),
					arrayOf("den", "Density", "kg/m^3"),
					arrayOf("runof", "Runoff", "kg/m^2"),
					arrayOf("icec", "Ice concentration (ice=1;no ice=0)", "fraction"),
					arrayOf("icetk", "Ice thickness", "m"),
					arrayOf("diced", "Direction of ice drift", "deg"),
					arrayOf("siced", "Speed of ice drift", "m/s"),
					arrayOf("uice", "u of ice drift", "m/s"),
					arrayOf("vice", "v of ice drift", "m/s"),
					arrayOf("iceg", "Ice growth rate", "m/s"),
					arrayOf("iced", "Ice divergence", "/s"),
					arrayOf("snom", "Snow melt", "kg/m^2"),
					arrayOf("htsgw", "Sig height of wind waves and swell", "m"),
					arrayOf("wvdir", "Direction of wind waves", "deg"),
					arrayOf("wvhgt", "Sig height of wind waves", "m"),
					arrayOf("wvper", "Mean period of wind waves", "s"),
					arrayOf("swdir", "Direction of swell waves", "deg"),
					arrayOf("swell", "Sig height of swell waves", "m"),
					arrayOf("swper", "Mean period of swell waves", "s"),
					arrayOf("dirpw", "Primary wave direction", "deg"),
					arrayOf("perpw", "Primary wave mean period", "s"),
					arrayOf("dirsw", "Secondary wave direction", "deg"),
					arrayOf("persw", "Secondary wave mean period", "s"),
					arrayOf("nswrs", "Net short wave (surface)", "W/m^2"),
					arrayOf("nlwrs", "Net long wave (surface)", "W/m^2"),
					arrayOf("nswrt", "Net short wave (top)", "W/m^2"),
					arrayOf("nlwrt", "Net long wave (top)", "W/m^2"),
					arrayOf("lwavr", "Long wave", "W/m^2"),
					arrayOf("swavr", "Short wave", "W/m^2"),
					arrayOf("grad", "Global radiation", "W/m^2"),
					arrayOf("var118", "undefined", "undefined"),
					arrayOf("var119", "undefined", "undefined"),
					arrayOf("var120", "undefined", "undefined"),
					arrayOf("lhtfl", "Latent heat flux", "W/m^2"),
					arrayOf("shtfl", "Sensible heat flux", "W/m^2"),
					arrayOf("blydp", "Boundary layer dissipation", "W/m^2"),
					arrayOf("uflx", "Zonal momentum flux", "N/m^2"),
					arrayOf("vflx", "Meridional momentum flux", "N/m^2"),
					arrayOf("wmixe", "Wind mixing energy", "J"),
					arrayOf("imgd", "Image data", ""),
					arrayOf("mslsa", "Mean sea level pressure (Std Atm)", "Pa"),
					arrayOf("mslma", "Mean sea level pressure (MAPS)", "Pa"),
					arrayOf("mslet", "Mean sea level pressure (ETA model)", "Pa"),
					arrayOf("lftx", "Surface lifted index", "K"),
					arrayOf("4lftx", "Best (4-layer) lifted index", "K"),
					arrayOf("kx", "K index", "K"),
					arrayOf("sx", "Sweat index", "K"),
					arrayOf("mconv", "Horizontal moisture divergence", "kg/kg/s"),
					arrayOf("vssh", "Vertical speed shear", "1/s"),
					arrayOf("tslsa", "3-hr pressure tendency (Std Atmos Red)", "Pa/s"),
					arrayOf("bvf2", "Brunt-Vaisala frequency^2", "1/s^2"),
					arrayOf("pvmw", "Potential vorticity (mass-weighted)", "1/s/m"),
					arrayOf("crain", "Categorical rain", "yes=1;no=0"),
					arrayOf("cfrzr", "Categorical freezing rain", "yes=1;no=0"),
					arrayOf("cicep", "Categorical ice pellets", "yes=1;no=0"),
					arrayOf("csnow", "Categorical snow", "yes=1;no=0"),
					arrayOf("soilw", "Volumetric soil moisture", "fraction"),
					arrayOf("pevpr", "Potential evaporation rate", "W/m^2"),
					arrayOf("cwork", "Cloud work function", "J/kg"),
					arrayOf("u-gwd", "Zonal gravity wave stress", "N/m^2"),
					arrayOf("v-gwd", "Meridional gravity wave stress", "N/m^2"),
					arrayOf("pvort", "Potential vorticity", "m^2/s/kg"),
					arrayOf("var150", "undefined", "undefined"),
					arrayOf("var151", "undefined", "undefined"),
					arrayOf("var152", "undefined", "undefined"),
					arrayOf("mfxdv", "Moisture flux divergence", "gr/gr*m/s/m"),
					arrayOf("vqr154", "undefined", "undefined"),
					arrayOf("gflux", "Ground heat flux", "W/m^2"),
					arrayOf("cin", "Convective inhibition", "J/kg"),
					arrayOf("cape", "Convective Avail. Pot. Energy", "J/kg"),
					arrayOf("tke", "Turbulent kinetic energy", "J/kg"),
					arrayOf("condp", "Lifted parcel condensation pressure", "Pa"),
					arrayOf("csusf", "Clear sky upward solar flux", "W/m^2"),
					arrayOf("csdsf", "Clear sky downward solar flux", "W/m^2"),
					arrayOf("csulf", "Clear sky upward long wave flux", "W/m^2"),
					arrayOf("csdlf", "Clear sky downward long wave flux", "W/m^2"),
					arrayOf("cfnsf", "Cloud forcing net solar flux", "W/m^2"),
					arrayOf("cfnlf", "Cloud forcing net long wave flux", "W/m^2"),
					arrayOf("vbdsf", "Visible beam downward solar flux", "W/m^2"),
					arrayOf("vddsf", "Visible diffuse downward solar flux", "W/m^2"),
					arrayOf("nbdsf", "Near IR beam downward solar flux", "W/m^2"),
					arrayOf("nddsf", "Near IR diffuse downward solar flux", "W/m^2"),
					arrayOf("ustr", "U wind stress", "N/m^2"),
					arrayOf("vstr", "V wind stress", "N/m^2"),
					arrayOf("mflx", "Momentum flux", "N/m^2"),
					arrayOf("lmh", "Mass point model surface", ""),
					arrayOf("lmv", "Velocity point model surface", ""),
					arrayOf("sglyr", "Neraby model level", ""),
					arrayOf("nlat", "Latitude", "deg"),
					arrayOf("nlon", "Longitude", "deg"),
					arrayOf("umas", "Mass weighted u", "gm/m*K*s"),
					arrayOf("vmas", "Mass weigtted v", "gm/m*K*s"),
					arrayOf("var180", "undefined", "undefined"),
					arrayOf("lpsx", "x-gradient of log pressure", "1/m"),
					arrayOf("lpsy", "y-gradient of log pressure", "1/m"),
					arrayOf("hgtx", "x-gradient of height", "m/m"),
					arrayOf("hgty", "y-gradient of height", "m/m"),
					arrayOf("stdz", "Standard deviation of Geop. hgt.", "m"),
					arrayOf("stdu", "Standard deviation of zonal wind", "m/s"),
					arrayOf("stdv", "Standard deviation of meridional wind", "m/s"),
					arrayOf("stdq", "Standard deviation of spec. hum.", "gm/gm"),
					arrayOf("stdt", "Standard deviation of temperature", "K"),
					arrayOf("cbuw", "Covariance between u and omega", "m/s*Pa/s"),
					arrayOf("cbvw", "Covariance between v and omega", "m/s*Pa/s"),
					arrayOf("cbuq", "Covariance between u and specific hum", "m/s*gm/gm"),
					arrayOf("cbvq", "Covariance between v and specific hum", "m/s*gm/gm"),
					arrayOf("cbtw", "Covariance between T and omega", "K*Pa/s"),
					arrayOf("cbqw", "Covariance between spec. hum and omeg", "gm/gm*Pa/s"),
					arrayOf("cbmzw", "Covariance between v and u", "m^2/si^2"),
					arrayOf("cbtzw", "Covariance between u and T", "K*m/s"),
					arrayOf("cbtmw", "Covariance between v and T", "K*m/s"),
					arrayOf("stdrh", "Standard deviation of Rel. Hum.", "%"),
					arrayOf("sdtz", "Std dev of time tend of geop. hgt", "m"),
					arrayOf("icwat", "Ice-free water surface", "%"),
					arrayOf("sdtu", "Std dev of time tend of zonal wind", "m/s"),
					arrayOf("sdtv", "Std dev of time tend of merid wind", "m/s"),
					arrayOf("dswrf", "Downward solar radiation flux", "W/m^2"),
					arrayOf("dlwrf", "Downward long wave radiation flux", "W/m^2"),
					arrayOf("sdtq", "Std dev of time tend of spec. hum", "gm/gm"),
					arrayOf("mstav", "Moisture availability", "%"),
					arrayOf("sfexc", "Exchange coefficient", "(kg/m^3)(m/s)"),
					arrayOf("mixly", "No. of mixed layers next to surface", "integer"),
					arrayOf("sdtt", "Std dev of time tend of temperature", "K"),
					arrayOf("uswrf", "Upward short wave flux", "W/m^2"),
					arrayOf("ulwrf", "Upward long wave flux", "W/m^2"),
					arrayOf("cdlyr", "Non-convective cloud", "%"),
					arrayOf("cprat", "Convective precip. rate", "kg/m^2/s"),
					arrayOf("ttdia", "Temperature tendency by all physics", "K/s"),
					arrayOf("ttrad", "Temperature tendency by all radiation", "K/s"),
					arrayOf("ttphy", "Temperature tendency by non-radiation physics", "K/s"),
					arrayOf("preix", "Precip index (0.0-1.00)", "fraction"),
					arrayOf("tsd1d", "Std. dev. of IR T over 1x1 deg area", "K"),
					arrayOf("nlgsp", "Natural log of surface pressure", "ln(kPa)"),
					arrayOf("sdtrh", "Std dev of time tend of rel humt", "%"),
					arrayOf("5wavh", "5-wave geopotential height", "gpm"),
					arrayOf("cwat", "Plant canopy surface water", "kg/m^2"),
					arrayOf("pltrs", "Maximum stomato plant resistance", "s/m"),
					arrayOf("rhcld", "RH-type cloud cover", "%"),
					arrayOf("bmixl", "Blackadar's mixing length scale", "m"),
					arrayOf("amixl", "Asymptotic mixing length scale", "m"),
					arrayOf("pevap", "Potential evaporation", "kg^2"),
					arrayOf("snohf", "Snow melt heat flux", "W/m^2"),
					arrayOf("snoev", "Snow sublimation heat flux", "W/m^2"),
					arrayOf("mflux", "Convective cloud mass flux", "Pa/s"),
					arrayOf("dtrf", "Downward total radiation flux", "W/m^2"),
					arrayOf("utrf", "Upward total radiation flux", "W/m^2"),
					arrayOf("bgrun", "Baseflow-groundwater runoff", "kg/m^2"),
					arrayOf("ssrun", "Storm surface runoff", "kg/m^2"),
					arrayOf("var236", "undefined", "undefined"),
					arrayOf("ozone", "Total column ozone concentration", "Dobson"),
					arrayOf("snoc", "Snow cover", "%"),
					arrayOf("snot", "Snow temperature", "K"),
					arrayOf("glcr", "Permanent snow points", "mask"),
					arrayOf("lrghr", "Large scale condensation heating rate", "K/s"),
					arrayOf("cnvhr", "Deep convective heating rate", "K/s"),
					arrayOf("cnvmr", "Deep convective moistening rate", "kg/kg/s"),
					arrayOf("shahr", "Shallow convective heating rate", "K/s"),
					arrayOf("shamr", "Shallow convective moistening rate", "kg/kg/s"),
					arrayOf("vdfhr", "Vertical diffusion heating rate", "K/s"),
					arrayOf("vdfua", "Vertical diffusion zonal accel", "m/s/s"),
					arrayOf("vdfva", "Vertical diffusion meridional accel", "m/s/s"),
					arrayOf("vdfmr", "Vertical diffusion moistening rate", "kg/kg/s"),
					arrayOf("swhr", "Solar radiative heating rate", "K/s"),
					arrayOf("lwhr", "Longwave radiative heating rate", "K/s"),
					arrayOf("cd", "Drag coefficient", ""),
					arrayOf("fricv", "Friction velocity", "m/s"),
					arrayOf("ri", "Richardson number", ""),
					arrayOf("missing", "Missing parameter", ""))
			//assert npar <= NPARAMETERS;

			val parameters = defaulttableNcepReanal2
					.mapIndexed { n, (name, desc, unit) -> Grib1Parameter(n, name, desc, unit) }
					.toTypedArray()
			tables.add(Grib1PDSParamTable("ncep_reanal2.1", 7, -1, 1, parameters))
			tables.add(Grib1PDSParamTable("ncep_reanal2.2", 7, -1, 2, parameters))
			tables.add(Grib1PDSParamTable("ncep_reanal2.3", 7, -1, 3, parameters))
			tables.add(Grib1PDSParamTable("ncep_reanal2.4", 81, -1, 3, parameters))
			tables.add(Grib1PDSParamTable("ncep_reanal2.5", 88, -1, 2, parameters))
			tables.add(Grib1PDSParamTable("ncep_reanal2.6", 88, -1, 128, parameters))
		}

		fun readTableEntry(aFileUrl: URL, aTables: ArrayList<Grib1PDSParamTable>) {
			Logger.debug("JGRIB: readTableEntry: aFileUrl = $aFileUrl")
			val isr = InputStreamReader(aFileUrl.openStream())
			val br = BufferedReader(isr)
			val line = br.readLine()
			if (line.isEmpty() || line.startsWith("//")) {
				throw NotSupportedException("Gribtab files cannot start with blanks " +
						"or comments, - Please follow standard (-1:center:subcenter:tablenumber)")
			}
			val table = Grib1PDSParamTable()
			val tableDefArr = line.split(":")
			table.centerId = tableDefArr[1].trim { it <= ' ' }.toInt()
			table.subCenterId = tableDefArr[2].trim { it <= ' ' }.toInt()
			table.tableNumber = tableDefArr[3].trim { it <= ' ' }.toInt()
			table.filename = aFileUrl.toExternalForm()
			table.url = aFileUrl
			aTables.add(table)
			br.close()
			isr.close()
		}

		private fun readTableEntries(baseUrl: String, aTables: ArrayList<Grib1PDSParamTable>) {
			// Open file
			Logger.debug("JGRIB: readTableEntries: aBaseUrl =$baseUrl")
			val inputStream = URL("${baseUrl}/$TABLE_LIST").openStream()
			val isr = InputStreamReader(inputStream)
			val br = BufferedReader(isr)
			var line: String? = br.readLine()
			while (line != null) {
				// Skip blank lines and comment lines (//)
				if (line.isEmpty() || line.startsWith("//")) continue
				val table = Grib1PDSParamTable()
				val cix = line.indexOf("//") //comment index
				if (cix > 0) {
					line = line.substring(0, cix)
				}
				val tableDefArr = line.split(":")
				if (tableDefArr.size < 4) continue
				table.centerId = tableDefArr[0].trim { it <= ' ' }.toInt()
				table.subCenterId = tableDefArr[1].trim { it <= ' ' }.toInt()
				table.tableNumber = tableDefArr[2].trim { it <= ' ' }.toInt()
				table.filename = tableDefArr[3].trim { it <= ' ' }
				table.url = URL(baseUrl + "/" + table.filename)
				aTables.add(table)
				line = br.readLine()
			}
			inputStream.close()
		}

		/**
		 * Looks for the parameter table which matches the center, subcenter
		 * and table version from the tables array.
		 * If this is the first time asking for this table, then the parameters for
		 * this table have not been read in yet, so this is done as well.
		 *
		 * @param centerId - integer from PDS octet 5, representing Center.
		 * @param subCenterId - integer from PDS octet 26, representing Subcenter
		 * @param number - integer from PDS octet 4, representing Parameter Table Version
		 * @return GribPDSParamTable matching center, subcenter, and number
		 * @throws NotSupportedException
		 */
		fun getParameterTable(centerId: Int, subCenterId: Int, number: Int): Grib1PDSParamTable {
			// 1) search excact match                   (center, table)
			// 2) if (1) failed, search matching table  ( - ,table(1..3))
			paramTables?.reversed()?.forEach { table ->
				// for (int i=0; i < paramTables.length; i++) {
				if (table.centerId != -1 && table.centerId == centerId) {
					if (table.subCenterId == -1 ||
							subCenterId == table.subCenterId) {
						if (number == table.tableNumber) {
							// now that this table is being used, check to see if the
							//   parameters for this table have been read in yet.
							// If not, initialize table and read them in now.
							table.readParameterTable()
							return table
						}
					}
				}
			}
			//search matching table  ( - ,table(1..3))
			paramTables?.reversed()?.forEach { table ->
				// for (int i = 0; i < paramTables.length; i++){
				if (table.centerId == -1 && number == table.tableNumber) {
					table.readParameterTable()
					return table
				}
			}
			throw NotSupportedException("Grib table not supported; center: ${centerId}, sub: ${subCenterId}, table: ${number}")

			// System.out.println("cent, sub, tab: "+center+" "+subcenter+" "+number);
			// return null;
		}

		/**
		 * Gets parameter information from a locally stored file corresponding to the specified centre, subcentre and table version
		 * <br></br><br></br>
		 * 24/04/2017 - Andrew Spiteri - first version
		 * @param centre
		 * @param subcentre
		 * @param tableVersion
		 * @param entryNum
		 * @return
		 */
		fun getParameterFromFile(centre: Int, subcentre: Int, tableVersion: Int, entryNum: Int): Grib1Parameter? {
			var param: Grib1Parameter? = null
			val filename = "res/" + centre + "_" + subcentre + "_" + tableVersion + ".gpt"
			val tableFile = File(filename)
			Logger.info("Accessing parameter $entryNum in table file: ")
			if (!tableFile.exists()) {
				Logger.warning("Cannot find " + tableFile.absolutePath)
				return null
			}
			try {
				BufferedReader(FileReader(tableFile)).use { reader ->
					var line: String
					val pattern = Pattern.compile("(\\d+)\\s*,(.*?)\\s*,(.*?)\\s*,(\\w*)")
					var m: Matcher
					while (reader.readLine().also { line = it } != null) {
						m = pattern.matcher(line)
						if (m.find()) {
							val entry = m.group(1).toInt()
							if (entry == entryNum) {
								val paramDesc = m.group(2)
								val paramUnits = m.group(3)
								val paramName = m.group(4)
								param = Grib1Parameter(entry, paramName, paramDesc, paramUnits)
								break
							}
						}
					}
				}
			} catch (e: FileNotFoundException) {
				Logger.error("Cannot find ${tableFile.absolutePath}")
				return null
			} catch (e: IOException) {
				Logger.error("Cannot read ${tableFile.absolutePath}")
				return null
			}
			return param
		}
		/**
		 * - peg - As of 2005-12-09
		 * Re-implemented static method to allow for user supplied
		 * directory structures as known from initFromJar and also to
		 * make it possible to read a single gribtab file. IE without
		 * having to create a tablelookup.lst file
		 */
	}

	/** URL store corresponding url of filename containint this table. Opened if required for lookup. */
	var url: URL? = null
		protected set

	/**
	 * Get the parameter with id <tt>id</tt>.
	 * @param id
	 * @return description of the unit for the parameter
	 */
	fun getParameter(id: Int): Grib1Parameter {
		require(!(id < 0 || id >= NPARAMETERS)) { "Bad id: $id" }
		return parameters?.getOrNull(id) ?: Grib1Parameter(id, "undef_$id", "undef", "undef")
	}

	/**
	 * Get the tag/name of the parameter with id <tt>id</tt>.
	 *
	 * @param id
	 * @return tag/name of the parameter
	 */
	fun getParameterTag(id: Int): String = getParameter(id).abbreviation

	/**
	 * Get a description for the parameter with id <tt>id</tt>.
	 *
	 * @param id
	 * @return description for the parameter
	 */
	fun getParameterDescription(id: Int): String = getParameter(id).description

	/**
	 * Get a description for the unit with id <tt>id</tt>.
	 *
	 * @param id
	 * @return description of the unit for the parameter
	 */
	fun getParameterUnit(id: Int): String = getParameter(id).units

	/**
	 * @param pdsPar
	 * @return true/false
	 */
	private fun setParameter(pdsPar: Grib1Parameter?): Boolean {
		if (pdsPar == null) return false
		val id = pdsPar.id
		if (id < 0 || id >= NPARAMETERS) return false
		parameters?.set(id, pdsPar)
		return true
	}

	/** Read parameter table */
	private fun readParameterTable() {
		if (parameters != null) return
		//parameters = arrayOfNulls(NPARAMETERS)
		val center: Int
		val subcenter: Int
		val number: Int
		try {
			val br: BufferedReader
			val filename = filename
			if (filename != null && filename.isNotEmpty()) {
				val tab = fileTabMap[filename] as? Grib1PDSParamTable
				if (tab != null) {
					parameters = tab.parameters
					return
				}
			}
			br = url?.openStream()
					?.let { InputStreamReader(it) }
					?.let { BufferedReader(it) }
					?: BufferedReader(FileReader("tables\\${filename}"))

			// Read first
			var line = br.readLine()
			var tableDefArr = line.split(":")
			center = tableDefArr[1].trim { it <= ' ' }.toInt()
			subcenter = tableDefArr[2].trim { it <= ' ' }.toInt()
			number = tableDefArr[3].trim { it <= ' ' }.toInt()
			// if (center != center_id && subcenter != subcenter_id &&
			// 		number!= table_number){
			// 	throw IOException("parameter table header values do not match values in GRIB file. Possible error in lookup table.");
			//  }

			// int i=0;  // peg - Variable never used
			// rdg - added the 0 line length check to cover the case of blank lines at
			//       the end of the parameter table file.
			while (br.readLine().also { line = it } != null) {
				line = line.trim { it <= ' ' }
				if (line.isEmpty() || line.startsWith("//")) continue
				//val parameter = Grib1Parameter()
				tableDefArr = line.split(":")
				val num = tableDefArr[0].trim { it <= ' ' }.toInt()
				val abbreviation = tableDefArr[1].trim { it <= ' ' }
				// check to see if unit defined, if not, parameter is undefined
				val units: String
				val description: String
				if (tableDefArr[2].indexOf('[') == -1) {
					// Undefined unit
					units = tableDefArr[2].trim { it <= ' ' }
					description = units
				} else {
					val arr2 = tableDefArr[2].split("[")
					description = arr2[0].trim { it <= ' ' }
					// Remove "]"
					units = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim { it <= ' ' }
					//            parameter.unit = arr2[1].substring(0, arr2[1].lastIndexOf(']')).trim();
				}
				val parameter = Grib1Parameter(num, abbreviation, description, units)
				//this.parameters[i++]=parameter;
				if (!setParameter(parameter)) {
					Logger.error("Warning, bad parameter ignored ($filename): $parameter")
				}
			}
			if (filename != null && filename.isNotEmpty()) {
				val loadedTable = Grib1PDSParamTable(filename, center, subcenter, number, parameters)
				fileTabMap[filename] = loadedTable
			}
		} catch (e: IOException) {
			Logger.error("An error occurred in GribPDSParamTable while trying to open the parameter table ${filename}: ${e.message}",
					e)
		}
	}

	override fun toString(): String = listOfNotNull(
			"-1:${centerId}:${subCenterId}:${tableNumber}",
			*(parameters?.map { "\t${it}" }?.toTypedArray() ?: emptyArray()))
			.joinToString("\n")
}
