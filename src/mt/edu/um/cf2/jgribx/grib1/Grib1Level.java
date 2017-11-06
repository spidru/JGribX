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

/*
 * Reference: http://nimbus.com.uy/weather/grads/grib_levels.html
*/
package mt.edu.um.cf2.jgribx.grib1;

import mt.edu.um.cf2.jgribx.Logger;

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.<p/>
 * 
 * <b>2006-07-26 frv_peg: Added NCEP extention levels for use of GFS files.</b><p/>
 * (level: 117, 211,212,213,222,223,232,233,242,243,244)</p>
 * 
 * See: 
 * <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html">table3.html</a> 
 */

public class Grib1Level
{
    /**
    * Index number from table 3 - can be used for comparison even if the
    * description of the level changes
    */
    private int index;

   /**
    * Name of the vertical coordinate/level
    *
    */
   private String name=null;

   /**
    * Value of PDS octet10 if separate from 11, otherwise value from octet10&11
    */
   private float value1 = Float.NaN;

   /**
    * Value of PDS octet11
    */
   private float value2 = Float.NaN;

   /**
    * Stores a short name of the level - same as the string "level" in the original
    * GribRecordPDS implementation
    */
   private String code = "";

   /**
    * Stores a descriptive name of the level
    * GribRecordPDS implementation
    */
   private String description = "";

   /**
    * Stores the name of the level - same as the string "level" in the original
    * GribRecordPDS implementation
    */
   private String units = "";

   /**
    * Stores whether this is (usually) a vertical coordinate for a single layer
    *  (e.g. surface, tropopause level) or multiple layers (e.g. hPa, m AGL)
    * Aids in deciding whether to build 2D or 3D grids from the data
    */
   private boolean isSingleLayer = true;

   /**
    * Indicates whether the vertical coordinate increases with height.
    * e.g. false for pressure and sigma, true for height above ground or if unknown
    */
   private boolean isIncreasingUp = true;

   /**
    * True if a numeric values are used for this level (e.g. 1000 mb)
    * False if level doesn't use values (e.g. surface).
    * Basically indicates whether you will be able to get a value for this level.
    */
   private boolean isNumeric=false;
    
