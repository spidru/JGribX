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
package mt.edu.um.cf2.jgribx.grib1;

import mt.edu.um.cf2.jgribx.Logger;

/**
 * Title:        JGrib
 * Description:  Class which represents a parameter from a PDS parameter table
 * Copyright:    Copyright (c) 2002
 * Company:      U.S. Air Force
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */

public class Grib1Parameter {

	/**
	 * Parameter number [0 - 255]
	 */
   protected int number;
   
   /**
    * Parameter abbreviation
    */
   protected String code;
   
   /**
    * Parameter description
    */
   protected String description;

   /**
    * Parameter unit
    */
   protected String units;

   /**
    * Constuctor - Default
    */
   public Grib1Parameter() {
      this.number = 0;
      this.code = "";
      this.description = "";
      this.units = "";
   }

   /**
    * Constructor
    * @param aNum - Parameter number
 	* @param aName - Parameter name
 	* @param aDesc - Parameter description
 	* @param aUnit - Parameter unit
 	*/
   public Grib1Parameter(int aNum, String aName, String aDesc, String aUnit){
      this.number=aNum;
      this.code=aName;
      this.description=aDesc;
      this.units=aUnit;
   }

   /**
    * @return Parameter number
    */
   public int getNumber(){
      return number;
   }

   /**
    * @return Parameter abbreviation
    */
   public String getAbbreviation(){
      return code;
   }

   /**
    * @return Parameter description
    */
   public String getDescription(){
      return description;
   }

   /**
    * @return Parameter unit
    */
   public String getUnits(){
      return units;
   }

   
   /**
    * Overrides Object.toString()
    * 
    * @see java.lang.Object#toString()
    * @return String representation of the parameter
    */
   @Override
   public String toString(){
      return number + ":" + code + ":" + description + " [" + units +"]";
   }

   
   /**
    * Overrides Object.equals()
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    * @return true/false
    */
   public boolean equals(Object obj){
      if (!(obj instanceof Grib1Parameter))
         return false;

      if (this == obj)
         return true;

      Grib1Parameter param = (Grib1Parameter)obj;

      if (code != param.code) return false;
      if (number != param.number) return false;
      if (description != param.description) return false;
      if (units != param.units) return false;

      return true;
   }

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * @param param to compare
    * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
    *
    */
   public int compare(Grib1Parameter param){
      if (this.equals(param))
         return 0;

      // check if param is less than this
      // really only one thing to compare because parameter table sets info
      // compare tables in GribRecordPDS
      if (number > param.number) return -1;

      return 1;
   }
   
    /**************************************************************************/
    /* Static Methods */

    /**
     * 
     * @param version
     * @param paramId
     * @param centreId
     * @return 
     */
    public static Grib1Parameter getParameter(int version, int paramId, int centreId)
    {
        Grib1Parameter param;
        
        // As of Nov 2017, all Table Versions make use of Table 2 for Parameter IDs between 0 and 128
        if (paramId < 128)
        {
            param = getParameterFromTable2(paramId, centreId);
            return param;
        }
        
        switch(version)
        {
            case 2:
                param = getParameterFromTable2(paramId, centreId);
                break;
            case 128:
                param = getParameterFromTable128(paramId);
                break;
            case 129:
                param = getParameterFromTable129(paramId);
                break;
            default:
                param = null;
                break;
        }
        return param;
    }

