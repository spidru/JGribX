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

import mt.edu.um.cf2.jgribx.api.GribParameter

/**
 * Title:        JGrib
 * Description:  Class which represents a parameter from a PDS parameter table
 * Copyright:    Copyright (c) 2002
 * Company:      U.S. Air Force
 *
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 *
 * @property id - Parameter number [0 - 255]
 * @property code - Parameter abbreviation
 * @property description - Parameter description
 * @property units - Parameter unit
 */
class Grib1Parameter(override val id: Int,
					 override val code: String = "",
					 override val description: String = "",
					 internal val units: String = "") : GribParameter {

	companion object {
		// As of Nov 2017, all Table Versions make use of Table 2 for Parameter IDs between 0 and 128
		fun getParameter(version: Int, id: Int, centreId: Int): Grib1Parameter? = when (version) {
			in 0..127 -> getParameterFromTable2(id, centreId)
			128 -> getParameterFromTable128(id)
			129 -> getParameterFromTable129(id)
			else -> null
		}

		/**
		 * Returns the parameter in Parameter Table 2 corresponding to the specified ID.
		 * <br></br><br></br>
		 * Reference: [http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html](http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html)
		 * @param id
		 * @return
		 */
		private fun getParameterFromTable2(id: Int, centreId: Int): Grib1Parameter? = when (id) {
			1 -> Grib1Parameter(id,
					code = "PRES",
					description = "Pressure",
					units = "Pa")
			2 -> Grib1Parameter(id,
					code = "PRMSL",
					description = "Pressure reduced to MSL",
					units = "Pa")
			3 -> Grib1Parameter(id,
					code = "PTEND",
					description = "Pressure tendency",
					units = "Pa/s")
			4 -> Grib1Parameter(id,
					code = "PVORT",
					description = "Potential vorticity",
					units = "K m^2 kg^-1 s^-1")
			5 -> Grib1Parameter(id,
					code = "ICAHT",
					description = "ICAO Standard Atmosphere Reference Height",
					units = "m")
			6 -> Grib1Parameter(id,
					code = "GP",
					description = "Geopotential",
					units = "m^2/s^2")
			7 -> Grib1Parameter(id,
					code = "HGT",
					description = "Geopotential height",
					units = "gpm")
			8 -> Grib1Parameter(id,
					code = "DIST",
					description = "Geometric height",
					units = "m")
			9 -> Grib1Parameter(id,
					code = "HSTDV",
					description = "Standard deviation of height",
					units = "m")
			10 -> Grib1Parameter(id,
					code = "TOZNE",
					description = "Total ozone",
					units = "Dobson")
			11 -> Grib1Parameter(id,
					code = "TMP",
					description = "Temperature",
					units = "K")
			12 -> Grib1Parameter(id,
					code = "VTMP",
					description = "Virtual temperature",
					units = "K")
			13 -> Grib1Parameter(id,
					code = "POT",
					description = "Potential temperature",
					units = "K")
			14 -> Grib1Parameter(id,
					code = "EPOT",
					description = "Pseudo-adiabatic potential temperature (or equivalent potential temperature)",
					units = "K")
			15 -> Grib1Parameter(id,
					code = "TMAX",
					description = "Maximum temperature",
					units = "K")
			16 -> Grib1Parameter(id,
					code = "TMIN",
					description = "Minimum temperature",
					units = "K")
			17 -> Grib1Parameter(id,
					code = "DPT",
					description = "Dew point temperature",
					units = "K")
			18 -> Grib1Parameter(id,
					code = "DEPR",
					description = "Dew point depression (or deficit)",
					units = "K")
			19 -> Grib1Parameter(id,
					code = "LAPR",
					description = "Lapse rate",
					units = "K/m")
			20 -> Grib1Parameter(id,
					code = "VIS",
					description = "Visibility",
					units = "m")
			21 -> Grib1Parameter(id,
					code = "RDSP1",
					description = "Radar Spectra (1)",
					units = "-")
			22 -> Grib1Parameter(id,
					code = "RDSP2",
					description = "Radar Spectra (2)",
					units = "-")
			23 -> Grib1Parameter(id,
					code = "RDSP3",
					description = "Radar Spectra (3)",
					units = "-")
			24 -> Grib1Parameter(id,
					code = "PLI",
					description = "Parcel lifted index (to 500 hPa)",
					units = "K")
			25 -> Grib1Parameter(id,
					code = "TMPA",
					description = "Temperature anomaly",
					units = "K")
			26 -> Grib1Parameter(id,
					code = "PRESA",
					description = "Pressure anomaly",
					units = "Pa")
			27 -> Grib1Parameter(id,
					code = "GPA",
					description = "Geopotential height anomaly",
					units = "gpm")
			28 -> Grib1Parameter(id,
					code = "WVSP1",
					description = "Wave Spectra (1)",
					units = "-")
			29 -> Grib1Parameter(id,
					code = "WVSP2",
					description = "Wave Spectra (2)",
					units = "-")
			30 -> Grib1Parameter(id,
					code = "WVSP3",
					description = "Wave Spectra (3)",
					units = "-")
			31 -> Grib1Parameter(id,
					code = "WDIR",
					description = "Wind direction (from which blowing)",
					units = "deg true")
			32 -> Grib1Parameter(id,
					code = "WIND",
					description = "Wind speed",
					units = "m/s")
			33 -> Grib1Parameter(id,
					code = "UGRD",
					description = "u-component of wind",
					units = "m/s")
			34 -> Grib1Parameter(id,
					code = "VGRD",
					description = "v-component of wind",
					units = "m/s")
			35 -> Grib1Parameter(id,
					code = "STRM",
					description = "Stream function",
					units = "m^2/s")
			36 -> Grib1Parameter(id,
					code = "VPOT",
					description = "Velocity potential",
					units = "m^2/s")
			37 -> Grib1Parameter(id,
					code = "MNTSF",
					description = "Montgomery stream function",
					units = "m2/s2")
			38 -> Grib1Parameter(id,
					code = "SGCVV",
					description = "Sigma coordinate vertical velocity",
					units = "/s")
			39 -> Grib1Parameter(id,
					code = "VVEL",
					description = "Vertical velocity (pressure)",
					units = "Pa/s")
			40 -> Grib1Parameter(id,
					code = "DZDT",
					description = "Vertical velocity (geometric)",
					units = "m/s")
			41 -> Grib1Parameter(id,
					code = "ABSV",
					description = "Absolute vorticity",
					units = "/s")
			42 -> Grib1Parameter(id,
					code = "ABSD",
					description = "Absolute divergence",
					units = "/s")
			43 -> Grib1Parameter(id,
					code = "RELV",
					description = "Relative vorticity",
					units = "/s")
			44 -> Grib1Parameter(id,
					code = "RELD",
					description = "Relative divergence",
					units = "/s")
			45 -> Grib1Parameter(id,
					code = "VUCSH",
					description = "Vertical u-component shear",
					units = "/s")
			46 -> Grib1Parameter(id,
					code = "VVCSH",
					description = "Vertical v-component shear",
					units = "/s")
			47 -> Grib1Parameter(id,
					code = "DIRC",
					description = "Direction of current",
					units = "Degree true")
			48 -> Grib1Parameter(id,
					code = "SPC",
					description = "Speed of current",
					units = "m/s")
			49 -> Grib1Parameter(id,
					code = "UOGRD",
					description = "u-component of current",
					units = "m/s")
			50 -> Grib1Parameter(id,
					code = "VOGRD",
					description = "v-component of current",
					units = "m/s")
			51 -> Grib1Parameter(id,
					code = "SPFH",
					description = "Specific humidity",
					units = "kg/kg")
			52 -> Grib1Parameter(id,
					code = "RH",
					description = "Relative humidity",
					units = "%")
			53 -> Grib1Parameter(id,
					code = "MIXR",
					description = "Humidity mixing ratio",
					units = "kg/kg")
			54 -> Grib1Parameter(id,
					code = "PWAT",
					description = "Precipitable water",
					units = "kg/m^2")
			55 -> Grib1Parameter(id,
					code = "VAPP",
					description = "Vapor pressure",
					units = "Pa")
			56 -> Grib1Parameter(id,
					code = "SATD",
					description = "Saturation deficit",
					units = "Pa")
			57 -> Grib1Parameter(id,
					code = "EVP",
					description = "Evaporation",
					units = "kg/m^2")
			58 -> Grib1Parameter(id,
					code = "CICE",
					description = "Cloud Ice",
					units = "kg/m^2")
			59 -> Grib1Parameter(id,
					code = "PRATE",
					description = "Precipitation rate",
					units = "kg/m^2/s")
			60 -> Grib1Parameter(id,
					code = "TSTM",
					description = "Thunderstorm probability",
					units = "%")
			61 -> Grib1Parameter(id,
					code = "APCP",
					description = "Total precipitation",
					units = "kg/m^2")
			62 -> Grib1Parameter(id,
					code = "NCPCP",
					description = "Large scale precipitation (non-conv.)",
					units = "kg/m^2")
			63 -> Grib1Parameter(id,
					code = "ACPCP",
					description = "Convective precipitation",
					units = "kg/m^2")
			64 -> Grib1Parameter(id,
					code = "SRWEQ",
					description = "Snowfall rate water equivalent",
					units = "kg/m^2/s")
			65 -> Grib1Parameter(id,
					code = "WEASD",
					description = "Water equiv. of accum. snow depth",
					units = "kg/m^2")
			66 -> Grib1Parameter(id,
					code = "SNOD",
					description = "Snow depth",
					units = "m")
			67 -> Grib1Parameter(id,
					code = "MIXHT",
					description = "Mixed layer depth",
					units = "m")
			68 -> Grib1Parameter(id,
					code = "TTHDP",
					description = "Transient thermocline depth",
					units = "m")
			69 -> Grib1Parameter(id,
					code = "MTHD",
					description = "Main thermocline depth",
					units = "m")
			70 -> Grib1Parameter(id,
					code = "MTHA",
					description = "Main thermocline anomaly",
					units = "m")
			71 -> Grib1Parameter(id,
					code = "TCDC",
					description = "Total cloud cover",
					units = "%")
			72 -> Grib1Parameter(id,
					code = "CDCON",
					description = "Convective cloud cover",
					units = "%")
			73 -> Grib1Parameter(id,
					code = "LCDC",
					description = "Low cloud cover",
					units = "%")
			74 -> Grib1Parameter(id,
					code = "MCDC",
					description = "Medium cloud cover",
					units = "%")
			75 -> Grib1Parameter(id,
					code = "HCDC",
					description = "High cloud cover",
					units = "%")
			76 -> Grib1Parameter(id,
					code = "CWAT",
					description = "Cloud water",
					units = "kg/m^2")
			77 -> Grib1Parameter(id,
					code = "BLI",
					description = "Best lifted index (to 500 hPa)",
					units = "K")
			78 -> Grib1Parameter(id,
					code = "SNOC",
					description = "Convective snow",
					units = "kg/m^2")
			79 -> Grib1Parameter(id,
					code = "SNOL",
					description = "Large scale snow",
					units = "kg/m^2")
			80 -> Grib1Parameter(id,
					code = "WTMP",
					description = "Water Temperature",
					units = "K")
			81 -> Grib1Parameter(id,
					code = "LAND",
					description = "Land cover (land=1, sea=0)",
					units = "proportion")
			82 -> Grib1Parameter(id,
					code = "DSLM",
					description = "Deviation of sea level from mean",
					units = "m")
			83 -> Grib1Parameter(id,
					code = "SFCR",
					description = "Surface roughness",
					units = "m")
			84 -> Grib1Parameter(id,
					code = "ALBDO",
					description = "Albedo",
					units = "%")
			85 -> Grib1Parameter(id,
					code = "TSOIL",
					description = "Soil temperature",
					units = "K")
			86 -> Grib1Parameter(id,
					code = "SOILM",
					description = "Soil moisture content",
					units = "kg/m2")
			87 -> Grib1Parameter(id,
					code = "VEG",
					description = "Vegetation",
					units = "%")
			88 -> Grib1Parameter(id,
					code = "SALTY",
					description = "Salinity",
					units = "kg/kg")
			89 -> Grib1Parameter(id,
					code = "DEN",
					description = "Density",
					units = "kg/m^3")
			90 -> Grib1Parameter(id,
					code = "WATR",
					description = "Water runoff",
					units = "kg/m^2")
			91 -> Grib1Parameter(id,
					code = "ICEC",
					description = "Ice cover (ice=1, no ice=0)",
					units = "proportion")
			92 -> Grib1Parameter(id,
					code = "ICETK",
					description = "Ice thickness",
					units = "m")
			93 -> Grib1Parameter(id,
					code = "DICED",
					description = "Direction of ice drift",
					units = "deg true")
			94 -> Grib1Parameter(id,
					code = "SICED",
					description = "Speed of ice drift",
					units = "m/s")
			95 -> Grib1Parameter(id,
					code = "UICE",
					description = "u-component of ice drift",
					units = "m/s")
			96 -> Grib1Parameter(id,
					code = "VICE",
					description = "v-component of ice drift",
					units = "m/s")
			97 -> Grib1Parameter(id,
					code = "ICEG",
					description = "Ice growth rate",
					units = "m/s")
			98 -> Grib1Parameter(id,
					code = "ICED",
					description = "Ice divergence",
					units = "m/s")
			99 -> Grib1Parameter(id,
					code = "SNOM",
					description = "Snow melt",
					units = "kg/m^2")
			100 -> Grib1Parameter(id,
					code = "HTSGW",
					description = "Significant height of combined wind waves and swell",
					units = "m")
			101 -> Grib1Parameter(id,
					code = "WVDIR",
					description = "Direction of wind waves (from which)",
					units = "Degree true")
			102 -> Grib1Parameter(id,
					code = "WVHGT",
					description = "Significant height of wind waves",
					units = "m")
			103 -> Grib1Parameter(id,
					code = "WVPER",
					description = "Mean period of wind waves",
					units = "s")
			104 -> Grib1Parameter(id,
					code = "SWDIR",
					description = "Direction of swell waves",
					units = "Degree true")
			105 -> Grib1Parameter(id,
					code = "SWELL",
					description = "Significant height of swell waves",
					units = "m")
			106 -> Grib1Parameter(id,
					code = "SWPER",
					description = "Mean period of swell waves",
					units = "s")
			107 -> Grib1Parameter(id,
					code = "DIRPW",
					description = "Primary wave direction",
					units = "Degree true")
			108 -> Grib1Parameter(id,
					code = "PERPW",
					description = "Primary wave mean period",
					units = "s")
			109 -> Grib1Parameter(id,
					code = "DIRSW",
					description = "Secondary wave direction",
					units = "Degree true")
			110 -> Grib1Parameter(id,
					code = "PERSW",
					description = "Secondary wave mean period",
					units = "s")
			111 -> Grib1Parameter(id,
					code = "NSWRS",
					description = "Net short-wave radiation flux (surface)",
					units = "W/m^2")
			112 -> Grib1Parameter(id,
					code = "NLWRS",
					description = "Net long wave radiation flux (surface)",
					units = "W/m^2")
			113 -> Grib1Parameter(id,
					code = "NSWRT",
					description = "Net short-wave radiation flux (top of atmosphere)",
					units = "W/m^2")
			114 -> Grib1Parameter(id,
					code = "NLWRT",
					description = "Net long wave radiation flux (top of atmosphere)",
					units = "W/m^2")
			115 -> Grib1Parameter(id,
					code = "LWAVR",
					description = "Long wave radiation flux",
					units = "W/m^2")
			116 -> Grib1Parameter(id,
					code = "SWAVR",
					description = "Short wave radiation flux",
					units = "W/m^2")
			117 -> Grib1Parameter(id,
					code = "GRAD",
					description = "Global radiation flux",
					units = "W/m^2")
			118 -> Grib1Parameter(id,
					code = "BRTMP",
					description = "Brightness temperature",
					units = "K")
			119 -> Grib1Parameter(id,
					code = "LWRAD",
					description = "Radiance (with respect to wave number)",
					units = "W/m/sr")
			120 -> Grib1Parameter(id,
					code = "SWRAD",
					description = "Radiance (with respect to wave length)",
					units = "W/m^3/sr")
			121 -> Grib1Parameter(id,
					code = "LHTFL",
					description = "Latent heat net flux",
					units = "W/m^2")
			122 -> Grib1Parameter(id,
					code = "SHTFL",
					description = "Sensible heat net flux",
					units = "W/m^2")
			123 -> Grib1Parameter(id,
					code = "BLYDP",
					description = "Boundary layer dissipation",
					units = "W/m^2")
			124 -> Grib1Parameter(id,
					code = "UFLX",
					description = "Momentum flux, u component",
					units = "N/m^2")
			125 -> Grib1Parameter(id,
					code = "VFLX",
					description = "Momentum flux, v component",
					units = "N/m^2")
			126 -> Grib1Parameter(id,
					code = "WMIXE",
					description = "Wind mixing energy",
					units = "J")
			127 -> Grib1Parameter(id,
					code = "IMGD",
					description = "Image data",
					units = "")
			else -> when (centreId) {
				7 -> getParameterFromTable2Centre7(id)
				else -> null
			}
		}

		private fun getParameterFromTable2Centre7(id: Int): Grib1Parameter? = when (id) {
			128 -> Grib1Parameter(id,
					code = "MSLSA",
					description = "Mean Sea Level Pressure (Standard Atmosphere Reduction)",
					units = "Pa")
			129 -> Grib1Parameter(id,
					code = "MSLMA",
					description = "Mean Sea Level Pressure (MAPS System Reduction)",
					units = "Pa")
			130 -> Grib1Parameter(id,
					code = "MSLET",
					description = "Mean Sea Level Pressure (NAM Model Reduction)",
					units = "Pa")
			131 -> Grib1Parameter(id,
					code = "LFTX",
					description = "Surface lifted index",
					units = "K")
			132 -> Grib1Parameter(id,
					code = "4LFTX",
					description = "Best (4 layer) lifted index",
					units = "K")
			133 -> Grib1Parameter(id,
					code = "KX",
					description = "K index",
					units = "K")
			134 -> Grib1Parameter(id,
					code = "SX",
					description = "Sweat index",
					units = "K")
			135 -> Grib1Parameter(id,
					code = "MCONV",
					description = "Horizontal moisture divergence",
					units = "kg/kg/s")
			136 -> Grib1Parameter(id,
					code = "VWSH",
					description = "Vertical speed shear",
					units = "1/s")
			137 -> Grib1Parameter(id,
					code = "TSLSA",
					description = "3-hr pressure tendency Std. Atmos. Reduction",
					units = "Pa/s")
			138 -> Grib1Parameter(id,
					code = "BVF2",
					description = "Brunt-Vaisala frequency (squared)",
					units = "1/s^2")
			139 -> Grib1Parameter(id,
					code = "PVMW",
					description = "Potential vorticity (density weighted)",
					units = "1/s/m")
			140 -> Grib1Parameter(id,
					code = "CRAIN",
					description = "Categorical rain (yes=1; no=0)")
			141 -> Grib1Parameter(id,
					code = "CFRZR",
					description = "Categorical freezing rain (yes=1; no=0)")
			142 -> Grib1Parameter(id,
					code = "CICEP",
					description = "Categorical ice pellets (yes=1; no=0)")
			143 -> Grib1Parameter(id,
					code = "CSNOW",
					description = "Categorical snow (yes=1; no=0)")
			144 -> Grib1Parameter(id,
					code = "SOILW",
					description = "Volumetric soil moisture content",
					units = "fraction")
			145 -> Grib1Parameter(id,
					code = "PEVPR",
					description = "Potential evaporation rate",
					units = "W/m^2")
			146 -> Grib1Parameter(id,
					code = "CWORK",
					description = "Cloud work function",
					units = "J/kg")
			147 -> Grib1Parameter(id,
					code = "UGWD",
					description = "Zonal flux of gravity wave stress",
					units = "N/m^2")
			148 -> Grib1Parameter(id,
					code = "VGWD",
					description = "Meridional flux of gravity wave stress",
					units = "N/m^2")
			149 -> Grib1Parameter(id,
					code = "PVORT",
					description = "Potential vorticity",
					units = "m^2/s/kg")
			150 -> Grib1Parameter(id,
					code = "COVMZ",
					description = "Covariance between meridional and zonal components of the wind",
					units = "m^2/s^2")
			151 -> Grib1Parameter(id,
					code = "COVTZ",
					description = "Covariance between temperature and zonal components of the wind",
					units = "K*m/s")
			152 -> Grib1Parameter(id,
					code = "COVTM",
					description = "Covariance between temperature and meridional components of the wind",
					units = "K*m/s")
			153 -> Grib1Parameter(id,
					code = "CLWMR",
					description = "Cloud Mixing Ratio",
					units = "kg/kg")
			154 -> Grib1Parameter(id,
					code = "O3MR",
					description = "Ozone mixing ratio",
					units = "kg/kg")
			155 -> Grib1Parameter(id,
					code = "GFLUX",
					description = "Ground Heat Flux",
					units = "W/m^2")
			156 -> Grib1Parameter(id,
					code = "CIN",
					description = "Convective inhibition",
					units = "J/kg")
			157 -> Grib1Parameter(id,
					code = "CAPE",
					description = "Convective Available Potential Energy",
					units = "J/kg")
			158 -> Grib1Parameter(id,
					code = "TKE",
					description = "Turbulent Kinetic Energy",
					units = "J/kg")
			159 -> Grib1Parameter(id,
					code = "CONDP",
					description = "Condensation pressure of parcel lifted from indicated surface",
					units = "Pa")
			160 -> Grib1Parameter(id,
					code = "CSUSF",
					description = "Clear Sky Upward Solar Flux",
					units = "W/m^2")
			161 -> Grib1Parameter(id,
					code = "CSDSF",
					description = "Clear Sky Downward Solar Flux",
					units = "W/m^2")
			162 -> Grib1Parameter(id,
					code = "CSULF",
					description = "Clear Sky upward long wave flux",
					units = "W/m^2")
			163 -> Grib1Parameter(id,
					code = "CSDLF",
					description = "Clear Sky downward long wave flux",
					units = "W/m^2")
			164 -> Grib1Parameter(id,
					code = "CFNSF",
					description = "Cloud forcing net solar flux",
					units = "W/m^2")
			165 -> Grib1Parameter(id,
					code = "CFNLF",
					description = "Cloud forcing net long wave flux",
					units = "W/m^2")
			166 -> Grib1Parameter(id,
					code = "VBDSF",
					description = "Visible beam downward solar flux",
					units = "W/m^2")
			167 -> Grib1Parameter(id,
					code = "VDDSF",
					description = "Visible diffuse downward solar flux",
					units = "W/m^2")
			168 -> Grib1Parameter(id,
					code = "NBDSF",
					description = "Near IR beam downward solar flux",
					units = "W/m^2")
			169 -> Grib1Parameter(id,
					code = "NDDSF",
					description = "Near IR diffuse downward solar flux",
					units = "W/m^2")
			170 -> Grib1Parameter(id,
					code = "RWMR",
					description = "Rain water mixing ratio",
					units = "kg/kg")
			171 -> Grib1Parameter(id,
					code = "SNMR",
					description = "Snow mixing ratio",
					units = "kg/kg")
			172 -> Grib1Parameter(id,
					code = "MFLX",
					description = "Horizontal Momentum flux",
					units = "N/m^2")
			173 -> Grib1Parameter(id,
					code = "LMH",
					description = "Mass point model surface")
			174 -> Grib1Parameter(id,
					code = "LMV",
					description = "Velocity point model surface")
			175 -> Grib1Parameter(id,
					code = "MLYNO",
					description = "Model layer number (from bottom up)")
			176 -> Grib1Parameter(id,
					code = "NLAT",
					description = "latitude (-90 to +90)",
					units = "deg")
			177 -> Grib1Parameter(id,
					code = "ELON",
					description = "east longitude (0-360)",
					units = "deg")
			178 -> Grib1Parameter(id,
					code = "ICMR",
					description = "Ice mixing ratio",
					units = "kg/kg")
			179 -> Grib1Parameter(id,
					code = "GRMR",
					description = "Graupel mixing ratio",
					units = "kg/kg")
			180 -> Grib1Parameter(id,
					code = "GUST",
					description = "Wind speed (gust)",
					units = "m/s")
			181 -> Grib1Parameter(id,
					code = "LPSX",
					description = "x-gradient of log pressure",
					units = "1/m")
			182 -> Grib1Parameter(id,
					code = "LPSY",
					description = "y-gradient of log pressure",
					units = "1/m")
			183 -> Grib1Parameter(id,
					code = "HGTX",
					description = "x-gradient of height",
					units = "m/m")
			184 -> Grib1Parameter(id,
					code = "HGTY",
					description = "y-gradient of height",
					units = "m/m")
			185 -> Grib1Parameter(id,
					code = "TPFI",
					description = "Turbulence Potential Forecast Index")
			186 -> Grib1Parameter(id,
					code = "TIPD",
					description = "Total Icing Potential Diagnostic")
			187 -> Grib1Parameter(id,
					code = "LTNG",
					description = "Lightning")
			188 -> Grib1Parameter(id,
					code = "RDRIP",
					description = "Rate of water dropping from canopy to ground")
			189 -> Grib1Parameter(id,
					code = "VPTMP",
					description = "Virtual potential temperature",
					units = "K")
			190 -> Grib1Parameter(id,
					code = "HLCY",
					description = "Storm relative helicity",
					units = "m^2/s^2")
			191 -> Grib1Parameter(id,
					code = "PROB",
					description = "Probability from ensemble",
					units = "%")
			192 -> Grib1Parameter(id,
					code = "PROBN",
					description = "Probability from ensemble normalized with respect to climate expectancy",
					units = "%")
			193 -> Grib1Parameter(id,
					code = "POP",
					description = "Probability of precipitation",
					units = "%")
			194 -> Grib1Parameter(id,
					code = "CPOFP",
					description = "Percent of frozen precipitation",
					units = "%")
			195 -> Grib1Parameter(id,
					code = "CPOZP",
					description = "Probability of freezing precipitation",
					units = "%")
			196 -> Grib1Parameter(id,
					code = "USTM",
					description = "u-component of storm motion",
					units = "m/s")
			197 -> Grib1Parameter(id,
					code = "VSTM",
					description = "v-component of storm motion",
					units = "m/s")
			198 -> Grib1Parameter(id,
					code = "NCIP",
					description = "Number concentration for ice particles")
			199 -> Grib1Parameter(id,
					code = "EVBS",
					description = "Direct evaporation from bare soil",
					units = "W/m^2")
			200 -> Grib1Parameter(id,
					code = "EVCW",
					description = "Canopy water evaporation",
					units = "W/m2")
			201 -> Grib1Parameter(id,
					code = "ICWAT",
					description = "Ice-free water surface",
					units = "%")
			202 -> Grib1Parameter(id,
					code = "CWDI",
					description = "Convective weather detection index")
			203 -> Grib1Parameter(id,
					code = "VAFTD",
					description = "VAFTAD",
					units = "log10(kg/m^3)")
			204 -> Grib1Parameter(id,
					code = "DSWRF",
					description = "downward short wave rad. flux",
					units = "W/m^2")
			205 -> Grib1Parameter(id,
					code = "DLWRF",
					description = "downward long wave rad. flux",
					units = "W/m^2")
			206 -> Grib1Parameter(id,
					code = "UVI",
					description = "Ultra violet index (1 hour integration centered at solar noon)",
					units = "W/m^2")
			207 -> Grib1Parameter(id,
					code = "MSTAV",
					description = "Moisture availability",
					units = "%")
			208 -> Grib1Parameter(id,
					code = "SFEXC",
					description = "Exchange coefficient",
					units = "(kg/m^3)(m/s)")
			209 -> Grib1Parameter(id,
					code = "MIXLY",
					description = "No. of mixed layers next to surface",
					units = "")
			210 -> Grib1Parameter(id,
					code = "TRANS",
					description = "Transpiration",
					units = "W/m^2")
			211 -> Grib1Parameter(id,
					code = "USWRF",
					description = "upward short wave rad. flux",
					units = "W/m2")
			212 -> Grib1Parameter(id,
					code = "ULWRF",
					description = "upward long wave rad. flux",
					units = "W/m^2")
			213 -> Grib1Parameter(id,
					code = "CDLYR",
					description = "Amount of non-convective cloud",
					units = "%")
			214 -> Grib1Parameter(id,
					code = "CPRAT",
					description = "Convective Precipitation rate",
					units = "kg/m^2/s")
			215 -> Grib1Parameter(id,
					code = "TTDIA",
					description = "Temperature tendency by all physics",
					units = "K/s")
			216 -> Grib1Parameter(id,
					code = "TTRAD",
					description = "Temperature tendency by all radiation",
					units = "K/s")
			217 -> Grib1Parameter(id,
					code = "TTPHY",
					description = "Temperature tendency by non-radiation physics",
					units = "K/s")
			218 -> Grib1Parameter(id,
					code = "PREIX",
					description = "Precipitation index(0.0-1.00)",
					units = "fraction")
			219 -> Grib1Parameter(id,
					code = "TSD1D",
					description = "Std. dev. of IR T over 1x1 deg area",
					units = "K")
			220 -> Grib1Parameter(id,
					code = "NLGSP",
					description = "Natural log of surface pressure",
					units = "ln(kPa)")
			221 -> Grib1Parameter(id,
					code = "HPBL",
					description = "Planetary boundary layer height",
					units = "m")
			222 -> Grib1Parameter(id,
					code = "5WAVH",
					description = "5-wave geopotential height",
					units = "gpm")
			223 -> Grib1Parameter(id,
					code = "CNWAT",
					description = "Plant canopy surface water",
					units = "kg/m^2")
			224 -> Grib1Parameter(id,
					code = "SOTYP",
					description = "Soil type (as in Zobler)",
					units = "")
			225 -> Grib1Parameter(id,
					code = "VGTYP",
					description = "Vegitation type (as in SiB)",
					units = "")
			226 -> Grib1Parameter(id,
					code = "BMIXL",
					description = "Blackadar's mixing length scale",
					units = "m")
			227 -> Grib1Parameter(id,
					code = "AMIXL",
					description = "Asymptotic mixing length scale",
					units = "m")
			228 -> Grib1Parameter(id,
					code = "PEVAP",
					description = "Potential evaporation",
					units = "kg/m^2")
			229 -> Grib1Parameter(id,
					code = "SNOHF",
					description = "Snow phase-change heat flux",
					units = "W/m^2")
			230 -> Grib1Parameter(id,
					code = "5WAVA",
					description = "5-wave geopotential height anomaly",
					units = "gpm")
			231 -> Grib1Parameter(id,
					code = "MFLUX",
					description = "Convective cloud mass flux",
					units = "Pa/s")
			232 -> Grib1Parameter(id,
					code = "DTRF",
					description = "Downward total radiation flux",
					units = "W/m^2")
			233 -> Grib1Parameter(id,
					code = "UTRF",
					description = "Upward total radiation flux",
					units = "W/m^2")
			234 -> Grib1Parameter(id,
					code = "BGRUN",
					description = "Baseflow-groundwater runoff",
					units = "kg/m^2")
			235 -> Grib1Parameter(id,
					code = "SSRUN",
					description = "Storm surface runoff",
					units = "kg/m^2")
			236 -> Grib1Parameter(id,
					code = "SIPD",
					description = "Supercooled Large Droplet (SLD) Icing Potential Diagnostic")
			237 -> Grib1Parameter(id,
					code = "03TOT",
					description = "Total ozone",
					units = "kg/m^2")
			238 -> Grib1Parameter(id,
					code = "SNOWC",
					description = "Snow cover",
					units = "%")
			239 -> Grib1Parameter(id,
					code = "SNOT",
					description = "Snow temperature",
					units = "K")
			240 -> Grib1Parameter(id,
					code = "COVTW",
					description = "Covariance between temperature and vertical component of the wind",
					units = "K*m/s")
			241 -> Grib1Parameter(id,
					code = "LRGHR",
					description = "Large scale condensate heat rate",
					units = "K/s")
			242 -> Grib1Parameter(id,
					code = "CNVHR",
					description = "Deep convective heating rate",
					units = "K/s")
			243 -> Grib1Parameter(id,
					code = "CNVMR",
					description = "Deep convective moistening rate",
					units = "kg/kg/s")
			244 -> Grib1Parameter(id,
					code = "SHAHR",
					description = "Shallow convective heating rate",
					units = "K/s")
			245 -> Grib1Parameter(id,
					code = "SHAMR",
					description = "Shallow convective moistening rate",
					units = "kg/kg/s")
			246 -> Grib1Parameter(id,
					code = "VDFHR",
					description = "Vertical diffusion heating rate",
					units = "K/s")
			247 -> Grib1Parameter(id,
					code = "VDFUA",
					description = "Vertical diffusion zonal acceleration",
					units = "m/s^2")
			248 -> Grib1Parameter(id,
					code = "VDFVA",
					description = "Vertical diffusion meridional acceleration",
					units = "m/s^2")
			249 -> Grib1Parameter(id,
					code = "VDFMR",
					description = "Vertical diffusion moistening rate",
					units = "kg/kg/s")
			250 -> Grib1Parameter(id,
					code = "SWHR",
					description = "Solar radiative heating rate",
					units = "K/s")
			251 -> Grib1Parameter(id,
					code = "LWHR",
					description = "Long wave radiative heating rate",
					units = "K/s")
			252 -> Grib1Parameter(id,
					code = "CD",
					description = "Drag coefficient")
			253 -> Grib1Parameter(id,
					code = "FRICV",
					description = "Friction velocity",
					units = "m/s")
			254 -> Grib1Parameter(id,
					code = "RI",
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
			128 -> Grib1Parameter(id,
					code = "AVDEPTH",
					description = "Ocean depth - mean",
					units = "m")
			129 -> Grib1Parameter(id,
					code = "DEPTH",
					description = "Ocean depth - instantaneous",
					units = "m")
			130 -> Grib1Parameter(id,
					code = "ELEV",
					description = "Ocean surface elevation relative to geoid",
					units = "m")
			131 -> Grib1Parameter(id,
					code = "MXEL24",
					description = "Max ocean surface elevation in last 24 hours",
					units = "m")
			132 -> Grib1Parameter(id,
					code = "MNEL24",
					description = "Min ocean surface elevation in last 24 hours",
					units = "m")
			135 -> Grib1Parameter(id,
					code = "O2",
					description = "Oxygen (O2 (aq))",
					units = "Mol/kg")
			136 -> Grib1Parameter(id,
					code = "PO4",
					description = "PO4",
					units = "Mol/kg")
			137 -> Grib1Parameter(id,
					code = "NO3",
					description = "NO3",
					units = "Mol/kg")
			138 -> Grib1Parameter(id,
					code = "SIO4",
					description = "SiO4",
					units = "Mol/kg")
			139 -> Grib1Parameter(id,
					code = "CO2AQ",
					description = "CO2 (aq)",
					units = "Mol/kg")
			140 -> Grib1Parameter(id,
					code = "HCO3",
					description = "HCO3",
					units = "Mol/kg")
			141 -> Grib1Parameter(id,
					code = "CO3",
					description = "CO3",
					units = "Mol/kg")
			142 -> Grib1Parameter(id,
					code = "TCO2",
					description = "TCO2",
					units = "Mol/kg")
			143 -> Grib1Parameter(id,
					code = "TALK",
					description = "TALK",
					units = "Mol/kg")
			144 -> Grib1Parameter(id,
					code = "CH",
					description = "Heat Exchange Coefficient",
					units = "")
			146 -> Grib1Parameter(id,
					code = "S11",
					description = "S11 - 1,1 component of ice stress tensor",
					units = "")
			147 -> Grib1Parameter(id,
					code = "S12",
					description = "S12 - 1,2 component of ice stress tensor",
					units = "")
			148 -> Grib1Parameter(id,
					code = "S22",
					description = "S22 - 2,2 component of ice stress tensor",
					units = "")
			149 -> Grib1Parameter(id,
					code = "INV1",
					description = "T1 - First invariant of stress tensor",
					units = "")
			150 -> Grib1Parameter(id,
					code = "INV2",
					description = "T2 - Second invariant of stress tensor",
					units = "")
			155 -> Grib1Parameter(id,
					code = "WVRGH",
					description = "Wave roughness",
					units = "")
			156 -> Grib1Parameter(id,
					code = "WVSTRS",
					description = "Wave stresses",
					units = "")
			157 -> Grib1Parameter(id,
					code = "WHITE",
					description = "Whitecap coverage",
					units = "WHITE")
			158 -> Grib1Parameter(id,
					code = "SWDIRWID",
					description = "Swell direction width",
					units = "")
			159 -> Grib1Parameter(id,
					code = "SWFREWID",
					description = "Swell frequency width",
					units = "")
			160 -> Grib1Parameter(id,
					code = "WVAGE",
					description = "Wave age",
					units = "")
			161 -> Grib1Parameter(id,
					code = "PWVAGE",
					description = "Physical Wave",
					units = "age")
			165 -> Grib1Parameter(id,
					code = "LTURB",
					description = "Master length scale (turbulence)",
					units = "m")
			170 -> Grib1Parameter(id,
					code = "AIHFLX",
					description = "Net air-ice heat flux",
					units = "W/m2")
			171 -> Grib1Parameter(id,
					code = "AOHFLX",
					description = "Net air-ocean heat flux",
					units = "W/m2")
			172 -> Grib1Parameter(id,
					code = "IOHFLX",
					description = "Net ice-ocean heat flux",
					units = "W/m2")
			173 -> Grib1Parameter(id,
					code = "IOSFLX",
					description = "Net ice-ocean salt flux",
					units = "kg/s")
			175 -> Grib1Parameter(id,
					code = "OMLT",
					description = "Ocean mixed layer temperature",
					units = "K")
			176 -> Grib1Parameter(id,
					code = "OMLS",
					description = "Ocean mixed layer salinity",
					units = "kg/kg")
			177 -> Grib1Parameter(id,
					code = "P2OMLT",
					description = "Ocean mixed layer potential density (Referenced to 2000m)",
					units = "kg/m3")
			178 -> Grib1Parameter(id,
					code = "OMLU",
					description = "Ocean mixed layer u velocity",
					units = "m/s")
			179 -> Grib1Parameter(id,
					code = "OMLV",
					description = "Ocean mixed layer v velocity",
					units = "m/s")
			180 -> Grib1Parameter(id,
					code = "ASHFL",
					description = "Assimilative heat flux",
					units = "W/m2")
			181 -> Grib1Parameter(id,
					code = "ASSFL",
					description = "Assimilative salt flux",
					units = "mm/day")
			182 -> Grib1Parameter(id,
					code = "BOTLD",
					description = "Bottom layer depth",
					units = "m")
			183 -> Grib1Parameter(id,
					code = "UBARO",
					description = "Barotropic U velocity",
					units = "m/s")
			184 -> Grib1Parameter(id,
					code = "VBARO",
					description = "Barotropic V velocity",
					units = "m/s")
			185 -> Grib1Parameter(id,
					code = "INTFD",
					description = "Interface depths",
					units = "m")
			186 -> Grib1Parameter(id,
					code = "WTMPC",
					description = "3-D temperature",
					units = "deg c")
			187 -> Grib1Parameter(id,
					code = "SALIN",
					description = "3-D Salinity",
					units = "psu")
			188 -> Grib1Parameter(id,
					code = "EMNP",
					description = "Evaporation - precipitation",
					units = "cm/day")
			190 -> Grib1Parameter(id,
					code = "KENG",
					description = "Kinetic energy",
					units = "J/kg")
			191 -> Grib1Parameter(id,
					code = "BKENG",
					description = "Barotropic Kinetic energy",
					units = "J/kg")
			192 -> Grib1Parameter(id,
					code = "LAYTH",
					description = "Layer Thickness",
					units = "m")
			193 -> Grib1Parameter(id,
					code = "SSTT",
					description = "Surface temperature trend",
					units = "deg/day")
			194 -> Grib1Parameter(id,
					code = "SSST",
					description = "Surface salinity trend",
					units = "psu/day")
			195 -> Grib1Parameter(id,
					code = "OVHD",
					description = "Ocean Vertical Heat Diffusivity",
					units = "m2s-1")
			196 -> Grib1Parameter(id,
					code = "OVSD",
					description = "Ocean Vertical Salt Diffusivity",
					units = "m2s-1")
			197 -> Grib1Parameter(id,
					code = "OVMD",
					description = "Ocean Vertical Momentum Diffusivity",
					units = "m2s-1")
			254 -> Grib1Parameter(id,
					code = "REV",
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
			128 -> Grib1Parameter(id,
					code = "PAOT",
					description = "Probability anomaly of temperature",
					units = "%")
			129 -> Grib1Parameter(id,
					code = "PAOP",
					description = "Probability anomaly of precipitation",
					units = "%")
			130 -> Grib1Parameter(id,
					code = "CWR",
					description = "Probability of Wetting Rain, exceeding 0.10 in a given time period",
					units = "%")
			131 -> Grib1Parameter(id,
					code = "FRAIN",
					description = "Rain fraction of total liquid water",
					units = "")
			132 -> Grib1Parameter(id,
					code = "FICE",
					description = "Ice fraction of total condensate",
					units = "")
			133 -> Grib1Parameter(id,
					code = "RIME",
					description = "Rime Factor",
					units = "")
			134 -> Grib1Parameter(id,
					code = "CUEFI",
					description = "Convective cloud efficiency",
					units = "")
			135 -> Grib1Parameter(id,
					code = "TCOND",
					description = "Total condensate",
					units = "kg/kg")
			136 -> Grib1Parameter(id,
					code = "TCOLW",
					description = "Total column-integrated cloud water",
					units = "kg/m2")
			137 -> Grib1Parameter(id,
					code = "TCOLI",
					description = "Total column-integrated cloud ice",
					units = "kg/m2")
			138 -> Grib1Parameter(id,
					code = "TCOLR",
					description = "Total column-integrated rain",
					units = "kg/m2")
			139 -> Grib1Parameter(id,
					code = "TCOLS",
					description = "Total column-integrated snow",
					units = "kg/m2")
			140 -> Grib1Parameter(id,
					code = "TCOLC",
					description = "Total column-integrated condensate",
					units = "kg/m2")
			141 -> Grib1Parameter(id,
					code = "PLPL",
					description = "Pressure of level from which parcel was lifted",
					units = "Pa")
			142 -> Grib1Parameter(id,
					code = "HLPL",
					description = "Height of level from which parcel was lifted",
					units = "m")
			143 -> Grib1Parameter(id,
					code = "CEMS",
					description = "Cloud Emissivity",
					units = "Fraction 0-1")
			144 -> Grib1Parameter(id,
					code = "COPD",
					description = "Cloud Optical Depth",
					units = "")
			145 -> Grib1Parameter(id,
					code = "PSIZ",
					description = "Effective Particle size",
					units = "Microns")
			146 -> Grib1Parameter(id,
					code = "TCWAT",
					description = "Total Water Cloud",
					units = "%")
			147 -> Grib1Parameter(id,
					code = "TCICE",
					description = "Total Ice Cloud",
					units = "%")
			148 -> Grib1Parameter(id,
					code = "WDIF",
					description = "Wind difference",
					units = "m/s")
			149 -> Grib1Parameter(id,
					code = "WSTP",
					description = "Wave Steepness",
					units = "")
			150 -> Grib1Parameter(id,
					code = "PTAN",
					description = "Probability of Temperature being above normal",
					units = "%")
			151 -> Grib1Parameter(id,
					code = "PTNN",
					description = "Probability of Temperature being near normal",
					units = "%")
			152 -> Grib1Parameter(id,
					code = "PTBN",
					description = "Probability of Temperature being below normal",
					units = "%")
			153 -> Grib1Parameter(id,
					code = "PPAN",
					description = "Probability of Precipitation being above normal",
					units = "%")
			154 -> Grib1Parameter(id,
					code = "PPNN",
					description = "Probability of Precipitation being near normal",
					units = "%")
			155 -> Grib1Parameter(id,
					code = "PPBN",
					description = "Probability of Precipitation being below normal",
					units = "%")
			156 -> Grib1Parameter(id,
					code = "PMTC",
					description = "Particulate matter (coarse)",
					units = "µg/m3")
			157 -> Grib1Parameter(id,
					code = "PMTF",
					description = "Particulate matter (fine)",
					units = "µg/m3")
			158 -> Grib1Parameter(id,
					code = "AETMP",
					description = "Analysis error of temperature",
					units = "K")
			159 -> Grib1Parameter(id,
					code = "AEDPT",
					description = "Analysis error of dew point",
					units = "%")
			160 -> Grib1Parameter(id,
					code = "AESPH",
					description = "Analysis error of specific humidity",
					units = "kg/kg")
			161 -> Grib1Parameter(id,
					code = "AEUWD",
					description = "Analysis error of u-wind",
					units = "m/s")
			162 -> Grib1Parameter(id,
					code = "AEVWD",
					description = "Analysis error of v-wind",
					units = "m/s")
			163 -> Grib1Parameter(id,
					code = "LPMTF",
					description = "Particulate matter (fine)",
					units = "log10(µg/m3)")
			164 -> Grib1Parameter(id,
					code = "LIPMF",
					description = "Integrated column particulate matter (fine)",
					units = "log10(µg/m3)")
			165 -> Grib1Parameter(id,
					code = "REFZR",
					description = "Derived radar reflectivity backscatter from rain",
					units = "mm6/m3")
			166 -> Grib1Parameter(id,
					code = "REFZI",
					description = "Derived radar reflectivity backscatter from ice",
					units = "mm6/m3")
			167 -> Grib1Parameter(id,
					code = "REFZC",
					description = "Derived radar reflectivity backscatter from parameterized convection",
					units = "mm6/m3")
			168 -> Grib1Parameter(id,
					code = "TCLSW",
					description = "Total column-integrated supercooled liquid water",
					units = "kg/m2")
			169 -> Grib1Parameter(id,
					code = "TCOLM",
					description = "Total column-integrated melting ice",
					units = "kg/m2")
			170 -> Grib1Parameter(id,
					code = "ELRDI",
					description = "Ellrod Index",
					units = "")
			171 -> Grib1Parameter(id,
					code = "TSEC",
					description = "Seconds prior to initial reference time",
					units = "sec")
			172 -> Grib1Parameter(id,
					code = "TSECA",
					description = "Seconds after initial reference time",
					units = "sec")
			173 -> Grib1Parameter(id,
					code = "NUM",
					description = "Number of samples/observations",
					units = "")
			174 -> Grib1Parameter(id,
					code = "AEPRS",
					description = "Analysis error of pressure",
					units = "Pa")
			175 -> Grib1Parameter(id,
					code = "ICSEV",
					description = "Icing severity",
					units = "")
			176 -> Grib1Parameter(id,
					code = "ICPRB",
					description = "Icing probability",
					units = "")
			177 -> Grib1Parameter(id,
					code = "LAVNI",
					description = "Low-level aviation interest",
					units = "")
			178 -> Grib1Parameter(id,
					code = "HAVNI",
					description = "High-level aviation interest",
					units = "")
			179 -> Grib1Parameter(id,
					code = "FLGHT",
					description = "Flight Category",
					units = "")
			180 -> Grib1Parameter(id,
					code = "OZCON",
					description = "Ozone concentration",
					units = "PPB")
			181 -> Grib1Parameter(id,
					code = "OZCAT",
					description = "Categorical ozone concentration",
					units = "")
			182 -> Grib1Parameter(id,
					code = "VEDH",
					description = "Vertical eddy diffusivity heat exchange (Kh)",
					units = "m2/s")
			183 -> Grib1Parameter(id,
					code = "SIGV",
					description = "Sigma level value",
					units = "")
			184 -> Grib1Parameter(id,
					code = "EWGT",
					description = "Ensemble Weight",
					units = "")
			185 -> Grib1Parameter(id,
					code = "CICEL",
					description = "Confidence indicator - Ceiling",
					units = "")
			186 -> Grib1Parameter(id,
					code = "CIVIS",
					description = "Confidence indicator - Visibility",
					units = "")
			187 -> Grib1Parameter(id,
					code = "CIFLT",
					description = "Confidence indicator - Flight Category",
					units = "")
			188 -> Grib1Parameter(id,
					code = "LAVV",
					description = "Latitude of V wind component of velocity",
					units = "deg")
			189 -> Grib1Parameter(id,
					code = "LOVV",
					description = "Longitude of V wind component of velocity",
					units = "deg")
			190 -> Grib1Parameter(id,
					code = "USCT",
					description = "Scatterometer estimated U wind component",
					units = "m/s")
			191 -> Grib1Parameter(id,
					code = "VSCT",
					description = "Scatterometer estimated V wind component",
					units = "m/s")
			192 -> Grib1Parameter(id,
					code = "LAUV",
					description = "Latitude of U wind component of velocity",
					units = "deg")
			193 -> Grib1Parameter(id,
					code = "LOUV",
					description = "Longitude of U wind component of velocity",
					units = "deg")
			194 -> Grib1Parameter(id,
					code = "TCHP",
					description = "Tropical Cyclone Heat Potential",
					units = "J/m2K")
			195 -> Grib1Parameter(id,
					code = "DBSS",
					description = "Geometric Depth Below Sea Surface",
					units = "m")
			196 -> Grib1Parameter(id,
					code = "ODHA",
					description = "Ocean Dynamic Height Anomaly",
					units = "dynamic m")
			197 -> Grib1Parameter(id,
					code = "OHC",
					description = "Ocean Heat Content",
					units = "J/m2")
			198 -> Grib1Parameter(id,
					code = "SSHG",
					description = "Sea Surface Height Relative to Geoid",
					units = "m")
			199 -> Grib1Parameter(id,
					code = "SLTFL",
					description = "Salt Flux",
					units = "kg/m2s")
			200 -> Grib1Parameter(id,
					code = "DUVB",
					description = "UV-B downward solar flux",
					units = "W/m2")
			201 -> Grib1Parameter(id,
					code = "CDUVB",
					description = "Clear sky UV-B downward solar flux",
					units = "W/m2")
			202 -> Grib1Parameter(id,
					code = "THFLX",
					description = "Total downward heat flux at surface (downward is positive)",
					units = "W/m2")
			203 -> Grib1Parameter(id,
					code = "UVAR",
					description = "U velocity variance",
					units = "m2/s2")
			204 -> Grib1Parameter(id,
					code = "VVAR",
					description = "V velocity variance",
					units = "m2/s2")
			205 -> Grib1Parameter(id,
					code = "UVVCC",
					description = "UV Velocity Cross Correlation",
					units = "m2/s2")
			206 -> Grib1Parameter(id,
					code = "MCLS",
					description = "Meteorological Correlation Length Scale",
					units = "m")
			207 -> Grib1Parameter(id,
					code = "LAPP",
					description = "Latitude of pressure point",
					units = "deg")
			208 -> Grib1Parameter(id,
					code = "LOPP",
					description = "Longitude of pressure point",
					units = "deg")
			210 -> Grib1Parameter(id,
					code = "REFO",
					description = "Observed radar reflectivity",
					units = "dbZ")
			211 -> Grib1Parameter(id,
					code = "REFD",
					description = "Derived radar reflectivity",
					units = "dbZ")
			212 -> Grib1Parameter(id,
					code = "REFC",
					description = "Maximum/Composite radar reflectivity",
					units = "dbZ")
			213 -> Grib1Parameter(id,
					code = "SBT122",
					description = "Simulated Brightness Temperature for GOES12, Channel 2",
					units = "K")
			214 -> Grib1Parameter(id,
					code = "SBT123",
					description = "Simulated Brightness Temperature for GOES12, Channel 3",
					units = "K")
			215 -> Grib1Parameter(id,
					code = "SBT124",
					description = "Simulated Brightness Temperature for GOES12, Channel 4",
					units = "K")
			216 -> Grib1Parameter(id,
					code = "SBT126",
					description = "Simulated Brightness Temperature for GOES12, Channel 6",
					units = "K")
			217 -> Grib1Parameter(id,
					code = "MINRH",
					description = "Minimum Relative Humidity",
					units = "%")
			218 -> Grib1Parameter(id,
					code = "MAXRH",
					description = "Maximum Relative Humidity",
					units = "%")
			219 -> Grib1Parameter(id,
					code = "CEIL",
					description = "Ceiling",
					units = "m")
			220 -> Grib1Parameter(id,
					code = "PBLREG",
					description = "Planetary boundary layer Regime",
					units = "")
			221 -> Grib1Parameter(id,
					code = "SBC123",
					description = "Simulated Brightness Counts for GOES12, Channel 3",
					units = "Byte")
			222 -> Grib1Parameter(id,
					code = "SBC124",
					description = "Simulated Brightness Counts for GOES12, Channel 4",
					units = "Byte")
			223 -> Grib1Parameter(id,
					code = "RPRATE",
					description = "Rain Precipitation Rate",
					units = "kg/m2/s")
			224 -> Grib1Parameter(id,
					code = "SPRATE",
					description = "Snow Precipitation Rate",
					units = "kg/m2/s")
			225 -> Grib1Parameter(id,
					code = "FPRATE",
					description = "Freezing Rain Precipitation Rate",
					units = "kg/m2/s")
			226 -> Grib1Parameter(id,
					code = "IPRATE",
					description = "Ice Pellets Precipitation Rate",
					units = "kg/m2/s")
			227 -> Grib1Parameter(id,
					code = "UPHL",
					description = "Updraft Helicity",
					units = "m2/s2")
			228 -> Grib1Parameter(id,
					code = "SURGE",
					description = "Storm Surge",
					units = "m")
			229 -> Grib1Parameter(id,
					code = "ETSRG",
					description = "Extra Tropical Storm Surge",
					units = "m")
			230 -> Grib1Parameter(id,
					code = "RHPW",
					description = "Relative Humidity with Respect to Precipitable Water",
					units = "%")
			231 -> Grib1Parameter(id,
					code = "OZMAX1",
					description = "Ozone Daily Max from 1-hour Average",
					units = "ppbV")
			232 -> Grib1Parameter(id,
					code = "OZMAX8",
					description = "Ozone Daily Max from 8-hour Average",
					units = "ppbV")
			233 -> Grib1Parameter(id,
					code = "PDMAX1",
					description = "PM 2.5 Daily Max from 1-hour Average",
					units = "μg/m3")
			234 -> Grib1Parameter(id,
					code = "PDMX24",
					description = "PM 2.5 Daily Max from 24-hour Average",
					units = "μg/m3")
			235 -> Grib1Parameter(id,
					code = "MAXREF",
					description = "Hourly Maximum of Simulated Reflectivity at 1 km AGL",
					units = "dbZ")
			236 -> Grib1Parameter(id,
					code = "MXUPHL",
					description = "Hourly Maximum of Updraft Helicity over layer 2km to 5 km AGL",
					units = "m2/s2")
			237 -> Grib1Parameter(id,
					code = "MAXUVV",
					description = "Hourly Maximum of Upward Vertical Velocity in the lowest 400hPa",
					units = "m/s")
			238 -> Grib1Parameter(id,
					code = "MAXDVV",
					description = "Hourly Maximum of Downward Vertical Velocity in the lowest 400hPa",
					units = "m/s")
			239 -> Grib1Parameter(id,
					code = "MAXVIG",
					description = "Hourly Maximum of Column Vertical Integrated Graupel",
					units = "kg/m2")
			240 -> Grib1Parameter(id,
					code = "RETOP",
					description = "Radar Echo Top (18.3 DBZ)",
					units = "m")
			241 -> Grib1Parameter(id,
					code = "VRATE",
					description = "Ventilation Rate",
					units = "m2/s")
			242 -> Grib1Parameter(id,
					code = "TCSRG20",
					description = "20% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			243 -> Grib1Parameter(id,
					code = "TCSRG30",
					description = "30% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			244 -> Grib1Parameter(id,
					code = "TCSRG40",
					description = "40% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			245 -> Grib1Parameter(id,
					code = "TCSRG50",
					description = "50% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			246 -> Grib1Parameter(id,
					code = "TCSRG60",
					description = "60% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			247 -> Grib1Parameter(id,
					code = "TCSRG70",
					description = "70% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			248 -> Grib1Parameter(id,
					code = "TCSRG80",
					description = "80% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			249 -> Grib1Parameter(id,
					code = "TCSRG90",
					description = "90% Tropical Cyclone Storm Surge Exceedance",
					units = "m")
			250 -> Grib1Parameter(id,
					code = "HINDEX",
					description = "Haines Index",
					units = "")
			251 -> Grib1Parameter(id,
					code = "DIFTEN",
					description = "Difference Between 2 States In Total Energy Norm",
					units = "J/kg")
			252 -> Grib1Parameter(id,
					code = "PSPCP",
					description = "Pseudo-Precipitation",
					units = "kg/m2")
			253 -> Grib1Parameter(id,
					code = "MAXUW",
					description = "U Component of Hourly Maximum 10m Wind Speed",
					units = "m/s")
			254 -> Grib1Parameter(id,
					code = "MAXVW",
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
		return if (id > id) -1 else 1
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Grib1Parameter) return false
		if (this === other) return true
		if (code !== other.code) return false
		if (id != other.id) return false
		if (description !== other.description) return false
		return units === other.units
	}

	override fun hashCode(): Int {
		var result = id
		result = 31 * result + code.hashCode()
		result = 31 * result + description.hashCode()
		result = 31 * result + units.hashCode()
		return result
	}

	override fun toString(): String = "${id}:${code}:${description} [${units}]"
}