   /**
    * Constructor.  Creates a GribPDSLevel based on octets 10-12 of the PDS.
    * Implements tables 3 and 3a.
    *
     * @param levelType
     * @param levelData
    */
    public Grib1Level(int levelType, int levelData)
    {
        int v1 = (levelData & 0xFF00) >> 8;
        int v2 = levelData & 0xFF;
        this.index = levelType;
        switch (index)
        {
            case 1:
                code = "SFC";
                name = description = "ground or water surface";
                break;
            case 2:
                code = "CBL";
                name = description = "cloud base level";
                break;
            case 3:
                name = description = "level of cloud tops";
                code = "CTL";
                break;
            case 4:
                code = "0DEG";
                name = description = "level of 0 degC isotherm";
                break;
            case 5:
                code = "ADCL";
                name = description = "level of adiabatic condensation lifted from the surface";
              break;
            case 6:
                code = "MWSL";
                name = description = "maximum wind level";
                break;
            case 7:
                code = "TRO";
                name = description = "tropopause";
                break;
            case 8:
                code = "NTAT";
                name = description = "nominal top of atmosphere";
                break;
            case 9:
                code = "SEAB";
                name = description = "sea bottom";
              break;
           case 20:
                code = "TMPL"; 
                name = "isothermal level";
                value1 = levelData;
                units = "K";
                isNumeric = true;
                description = "Isothermal level at " + value1/100 + units;
                break;
            case 100:
                code = "ISBL";
                name = "P";
                value1 = levelData;
                units = "hPa";
                isNumeric = true;
                isIncreasingUp = false;
                isSingleLayer = false;              
                description = "pressure at " + value1 + " " + units;
                break;
            case 101:
                code = "ISBY";
                name = "layer between two isobaric levels";
                value1 = ((levelData & 0xFF00) >> 8) *10; // convert from kPa to hPa - who uses kPa???
                value2 = v2*10;
                units = "hPa";
                description = "layer between " + value1 + " and " + value2 + " " + units;
                break;
            case 102:
                code = "MSL";
                name = description = "mean sea level";
                break;
            case 103:
                code = "GPML";
                name = "specified altitude above MSL";
                value1 = levelData;
                units = "m";
                isNumeric = true;
                isSingleLayer = false;
                description = value1 + " m above mean sea level";
                break;
            case 104:
                code = "GPMY";
                name = "layer between two specified altitudes above MSL";
                value1 = (v1 * 100); // convert hm to m
                value2 = (v2 * 100);
                units = "m";
                code =  value1 + "-" + value2 + " " + units;
                description = "Layer between " + v1 + " and " +
                                v2 + " m above mean sea level";
                break;
            case 105:
                code = "TGL";
                name = "specified height level above ground";
                value1 = levelData;
                units = "m";
                isNumeric = true;
                isSingleLayer = false;
                description = value1 + " m above ground";
                break;
            case 106:
                code = "HTGY";
                name = "layer between two specified height levels above ground";
                value1 = (v1 * 100); // convert hm to m
                value2 = (v2 * 100);
                units = "m";
                isNumeric = true;
                description = "Layer between " + value1 + " and " +
                              value2 + " " + units + " above ground";
                break;
            case 107:
                code = "SIGL";
                name = "Sigma level";
                value1 = (levelData/10000.0f);
                units = "sigma";
                isNumeric = true;
                isSingleLayer = false;
                isIncreasingUp = false;
                description = "sigma = " + value1;
                break;
            case 108:
                code = "SIGY";
                name = "Layer between two sigma layers";
                value1 = (v1 / 100.0f);
                value2 = (v2 / 100.0f);
                isNumeric = true;
                description = "Layer between sigma levels " + value1 + " and " + value2;
                break;
            case 109:
                code = "HYBL";
                name = "hybrid level";
                value1 = levelData;
                isNumeric = true;
                description = "hybrid level " + value1;
                break;
            case 110:
                code = "HYBY";
                name = "Layer between two hybrid levels";
                value1 = v1;
                value2 = v2;
                isNumeric = true;
                description = "Layer between hybrid levels " + value1 + " and " + value2;
                break;
           case 111:
              name = "Depth below land surface";
              value1 = levelData;
              units = "cm";
              isNumeric = true;
              code = value1 + " " + units;
              description = value1 + " " + units;
              break;
           case 112:
              name = "Layer between two levels below land surface";
              value1 = v1;
              value2 = v2;
              units = "cm";
              isNumeric = true;
              code = value1 + " - " + value2 + " " + units;
              description = "Layer between " + value1 + " and " + value2 +
                            " cm below land surface";
              break;
           case 113:
              name = "Isentropic (theta) level";
              value1 = levelData;
              units = "K";
              isNumeric = true;
              isSingleLayer = false;
              code = value1 + " K";
              description = value1 + " K";
              break;
          case 114:
              name = "Layer between two isentropic layers";
              value1 = (v1+475);
              value2 = (v2+475);
              units = "K";
              isNumeric = true;
              description = "Layer between " + value1 + " and " + value2 + " K";
              break;
          case 116:
              name = "Layer between pressure differences from ground to levels";
              value1 = v1;
              value2 = v2;
              units = "hPa";
              isNumeric = true;
              code = value1 + units + " - " + value2 + units;
              description = "Layer between pressure differences from ground: " +
                             value1 + " and " + value2 + " K";
              break;
          case 117:
                  // frv_peg - 2006-07-26 values from
                  // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                  name = "potential vorticity(pv) surface";
                  value1 = levelData;
                  units = "10^9 km^2/kgs";
                  isNumeric = false;
                  code = "surface";
                  description = name;
                  break;
           case 125:
              name = "Height above ground (high precision)";
              value1 = levelData;
              units = "cm";
              isNumeric = true;
              isSingleLayer = false;
              code = value1 + " " + units;
              description = value1 + " " + units + " above ground";
              break;
           case 160:
              name = "Depth below sea level";
              value1 = levelData;
              units = "m";
              isNumeric = true;
              code = value1 + " m below sea level";
              description = levelData + " m below sea level";
              break;
           case 200:
              name = description = code = "entire atmosphere layer";
              break;
           case 201:
              name = description = code = "entire ocean layer";
              break;
           case 204:
              name = description = code = "Highest tropospheric freezing level";
              break;
           case 211:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Boundary layer cloud layer (BCY)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 212:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Low cloud bottom level (LCBL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 213:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Low cloud top level (LCTL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;            
           case 214:
              name = description = code = "Low Cloud Layer";
              break;
           case 222:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Middle cloud bottom level (MCBL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 223:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Middle cloud top level (MCTL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;            
           case 224:
              name = description = code = "Middle Cloud Layer";
              break;
           case 232:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "High cloud bottom level (HCBL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 233:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "High cloud top level (HCTL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 234:
              name = description = code = "High Cloud Layer";
              break;
           case 242:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Convective cloud bottom level (CCBL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 243:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Convective cloud top level (CCTL)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;
           case 244:
                   // frv_peg - 2006-07-26 values from
                   // http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
                   name = code = "Convective cloud layer (CCY)";
                   description = "NCEP extention: " + "name";
                   isNumeric = false;
                   break;

           default:
              name = description = "undefined level";
              units = "undefined units";
              Logger.println("GribPDSLevel: Table 3 level "+index+" is not implemented yet", Logger.WARNING);
              break;
        }
   }

   /**
    * @return true if negative z-value
    */
   public boolean isDepth()
   {
      return index == 111 || index == 160;
   }

   /**
    * @return Index
    */
   public int getIndex(){
      return index;
   }

   /**
    * @return Name
    */
   public String getName(){
      return name;
   }

   /**
    * 
    * @return Level
    */
   public String getCode(){
      return code;
   }

   /**
    * 
    * @return Description
    */
   public String getDescription(){
      return description;
   }

    /**
     * Returns a unique ID for the given combination code-value combination.
     * @return 
     */
    public String getIdentifier()
    {
        String id = code;
        if (Float.isNaN(value1))
            return id;
        id += ":";
        if (value1 % 1 == 0)
           id += (int) value1;
       else
           id += value1;
       return id;
   }
   
   /**
    * 
    * @return Unit
    */
   public String getUnits(){
      return units;
   }

   
   public float[] getValues()
   {
       float[] values = {value1, value2};
       return values;
   }
   
   /**
    * 
    * @return Value1
    */
   public float getValue1(){
      return value1;
   }

   /**
    * 
    * @return Value2
    */
   public float getValue2(){
      return value2;
   }

   /**
    * 
    * @return true/false if numeric
    */
   public boolean getIsNumeric(){
      return isNumeric;
   }

   /**
    * 
    * @return true/false if increasing up
    */
   public boolean getIsIncreasingUp(){
      return isIncreasingUp;
   }

   /**
    * 
    * @return true/false, if is 2D variable
    */
   public boolean getIsSingleLayer(){
      return isSingleLayer;
   }

   /**
    * Formats the class for output
    * @return String holding description of the object parameters
    * @see java.lang.Object#toString()
    */
   public String toString(){
      return "Level description:" + '\n' +
             "        \tparameter id: " + this.index + "\n" +
             "        \tname: " + this.name + "\n" +
             "        \tdescription: " + this.description + "\n" +
             "        \tunits: " + this.units + "\n" +
             "        \tshort descr: " + this.code + "\n" +
             "        \tincreasing up?: " + this.isIncreasingUp + "\n" +
             "        \tsingle layer?: " + this.isSingleLayer + "\n" +
             "        \tvalue1: " + this.value1 + "\n" +
             "        \tvalue2: " + this.value2 + "\n";
   }

   /**
    * rdg - added equals method
    * didn't check everything as most are set in the constructor
    * 
    * @param obj - Object to check
    * @return true/false depends upon succes
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj){
      if (!(obj instanceof Grib1Level))
         return false;

      // quick check to see if same object
      if (this == obj) return true;

      Grib1Level lvl = (Grib1Level)obj;
      if (index != lvl.getIndex()) return false;
      if (value1 != lvl.getValue1()) return false;
      if (value2 != lvl.getValue2()) return false;

      return true;
   }

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * @param level 
    *
    * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
    *
    */
   public int compare(Grib1Level level){
      if (this.equals(level))
         return 0;

      // check if level is less than this
      if (index > level.getIndex()) return -1;
      if (value1 > level.getValue1()) return -1;
      if (value2 > level.getValue2()) return -1;

      return 1;
   }
}