    /**
     * Returns the parameter in Parameter Table 2 corresponding to the specified ID.
     * <br><br>
     * Reference: <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html">
     * http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html</a>
     * @param paramId
     * @return 
     */
    private static Grib1Parameter getParameterFromTable2(int paramId, int centreId)
    {
        Grib1Parameter param = new Grib1Parameter();
        switch(paramId)
        {
            case 1:
                param.code = "PRES";
                param.description = "Pressure";
                param.units = "Pa";
                break;
            case 2:
                param.code = "PRMSL";
                param.description = "Pressure reduced to MSL";
                param.units = "Pa";
                break;
            case 3:
                param.code = "PTEND";
                param.description = "Pressure tendency";
                param.units = "Pa/s";
                break;
            case 4:
                param.code = "PVORT";
                param.description = "Potential vorticity";
                param.units = "K m^2 kg^-1 s^-1";
                break;
            case 5:
                param.code = "ICAHT";
                param.description = "ICAO Standard Atmosphere Reference Height";
                param.units = "m";
                break;
            case 6:
                param.code = "GP";
                param.description = "Geopotential";
                param.units = "m^2/s^2";
                break;
            case 7:
                param.code = "HGT";
                param.description = "Geopotential height";
                param.units = "gpm";
                break;
            case 8:
                param.code = "DIST";
                param.description = "Geometric height";
                param.units = "m";
                break;
            case 9:
                param.code = "HSTDV";
                param.description = "Standard deviation of height";
                param.units = "m";
                break;
            case 10:
                param.code = "TOZNE";
                param.description = "Total ozone";
                param.units = "Dobson";
                break;
            case 11:
                param.code = "TMP";
                param.description = "Temperature";
                param.units = "K";
                break;
            case 12:
                param.code = "VTMP";
                param.description = "Virtual temperature";
                param.units = "K";
                break;
            case 13:
                param.code = "POT";
                param.description = "Potential temperature";
                param.units = "K";
                break;
            case 14:
                param.code = "EPOT";
                param.description = "Pseudo-adiabatic potential temperature (or equivalent potential temperature)";
                param.units = "K";
                break;
            case 15:
                param.code = "TMAX";
                param.description = "Maximum temperature";
                param.units = "K";
                break;
            case 16:
                param.code = "TMIN";
                param.description = "Minimum temperature";
                param.units = "K";
                break;
            case 17:
                param.code = "DPT";
                param.description = "Dew point temperature";
                param.units = "K";
                break;
            case 18:
                param.code = "DEPR";
                param.description = "Dew point depression (or deficit)";
                param.units = "K";
                break;
            case 19:
                param.code = "LAPR";
                param.description = "Lapse rate";
                param.units = "K/m";
                break;
            case 20:
                param.code = "VIS";
                param.description = "Visibility";
                param.units = "m";
                break;
            case 21:
                param.code = "RDSP1";
                param.description = "Radar Spectra (1)";
                param.units = "-";
                break;
            case 22:
                param.code = "RDSP2";
                param.description = "Radar Spectra (2)";
                param.units = "-";
                break;
            case 23:
                param.code = "RDSP3";
                param.description = "Radar Spectra (3)";
                param.units = "-";
                break;
            case 24:
                param.code = "PLI";
                param.description = "Parcel lifted index (to 500 hPa)";
                param.units = "K";
                break;
            case 25:
                param.code = "TMPA";
                param.description = "Temperature anomaly";
                param.units = "K";
                break;
            case 26:
                param.code = "PRESA";
                param.description = "Pressure anomaly";
                param.units = "Pa";
                break;
            case 27:
                param.code = "GPA";
                param.description = "Geopotential height anomaly";
                param.units = "gpm";
                break;
            case 28:
                param.code = "WVSP1";
                param.description = "Wave Spectra (1)";
                param.units = "-";
                break;
            case 29:
                param.code = "WVSP2";
                param.description = "Wave Spectra (2)";
                param.units = "-";
                break;
            case 30:
                param.code = "WVSP3";
                param.description = "Wave Spectra (3)";
                param.units = "-";
                break;
            case 31:
                param.code = "WDIR";
                param.description = "Wind direction (from which blowing)";
                param.units = "deg true";
                break;
            case 32:
                param.code = "WIND";
                param.description = "Wind speed";
                param.units = "m/s";
                break;
            case 33:
                param.code = "UGRD";
                param.description = "u-component of wind";
                param.units = "m/s";
                break;
            case 34:
                param.code = "VGRD";
                param.description = "v-component of wind";
                param.units = "m/s";
                break;
            case 35:
                param.code = "STRM";
                param.description = "Stream function";
                param.units = "m^2/s";
                break;
            case 36:
                param.code = "VPOT";
                param.description = "Velocity potential";
                param.units = "m^2/s";
                break;
            case 37:
                param.code = "MNTSF";
                param.description = "Montgomery stream function";
                param.units = "m2/s2";
                break;
            case 38:
                param.code = "SGCVV";
                param.description = "Sigma coordinate vertical velocity";
                param.units = "/s";
                break;
            case 39:
                param.code = "VVEL";
                param.description = "Vertical velocity (pressure)";
                param.units = "Pa/s";
                break;
            case 40:
                param.code = "DZDT";
                param.description = "Vertical velocity (geometric)";
                param.units = "m/s";
                break;
            case 41:
                param.code = "ABSV";
                param.description = "Absolute vorticity";
                param.units = "/s";
                break;
            case 42:
                param.code = "ABSD";
                param.description = "Absolute divergence";
                param.units = "/s";
                break;
            case 43:
                param.code = "RELV";
                param.description = "Relative vorticity";
                param.units = "/s";
                break;
            case 44:
                param.code = "RELD";
                param.description = "Relative divergence";
                param.units = "/s";
                break;
            case 45:
                param.code = "VUCSH";
                param.description = "Vertical u-component shear";
                param.units = "/s";
                break;
            case 46:
                param.code = "VVCSH";
                param.description = "Vertical v-component shear";
                param.units = "/s";
                break;
            case 47:
                param.code = "DIRC";
                param.description = "Direction of current";
                param.units = "Degree true";
                break;
            case 48:
                param.code = "SPC";
                param.description = "Speed of current";
                param.units = "m/s";
                break;
            case 49:
                param.code = "UOGRD";
                param.description = "u-component of current";
                param.units = "m/s";
                break;
            case 50:
                param.code = "VOGRD";
                param.description = "v-component of current";
                param.units = "m/s";
                break;
            case 51:
                param.code = "SPFH";
                param.description = "Specific humidity";
                param.units = "kg/kg";
                break;
            case 52:
                param.code = "RH";
                param.description = "Relative humidity";
                param.units = "%";
                break;
            case 53:
                param.code = "MIXR";
                param.description = "Humidity mixing ratio";
                param.units = "kg/kg";
                break;
            case 54:
                param.code = "PWAT";
                param.description = "Precipitable water";
                param.units = "kg/m^2";
                break;
            case 55:
                param.code = "VAPP";
                param.description = "Vapor pressure";
                param.units = "Pa";
                break;
            case 56:
                param.code = "SATD";
                param.description = "Saturation deficit";
                param.units = "Pa";
                break;
            case 57:
                param.code = "EVP";
                param.description = "Evaporation";
                param.units = "kg/m^2";
                break;
            case 58:
                param.code = "CICE";
                param.description = "Cloud Ice";
                param.units = "kg/m^2";
                break;
            case 59:
                param.code = "PRATE";
                param.description = "Precipitation rate";
                param.units = "kg/m^2/s";
                break;
            case 60:
                param.code = "TSTM";
                param.description = "Thunderstorm probability";
                param.units = "%";
                break;
            case 61:
                param.code = "APCP";
                param.description = "Total precipitation";
                param.units = "kg/m^2";
                break;
            case 62:
                param.code = "NCPCP";
                param.description = "Large scale precipitation (non-conv.)";
                param.units = "kg/m^2";
                break;
            case 63:
                param.code = "ACPCP";
                param.description = "Convective precipitation";
                param.units = "kg/m^2";
                break;
            case 64:
                param.code = "SRWEQ";
                param.description = "Snowfall rate water equivalent";
                param.units = "kg/m^2/s";
                break;
            case 65:
                param.code = "WEASD";
                param.description = "Water equiv. of accum. snow depth";
                param.units = "kg/m^2";
                break;
            case 66:
                param.code = "SNOD";
                param.description = "Snow depth";
                param.units = "m";
                break;
            case 67:
                param.code = "MIXHT";
                param.description = "Mixed layer depth";
                param.units = "m";
                break;
            case 68:
                param.code = "TTHDP";
                param.description = "Transient thermocline depth";
                param.units = "m";
                break;
            case 69:
                param.code = "MTHD";
                param.description = "Main thermocline depth";
                param.units = "m";
                break;
            case 70:
                param.code = "MTHA";
                param.description = "Main thermocline anomaly";
                param.units = "m";
                break;
            case 71:
                param.code = "TCDC";
                param.description = "Total cloud cover";
                param.units = "%";
                break;
            case 72:
                param.code = "CDCON";
                param.description = "Convective cloud cover";
                param.units = "%";
                break;
            case 73:
                param.code = "LCDC";
                param.description = "Low cloud cover";
                param.units = "%";
                break;
            case 74:
                param.code = "MCDC";
                param.description = "Medium cloud cover";
                param.units = "%";
                break;
            case 75:
                param.code = "HCDC";
                param.description = "High cloud cover";
                param.units = "%";
                break;
            case 76:
                param.code = "CWAT";
                param.description = "Cloud water";
                param.units = "kg/m^2";
                break;
            case 77:
                param.code = "BLI";
                param.description = "Best lifted index (to 500 hPa)";
                param.units = "K";
                break;
            case 78:
                param.code = "SNOC";
                param.description = "Convective snow";
                param.units = "kg/m^2";
                break;
            case 79:
                param.code = "SNOL";
                param.description = "Large scale snow";
                param.units = "kg/m^2";
                break;
            case 80:
                param.code = "WTMP";
                param.description = "Water Temperature";
                param.units = "K";
                break;
            case 81:
                param.code = "LAND";
                param.description = "Land cover (land=1, sea=0)";
                param.units = "proportion";
                break;
            case 82:
                param.code = "DSLM";
                param.description = "Deviation of sea level from mean";
                param.units = "m";
                break;
            case 83:
                param.code = "SFCR";
                param.description = "Surface roughness";
                param.units = "m";
                break;
            case 84:
                param.code = "ALBDO";
                param.description = "Albedo";
                param.units = "%";
                break;
            case 85:
                param.code = "TSOIL";
                param.description = "Soil temperature";
                param.units = "K";
                break;
            case 86:
                param.code = "SOILM";
                param.description = "Soil moisture content";
                param.units = "kg/m2";
                break;
            case 87:
                param.code = "VEG";
                param.description = "Vegetation";
                param.units = "%";
                break;
            case 88:
                param.code = "SALTY";
                param.description = "Salinity";
                param.units = "kg/kg";
                break;
            case 89:
                param.code = "DEN";
                param.description = "Density";
                param.units = "kg/m^3";
                break;
            case 90:
                param.code = "WATR";
                param.description = "Water runoff";
                param.units = "kg/m^2";
                break;
            case 91:
                param.code = "ICEC";
                param.description = "Ice cover (ice=1, no ice=0)";
                param.units = "proportion";
                break;
            case 92:
                param.code = "ICETK";
                param.description = "Ice thickness";
                param.units = "m";
                break;
            case 93:
                param.code = "DICED";
                param.description = "Direction of ice drift";
                param.units = "deg true";
                break;
            case 94:
                param.code = "SICED";
                param.description = "Speed of ice drift";
                param.units = "m/s";
                break;
            case 95:
                param.code = "UICE";
                param.description = "u-component of ice drift";
                param.units = "m/s";
                break;
            case 96:
                param.code = "VICE";
                param.description = "v-component of ice drift";
                param.units = "m/s";
                break;
            case 97:
                param.code = "ICEG";
                param.description = "Ice growth rate";
                param.units = "m/s";
                break;
            case 98:
                param.code = "ICED";
                param.description = "Ice divergence";
                param.units = "m/s";
                break;
            case 99:
                param.code = "SNOM";
                param.description = "Snow melt";
                param.units = "kg/m^2";
                break;
            case 100:
                param.code = "HTSGW";
                param.description = "Significant height of combined wind waves and swell";
                param.units = "m";
                break;
            case 101:
                param.code = "WVDIR";
                param.description = "Direction of wind waves (from which)";
                param.units = "Degree true";
                break;
            case 102:
                param.code = "WVHGT";
                param.description = "Significant height of wind waves";
                param.units = "m";
                break;
            case 103:
                param.code = "WVPER";
                param.description = "Mean period of wind waves";
                param.units = "s";
                break;
            case 104:
                param.code = "SWDIR";
                param.description = "Direction of swell waves";
                param.units = "Degree true";
                break;
            case 105:
                param.code = "SWELL";
                param.description = "Significant height of swell waves";
                param.units = "m";
                break;
            case 106:
                param.code = "SWPER";
                param.description = "Mean period of swell waves";
                param.units = "s";
                break;
            case 107:
                param.code = "DIRPW";
                param.description = "Primary wave direction";
                param.units = "Degree true";
                break;
            case 108:
                param.code = "PERPW";
                param.description = "Primary wave mean period";
                param.units = "s";
                break;
            case 109:
                param.code = "DIRSW";
                param.description = "Secondary wave direction";
                param.units = "Degree true";
                break;
            case 110:
                param.code = "PERSW";
                param.description = "Secondary wave mean period";
                param.units = "s";
                break;
            case 111:
                param.code = "NSWRS";
                param.description = "Net short-wave radiation flux (surface)";
                param.units = "W/m^2";
                break;
            case 112:
                param.code = "NLWRS";
                param.description = "Net long wave radiation flux (surface)";
                param.units = "W/m^2";
                break;
            case 113:
                param.code = "NSWRT";
                param.description = "Net short-wave radiation flux (top of atmosphere)";
                param.units = "W/m^2";
                break;
            case 114:
                param.code = "NLWRT";
                param.description = "Net long wave radiation flux (top of atmosphere)";
                param.units = "W/m^2";
                break;
            case 115:
                param.code = "LWAVR";
                param.description = "Long wave radiation flux";
                param.units = "W/m^2";
                break;
            case 116:
                param.code = "SWAVR";
                param.description = "Short wave radiation flux";
                param.units = "W/m^2";
                break;
            case 117:
                param.code = "GRAD";
                param.description = "Global radiation flux";
                param.units = "W/m^2";
                break;
            case 118:
                param.code = "BRTMP";
                param.description = "Brightness temperature";
                param.units = "K";
                break;
            case 119:
                param.code = "LWRAD";
                param.description = "Radiance (with respect to wave number)";
                param.units = "W/m/sr";
                break;
            case 120:
                param.code = "SWRAD";
                param.description = "Radiance (with respect to wave length)";
                param.units = "W/m^3/sr";
                break;
            case 121:
                param.code = "LHTFL";
                param.description = "Latent heat net flux";
                param.units = "W/m^2";
                break;
            case 122:
                param.code = "SHTFL";
                param.description = "Sensible heat net flux";
                param.units = "W/m^2";
                break;
            case 123:
                param.code = "BLYDP";
                param.description = "Boundary layer dissipation";
                param.units = "W/m^2";
                break;
            case 124:
                param.code = "UFLX";
                param.description = "Momentum flux, u component";
                param.units = "N/m^2";
                break;
            case 125:
                param.code = "VFLX";
                param.description = "Momentum flux, v component";
                param.units = "N/m^2";
                break;
            case 126:
                param.code = "WMIXE";
                param.description = "Wind mixing energy";
                param.units = "J";
                break;
            case 127:
                param.code = "IMGD";
                param.description = "Image data";
                param.units = "";
                break;
            default:
                switch(centreId)
                {
                    case 7:
                        param = getParameterFromTable2Centre7(paramId);
                        break;
                    default:
                        param = null;
                        break;
                }
                break;
        }
        return param;
    }
    
