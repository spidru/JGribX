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

/**
 * Title:        JGrib
 * Description:  Class which represents a parameter from a PDS parameter table
 * Copyright:    Copyright (c) 2002
 * Company:      U.S. Air Force
 *
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 *
 * @property number - Parameter number [0 - 255]
 * @property abbreviation - Parameter abbreviation
 * @property description - Parameter description
 * @property units - Parameter unit
 */
class Grib1Parameter(internal val number: Int = 0,
					 internal val abbreviation: String = "",
					 internal val description: String = "",
					 internal val units: String = "") {

	companion object {
		// As of Nov 2017, all Table Versions make use of Table 2 for Parameter IDs between 0 and 128
		fun getParameter(version: Int, paramId: Int, centreId: Int): Grib1Parameter? = if (paramId < 128)
			getParameterFromTable2(paramId, centreId)
		else when (version) {
			2 -> getParameterFromTable2(paramId, centreId)
			128 -> getParameterFromTable128(paramId)
			129 -> getParameterFromTable129(paramId)
			else -> null
		}

		/**
		 * Returns the parameter in Parameter Table 2 corresponding to the specified ID.
		 * <br></br><br></br>
		 * Reference: [http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html](http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html)
		 * @param paramId
		 * @return
		 */
		private fun getParameterFromTable2(paramId: Int, centreId: Int): Grib1Parameter? = when (paramId) {
			1 -> Grib1Parameter(
					abbreviation = "PRES",
					description = "Pressure",
					units = "Pa")
			2 -> Grib1Parameter(
					abbreviation = "PRMSL",
					description = "Pressure reduced to MSL",
					units = "Pa")
			3 -> Grib1Parameter(
					abbreviation = "PTEND",
					description = "Pressure tendency",
					units = "Pa/s")
			4 -> Grib1Parameter(
					abbreviation = "PVORT",
					description = "Potential vorticity",
					units = "K m^2 kg^-1 s^-1")
			5 -> Grib1Parameter(
					abbreviation = "ICAHT",
					description = "ICAO Standard Atmosphere Reference Height",
					units = "m")
			6 -> Grib1Parameter(
					abbreviation = "GP",
					description = "Geopotential",
					units = "m^2/s^2")
			7 -> Grib1Parameter(
					abbreviation = "HGT",
					description = "Geopotential height",
					units = "gpm")
			8 -> Grib1Parameter(
					abbreviation = "DIST",
					description = "Geometric height",
					units = "m")
			9 -> Grib1Parameter(
					abbreviation = "HSTDV",
					description = "Standard deviation of height",
					units = "m")
			10 -> Grib1Parameter(
					abbreviation = "TOZNE",
					description = "Total ozone",
					units = "Dobson")
			11 -> Grib1Parameter(
					abbreviation = "TMP",
					description = "Temperature",
					units = "K")
			12 -> Grib1Parameter(
					abbreviation = "VTMP",
					description = "Virtual temperature",
					units = "K")
			13 -> Grib1Parameter(
					abbreviation = "POT",
					description = "Potential temperature",
					units = "K")
			14 -> Grib1Parameter(
					abbreviation = "EPOT",
					description = "Pseudo-adiabatic potential temperature (or equivalent potential temperature)",
					units = "K")
			15 -> Grib1Parameter(
					abbreviation = "TMAX",
					description = "Maximum temperature",
					units = "K")
			16 -> Grib1Parameter(
					abbreviation = "TMIN",
					description = "Minimum temperature",
					units = "K")
			17 -> Grib1Parameter(
					abbreviation = "DPT",
					description = "Dew point temperature",
					units = "K")
			18 -> Grib1Parameter(
					abbreviation = "DEPR",
					description = "Dew point depression (or deficit)",
					units = "K")
			19 -> Grib1Parameter(
					abbreviation = "LAPR",
					description = "Lapse rate",
					units = "K/m")
			20 -> Grib1Parameter(
					abbreviation = "VIS",
					description = "Visibility",
					units = "m")
			21 -> Grib1Parameter(
					abbreviation = "RDSP1",
					description = "Radar Spectra (1)",
					units = "-")
			22 -> Grib1Parameter(
					abbreviation = "RDSP2",
					description = "Radar Spectra (2)",
					units = "-")
			23 -> Grib1Parameter(
					abbreviation = "RDSP3",
					description = "Radar Spectra (3)",
					units = "-")
			24 -> Grib1Parameter(
					abbreviation = "PLI",
					description = "Parcel lifted index (to 500 hPa)",
					units = "K")
			25 -> Grib1Parameter(
					abbreviation = "TMPA",
					description = "Temperature anomaly",
					units = "K")
			26 -> Grib1Parameter(
					abbreviation = "PRESA",
					description = "Pressure anomaly",
					units = "Pa")
			27 -> Grib1Parameter(
					abbreviation = "GPA",
					description = "Geopotential height anomaly",
					units = "gpm")
			28 -> Grib1Parameter(
					abbreviation = "WVSP1",
					description = "Wave Spectra (1)",
					units = "-")
			29 -> Grib1Parameter(
					abbreviation = "WVSP2",
					description = "Wave Spectra (2)",
					units = "-")
			30 -> Grib1Parameter(
					abbreviation = "WVSP3",
					description = "Wave Spectra (3)",
					units = "-")
			31 -> Grib1Parameter(
					abbreviation = "WDIR",
					description = "Wind direction (from which blowing)",
					units = "deg true")
			32 -> Grib1Parameter(
					abbreviation = "WIND",
					description = "Wind speed",
					units = "m/s")
			33 -> Grib1Parameter(
					abbreviation = "UGRD",
					description = "u-component of wind",
					units = "m/s")
			34 -> Grib1Parameter(
					abbreviation = "VGRD",
					description = "v-component of wind",
					units = "m/s")
			35 -> Grib1Parameter(
					abbreviation = "STRM",
					description = "Stream function",
					units = "m^2/s")
			36 -> Grib1Parameter(
					abbreviation = "VPOT",
					description = "Velocity potential",
					units = "m^2/s")
			37 -> Grib1Parameter(
					abbreviation = "MNTSF",
					description = "Montgomery stream function",
					units = "m2/s2")
			38 -> Grib1Parameter(
					abbreviation = "SGCVV",
					description = "Sigma coordinate vertical velocity",
					units = "/s")
			39 -> Grib1Parameter(
					abbreviation = "VVEL",
					description = "Vertical velocity (pressure)",
					units = "Pa/s")
			40 -> Grib1Parameter(
					abbreviation = "DZDT",
					description = "Vertical velocity (geometric)",
					units = "m/s")
			41 -> Grib1Parameter(
					abbreviation = "ABSV",
					description = "Absolute vorticity",
					units = "/s")
			42 -> Grib1Parameter(
					abbreviation = "ABSD",
					description = "Absolute divergence",
					units = "/s")
			43 -> Grib1Parameter(
					abbreviation = "RELV",
					description = "Relative vorticity",
					units = "/s")
			44 -> Grib1Parameter(
					abbreviation = "RELD",
					description = "Relative divergence",
					units = "/s")
			45 -> Grib1Parameter(
					abbreviation = "VUCSH",
					description = "Vertical u-component shear",
					units = "/s")
			46 -> Grib1Parameter(
					abbreviation = "VVCSH",
					description = "Vertical v-component shear",
					units = "/s")
			47 -> Grib1Parameter(
					abbreviation = "DIRC",
					description = "Direction of current",
					units = "Degree true")
			48 -> Grib1Parameter(
					abbreviation = "SPC",
					description = "Speed of current",
					units = "m/s")
			49 -> Grib1Parameter(
					abbreviation = "UOGRD",
					description = "u-component of current",
					units = "m/s")
			50 -> Grib1Parameter(
					abbreviation = "VOGRD",
					description = "v-component of current",
					units = "m/s")
			51 -> Grib1Parameter(
					abbreviation = "SPFH",
					description = "Specific humidity",
					units = "kg/kg")
			52 -> Grib1Parameter(
					abbreviation = "RH",
					description = "Relative humidity",
					units = "%")
			53 -> Grib1Parameter(
					abbreviation = "MIXR",
					description = "Humidity mixing ratio",
					units = "kg/kg")
			54 -> Grib1Parameter(
					abbreviation = "PWAT",
					description = "Precipitable water",
					units = "kg/m^2")
			55 -> Grib1Parameter(
					abbreviation = "VAPP",
					description = "Vapor pressure",
					units = "Pa")
			56 -> Grib1Parameter(
					abbreviation = "SATD",
					description = "Saturation deficit",
					units = "Pa")
			57 -> Grib1Parameter(
					abbreviation = "EVP",
					description = "Evaporation",
					units = "kg/m^2")
			58 -> Grib1Parameter(
					abbreviation = "CICE",
					description = "Cloud Ice",
					units = "kg/m^2")
			59 -> Grib1Parameter(
					abbreviation = "PRATE",
					description = "Precipitation rate",
					units = "kg/m^2/s")
			60 -> Grib1Parameter(
					abbreviation = "TSTM",
					description = "Thunderstorm probability",
					units = "%")
			61 -> Grib1Parameter(
					abbreviation = "APCP",
					description = "Total precipitation",
					units = "kg/m^2")
			62 -> Grib1Parameter(
					abbreviation = "NCPCP",
					description = "Large scale precipitation (non-conv.)",
					units = "kg/m^2")
			63 -> Grib1Parameter(
					abbreviation = "ACPCP",
					description = "Convective precipitation",
					units = "kg/m^2")
			64 -> Grib1Parameter(
					abbreviation = "SRWEQ",
					description = "Snowfall rate water equivalent",
					units = "kg/m^2/s")
			65 -> Grib1Parameter(
					abbreviation = "WEASD",
					description = "Water equiv. of accum. snow depth",
					units = "kg/m^2")
			66 -> Grib1Parameter(
					abbreviation = "SNOD",
					description = "Snow depth",
					units = "m")
			67 -> Grib1Parameter(
					abbreviation = "MIXHT",
					description = "Mixed layer depth",
					units = "m")
			68 -> Grib1Parameter(
					abbreviation = "TTHDP",
					description = "Transient thermocline depth",
					units = "m")
			69 -> Grib1Parameter(
					abbreviation = "MTHD",
					description = "Main thermocline depth",
					units = "m")
			70 -> Grib1Parameter(
					abbreviation = "MTHA",
					description = "Main thermocline anomaly",
					units = "m")
			71 -> Grib1Parameter(
					abbreviation = "TCDC",
					description = "Total cloud cover",
					units = "%")
			72 -> Grib1Parameter(
					abbreviation = "CDCON",
					description = "Convective cloud cover",
					units = "%")
			73 -> Grib1Parameter(
					abbreviation = "LCDC",
					description = "Low cloud cover",
					units = "%")
			74 -> Grib1Parameter(
					abbreviation = "MCDC",
					description = "Medium cloud cover",
					units = "%")
			75 -> Grib1Parameter(
					abbreviation = "HCDC",
					description = "High cloud cover",
					units = "%")
			76 -> Grib1Parameter(
					abbreviation = "CWAT",
					description = "Cloud water",
					units = "kg/m^2")
			77 -> Grib1Parameter(
					abbreviation = "BLI",
					description = "Best lifted index (to 500 hPa)",
					units = "K")
			78 -> Grib1Parameter(
					abbreviation = "SNOC",
					description = "Convective snow",
					units = "kg/m^2")
			79 -> Grib1Parameter(
					abbreviation = "SNOL",
					description = "Large scale snow",
					units = "kg/m^2")
			80 -> Grib1Parameter(
					abbreviation = "WTMP",
					description = "Water Temperature",
					units = "K")
			81 -> Grib1Parameter(
					abbreviation = "LAND",
					description = "Land cover (land=1, sea=0)",
					units = "proportion")
			82 -> Grib1Parameter(
					abbreviation = "DSLM",
					description = "Deviation of sea level from mean",
					units = "m")
			83 -> Grib1Parameter(
					abbreviation = "SFCR",
					description = "Surface roughness",
					units = "m")
			84 -> Grib1Parameter(
					abbreviation = "ALBDO",
					description = "Albedo",
					units = "%")
			85 -> Grib1Parameter(
					abbreviation = "TSOIL",
					description = "Soil temperature",
					units = "K")
			86 -> Grib1Parameter(
					abbreviation = "SOILM",
					description = "Soil moisture content",
					units = "kg/m2")
			87 -> Grib1Parameter(
					abbreviation = "VEG",
					description = "Vegetation",
					units = "%")
			88 -> Grib1Parameter(
					abbreviation = "SALTY",
					description = "Salinity",
					units = "kg/kg")
			89 -> Grib1Parameter(
					abbreviation = "DEN",
					description = "Density",
					units = "kg/m^3")
			90 -> Grib1Parameter(
					abbreviation = "WATR",
					description = "Water runoff",
					units = "kg/m^2")
			91 -> Grib1Parameter(
					abbreviation = "ICEC",
					description = "Ice cover (ice=1, no ice=0)",
					units = "proportion")
			92 -> Grib1Parameter(
					abbreviation = "ICETK",
					description = "Ice thickness",
					units = "m")
			93 -> Grib1Parameter(
					abbreviation = "DICED",
					description = "Direction of ice drift",
					units = "deg true")
			94 -> Grib1Parameter(
					abbreviation = "SICED",
					description = "Speed of ice drift",
					units = "m/s")
			95 -> Grib1Parameter(
					abbreviation = "UICE",
					description = "u-component of ice drift",
					units = "m/s")
			96 -> Grib1Parameter(
					abbreviation = "VICE",
					description = "v-component of ice drift",
					units = "m/s")
			97 -> Grib1Parameter(
					abbreviation = "ICEG",
					description = "Ice growth rate",
					units = "m/s")
			98 -> Grib1Parameter(
					abbreviation = "ICED",
					description = "Ice divergence",
					units = "m/s")
			99 -> Grib1Parameter(
					abbreviation = "SNOM",
					description = "Snow melt",
					units = "kg/m^2")
			100 -> Grib1Parameter(
					abbreviation = "HTSGW",
					description = "Significant height of combined wind waves and swell",
					units = "m")
			101 -> Grib1Parameter(
					abbreviation = "WVDIR",
					description = "Direction of wind waves (from which)",
					units = "Degree true")
			102 -> Grib1Parameter(
					abbreviation = "WVHGT",
					description = "Significant height of wind waves",
					units = "m")
			103 -> Grib1Parameter(
					abbreviation = "WVPER",
					description = "Mean period of wind waves",
					units = "s")
			104 -> Grib1Parameter(
					abbreviation = "SWDIR",
					description = "Direction of swell waves",
					units = "Degree true")
			105 -> Grib1Parameter(
					abbreviation = "SWELL",
					description = "Significant height of swell waves",
					units = "m")
			106 -> Grib1Parameter(
					abbreviation = "SWPER",
					description = "Mean period of swell waves",
					units = "s")
			107 -> Grib1Parameter(
					abbreviation = "DIRPW",
					description = "Primary wave direction",
					units = "Degree true")
			108 -> Grib1Parameter(
					abbreviation = "PERPW",
					description = "Primary wave mean period",
					units = "s")
			109 -> Grib1Parameter(
					abbreviation = "DIRSW",
					description = "Secondary wave direction",
					units = "Degree true")
			110 -> Grib1Parameter(
					abbreviation = "PERSW",
					description = "Secondary wave mean period",
					units = "s")
			111 -> Grib1Parameter(
					abbreviation = "NSWRS",
					description = "Net short-wave radiation flux (surface)",
					units = "W/m^2")
			112 -> Grib1Parameter(
					abbreviation = "NLWRS",
					description = "Net long wave radiation flux (surface)",
					units = "W/m^2")
			113 -> Grib1Parameter(
					abbreviation = "NSWRT",
					description = "Net short-wave radiation flux (top of atmosphere)",
					units = "W/m^2")
			114 -> Grib1Parameter(
					abbreviation = "NLWRT",
					description = "Net long wave radiation flux (top of atmosphere)",
					units = "W/m^2")
			115 -> Grib1Parameter(
					abbreviation = "LWAVR",
					description = "Long wave radiation flux",
					units = "W/m^2")
			116 -> Grib1Parameter(
					abbreviation = "SWAVR",
					description = "Short wave radiation flux",
					units = "W/m^2")
			117 -> Grib1Parameter(
					abbreviation = "GRAD",
					description = "Global radiation flux",
					units = "W/m^2")
			118 -> Grib1Parameter(
					abbreviation = "BRTMP",
					description = "Brightness temperature",
					units = "K")
			119 -> Grib1Parameter(
					abbreviation = "LWRAD",
					description = "Radiance (with respect to wave number)",
					units = "W/m/sr")
			120 -> Grib1Parameter(
					abbreviation = "SWRAD",
					description = "Radiance (with respect to wave length)",
					units = "W/m^3/sr")
			121 -> Grib1Parameter(
					abbreviation = "LHTFL",
					description = "Latent heat net flux",
					units = "W/m^2")
			122 -> Grib1Parameter(
					abbreviation = "SHTFL",
					description = "Sensible heat net flux",
					units = "W/m^2")
			123 -> Grib1Parameter(
					abbreviation = "BLYDP",
					description = "Boundary layer dissipation",
					units = "W/m^2")
			124 -> Grib1Parameter(
					abbreviation = "UFLX",
					description = "Momentum flux, u component",
					units = "N/m^2")
			125 -> Grib1Parameter(
					abbreviation = "VFLX",
					description = "Momentum flux, v component",
					units = "N/m^2")
			126 -> Grib1Parameter(
					abbreviation = "WMIXE",
					description = "Wind mixing energy",
					units = "J")
			127 -> Grib1Parameter(
					abbreviation = "IMGD",
					description = "Image data",
					units = "")
			else -> when (centreId) {
				7 -> getParameterFromTable2Centre7(paramId)
				else -> null
			}
		}

		private fun getParameterFromTable2Centre7(paramId: Int): Grib1Parameter? = when (paramId) {
			128 -> Grib1Parameter(
					abbreviation = "MSLSA",
					description = "Mean Sea Level Pressure (Standard Atmosphere Reduction)",
					units = "Pa")
			129 -> Grib1Parameter(
					abbreviation = "MSLMA",
					description = "Mean Sea Level Pressure (MAPS System Reduction)",
					units = "Pa")
			130 -> Grib1Parameter(
					abbreviation = "MSLET",
					description = "Mean Sea Level Pressure (NAM Model Reduction)",
					units = "Pa")
			131 -> Grib1Parameter(
					abbreviation = "LFTX",
					description = "Surface lifted index",
					units = "K")
			132 -> Grib1Parameter(
					abbreviation = "4LFTX",
					description = "Best (4 layer) lifted index",
					units = "K")
			133 -> Grib1Parameter(
					abbreviation = "KX",
					description = "K index",
					units = "K")
			134 -> Grib1Parameter(
					abbreviation = "SX",
					description = "Sweat index",
					units = "K")
			135 -> Grib1Parameter(
					abbreviation = "MCONV",
					description = "Horizontal moisture divergence",
					units = "kg/kg/s")
			136 -> Grib1Parameter(
					abbreviation = "VWSH",
					description = "Vertical speed shear",
					units = "1/s")
			137 -> Grib1Parameter(
					abbreviation = "TSLSA",
					description = "3-hr pressure tendency Std. Atmos. Reduction",
					units = "Pa/s")
			138 -> Grib1Parameter(
					abbreviation = "BVF2",
					description = "Brunt-Vaisala frequency (squared)",
					units = "1/s^2")
			139 -> Grib1Parameter(
					abbreviation = "PVMW",
					description = "Potential vorticity (density weighted)",
					units = "1/s/m")
			140 -> Grib1Parameter(
					abbreviation = "CRAIN",
					description = "Categorical rain (yes=1; no=0)")
			141 -> Grib1Parameter(
					abbreviation = "CFRZR",
					description = "Categorical freezing rain (yes=1; no=0)")
			142 -> Grib1Parameter(
					abbreviation = "CICEP",
					description = "Categorical ice pellets (yes=1; no=0)")
			143 -> Grib1Parameter(
					abbreviation = "CSNOW",
					description = "Categorical snow (yes=1; no=0)")
			144 -> Grib1Parameter(
					abbreviation = "SOILW",
					description = "Volumetric soil moisture content",
					units = "fraction")
			145 -> Grib1Parameter(
					abbreviation = "PEVPR",
					description = "Potential evaporation rate",
					units = "W/m^2")
			146 -> Grib1Parameter(
					abbreviation = "CWORK",
					description = "Cloud work function",
					units = "J/kg")
			147 -> Grib1Parameter(
					abbreviation = "UGWD",
					description = "Zonal flux of gravity wave stress",
					units = "N/m^2")
			148 -> Grib1Parameter(
					abbreviation = "VGWD",
					description = "Meridional flux of gravity wave stress",
					units = "N/m^2")
			149 -> Grib1Parameter(
					abbreviation = "PVORT",
					description = "Potential vorticity",
					units = "m^2/s/kg")
			150 -> Grib1Parameter(
					abbreviation = "COVMZ",
					description = "Covariance between meridional and zonal components of the wind",
					units = "m^2/s^2")
			151 -> Grib1Parameter(
					abbreviation = "COVTZ",
					description = "Covariance between temperature and zonal components of the wind",
					units = "K*m/s")
			152 -> Grib1Parameter(
					abbreviation = "COVTM",
					description = "Covariance between temperature and meridional components of the wind",
					units = "K*m/s")
			153 -> Grib1Parameter(
					abbreviation = "CLWMR",
					description = "Cloud Mixing Ratio",
					units = "kg/kg")
			154 -> Grib1Parameter(
					abbreviation = "O3MR",
					description = "Ozone mixing ratio",
					units = "kg/kg")
			155 -> Grib1Parameter(
					abbreviation = "GFLUX",
					description = "Ground Heat Flux",
					units = "W/m^2")
			156 -> Grib1Parameter(
					abbreviation = "CIN",
					description = "Convective inhibition",
					units = "J/kg")
			157 -> Grib1Parameter(
					abbreviation = "CAPE",
					description = "Convective Available Potential Energy",
					units = "J/kg")
			158 -> Grib1Parameter(
					abbreviation = "TKE",
					description = "Turbulent Kinetic Energy",
					units = "J/kg")
			159 -> Grib1Parameter(
					abbreviation = "CONDP",
					description = "Condensation pressure of parcel lifted from indicated surface",
					units = "Pa")
			160 -> Grib1Parameter(
					abbreviation = "CSUSF",
					description = "Clear Sky Upward Solar Flux",
					units = "W/m^2")
			161 -> Grib1Parameter(
					abbreviation = "CSDSF",
					description = "Clear Sky Downward Solar Flux",
					units = "W/m^2")
			162 -> Grib1Parameter(
					abbreviation = "CSULF",
					description = "Clear Sky upward long wave flux",
					units = "W/m^2")
			163 -> Grib1Parameter(
					abbreviation = "CSDLF",
					description = "Clear Sky downward long wave flux",
					units = "W/m^2")
			164 -> Grib1Parameter(
					abbreviation = "CFNSF",
					description = "Cloud forcing net solar flux",
					units = "W/m^2")
			165 -> Grib1Parameter(
					abbreviation = "CFNLF",
					description = "Cloud forcing net long wave flux",
					units = "W/m^2")
			166 -> Grib1Parameter(
					abbreviation = "VBDSF",
					description = "Visible beam downward solar flux",
					units = "W/m^2")
			167 -> Grib1Parameter(
					abbreviation = "VDDSF",
					description = "Visible diffuse downward solar flux",
					units = "W/m^2")
			168 -> Grib1Parameter(
					abbreviation = "NBDSF",
					description = "Near IR beam downward solar flux",
					units = "W/m^2")
			169 -> Grib1Parameter(
					abbreviation = "NDDSF",
					description = "Near IR diffuse downward solar flux",
					units = "W/m^2")
			170 -> Grib1Parameter(
					abbreviation = "RWMR",
					description = "Rain water mixing ratio",
					units = "kg/kg")
			171 -> Grib1Parameter(
					abbreviation = "SNMR",
					description = "Snow mixing ratio",
					units = "kg/kg")
			172 -> Grib1Parameter(
					abbreviation = "MFLX",
					description = "Horizontal Momentum flux",
					units = "N/m^2")
			173 -> Grib1Parameter(
					abbreviation = "LMH",
					description = "Mass point model surface")
			174 -> Grib1Parameter(
					abbreviation = "LMV",
					description = "Velocity point model surface")
			175 -> Grib1Parameter(
					abbreviation = "MLYNO",
					description = "Model layer number (from bottom up)")
			176 -> Grib1Parameter(
					abbreviation = "NLAT",
					description = "latitude (-90 to +90)",
					units = "deg")
			177 -> Grib1Parameter(
					abbreviation = "ELON",
					description = "east longitude (0-360)",
					units = "deg")
			178 -> Grib1Parameter(
					abbreviation = "ICMR",
					description = "Ice mixing ratio",
					units = "kg/kg")
			179 -> Grib1Parameter(
					abbreviation = "GRMR",
					description = "Graupel mixing ratio",
					units = "kg/kg")
			180 -> Grib1Parameter(
					abbreviation = "GUST",
					description = "Wind speed (gust)",
					units = "m/s")
			181 -> Grib1Parameter(
					abbreviation = "LPSX",
					description = "x-gradient of log pressure",
					units = "1/m")
			182 -> Grib1Parameter(
					abbreviation = "LPSY",
					description = "y-gradient of log pressure",
					units = "1/m")
			183 -> Grib1Parameter(
					abbreviation = "HGTX",
					description = "x-gradient of height",
					units = "m/m")
			184 -> Grib1Parameter(
					abbreviation = "HGTY",
					description = "y-gradient of height",
					units = "m/m")
			185 -> Grib1Parameter(
					abbreviation = "TPFI",
					description = "Turbulence Potential Forecast Index")
			186 -> Grib1Parameter(
					abbreviation = "TIPD",
					description = "Total Icing Potential Diagnostic")
			187 -> Grib1Parameter(
					abbreviation = "LTNG",
					description = "Lightning")
			188 -> Grib1Parameter(
					abbreviation = "RDRIP",
					description = "Rate of water dropping from canopy to ground")
			189 -> Grib1Parameter(
					abbreviation = "VPTMP",
					description = "Virtual potential temperature",
					units = "K")
			190 -> Grib1Parameter(
					abbreviation = "HLCY",
					description = "Storm relative helicity",
					units = "m^2/s^2")
			191 -> Grib1Parameter(
					abbreviation = "PROB",
					description = "Probability from ensemble",
					units = "%")
			192 -> Grib1Parameter(
					abbreviation = "PROBN",
					description = "Probability from ensemble normalized with respect to climate expectancy",
					units = "%")
			193 -> Grib1Parameter(
					abbreviation = "POP",
					description = "Probability of precipitation",
					units = "%")
			194 -> Grib1Parameter(
					abbreviation = "CPOFP",
					description = "Percent of frozen precipitation",
					units = "%")
			195 -> Grib1Parameter(
					abbreviation = "CPOZP",
					description = "Probability of freezing precipitation",
					units = "%")
			196 -> Grib1Parameter(
					abbreviation = "USTM",
					description = "u-component of storm motion",
					units = "m/s")
			197 -> Grib1Parameter(
					abbreviation = "VSTM",
					description = "v-component of storm motion",
					units = "m/s")
			198 -> Grib1Parameter(
					abbreviation = "NCIP",
					description = "Number concentration for ice particles")
			199 -> Grib1Parameter(
					abbreviation = "EVBS",
					description = "Direct evaporation from bare soil",
					units = "W/m^2")
			200 -> Grib1Parameter(
					abbreviation = "EVCW",
					description = "Canopy water evaporation",
					units = "W/m2")
			201 -> Grib1Parameter(
					abbreviation = "ICWAT",
					description = "Ice-free water surface",
					units = "%")
			202 -> Grib1Parameter(
					abbreviation = "CWDI",
					description = "Convective weather detection index")
			203 -> Grib1Parameter(
					abbreviation = "VAFTD",
					description = "VAFTAD",
					units = "log10(kg/m^3)")
			204 -> Grib1Parameter(
					abbreviation = "DSWRF",
					description = "downward short wave rad. flux",
					units = "W/m^2")
			205 -> Grib1Parameter(
					abbreviation = "DLWRF",
					description = "downward long wave rad. flux",
					units = "W/m^2")
			206 -> Grib1Parameter(
					abbreviation = "UVI",
					description = "Ultra violet index (1 hour integration centered at solar noon)",
					units = "W/m^2")
			207 -> Grib1Parameter(
					abbreviation = "MSTAV",
					description = "Moisture availability",
					units = "%")
			208 -> Grib1Parameter(
					abbreviation = "SFEXC",
					description = "Exchange coefficient",
					units = "(kg/m^3)(m/s)")
			209 -> Grib1Parameter(
					abbreviation = "MIXLY",
					description = "No. of mixed layers next to surface",
					units = "")
			210 -> Grib1Parameter(
					abbreviation = "TRANS",
					description = "Transpiration",
					units = "W/m^2")
			211 -> Grib1Parameter(
					abbreviation = "USWRF",
					description = "upward short wave rad. flux",
					units = "W/m2")
			212 -> Grib1Parameter(
					abbreviation = "ULWRF",
					description = "upward long wave rad. flux",
					units = "W/m^2")
			213 -> Grib1Parameter(
					abbreviation = "CDLYR",
					description = "Amount of non-convective cloud",
					units = "%")
			214 -> Grib1Parameter(
					abbreviation = "CPRAT",
					description = "Convective Precipitation rate",
					units = "kg/m^2/s")
			215 -> Grib1Parameter(
					abbreviation = "TTDIA",
					description = "Temperature tendency by all physics",
					units = "K/s")
			216 -> Grib1Parameter(
					abbreviation = "TTRAD",
					description = "Temperature tendency by all radiation",
					units = "K/s")
			217 -> Grib1Parameter(
					abbreviation = "TTPHY",
					description = "Temperature tendency by non-radiation physics",
					units = "K/s")
			218 -> Grib1Parameter(
					abbreviation = "PREIX",
					description = "Precipitation index(0.0-1.00)",
					units = "fraction")
			219 -> Grib1Parameter(
					abbreviation = "TSD1D",
					description = "Std. dev. of IR T over 1x1 deg area",
					units = "K")
			220 -> Grib1Parameter(
					abbreviation = "NLGSP",
					description = "Natural log of surface pressure",
					units = "ln(kPa)")
			221 -> Grib1Parameter(
					abbreviation = "HPBL",
					description = "Planetary boundary layer height",
					units = "m")
			222 -> Grib1Parameter(
					abbreviation = "5WAVH",
					description = "5-wave geopotential height",
					units = "gpm")
			223 -> Grib1Parameter(
					abbreviation = "CNWAT",
					description = "Plant canopy surface water",
					units = "kg/m^2")
			224 -> Grib1Parameter(
					abbreviation = "SOTYP",
					description = "Soil type (as in Zobler)",
					units = "")
			225 -> Grib1Parameter(
					abbreviation = "VGTYP",
					description = "Vegitation type (as in SiB)",
					units = "")
			226 -> Grib1Parameter(
					abbreviation = "BMIXL",
					description = "Blackadar's mixing length scale",
					units = "m")
			227 -> Grib1Parameter(
					abbreviation = "AMIXL",
					description = "Asymptotic mixing length scale",
					units = "m")
			228 -> Grib1Parameter(
					abbreviation = "PEVAP",
					description = "Potential evaporation",
					units = "kg/m^2")
			229 -> Grib1Parameter(
					abbreviation = "SNOHF",
					description = "Snow phase-change heat flux",
					units = "W/m^2")
			230 -> Grib1Parameter(
					abbreviation = "5WAVA",
					description = "5-wave geopotential height anomaly",
					units = "gpm")
			231 -> Grib1Parameter(
					abbreviation = "MFLUX",
					description = "Convective cloud mass flux",
					units = "Pa/s")
			232 -> Grib1Parameter(
					abbreviation = "DTRF",
					description = "Downward total radiation flux",
					units = "W/m^2")
			233 -> Grib1Parameter(
					abbreviation = "UTRF",
					description = "Upward total radiation flux",
					units = "W/m^2")
			234 -> Grib1Parameter(
					abbreviation = "BGRUN",
					description = "Baseflow-groundwater runoff",
					units = "kg/m^2")
			235 -> Grib1Parameter(
					abbreviation = "SSRUN",
					description = "Storm surface runoff",
					units = "kg/m^2")
			236 -> Grib1Parameter(
					abbreviation = "SIPD",
					description = "Supercooled Large Droplet (SLD) Icing Potential Diagnostic")
			237 -> Grib1Parameter(
					abbreviation = "03TOT",
					description = "Total ozone",
					units = "kg/m^2")
			238 -> Grib1Parameter(
					abbreviation = "SNOWC",
					description = "Snow cover",
					units = "%")
			239 -> Grib1Parameter(
					abbreviation = "SNOT",
					description = "Snow temperature",
					units = "K")
			240 -> Grib1Parameter(
					abbreviation = "COVTW",
					description = "Covariance between temperature and vertical component of the wind",
					units = "K*m/s")
			241 -> Grib1Parameter(
					abbreviation = "LRGHR",
					description = "Large scale condensate heat rate",
					units = "K/s")
			242 -> Grib1Parameter(
					abbreviation = "CNVHR",
					description = "Deep convective heating rate",
					units = "K/s")
			243 -> Grib1Parameter(
					abbreviation = "CNVMR",
					description = "Deep convective moistening rate",
					units = "kg/kg/s")
			244 -> Grib1Parameter(
					abbreviation = "SHAHR",
					description = "Shallow convective heating rate",
					units = "K/s")
			245 -> Grib1Parameter(
					abbreviation = "SHAMR",
					description = "Shallow convective moistening rate",
					units = "kg/kg/s")
			246 -> Grib1Parameter(
					abbreviation = "VDFHR",
					description = "Vertical diffusion heating rate",
					units = "K/s")
			247 -> Grib1Parameter(
					abbreviation = "VDFUA",
					description = "Vertical diffusion zonal acceleration",
					units = "m/s^2")
			248 -> Grib1Parameter(
					abbreviation = "VDFVA",
					description = "Vertical diffusion meridional acceleration",
					units = "m/s^2")
			249 -> Grib1Parameter(
					abbreviation = "VDFMR",
					description = "Vertical diffusion moistening rate",
					units = "kg/kg/s")
			250 -> Grib1Parameter(
					abbreviation = "SWHR",
					description = "Solar radiative heating rate",
					units = "K/s")
			251 -> Grib1Parameter(
					abbreviation = "LWHR",
					description = "Long wave radiative heating rate",
					units = "K/s")
			252 -> Grib1Parameter(
					abbreviation = "CD",
					description = "Drag coefficient")
			253 -> Grib1Parameter(
					abbreviation = "FRICV",
					description = "Friction velocity",
					units = "m/s")
			254 -> Grib1Parameter(
					abbreviation = "RI",
					description = "Richardson number")
			else -> null
		}

		/**
		 * Returns the parameter in Parameter Table 128 corresponding to the specified ID.
		 * <br></br><br></br>
		 * Reference: [
 * http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html](http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html#TABLE128)
		 * @param id
		 * @return the parameter in Parameter Table 128 corresponding to the specified ID
		 */
		private fun getParameterFromTable128(id: Int): Grib1Parameter? = when (id) {
			128 -> Grib1Parameter(
					abbreviation = "AVDEPTH",
					description = "Ocean depth - mean",
					units = "m")
			129 -> Grib1Parameter(
					abbreviation = "DEPTH",
					description = "Ocean depth - instantaneous",
					units = "m")
			130 -> Grib1Parameter(
					abbreviation = "ELEV",
					description = "Ocean surface elevation relative to geoid",
					units = "m")
			131 -> Grib1Parameter(
					abbreviation = "MXEL24",
					description = "Max ocean surface elevation in last 24 hours",
					units = "m")
			132 -> Grib1Parameter(
					abbreviation = "MNEL24",
					description = "Min ocean surface elevation in last 24 hours",
					units = "m")
			135 -> Grib1Parameter(
					abbreviation = "O2",
					description = "Oxygen (O2 (aq))",
					units = "Mol/kg")
			136 -> Grib1Parameter(
					abbreviation = "PO4",
					description = "PO4",
					units = "Mol/kg")
			137 -> Grib1Parameter(
					abbreviation = "NO3",
					description = "NO3",
					units = "Mol/kg")
			138 -> Grib1Parameter(
					abbreviation = "SIO4",
					description = "SiO4",
					units = "Mol/kg")
			139 -> Grib1Parameter(
					abbreviation = "CO2AQ",
					description = "CO2 (aq)",
					units = "Mol/kg")
			140 -> Grib1Parameter(
					abbreviation = "HCO3",
					description = "HCO3",
					units = "Mol/kg")
			141 -> Grib1Parameter(
					abbreviation = "CO3",
					description = "CO3",
					units = "Mol/kg")
			142 -> Grib1Parameter(
					abbreviation = "TCO2",
					description = "TCO2",
					units = "Mol/kg")
			143 -> Grib1Parameter(
					abbreviation = "TALK",
					description = "TALK",
					units = "Mol/kg")
			144 -> Grib1Parameter(
					abbreviation = "CH",
					description = "Heat Exchange Coefficient",
					units = "")
			146 -> Grib1Parameter(
					abbreviation = "S11",
					description = "S11 - 1,1 component of ice stress tensor",
					units = "")
			147 -> Grib1Parameter(
					abbreviation = "S12",
					description = "S12 - 1,2 component of ice stress tensor",
					units = "")
			148 -> Grib1Parameter(
					abbreviation = "S22",
					description = "S22 - 2,2 component of ice stress tensor",
					units = "")
			149 -> Grib1Parameter(
					abbreviation = "INV1",
					description = "T1 - First invariant of stress tensor",
					units = "")
			150 -> Grib1Parameter(
					abbreviation = "INV2",
					description = "T2 - Second invariant of stress tensor",
					units = "")
			155 -> Grib1Parameter(
					abbreviation = "WVRGH",
					description = "Wave roughness",
					units = "")
			156 -> Grib1Parameter(
					abbreviation = "WVSTRS",
					description = "Wave stresses",
					units = "")
			157 -> Grib1Parameter(
					abbreviation = "WHITE",
					description = "Whitecap coverage",
					units = "WHITE")
			158 -> Grib1Parameter(
					abbreviation = "SWDIRWID",
					description = "Swell direction width",
					units = "")
			159 -> Grib1Parameter(
					abbreviation = "SWFREWID",
					description = "Swell frequency width",
					units = "")
			160 -> Grib1Parameter(
					abbreviation = "WVAGE",
					description = "Wave age",
					units = "")
			161 -> Grib1Parameter(
					abbreviation = "PWVAGE",
					description = "Physical Wave",
					units = "age")
			165 -> Grib1Parameter(
					abbreviation = "LTURB",
					description = "Master length scale (turbulence)",
					units = "m")
			170 -> Grib1Parameter(
					abbreviation = "AIHFLX",
					description = "Net air-ice heat flux",
					units = "W/m2")
			171 -> Grib1Parameter(
					abbreviation = "AOHFLX",
					description = "Net air-ocean heat flux",
					units = "W/m2")
			172 -> Grib1Parameter(
					abbreviation = "IOHFLX",
					description = "Net ice-ocean heat flux",
					units = "W/m2")
			173 -> Grib1Parameter(
					abbreviation = "IOSFLX",
					description = "Net ice-ocean salt flux",
					units = "kg/s")
			175 -> Grib1Parameter(
					abbreviation = "OMLT",
					description = "Ocean mixed layer temperature",
					units = "K")
			176 -> Grib1Parameter(
					abbreviation = "OMLS",
					description = "Ocean mixed layer salinity",
					units = "kg/kg")
			177 -> Grib1Parameter(
					abbreviation = "P2OMLT",
					description = "Ocean mixed layer potential density (Referenced to 2000m)",
					units = "kg/m3")
			178 -> Grib1Parameter(
					abbreviation = "OMLU",
					description = "Ocean mixed layer u velocity",
					units = "m/s")
			179 -> Grib1Parameter(
					abbreviation = "OMLV",
					description = "Ocean mixed layer v velocity",
					units = "m/s")
			180 -> Grib1Parameter(
					abbreviation = "ASHFL",
					description = "Assimilative heat flux",
					units = "W/m2")
			181 -> Grib1Parameter(
					abbreviation = "ASSFL",
					description = "Assimilative salt flux",
					units = "mm/day")
			182 -> Grib1Parameter(
					abbreviation = "BOTLD",
					description = "Bottom layer depth",
					units = "m")
			183 -> Grib1Parameter(
					abbreviation = "UBARO",
					description = "Barotropic U velocity",
					units = "m/s")
			184 -> Grib1Parameter(
					abbreviation = "VBARO",
					description = "Barotropic V velocity",
					units = "m/s")
			185 -> Grib1Parameter(
					abbreviation = "INTFD",
					description = "Interface depths",
					units = "m")
			186 -> Grib1Parameter(
					abbreviation = "WTMPC",
					description = "3-D temperature",
					units = "deg c")
			187 -> Grib1Parameter(
					abbreviation = "SALIN",
					description = "3-D Salinity",
					units = "psu")
			188 -> Grib1Parameter(
					abbreviation = "EMNP",
					description = "Evaporation - precipitation",
					units = "cm/day")
			190 -> Grib1Parameter(
					abbreviation = "KENG",
					description = "Kinetic energy",
					units = "J/kg")
			191 -> Grib1Parameter(
					abbreviation = "BKENG",
					description = "Barotropic Kinetic energy",
					units = "J/kg")
			192 -> Grib1Parameter(
					abbreviation = "LAYTH",
					description = "Layer Thickness",
					units = "m")
			193 -> Grib1Parameter(
					abbreviation = "SSTT",
					description = "Surface temperature trend",
					units = "deg/day")
			194 -> Grib1Parameter(
					abbreviation = "SSST",
					description = "Surface salinity trend",
					units = "psu/day")
			195 -> Grib1Parameter(
					abbreviation = "OVHD",
					description = "Ocean Vertical Heat Diffusivity",
					units = "m2s-1")
			196 -> Grib1Parameter(
					abbreviation = "OVSD",
					description = "Ocean Vertical Salt Diffusivity",
					units = "m2s-1")
			197 -> Grib1Parameter(
					abbreviation = "OVMD",
					description = "Ocean Vertical Momentum Diffusivity",
					units = "m2s-1")
			254 -> Grib1Parameter(
					abbreviation = "REV",
					description = "Relative error variance",
					units = "")
			else -> null
		}

		/**
		 * Returns the parameter in Parameter Table 129 corresponding to the specified ID.
		 * <br></br><br></br>
		 * Reference: [
 * http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html](http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html#TABLE129)
		 * @param id
		 * @return the parameter in Parameter Table 129 corresponding to the specified ID
		 */
		private fun getParameterFromTable129(id: Int): Grib1Parameter? = when (id) {
			128 -> Grib1Parameter(
					abbreviation = "PAOT",
					description = "Probability anomaly of temperature",
					units = "%")
			129 -> Grib1Parameter(
					abbreviation = "PAOP",
					description = "Probability anomaly of precipitation",
					units = "%")
			130 -> Grib1Parameter(
					abbreviation = "CWR",
					description = "Probability of Wetting Rain, exceeding 0.10 in a given time period",
					units = "%")
			131 -> Grib1Parameter(
					abbreviation = "FRAIN",
					description = "Rain fraction of total liquid water",
					units = "")
			132 -> Grib1Parameter(
					abbreviation = "FICE",
					description = "Ice fraction of total condensate",
					units = "")
			133 -> Grib1Parameter(
					abbreviation = "RIME",
					description = "Rime Factor",
					units = "")
			134 -> Grib1Parameter(
					abbreviation = "CUEFI",
					description = "Convective cloud efficiency",
					units = "")
			135 -> Grib1Parameter(
					abbreviation = "TCOND",
					description = "Total condensate",
					units = "kg/kg")
			136 -> Grib1Parameter(
					abbreviation = "TCOLW",
					description = "Total column-integrated cloud water",
					units = "kg/m2")
			137 -> Grib1Parameter(
					abbreviation = "TCOLI",
					description = "Total column-integrated cloud ice",
					units = "kg/m2")
			138 -> Grib1Parameter(
					abbreviation = "TCOLR",
					description = "Total column-integrated rain",
					units = "kg/m2")
			139 -> Grib1Parameter(
					abbreviation = "TCOLS",
					description = "Total column-integrated snow",
					units = "kg/m2")
			140 -> Grib1Parameter(
					abbreviation = "TCOLC",
					description = "Total column-integrated condensate",
					units = "kg/m2")
			141 -> Grib1Parameter(
					abbreviation = "PLPL",
					description = "Pressure of level from which parcel was lifted",
					units = "Pa")
			142 -> Grib1Parameter(
					abbreviation = "HLPL",
					description = "Height of level from which parcel was lifted",
					units = "m")
			143 -> Grib1Parameter(
					abbreviation = "CEMS",
					description = "Cloud Emissivity",
					units = "Fraction 0-1")
			144 -> Grib1Parameter(
					abbreviation = "COPD",
					description = "Cloud Optical Depth",
					units = "")
			145 -> Grib1Parameter(
					abbreviation = "PSIZ",
					description = "Effective Particle size",
					units = "Microns")
			146 -> Grib1Parameter(
					abbreviation = "TCWAT",
					description = "Total Water Cloud",
					units = "%")
			147 -> Grib1Parameter(
					abbreviation = "TCICE",
					description = "Total Ice Cloud",
					units = "%")
			148 -> Grib1Parameter(
					abbreviation = "WDIF",
					description = "Wind difference",
					units = "m/s")
			149 -> Grib1Parameter(
					abbreviation = "WSTP",
					description = "Wave Steepness",
					units = "")
			150 -> Grib1Parameter(
					abbreviation = "PTAN",
					description = "Probability of Temperature being above normal",
					units = "%")
			151 -> Grib1Parameter(
					abbreviation = "PTNN",
					description = "Probability of Temperature being near normal",
					units = "%")
			152 -> Grib1Parameter(
					abbreviation = "PTBN",
					description = "Probability of Temperature being below normal",
					units = "%")
			153 -> Grib1Parameter(
					abbreviation = "PPAN",
					description = "Probability of Precipitation being above normal",
					units = "%")
			154 -> Grib1Parameter(
					abbreviation = "PPNN",
					description = "Probability of Precipitation being near normal",
					units = "%")
			155 -> Grib1Parameter(
					abbreviation = "PPBN",
					description = "Probability of Precipitation being below normal",
					units = "%")
			156 -> Grib1Parameter(
					abbreviation = "PMTC",
					description = "Particulate matter (coarse)",
					units = "µg/m3")
			157 -> Grib1Parameter(
					abbreviation = "PMTF",
					description = "Particulate matter (fine)",
					units = "µg/m3")
			158 -> Grib1Parameter(
					abbreviation = "AETMP",
					description = "Analysis error of temperature",
					units = "K")
			159 -> Grib1Parameter(
					abbreviation = "AEDPT",
					description = "Analysis error of dew point",
					units = "%")
			160 -> Grib1Parameter(
					abbreviation = "AESPH",
					description = "Analysis error of specific humidity",
					units = "kg/kg")
			161 -> Grib1Parameter(
					abbreviation = "AEUWD",
					description = "Analysis error of u-wind",
					units = "m/s")
			162 -> Grib1Parameter(
					abbreviation = "AEVWD",
					description = "Analysis error of v-wind",
					units = "m/s")
			163 -> Grib1Parameter(
					abbreviation = "LPMTF",
					description = "Particulate matter (fine)",
					units = "log10(µg/m3)")
			164 -> Grib1Parameter(
					abbreviation = "LIPMF",
					description = "Integrated column particulate matter (fine)",
					units = "log10(µg/m3)")
			165 -> Grib1Parameter(
					abbreviation = "REFZR",
					description = "Derived radar reflectivity backscatter from rain",
					units = "mm6/m3")
			166 -> Grib1Parameter(
					abbreviation = "REFZI",
					description = "Derived radar reflectivity backscatter from ice",
					units = "mm6/m3")
			167 -> Grib1Parameter(
					abbreviation = "REFZC",
					description = "Derived radar reflectivity backscatter from parameterized convection",
					units = "mm6/m3")
			168 -> Grib1Parameter(
					abbreviation = "TCLSW",
					description = "Total column-integrated supercooled liquid water",
					units = "kg/m2")
			169 -> Grib1Parameter(
					abbreviation = "TCOLM",
					description = "Total column-integrated melting ice",
					units = "kg/m2")
			170 -> Grib1Parameter(
					abbreviation = "ELRDI",
					description = "Ellrod Index",
					units = "")
			171 -> Grib1Parameter(
					abbreviation = "TSEC",
					description = "Seconds prior to initial reference time",
					units = "sec")
			172 -> Grib1Parameter(
					abbreviation = "TSECA",
					description = "Seconds after initial reference time",
					units = "sec")
			173 -> Grib1Parameter(
					abbreviation = "NUM",
					description = "Number of samples/observations",
					units = "")
			174 -> Grib1Parameter(
					abbreviation = "AEPRS",
					description = "Analysis error of pressure",
					units = "Pa")
			175 -> Grib1Parameter(
					abbreviation = "ICSEV",
					description = "Icing severity",
					units = "")
			176 -> Grib1Parameter(
					abbreviation = "ICPRB",
					description = "Icing probability",
					units = "")
			177 -> Grib1Parameter(
					abbreviation = "LAVNI",
					description = "Low-level aviation interest",
					units = "")
			178 -> Grib1Parameter(
					abbreviation = "HAVNI",
					description = "High-level aviation interest",
					units = "")
			179 -> Grib1Parameter(
					abbreviation = "FLGHT",
					description = "Flight Category",
					units = "")
			180 -> Grib1Parameter(
					abbreviation = "OZCON",
					description = "Ozone concentration",
					units = "PPB")
			181 -> Grib1Parameter(
					abbreviation = "OZCAT",
					description = "Categorical ozone concentration",
					units = "")
			182 -> Grib1Parameter(
					abbreviation = "VEDH",
					description = "Vertical eddy diffusivity heat exchange (Kh)",
					units = "m2/s")
			183 -> Grib1Parameter(
					abbreviation = "SIGV",
					description = "Sigma level value",
					units = "")
			184 -> Grib1Parameter(
					abbreviation = "EWGT",
					description = "Ensemble Weight",
					units = "")
			185 -> Grib1Parameter(
					abbreviation = "CICEL",
					description = "Confidence indicator - Ceiling",
					units = "")
			186 -> Grib1Parameter(
					abbreviation = "CIVIS",
					description = "Confidence indicator - Visibility",
					units = "")
			187 -> Grib1Parameter(
					abbreviation = "CIFLT",
					description = "Confidence indicator - Flight Category",
					units = "")
			188 -> Grib1Parameter(
					abbreviation = "LAVV",
					description = "Latitude of V wind component of velocity",
					units = "deg")
			189 -> Grib1Parameter(
					abbreviation = "LOVV",
					description = "Longitude of V wind component of velocity",
					units = "deg")
			190 -> Grib1Parameter(
					abbreviation = "USCT",
					description = "Scatterometer estimated U wind component",
					units = "m/s")
			191 -> Grib1Parameter(
					abbreviation = "VSCT",
					description = "Scatterometer estimated V wind component",
					units = "m/s")
			192 -> Grib1Parameter(
					abbreviation = "LAUV",
					description = "Latitude of U wind component of velocity",
					units = "deg")
			193 -> Grib1Parameter(
					abbreviation = "LOUV",
					description = "Longitude of U wind component of velocity",
					units = "deg")
			194 -> Grib1Parameter(
					abbreviation = "TCHP",
					description = "Tropical Cyclone Heat Potential",
					units = "J/m2K")
			195 -> Grib1Parameter(
					abbreviation = "DBSS",
					description = "Geometric Depth Below Sea Surface",
					units = "m")
			196 -> Grib1Parameter(
					abbreviation = "ODHA",
					description = "Ocean Dynamic Height Anomaly",
					units = "dynamic m")
			197 -> Grib1Parameter(
					abbreviation = "OHC",
					description = "Ocean Heat Content",
					units = "J/m2")
			198 -> Grib1Parameter(
					abbreviation = "SSHG",
					description = "Sea Surface Height Relative to Geoid",
					units = "m")
			199 -> Grib1Parameter(
					abbreviation = "SLTFL",
					description = "Salt Flux",
					units = "kg/m2s")
			200 -> Grib1Parameter(
					abbreviation = "DUVB",
					description = "UV-B downward solar flux",
					units = "W/m2")
			201 -> Grib1Parameter(
					abbreviation = "CDUVB",
					description = "Clear sky UV-B downward solar flux",
					units = "W/m2")
			202 -> Grib1Parameter(
					abbreviation = "THFLX",
					description = "Total downward heat flux at surface (downward is positive)",
					units = "W/m2")
			203 -> Grib1Parameter(
					abbreviation = "UVAR",
					description = "U velocity variance",
					units = "m2/s2")
			204 -> Grib1Parameter(
					abbreviation = "VVAR",
					description = "V velocity variance",
					units = "m2/s2")
			205 -> Grib1Parameter(
					abbreviation = "UVVCC",
					description = "UV Velocity Cross Correlation",
					units = "m2/s2")
			206 -> Grib1Parameter(
					abbreviation = "MCLS",
					description = "Meteorological Correlation Length Scale",
					units = "m")
			207 -> Grib1Parameter(
					abbreviation = "LAPP",
					description = "Latitude of pressure point",
					units = "deg")
			208 -> Grib1Parameter(
					abbreviation = "LOPP",
					description = "Longitude of pressure point",
					units = "deg")
			210 -> Grib1Parameter(
					abbreviation = "REFO",
					description = "Observed radar reflectivity",
					units = "dbZ")
			211 -> Grib1Parameter(
					abbreviation = "REFD",
					description = "Derived radar reflectivity",
					units = "dbZ")
			212 -> Grib1Parameter(
					abbreviation = "REFC",
					description = "Maximum/Composite radar reflectivity",
					units = "dbZ")
			213 -> Grib1Parameter(
					abbreviation = "SBT122",
					description = "Simulated Brightness Temperature for GOES12, Channel 2",
					units = "K")
			214 -> Grib1Parameter(
					abbreviation = "SBT123",
					description = "Simulated Brightness Temperature for GOES12, Channel 3",
					units = "K")
			215 -> Grib1Parameter(
					abbreviation = "SBT124",
					description = "Simulated Brightness Temperature for GOES12, Channel 4",
					units = "K")
			216 -> Grib1Parameter(
					abbreviation = "SBT126",
					description = "Simulated Brightness Temperature for GOES12, Channel 6",
					units = "K")
			217 -> Grib1Parameter(
					abbreviation = "MINRH",
					description = "Minimum Relative Humidity",
					units = "%")
			218 -> Grib1Parameter(
					abbreviation = "MAXRH",
					description = "Maximum Relative Humidity",
					units = "%")
			219 -> Grib1Parameter(
					abbreviation = "CEIL",
					description = "Ceiling",
					units = "m")
			220 -> Grib1Parameter(
					abbreviation = "PBLREG",
					description = "Planetary boundary layer Regime",
					units = "")
			221 -> Grib1Parameter(
					abbreviation = "SBC123",
					description = "Simulated Brightness Counts for GOES12, Channel 3",
					units = "Byte")
			222 -> Grib1Parameter(
					abbreviation = "SBC124",
					description = "Simulated Brightness Counts for GOES12, Channel 4",
					units = "Byte")
			223 -> Grib1Parameter(
					abbreviation = "RPRATE",
					description = "Rain Precipitation Rate",
					units = "kg/m2/s")
			224 -> Grib1Parameter(
					abbreviation = "SPRATE",
					description = "Snow Precipitation Rate",
					units = "kg/m2/s")
			225 -> Grib1Parameter(
					abbreviation = "FPRATE",
					description = "Freezing Rain Precipitation Rate",
					units = "kg/m2/s")
			226 -> Grib1Parameter(
					abbreviation = "IPRATE",
					description = "Ice Pellets Precipitation Rate",
					units = "kg/m2/s")
			227 -> Grib1Parameter(
					abbreviation = "UPHL",
					description = "Updraft Helicity",
					units = "m2/s2")
			228 -> Grib1Parameter(
					abbreviation = "SURGE",
					description = "Storm Surge",
					units = "m")
			229 -> Grib1Parameter(
					abbreviation = "ETSRG",
					description = "Extra Tropical Storm Surge",
					units = "m")
			230 -> Grib1Parameter(
					abbreviation = "RHPW",
					description = "Relative Humidity with Respect to Precipitable Water",
					units = "%")
			231 -> Grib1Parameter(
					abbreviation = "OZMAX1",
					description = "Ozone Daily Max from 1-hour Average",
					units = "ppbV")
			232 -> Grib1Parameter(
					abbreviation = "OZMAX8",
					description = "Ozone Daily Max from 8-hour Average",
					units = "ppbV")
			233 -> Grib1Parameter(
					abbreviation = "PDMAX1",
					description = "PM 2.5 Daily Max from 1-hour Average",
					units = "μg/m3")
			234 -> Grib1Parameter(
					abbreviation = "PDMX24",
					description = "PM 2.5 Daily Max from 24-hour Average",
					units = "μg/m3")
			235 -> Grib1Parameter(
					abbreviation = "MAXREF",
					description = "Hourly Maximum of Simulated Reflectivity at 1 km AGL",
					units = "dbZ")
			236 -> Grib1Parameter(
					abbreviation = "MXUPHL",
					description = "Hourly Maximum of Updraft Helicity over layer 2km to 5 km AGL",
					units = "m2/s2")
			237 -> Grib1Parameter(
					abbreviation = "MAXUVV",
					description = "Hourly Maximum of Upward Vertical Velocity in the lowest 400hPa",
					units = "m/s")
			238 -> Grib1Parameter(
					abbreviation = "MAXDVV",
					description = "Hourly Maximum of Downward Vertical Velocity in the lowest 400hPa",
					units = "m/s")
			239 -> Grib1Parameter(
					abbreviation = "MAXVIG",
					description = "Hourly Maximum of Column Vertical Integrated Graupel",
					units = "kg/m2")
			240 -> Grib1Parameter(
					abbreviation = "RETOP",
					description = "Radar Echo Top (18.3 DBZ)",
					units = "m")
			241 -> Grib1Parameter(
					abbreviation = "VRATE",
					description = "Ventilation Rate",
					units = "m2/s")
			242 -> Grib1Parameter(
					abbreviation = "TCSRG20",
					description = "20% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			243 -> Grib1Parameter(
					abbreviation = "TCSRG30",
					description = "30% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			244 -> Grib1Parameter(
					abbreviation = "TCSRG40",
					description = "40% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			245 -> Grib1Parameter(
					abbreviation = "TCSRG50",
					description = "50% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			246 -> Grib1Parameter(
					abbreviation = "TCSRG60",
					description = "60% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			247 -> Grib1Parameter(
					abbreviation = "TCSRG70",
					description = "70% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			248 -> Grib1Parameter(
					abbreviation = "TCSRG80",
					description = "80% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			249 -> Grib1Parameter(
					abbreviation = "TCSRG90",
					description = "90% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			250 -> Grib1Parameter(
					abbreviation = "HINDEX",
					description = "Haines Index",
					units = "")
			251 -> Grib1Parameter(
					abbreviation = "DIFTEN",
					description = "Difference Between 2 States In Total Energy Norm",
					units = "J/kg")
			252 -> Grib1Parameter(
					abbreviation = "PSPCP",
					description = "Pseudo-Precipitation",
					units = "kg/m2")
			253 -> Grib1Parameter(
					abbreviation = "MAXUW",
					description = "U Component of Hourly Maximum 10m Wind Speed",
					units = "m/s")
			254 -> Grib1Parameter(
					abbreviation = "MAXVW",
					description = "V Component of Hourly Maximum 10m Wind Speed",
					units = "m/s")
			else -> null
		}
	}

	/**
	 * rdg - added this method to be used in a comparator for sorting while
	 * extracting records.
	 * @param param to compare
	 * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
	 */
	fun compare(param: Grib1Parameter): Int {
		if (this == param) return 0

		// check if param is less than this
		// really only one thing to compare because parameter table sets info
		// compare tables in GribRecordPDS
		return if (number > number) -1 else 1
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Grib1Parameter) return false
		if (this === other) return true
		if (abbreviation !== other.abbreviation) return false
		if (number != other.number) return false
		if (description !== other.description) return false
		return units === other.units
	}

	override fun hashCode(): Int {
		var result = number
		result = 31 * result + abbreviation.hashCode()
		result = 31 * result + description.hashCode()
		result = 31 * result + units.hashCode()
		return result
	}

	override fun toString(): String = "${number}:${abbreviation}:${description} [${units}]"
}