    private static Grib1Parameter getParameterFromTable2Centre7(int paramId)
    {
        Grib1Parameter param = new Grib1Parameter();
        switch(paramId)
        {
            case 128:
                param.code = "MSLSA";
                param.description = "Mean Sea Level Pressure (Standard Atmosphere Reduction)";
                param.units = "Pa";
                break;
            case 129:
                param.code = "MSLMA";
                param.description = "Mean Sea Level Pressure (MAPS System Reduction)";
                param.units = "Pa";
                break;
            case 130:
                param.code = "MSLET";
                param.description = "Mean Sea Level Pressure (NAM Model Reduction)";
                param.units = "Pa";
                break;
            case 131:
                param.code = "LFTX";
                param.description = "Surface lifted index";
                param.units = "K";
                break;
            case 132:
                param.code = "4LFTX";
                param.description = "Best (4 layer) lifted index";
                param.units = "K";
                break;
            case 133:
                param.code = "KX";
                param.description = "K index";
                param.units = "K";
                break;
            case 134:
                param.code = "SX";
                param.description = "Sweat index";
                param.units = "K";
                break;
            case 135:
                param.code = "MCONV";
                param.description = "Horizontal moisture divergence";
                param.units = "kg/kg/s";
                break;
            case 136:
                param.code = "VWSH";
                param.description = "Vertical speed shear";
                param.units = "1/s";
                break;
            case 137:
                param.code = "TSLSA";
                param.description = "3-hr pressure tendency Std. Atmos. Reduction";
                param.units = "Pa/s";
                break;
            case 138:
                param.code = "BVF2";
                param.description = "Brunt-Vaisala frequency (squared)";
                param.units = "1/s^2";
                break;
            case 139:
                param.code = "PVMW";
                param.description = "Potential vorticity (density weighted)";
                param.units = "1/s/m";
                break;
            case 140:
                param.code = "CRAIN";
                param.description = "Categorical rain (yes=1; no=0)";
                break;
            case 141:
                param.code = "CFRZR";
                param.description = "Categorical freezing rain (yes=1; no=0)";
                break;
            case 142:
                param.code = "CICEP";
                param.description = "Categorical ice pellets (yes=1; no=0)";
                break;
            case 143:
                param.code = "CSNOW";
                param.description = "Categorical snow (yes=1; no=0)";
                break;
            case 144:
                param.code = "SOILW";
                param.description = "Volumetric soil moisture content";
                param.units = "fraction";
                break;
            case 145:
                param.code = "PEVPR";
                param.description = "Potential evaporation rate";
                param.units = "W/m^2";
                break;
            case 146:
                param.code = "CWORK";
                param.description = "Cloud work function";
                param.units = "J/kg";
                break;
            case 147:
                param.code = "UGWD";
                param.description = "Zonal flux of gravity wave stress";
                param.units = "N/m^2";
                break;
            case 148:
                param.code = "VGWD";
                param.description = "Meridional flux of gravity wave stress";
                param.units = "N/m^2";
                break;
            case 149:
                param.code = "PVORT";
                param.description = "Potential vorticity";
                param.units = "m^2/s/kg";
                break;
            case 150:
                param.code = "COVMZ";
                param.description = "Covariance between meridional and zonal "
                        + "components of the wind";
                param.units = "m^2/s^2";
                break;
            case 151:
                param.code = "COVTZ";
                param.description = "Covariance between temperature and zonal components of"
                        + " the wind";
                param.units = "K*m/s";
                break;
            case 152:
                param.code = "COVTM";
                param.description = "Covariance between temperature and meridional "
                        + "components of the wind";
                param.units = "K*m/s";
                break;
            case 153:
                param.code = "CLWMR";
                param.description = "Cloud Mixing Ratio";
                param.units = "kg/kg";
                break;
            case 154:
                param.code = "O3MR";
                param.description = "Ozone mixing ratio";
                param.units = "kg/kg";
                break;
            case 155:
                param.code = "GFLUX";
                param.description = "Ground Heat Flux";
                param.units = "W/m^2";
                break;
            case 156:
                param.code = "CIN";
                param.description = "Convective inhibition";
                param.units = "J/kg";
                break;
            case 157:
                param.code = "CAPE";
                param.description = "Convective Available Potential Energy";
                param.units = "J/kg";
                break;
            case 158:
                param.code = "TKE";
                param.description = "Turbulent Kinetic Energy";
                param.units = "J/kg";
                break;
            case 159:
                param.code = "CONDP";
                param.description = "Condensation pressure of parcel lifted from "
                        + "indicated surface";
                param.units = "Pa";
                break;
            case 160:
                param.code = "CSUSF";
                param.description = "Clear Sky Upward Solar Flux";
                param.units = "W/m^2";
                break;
            case 161:
                param.code = "CSDSF";
                param.description = "Clear Sky Downward Solar Flux";
                param.units = "W/m^2";
                break;
            case 162:
                param.code = "CSULF";
                param.description = "Clear Sky upward long wave flux";
                param.units = "W/m^2";
                break;
            case 163:
                param.code = "CSDLF";
                param.description = "Clear Sky downward long wave flux";
                param.units = "W/m^2";
                break;
            case 164:
                param.code = "CFNSF";
                param.description = "Cloud forcing net solar flux";
                param.units = "W/m^2";
                break;
            case 165:
                param.code = "CFNLF";
                param.description = "Cloud forcing net long wave flux";
                param.units = "W/m^2";
                break;
            case 166:
                param.code = "VBDSF";
                param.description = "Visible beam downward solar flux";
                param.units = "W/m^2";
                break;
            case 167:
                param.code = "VDDSF";
                param.description = "Visible diffuse downward solar flux";
                param.units = "W/m^2";
                break;
            case 168:
                param.code = "NBDSF";
                param.description = "Near IR beam downward solar flux";
                param.units = "W/m^2";
                break;
            case 169:
                param.code = "NDDSF";
                param.description = "Near IR diffuse downward solar flux";
                param.units = "W/m^2";
                break;
            case 170:
                param.code = "RWMR";
                param.description = "Rain water mixing ratio";
                param.units = "kg/kg";
                break;
            case 171:
                param.code = "SNMR";
                param.description = "Snow mixing ratio";
                param.units = "kg/kg";
                break;
            case 172:
                param.code = "MFLX";
                param.description = "Horizontal Momentum flux";
                param.units = "N/m^2";
                break;
            case 173:
                param.code = "LMH";
                param.description = "Mass point model surface";
                break;
            case 174:
                param.code = "LMV";
                param.description = "Velocity point model surface";
                break;
            case 175:
                param.code = "MLYNO";
                param.description = "Model layer number (from bottom up)";
                break;
            case 176:
                param.code = "NLAT";
                param.description = "latitude (-90 to +90)";
                param.units = "deg";
                break;
            case 177:
                param.code = "ELON";
                param.description = "east longitude (0-360)";
                param.units = "deg";
                break;
            case 178:
                param.code = "ICMR";
                param.description = "Ice mixing ratio";
                param.units = "kg/kg";
                break;
            case 179:
                param.code = "GRMR";
                param.description = "Graupel mixing ratio";
                param.units = "kg/kg";
                break;
            case 180:
                param.code = "GUST";
                param.description = "Wind speed (gust)";
                param.units = "m/s";
                break;
            case 181:
                param.code = "LPSX";
                param.description = "x-gradient of log pressure";
                param.units = "1/m";
                break;
            case 182:
                param.code = "LPSY";
                param.description = "y-gradient of log pressure";
                param.units = "1/m";
                break;
            case 183:
                param.code = "HGTX";
                param.description = "x-gradient of height";
                param.units = "m/m";
                break;
            case 184:
                param.code = "HGTY";
                param.description = "y-gradient of height";
                param.units = "m/m";
                break;
            case 185:
                param.code = "TPFI";
                param.description = "Turbulence Potential Forecast Index";
                break;
            case 186:
                param.code = "TIPD";
                param.description = "Total Icing Potential Diagnostic";
                break;
            case 187:
                param.code = "LTNG";
                param.description = "Lightning";
                break;
            case 188:
                param.code = "RDRIP";
                param.description = "Rate of water dropping from canopy to ground";
                break;
            case 189:
                param.code = "VPTMP";
                param.description = "Virtual potential temperature";
                param.units = "K";
                break;                
            case 190:
                param.code = "HLCY";
                param.description = "Storm relative helicity";
                param.units = "m^2/s^2";
                break;
            case 191:
                param.code = "PROB";
                param.description = "Probability from ensemble";
                param.units = "%";
                break;
            case 192:
                param.code = "PROBN";
                param.description = "Probability from ensemble normalized with"
                        + "respect to climate expectancy";
                param.units = "%";
                break; 	 	
            case 193:
                param.code = "POP";
                param.description = "Probability of precipitation";
                param.units = "%";
                break; 	 	
            case 194:
                param.code = "CPOFP";
                param.description = "Percent of frozen precipitation";
                param.units = "%";
                break; 	 	
            case 195:
                param.code = "CPOZP";
                param.description = "Probability of freezing precipitation";
                param.units = "%";
                break; 	 	
            case 196:
                param.code = "USTM";
                param.description = "u-component of storm motion";
                param.units = "m/s";
                break;
            case 197:
                param.code = "VSTM";
                param.description = "v-component of storm motion";
                param.units = "m/s";
                break;
            case 198:
                param.code = "NCIP";
                param.description = "Number concentration for ice particles";
                break;
            case 199:
                param.code = "EVBS";
                param.description = "Direct evaporation from bare soil";
                param.units = "W/m^2";
                break;
            case 200:
                param.code = "EVCW";
                param.description = "Canopy water evaporation";
                param.units = "W/m2";
                break;
            case 201:
                param.code = "ICWAT";
                param.description = "Ice-free water surface";
                param.units = "%";
                break; 	 	
            case 202:
                param.code = "CWDI";
                param.description = "Convective weather detection index";
                break;
            case 203:
                param.code = "VAFTD";
                param.description = "VAFTAD";
                param.units = "log10(kg/m^3)";
                break; 	
            case 204:
                param.code = "DSWRF";
                param.description = "downward short wave rad. flux";
                param.units = "W/m^2";
                break;
            case 205:
                param.code = "DLWRF";
                param.description = "downward long wave rad. flux";
                param.units = "W/m^2";
                break;
            case 206:
                param.code = "UVI";
                param.description = "Ultra violet index (1 hour integration centered at solar noon)";
                param.units = "W/m^2";
                break;
            case 207:
                param.code = "MSTAV";
                param.description = "Moisture availability";
                param.units = "%";
                break;
            case 208:
                param.code = "SFEXC";
                param.description = "Exchange coefficient";
                param.units = "(kg/m^3)(m/s)";
                break;    
            case 209:
                param.code = "MIXLY";
                param.description = "No. of mixed layers next to surface";
                param.units = "";
                break;
            case 210:
                param.code = "TRANS";
                param.description = "Transpiration";
                param.units = "W/m^2";
                break;
            case 211:
                param.code = "USWRF";
                param.description = "upward short wave rad. flux";
                param.units = "W/m2";
                break;
            case 212:
                param.code = "ULWRF";
                param.description = "upward long wave rad. flux";
                param.units = "W/m^2";
                break;
            case 213:
                param.code = "CDLYR";
                param.description = "Amount of non-convective cloud";
                param.units = "%";
                break;	
            case 214:
                param.code = "CPRAT";
                param.description = "Convective Precipitation rate";
                param.units = "kg/m^2/s";
                break;
            case 215:
                param.code = "TTDIA";
                param.description = "Temperature tendency by all physics";
                param.units = "K/s";
                break;
            case 216:
                param.code = "TTRAD";
                param.description = "Temperature tendency by all radiation";
                param.units = "K/s";
                break;
            case 217:
                param.code = "TTPHY";
                param.description = "Temperature tendency by non-radiation physics";
                param.units = "K/s";
                break;
            case 218:
                param.code = "PREIX";
                param.description = "Precipitation index(0.0-1.00)";
                param.units = "fraction";
                break;
            case 219:
                param.code = "TSD1D";
                param.description = "Std. dev. of IR T over 1x1 deg area";
                param.units = "K";
                break;
            case 220:
                param.code = "NLGSP";
                param.description = "Natural log of surface pressure";
                param.units = "ln(kPa)";
                break;
            case 221:
                param.code = "HPBL";
                param.description = "Planetary boundary layer height";
                param.units = "m";
                break;
            case 222:
                param.code = "5WAVH";
                param.description = "5-wave geopotential height";
                param.units = "gpm";
                break;
            case 223:
                param.code = "CNWAT";
                param.description = "Plant canopy surface water";
                param.units = "kg/m^2";
                break;
            case 224:
                param.code = "SOTYP";
                param.description = "Soil type (as in Zobler)";
                param.units = "";
                break;	
            case 225:
                param.code = "VGTYP";
                param.description = "Vegitation type (as in SiB)";
                param.units = "";
                break; 	
            case 226:
                param.code = "BMIXL";
                param.description = "Blackadar's mixing length scale";
                param.units = "m";
                break;
            case 227:
                param.code = "AMIXL";
                param.description = "Asymptotic mixing length scale";
                param.units = "m";
                break;
            case 228:
                param.code = "PEVAP";
                param.description = "Potential evaporation";
                param.units = "kg/m^2";
                break;
            case 229:
                param.code = "SNOHF";
                param.description = "Snow phase-change heat flux";
                param.units = "W/m^2";
                break;
            case 230:
                param.code = "5WAVA";
                param.description = "5-wave geopotential height anomaly";
                param.units = "gpm";
                break;
            case 231:
                param.code = "MFLUX";
                param.description = "Convective cloud mass flux";
                param.units = "Pa/s";
                break;
            case 232:
                param.code = "DTRF";
                param.description = "Downward total radiation flux";
                param.units = "W/m^2";
                break;
            case 233:
                param.code = "UTRF";
                param.description = "Upward total radiation flux";
                param.units = "W/m^2";
                break;
            case 234:
                param.code = "BGRUN";
                param.description = "Baseflow-groundwater runoff";
                param.units = "kg/m^2";
                break;
            case 235:
                param.code = "SSRUN";
                param.description = "Storm surface runoff";
                param.units = "kg/m^2";
                break;
            case 236:
                param.code = "SIPD";
                param.description = "Supercooled Large Droplet (SLD) Icing Potential Diagnostic";
                break;
            case 237:
                param.code = "03TOT";
                param.description = "Total ozone";
                param.units = "kg/m^2";
                break;
            case 238:
                param.code = "SNOWC";
                param.description = "Snow cover";
                param.units = "%";
                break;
            case 239:
                param.code = "SNOT";
                param.description = "Snow temperature";
                param.units = "K";
                break;
            case 240:
                param.code = "COVTW";
                param.description = "Covariance between temperature and "
                        + "vertical component of the wind";
                param.units = "K*m/s";
                break;	 	
            case 241:
                param.code = "LRGHR";
                param.description = "Large scale condensate heat rate";
                param.units = "K/s";
                break;
            case 242:
                param.code = "CNVHR";
                param.description = "Deep convective heating rate";
                param.units = "K/s";
                break;
            case 243:
                param.code = "CNVMR";
                param.description = "Deep convective moistening rate";
                param.units = "kg/kg/s";
                break;
            case 244:
                param.code = "SHAHR";
                param.description = "Shallow convective heating rate";
                param.units = "K/s";
                break;
            case 245:
                param.code = "SHAMR";
                param.description = "Shallow convective moistening rate";
                param.units = "kg/kg/s";
                break;
            case 246:
                param.code = "VDFHR";
                param.description = "Vertical diffusion heating rate";
                param.units = "K/s";
                break;
            case 247:
                param.code = "VDFUA";
                param.description = "Vertical diffusion zonal acceleration";
                param.units = "m/s^2";
                break;
            case 248:
                param.code = "VDFVA";
                param.description = "Vertical diffusion meridional acceleration";
                param.units = "m/s^2";
                break;
            case 249:
                param.code = "VDFMR";
                param.description = "Vertical diffusion moistening rate";
                param.units = "kg/kg/s";
                break;
            case 250:
                param.code = "SWHR";
                param.description = "Solar radiative heating rate";
                param.units = "K/s";
                break;
            case 251:
                param.code = "LWHR";
                param.description = "Long wave radiative heating rate";
                param.units = "K/s";
                break;
            case 252:
                param.code = "CD";
                param.description = "Drag coefficient";
                break;
            case 253:
                param.code = "FRICV";
                param.description = "Friction velocity";
                param.units = "m/s";
                break;
            case 254:
                param.code = "RI";
                param.description = "Richardson number";
                break;
            default:
                param = null;
                break;
        }
        return param;
    }
    
    /**
     * Returns the parameter in Parameter Table 128 corresponding to the specified ID.
     * <br><br>
     * Reference: <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html#TABLE128">
     * http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html</a>
     * @param id
     * @return the parameter in Parameter Table 128 corresponding to the specified ID
     */
    private static Grib1Parameter getParameterFromTable128(int id)
    {
        Grib1Parameter param = new Grib1Parameter();
        switch(id)
        {
            case 128:
                param.code = "AVDEPTH";
                param.description = "Ocean depth - mean";
                param.units = "m";
                break;
            case 129:
                param.code = "DEPTH";
                param.description = "Ocean depth - instantaneous";
                param.units = "m";
                break;
            case 130:
                param.code = "ELEV";
                param.description = "Ocean surface elevation relative to geoid";
                param.units = "m";
                break;
            case 131:
                param.code = "MXEL24";
                param.description = "Max ocean surface elevation in last 24 hours";
                param.units = "m";
                break;
            case 132:
                param.code = "MNEL24";
                param.description = "Min ocean surface elevation in last 24 hours";
                param.units = "m";
                break;
            case 135:
                param.code = "O2";
                param.description = "Oxygen (O2 (aq))";
                param.units = "Mol/kg";
                break;
            case 136:
                param.code = "PO4";
                param.description = "PO4";
                param.units = "Mol/kg";
                break;
            case 137:
                param.code = "NO3";
                param.description = "NO3";
                param.units = "Mol/kg";
                break;
            case 138:
                param.code = "SIO4";
                param.description = "SiO4";
                param.units = "Mol/kg";
                break;
            case 139:
                param.code = "CO2AQ";
                param.description = "CO2 (aq)";
                param.units = "Mol/kg";
                break;
            case 140:
                param.code = "HCO3";
                param.description = "HCO3";
                param.units = "Mol/kg";
                break;
            case 141:
                param.code = "CO3";
                param.description = "CO3";
                param.units = "Mol/kg";
                break;
            case 142:
                param.code = "TCO2";
                param.description = "TCO2";
                param.units = "Mol/kg";
                break;
            case 143:
                param.code = "TALK";
                param.description = "TALK";
                param.units = "Mol/kg";
                break;
            case 144:
                param.code = "CH";
                param.description = "Heat Exchange Coefficient";
                param.units = "";
                break;
            case 146:
                param.code = "S11";
                param.description = "S11 - 1,1 component of ice stress tensor";
                param.units = "";
                break;
            case 147:
                param.code = "S12";
                param.description= "S12 - 1,2 component of ice stress tensor";
                param.units = "";
                break;
            case 148:
                param.code = "S22";
                param.description = "S22 - 2,2 component of ice stress tensor";
                param.units = "";
                break;
            case 149:
                param.code = "INV1";
                param.description = "T1 - First invariant of stress tensor";
                param.units = "";
                break;
            case 150:
                param.code = "INV2";
                param.description = "T2 - Second invariant of stress tensor";
                param.units = "";
                break;
            case 155:
                param.code = "WVRGH";
                param.description = "Wave roughness";
                param.units = "";
                break;
            case 156:
                param.code = "WVSTRS";
                param.description = "Wave stresses";
                param.units = "";
                break;
            case 157:
                param.code = "WHITE";
                param.description = "Whitecap coverage";
                param.units = "WHITE";
                break;
            case 158:
                param.code = "SWDIRWID";
                param.description = "Swell direction width";
                param.units = "";
                break;
            case 159:
                param.code = "SWFREWID";
                param.description = "Swell frequency width";
                param.units = "";
                break;
            case 160:
                param.code = "WVAGE";
                param.description = "Wave age";
                param.units = "";
                break;
            case 161:
                param.code = "PWVAGE";
                param.description = "Physical Wave";
                param.units = "age";
                break;
            case 165:
                param.code = "LTURB";
                param.description = "Master length scale (turbulence)";
                param.units = "m";
                break;
            case 170:
                param.code = "AIHFLX";
                param.description = "Net air-ice heat flux";
                param.units = "W/m2";
                break;	
            case 171:
                param.code = "AOHFLX";
                param.description = "Net air-ocean heat flux";
                param.units = "W/m2";
                break;
            case 172:
                param.code = "IOHFLX";
                param.description = "Net ice-ocean heat flux";
                param.units = "W/m2";
                break;
            case 173:
                param.code = "IOSFLX";
                param.description = "Net ice-ocean salt flux";
                param.units = "kg/s";
                break;
            case 175:
                param.code = "OMLT";
                param.description = "Ocean mixed layer temperature";
                param.units = "K";
                break;
            case 176:
                param.code = "OMLS";
                param.description = "Ocean mixed layer salinity";
                param.units = "kg/kg";
                break;
            case 177:
                param.code = "P2OMLT";
                param.description = "Ocean mixed layer potential density (Referenced to 2000m)";
                param.units = "kg/m3";
                break;
            case 178:
                param.code = "OMLU";
                param.description = "Ocean mixed layer u velocity";
                param.units = "m/s";
                break;
            case 179:
                param.code = "OMLV";
                param.description = "Ocean mixed layer v velocity";
                param.units = "m/s";
                break;
            case 180:
                param.code = "ASHFL";
                param.description = "Assimilative heat flux";
                param.units = "W/m2";
                break;
            case 181:
                param.code = "ASSFL";
                param.description = "Assimilative salt flux";
                param.units = "mm/day";
                break;
            case 182:
                param.code = "BOTLD";
                param.description = "Bottom layer depth";
                param.units = "m";
                break;
            case 183:
                param.code = "UBARO";
                param.description = "Barotropic U velocity";
                param.units = "m/s";
                break;
            case 184:
                param.code = "VBARO";
                param.description = "Barotropic V velocity";
                param.units = "m/s";
                break;
            case 185:
                param.code = "INTFD";
                param.description = "Interface depths";
                param.units = "m";
                break;
            case 186:
                param.code = "WTMPC";
                param.description = "3-D temperature";
                param.units = "deg c";
                break;
            case 187:
                param.code = "SALIN";
                param.description = "3-D Salinity";
                param.units = "psu";
                break;
            case 188:
                param.code = "EMNP";
                param.description = "Evaporation - precipitation";
                param.units = "cm/day";
                break;
            case 190:
                param.code = "KENG";
                param.description = "Kinetic energy";
                param.units = "J/kg";
                break;
            case 191:
                param.code = "BKENG";
                param.description = "Barotropic Kinetic energy";
                param.units = "J/kg";
                break;
            case 192:
                param.code = "LAYTH";
                param.description = "Layer Thickness";
                param.units = "m";
                break;
            case 193:
                param.code = "SSTT";
                param.description = "Surface temperature trend";
                param.units = "deg/day";
                break;
            case 194:
                param.code = "SSST";
                param.description = "Surface salinity trend";
                param.units = "psu/day";
                break;
            case 195:
                param.code = "OVHD";
                param.description = "Ocean Vertical Heat Diffusivity";
                param.units = "m2s-1";
                break;
            case 196:
                param.code = "OVSD";
                param.description = "Ocean Vertical Salt Diffusivity";
                param.units = "m2s-1";
                break;
            case 197:
                param.code = "OVMD";
                param.description = "Ocean Vertical Momentum Diffusivity";
                param.units = "m2s-1";
                break;
            case 254:
                param.code = "REV";
                param.description = "Relative error variance";
                param.units = "";
                break;
            default:
                param = null;
                break;
        }
        return param;
    }
    
    /**
     * Returns the parameter in Parameter Table 129 corresponding to the specified ID.
     * <br><br>
     * Reference: <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html#TABLE129">
     * http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html</a>
     * @param id
     * @return the parameter in Parameter Table 129 corresponding to the specified ID
     */    
    private static Grib1Parameter getParameterFromTable129(int id)
    {
        Grib1Parameter param = new Grib1Parameter();
        switch(id)
        {
            case 128:
                param.code = "PAOT";
                param.description = "Probability anomaly of temperature";
                param.units = "%";
                break;
            case 129:
                param.code = "PAOP";
                param.description = "Probability anomaly of precipitation";
                param.units = "%";
                break;
            case 130:
                param.code = "CWR";
                param.description = "Probability of Wetting Rain, exceeding 0.10 in a given time period";
                param.units = "%";
                break;	
            case 131:
                param.code = "FRAIN";
                param.description = "Rain fraction of total liquid water";
                param.units = "";
                break;
            case 132:
                param.code = "FICE";
                param.description = "Ice fraction of total condensate";
                param.units = "";
                break;
            case 133:
                param.code = "RIME";
                param.description = "Rime Factor";
                param.units = "";
                break;
            case 134:
                param.code = "CUEFI";
                param.description = "Convective cloud efficiency";
                param.units = "";
                break;
            case 135:
                param.code = "TCOND";
                param.description = "Total condensate";
                param.units = "kg/kg";
                break;
            case 136:
                param.code = "TCOLW";
                param.description = "Total column-integrated cloud water";
                param.units = "kg/m2";
                break;
            case 137:
                param.code = "TCOLI";
                param.description = "Total column-integrated cloud ice";
                param.units = "kg/m2";
                break;
            case 138:
                param.code = "TCOLR";
                param.description = "Total column-integrated rain";
                param.units = "kg/m2";
                break;
            case 139:
                param.code = "TCOLS";
                param.description = "Total column-integrated snow";
                param.units = "kg/m2";
                break;
            case 140:
                param.code = "TCOLC";
                param.description = "Total column-integrated condensate";
                param.units = "kg/m2";
                break;
            case 141:
                param.code = "PLPL";
                param.description = "Pressure of level from which parcel was lifted";
                param.units = "Pa";
                break;
            case 142:
                param.code = "HLPL";
                param.description = "Height of level from which parcel was lifted";
                param.units = "m";
                break;
            case 143:
                param.code = "CEMS";
                param.description = "Cloud Emissivity";
                param.units = "Fraction 0-1";
                break;
            case 144:
                param.code = "COPD";
                param.description = "Cloud Optical Depth";
                param.units = "";
                break;
            case 145:
                param.code = "PSIZ";
                param.description = "Effective Particle size";
                param.units = "Microns";
                break;
            case 146:
                param.code = "TCWAT";
                param.description = "Total Water Cloud";
                param.units = "%";
                break;	
            case 147:
                param.code = "TCICE";
                param.description = "Total Ice Cloud";
                param.units = "%";
                break;
            case 148:
                param.code = "WDIF";
                param.description = "Wind difference";
                param.units = "m/s";
                break;
            case 149:
                param.code = "WSTP";
                param.description = "Wave Steepness";
                param.units = "";
                break;
            case 150:
                param.code = "PTAN";
                param.description = "Probability of Temperature being above normal";
                param.units = "%";
                break;
            case 151:
                param.code = "PTNN";
                param.description = "Probability of Temperature being near normal";
                param.units = "%";
                break;
            case 152:
                param.code = "PTBN";
                param.description = "Probability of Temperature being below normal";
                param.units = "%";
                break;
            case 153:
                param.code = "PPAN";
                param.description = "Probability of Precipitation being above normal";
                param.units = "%";
                break;
            case 154:
                param.code = "PPNN";
                param.description = "Probability of Precipitation being near normal";
                param.units = "%";
                break;
            case 155:
                param.code = "PPBN";
                param.description = "Probability of Precipitation being below normal";
                param.units = "%";
                break;
            case 156:
                param.code = "PMTC";
                param.description = "Particulate matter (coarse)";
                param.units = "µg/m3";
                break;
            case 157:
                param.code = "PMTF";
                param.description = "Particulate matter (fine)";
                param.units = "µg/m3";
                break;
            case 158:
                param.code = "AETMP";
                param.description = "Analysis error of temperature";
                param.units = "K";
                break;
            case 159:
                param.code = "AEDPT";
                param.description = "Analysis error of dew point";
                param.units = "%";
                break;
            case 160:
                param.code = "AESPH";
                param.description = "Analysis error of specific humidity";
                param.units = "kg/kg";
                break;
            case 161:
                param.code = "AEUWD";
                param.description = "Analysis error of u-wind";
                param.units = "m/s";
                break;
            case 162:
                param.code = "AEVWD";
                param.description = "Analysis error of v-wind";
                param.units = "m/s";
                break;	
            case 163:
                param.code = "LPMTF";
                param.description = "Particulate matter (fine)";
                param.units = "log10(µg/m3)";
                break;
            case 164:
                param.code = "LIPMF";
                param.description = "Integrated column particulate matter (fine)";
                param.units = "log10(µg/m3)";
                break;
            case 165:
                param.code = "REFZR";
                param.description = "Derived radar reflectivity backscatter from rain";
                param.units = "mm6/m3";
                break;
            case 166:
                param.code = "REFZI";
                param.description = "Derived radar reflectivity backscatter from ice";
                param.units = "mm6/m3";
                break;
            case 167:
                param.code = "REFZC";
                param.description = "Derived radar reflectivity backscatter from parameterized convection";
                param.units = "mm6/m3";
                break;
            case 168:
                param.code = "TCLSW";
                param.description = "Total column-integrated supercooled liquid water";
                param.units = "kg/m2";
                break;
            case 169:
                param.code = "TCOLM";
                param.description = "Total column-integrated melting ice";
                param.units = "kg/m2";
                break;
            case 170:
                param.code = "ELRDI";
                param.description = "Ellrod Index";
                param.units = "";
                break;
            case 171:
                param.code = "TSEC";
                param.description = "Seconds prior to initial reference time";
                param.units = "sec";
                break; 
            case 172:
                param.code = "TSECA";
                param.description = "Seconds after initial reference time";
                param.units = "sec";
                break;
            case 173:
                param.code = "NUM";
                param.description = "Number of samples/observations";
                param.units = "";
                break;	
            case 174:
                param.code = "AEPRS";
                param.description = "Analysis error of pressure";
                param.units = "Pa";
                break;
            case 175:
                param.code = "ICSEV";
                param.description = "Icing severity";
                param.units = "";
                break;
            case 176:
                param.code = "ICPRB";
                param.description = "Icing probability";
                param.units = "";
                break;
            case 177:
                param.code = "LAVNI";
                param.description = "Low-level aviation interest";
                param.units = "";
                break;
            case 178:
                param.code = "HAVNI";
                param.description = "High-level aviation interest";
                param.units = "";
                break;
            case 179:
                param.code = "FLGHT";
                param.description = "Flight Category";
                param.units = "";
                break;
            case 180:
                param.code = "OZCON";
                param.description = "Ozone concentration";
                param.units = "PPB";
                break;
            case 181:
                param.code = "OZCAT";
                param.description = "Categorical ozone concentration";
                param.units = "";
                break;
            case 182:
                param.code = "VEDH";
                param.description = "Vertical eddy diffusivity heat exchange (Kh)";
                param.units = "m2/s";
                break;
            case 183:
                param.code = "SIGV";
                param.description = "Sigma level value";
                param.units = "";
                break;
            case 184:
                param.code = "EWGT";
                param.description = "Ensemble Weight";
                param.units = "";
                break;
            case 185:
                param.code = "CICEL";
                param.description = "Confidence indicator - Ceiling";
                param.units = "";
                break;
            case 186:
                param.code = "CIVIS";
                param.description = "Confidence indicator - Visibility";
                param.units = "";
                break;
            case 187:
                param.code = "CIFLT";
                param.description = "Confidence indicator - Flight Category";
                param.units = "";
                break;
            case 188:
                param.code = "LAVV";
                param.description = "Latitude of V wind component of velocity";
                param.units = "deg";
                break;
            case 189:
                param.code = "LOVV";
                param.description = "Longitude of V wind component of velocity";
                param.units = "deg";
                break;
            case 190:
                param.code = "USCT";
                param.description = "Scatterometer estimated U wind component";
                param.units = "m/s";
                break;
            case 191:
                param.code = "VSCT";
                param.description = "Scatterometer estimated V wind component";
                param.units = "m/s";
                break;
            case 192:
                param.code = "LAUV";
                param.description = "Latitude of U wind component of velocity";
                param.units = "deg";
                break;
            case 193:
                param.code = "LOUV";
                param.description = "Longitude of U wind component of velocity";
                param.units = "deg";
                break;
            case 194:
                param.code = "TCHP";
                param.description = "Tropical Cyclone Heat Potential";
                param.units = "J/m2K";
                break;
            case 195:
                param.code = "DBSS";
                param.description = "Geometric Depth Below Sea Surface";
                param.units = "m";
                break;
            case 196:
                param.code = "ODHA";
                param.description = "Ocean Dynamic Height Anomaly";
                param.units = "dynamic m";
                break;
            case 197:
                param.code = "OHC";
                param.description = "Ocean Heat Content";
                param.units = "J/m2";
                break;
            case 198:
                param.code = "SSHG";
                param.description = "Sea Surface Height Relative to Geoid";
                param.units = "m";
                break;
            case 199:
                param.code = "SLTFL";
                param.description = "Salt Flux";
                param.units = "kg/m2s";
                break;
            case 200:
                param.code = "DUVB";
                param.description = "UV-B downward solar flux";
                param.units = "W/m2";
                break;
            case 201:
                param.code = "CDUVB";
                param.description = "Clear sky UV-B downward solar flux";
                param.units = "W/m2";
                break;
            case 202:
                param.code = "THFLX";
                param.description = "Total downward heat flux at surface (downward is positive)";
                param.units = "W/m2";
                break;
            case 203:
                param.code = "UVAR";
                param.description = "U velocity variance";
                param.units = "m2/s2";
                break;
            case 204:
                param.code = "VVAR";
                param.description = "V velocity variance";
                param.units = "m2/s2";
                break;
            case 205:
                param.code = "UVVCC";
                param.description = "UV Velocity Cross Correlation";
                param.units = "m2/s2";
                break;
            case 206:
                param.code = "MCLS";
                param.description = "Meteorological Correlation Length Scale";
                param.units = "m";
                break;
            case 207:
                param.code = "LAPP";
                param.description = "Latitude of pressure point";
                param.units = "deg";
                break;
            case 208:
                param.code = "LOPP";
                param.description = "Longitude of pressure point";
                param.units = "deg";
                break;
            case 210:
                param.code = "REFO";
                param.description = "Observed radar reflectivity";
                param.units = "dbZ";
                break;    
            case 211:
                param.code = "REFD";
                param.description = "Derived radar reflectivity";
                param.units = "dbZ";
                break;
            case 212:
                param.code = "REFC";
                param.description = "Maximum/Composite radar reflectivity";
                param.units = "dbZ";
                break;
            case 213:
                param.code = "SBT122";
                param.description = "Simulated Brightness Temperature for GOES12, Channel 2";
                param.units = "K";
                break;
            case 214:
                param.code = "SBT123";
                param.description = "Simulated Brightness Temperature for GOES12, Channel 3";
                param.units = "K";
                break;
            case 215:
                param.code = "SBT124";
                param.description = "Simulated Brightness Temperature for GOES12, Channel 4";
                param.units = "K";
                break;
            case 216:
                param.code = "SBT126";
                param.description = "Simulated Brightness Temperature for GOES12, Channel 6";
                param.units = "K";
                break;
            case 217:
                param.code = "MINRH";
                param.description = "Minimum Relative Humidity";
                param.units = "%";
                break;
            case 218:
                param.code = "MAXRH";
                param.description = "Maximum Relative Humidity";
                param.units = "%";
                break;
            case 219:
                param.code = "CEIL";
                param.description = "Ceiling";
                param.units = "m";
                break;
            case 220:
                param.code = "PBLREG";
                param.description = "Planetary boundary layer Regime";
                param.units = "";
                break;
            case 221:
                param.code = "SBC123";
                param.description = "Simulated Brightness Counts for GOES12, Channel 3";
                param.units = "Byte";
                break;
            case 222:
                param.code = "SBC124";
                param.description = "Simulated Brightness Counts for GOES12, Channel 4";
                param.units = "Byte";
                break;
            case 223:
                param.code = "RPRATE";
                param.description = "Rain Precipitation Rate";
                param.units = "kg/m2/s";
                break;
            case 224:
                param.code = "SPRATE";
                param.description = "Snow Precipitation Rate";
                param.units = "kg/m2/s";
                break;
            case 225:
                param.code = "FPRATE";
                param.description = "Freezing Rain Precipitation Rate";
                param.units = "kg/m2/s";
                break;
            case 226:
                param.code = "IPRATE";
                param.description = "Ice Pellets Precipitation Rate";
                param.units = "kg/m2/s";
                break;
            case 227:
                param.code = "UPHL";
                param.description = "Updraft Helicity";
                param.units = "m2/s2";
                break;
            case 228:
                param.code = "SURGE";
                param.description = "Storm Surge";
                param.units = "m";
                break;
            case 229:
                param.code = "ETSRG";
                param.description = "Extra Tropical Storm Surge";
                param.units = "m";
                break;
            case 230:
                param.code = "RHPW";
                param.description = "Relative Humidity with Respect to Precipitable Water";
                param.units = "%";
                break;
            case 231:
                param.code = "OZMAX1";
                param.description = "Ozone Daily Max from 1-hour Average";
                param.units = "ppbV";
                break;
            case 232:
                param.code = "OZMAX8";
                param.description = "Ozone Daily Max from 8-hour Average";
                param.units = "ppbV";
                break;
            case 233:
                param.code = "PDMAX1";
                param.description = "PM 2.5 Daily Max from 1-hour Average";
                param.units = "μg/m3";
                break;
            case 234:
                param.code = "PDMX24";
                param.description = "PM 2.5 Daily Max from 24-hour Average";
                param.units = "μg/m3";
                break;    
            case 235:
                param.code = "MAXREF";
                param.description = "Hourly Maximum of Simulated Reflectivity at 1 km AGL";
                param.units = "dbZ";
                break;
            case 236:
                param.code = "MXUPHL";
                param.description = "Hourly Maximum of Updraft Helicity over layer 2km to 5 km AGL";
                param.units = "m2/s2";
                break;
            case 237:
                param.code = "MAXUVV";
                param.description = "Hourly Maximum of Upward Vertical Velocity in the lowest 400hPa";
                param.units = "m/s";
                break;
            case 238:
                param.code = "MAXDVV";
                param.description = "Hourly Maximum of Downward Vertical Velocity in the lowest 400hPa";
                param.units = "m/s";
                break;
            case 239:
                param.code = "MAXVIG";
                param.description = "Hourly Maximum of Column Vertical Integrated Graupel";
                param.units = "kg/m2";
                break;
            case 240:
                param.code = "RETOP";
                param.description = "Radar Echo Top (18.3 DBZ)";
                param.units = "m";
                break;
            case 241:
                param.code = "VRATE";
                param.description = "Ventilation Rate";
                param.units = "m2/s";
                break;
            case 242:
                param.code = "TCSRG20";
                param.description = "20% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 243:
                param.code = "TCSRG30";
                param.description = "30% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 244:
                param.code = "TCSRG40";
                param.description = "40% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 245:
                param.code = "TCSRG50";
                param.description = "50% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 246:
                param.code = "TCSRG60";
                param.description = "60% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 247:
                param.code = "TCSRG70";
                param.description = "70% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 248:
                param.code = "TCSRG80";
                param.description = "80% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 249:
                param.code = "TCSRG90";
                param.description = "90% Tropical Cyclone Storm Surge Exceedance";
                param.units = "m";
                break;
            case 250:
                param.code = "HINDEX";
                param.description = "Haines Index";
                param.units = "";
                break;
            case 251:
                param.code = "DIFTEN";
                param.description = "Difference Between 2 States In Total Energy Norm";
                param.units = "J/kg";
                break;
            case 252:
                param.code = "PSPCP";
                param.description = "Pseudo-Precipitation";
                param.units = "kg/m2";
                break;    
            case 253:
                param.code = "MAXUW";
                param.description = "U Component of Hourly Maximum 10m Wind Speed";
                param.units = "m/s";
                break;
            case 254:
                param.code = "MAXVW";
                param.description = "V Component of Hourly Maximum 10m Wind Speed";
                param.units = "m/s";
                break;
            default:
                param = null;
                break;
        }
        return param;
    }
}