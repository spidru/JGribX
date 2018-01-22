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
 * References: 
 *  http://nimbus.com.uy/weather/grads/grib_levels.html
 *  http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html
 */
package mt.edu.um.cf2.jgribx.grib1;

import mt.edu.um.cf2.jgribx.Logger;

/**
 * A class containing static methods which deliver descriptions and names of
 * parameters, levels and units for byte codes from GRIB records.
 *
 * <b>2006-07-26 frv_peg: Added NCEP extention levels for use of GFS
 * files.</b><p/>
 * (level: 117, 211,212,213,222,223,232,233,242,243,244)</p>
 *
 * See:
 * <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table3.html">table3.html</a>
 */
public class Grib1Level {

    /**
     * Index number from table 3 - can be used for comparison even if the
     * description of the level changes
     */
    private int index;

    /**
     * Name of the vertical coordinate/level
     *
     */
    private String name;

    /**
     * Value of PDS octet10 if separate from 11, otherwise value from octet10&11
     */
    private float value1 = Float.NaN;

    /**
     * Value of PDS octet11
     */
    private float value2 = Float.NaN;

    /**
     * Stores a short name of the level - same as the string "level" in the
     * original GribRecordPDS implementation
     */
    private String code;

    /**
     * Stores a descriptive name of the level GribRecordPDS implementation
     */
    private String description;

    /**
     * Stores the name of the level - same as the string "level" in the original
     * GribRecordPDS implementation
     */
    private String units;

    /**
     * Stores whether this is (usually) a vertical coordinate for a single layer
     * (e.g. surface, tropopause level) or multiple layers (e.g. hPa, m AGL)
     * Aids in deciding whether to build 2D or 3D grids from the data
     */
    private boolean isSingleLayer = true;

    /**
     * Indicates whether the vertical coordinate increases with height. e.g.
     * false for pressure and sigma, true for height above ground or if unknown
     */
    private boolean isIncreasingUp = true;

    /**
     * True if a numeric values are used for this level (e.g. 1000 mb) False if
     * level doesn't use values (e.g. surface). Basically indicates whether you
     * will be able to get a value for this level.
     */
    private boolean isNumeric = false;

    /**
     * Constructor. Creates a GribPDSLevel based on octets 10-12 of the PDS.
     * Implements tables 3 and 3a.
     *
     */
    public Grib1Level() {
        this.units = "";
    }

    public static Grib1Level getLevel(int levelType, int levelData) {
        Grib1Level level = new Grib1Level();
        int v1 = (levelData & 0xFF00) >> 8;
        int v2 = levelData & 0xFF;
        level.index = levelType;
        switch (level.index) {
            case 1:
                level.code = "SFC";
                level.name = level.description = "ground or water surface";
                break;
            case 2:
                level.code = "CBL";
                level.name = level.description = "cloud base level";
                break;
            case 3:
                level.code = "CTL";
                level.name = level.description = "level of cloud tops";
                break;
            case 4:
                level.code = "0DEG";
                level.name = level.description = "level of 0 degC isotherm";
                break;
            case 5:
                level.code = "ADCL";
                level.name = level.description = "level of adiabatic condensation lifted from the surface";
                break;
            case 6:
                level.code = "MWSL";
                level.name = level.description = "maximum wind level";
                break;
            case 7:
                level.code = "TRO";
                level.name = level.description = "tropopause";
                break;
            case 8:
                level.code = "NTAT";
                level.name = level.description = "nominal top of atmosphere";
                break;
            case 9:
                level.code = "SEAB";
                level.name = level.description = "sea bottom";
                break;
            case 20:
                level.code = "TMPL";
                level.name = "isothermal level";
                level.value1 = levelData;
                level.units = "K";
                level.isNumeric = true;
                level.description = "Isothermal level at " + level.value1 / 100 + level.units;
                break;
            case 100:
                level.code = "ISBL";
                level.name = "isobaric level";
                level.value1 = levelData;
                level.units = "hPa";
                level.isNumeric = true;
                level.isIncreasingUp = false;
                level.isSingleLayer = false;
                level.description = "pressure at " + level.value1 + " " + level.units;
                break;
            case 101:
                level.code = "ISBY";
                level.name = "layer between two isobaric levels";
                level.value1 = ((levelData & 0xFF00) >> 8) * 10; // convert from kPa to hPa - who uses kPa???
                level.value2 = v2 * 10;
                level.units = "hPa";
                level.description = "layer between " + level.value1 + " and " + level.value2 + " " + level.units;
                break;
            case 102:
                level.code = "MSL";
                level.name = level.description = "mean sea level";
                break;
            case 103:
                level.code = "GPML";
                level.name = "specified altitude above MSL";
                level.value1 = levelData;
                level.units = "m";
                level.isNumeric = true;
                level.isSingleLayer = false;
                level.description = level.value1 + " m above mean sea level";
                break;
            case 104:
                level.code = "GPMY";
                level.name = "layer between two specified altitudes above MSL";
                level.value1 = (v1 * 100); // convert hm to m
                level.value2 = (v2 * 100);
                level.units = "m";
                level.description = "Layer between " + v1 + " and "
                        + v2 + " m above mean sea level";
                break;
            case 105:
                level.code = "TGL";
                level.name = "specified height level above ground";
                level.value1 = levelData;
                level.units = "m";
                level.isNumeric = true;
                level.isSingleLayer = false;
                level.description = level.value1 + " m above ground";
                break;
            case 106:
                level.code = "HTGY";
                level.name = "layer between two specified height levels above ground";
                level.value1 = (v1 * 100); // convert hm to m
                level.value2 = (v2 * 100);
                level.units = "m";
                level.isNumeric = true;
                level.description = "Layer between " + level.value1 + " and "
                        + level.value2 + " " + level.units + " above ground";
                break;
            case 107:
                level.code = "SIGL";
                level.name = "Sigma level";
                level.value1 = (levelData / 10000.0f);
                level.units = "sigma";
                level.isNumeric = true;
                level.isSingleLayer = false;
                level.isIncreasingUp = false;
                level.description = "sigma = " + level.value1;
                break;
            case 108:
                level.code = "SIGY";
                level.name = "Layer between two sigma layers";
                level.value1 = (v1 / 100.0f);
                level.value2 = (v2 / 100.0f);
                level.isNumeric = true;
                level.description = "Layer between sigma levels " + level.value1 + " and " + level.value2;
                break;
            case 109:
                level.code = "HYBL";
                level.name = "hybrid level";
                level.value1 = levelData;
                level.isNumeric = true;
                level.description = "hybrid level " + level.value1;
                break;
            case 110:
                level.code = "HYBY";
                level.name = "Layer between two hybrid levels";
                level.value1 = v1;
                level.value2 = v2;
                level.isNumeric = true;
                level.description = "Layer between hybrid levels " + level.value1 + " and " + level.value2;
                break;
            case 111:
                level.code = "DBLL";
                level.name = "Depth below land surface";
                level.value1 = levelData;
                level.units = "cm";
                level.isNumeric = true;
                level.description = level.value1 + " " + level.units + "below land surface";
                break;
            case 112:
                level.code = "DBLY";
                level.name = "Layer between two levels below land surface";
                level.value1 = v1;
                level.value2 = v2;
                level.units = "cm";
                level.isNumeric = true;
                level.description = "Layer between " + level.value1 + " and " + level.value2
                        + " cm below land surface";
                break;
            case 113:
                level.code = "THEL";
                level.name = "Isentropic (theta) level";
                level.value1 = levelData;
                level.units = "K";
                level.isNumeric = true;
                level.isSingleLayer = false;
                level.description = level.value1 + " K";
                break;
            case 114:
                level.code = "THEY";
                level.name = "Layer between two isentropic layers";
                level.value1 = (v1 + 475);
                level.value2 = (v2 + 475);
                level.units = "K";
                level.isNumeric = true;
                level.description = "Layer between " + level.value1 + " and " + level.value2 + " K";
                break;
            case 115:
                level.code = "SPDL";
                level.name = "Pressure difference";
                level.units = "hPa";
                level.value1 = levelData;
                level.description = "Level at " + level.value1 + " " + level.units + "from ground";
            case 116:
                level.code = "SPDY";
                level.name = "Layer between pressure differences from ground to levels";
                level.value1 = v1;
                level.value2 = v2;
                level.units = "hPa";
                level.isNumeric = true;
                level.description = "Layer between pressure differences from ground: "
                        + level.value1 + " and " + level.value2 + " K";
                break;
            case 117:
                level.code = "PVL";
                level.name = "potential vorticity surface";
                level.value1 = levelData;
                level.units = "10^-6 km^2/kgs";
                level.isNumeric = false;
                level.description = level.name;
                break;
            case 119:
                level.code = "ETAL";
                level.name = "ETA level";
                level.value1 = levelData;
            case 125:
                level.code = "HGLH";
                level.name = "Height above ground (high precision)";
                level.value1 = levelData;
                level.units = "cm";
                level.isNumeric = true;
                level.isSingleLayer = false;
                level.description = level.value1 + " " + level.units + " above ground";
                break;
            case 160:
                level.code = "DBSL";
                level.name = "Depth below sea level";
                level.value1 = levelData;
                level.units = "m";
                level.isNumeric = true;
                level.description = levelData + " m below sea level";
                break;
            case 200:
                level.code = "EATM";
                level.name = level.description = "entire atmosphere layer";
                break;
            case 201:
                level.code = "EOCN";
                level.name = level.description = "entire ocean layer";
                break;
            case 204:
                level.code = "HTFL";
                level.name = level.description = "Highest tropospheric freezing level";
                break;
            case 211:
                level.code = "BCY";
                level.name = level.description = "Boundary layer cloud layer";
                level.isNumeric = false;
                break;
            case 212:
                level.code = "LCBL";
                level.name = level.description = "Low cloud bottom level";
                level.isNumeric = false;
                break;
            case 213:
                level.code = "LCTL";
                level.name = level.description = "Low cloud top level";
                level.isNumeric = false;
                break;
            case 214:
                level.code = "LCY";
                level.name = level.description = "Low Cloud Layer";
                break;
            case 222:
                level.code = "MCBL";
                level.name = level.description = "Middle cloud bottom level";
                level.isNumeric = false;
                break;
            case 223:
                level.code = "MCTL";
                level.name = level.description = "Middle cloud top level";
                level.isNumeric = false;
                break;
            case 224:
                level.code = "MCY";
                level.name = level.description = "Middle Cloud Layer";
                break;
            case 232:
                level.code = "HCBL";
                level.name = level.description = "High cloud bottom level";
                level.isNumeric = false;
                break;
            case 233:
                level.code = "HCTL";
                level.name = level.description = "High cloud top level";
                level.isNumeric = false;
                break;
            case 234:
                level.code = "HCY";
                level.name = level.description = "High Cloud Layer";
                break;
            case 242:
                level.code = "CCBL";
                level.name = level.description = "Convective cloud bottom level";
                level.isNumeric = false;
                break;
            case 243:
                level.code = "CCTL";
                level.name = level.description = "Convective cloud top level";
                level.isNumeric = false;
                break;
            case 244:
                level.code = "CCY";
                level.name = level.description = "Convective cloud layer";
                level.isNumeric = false;
                break;
            default:
                level = null;
//                level.name = level.description = "undefined level";
//                level.units = "undefined units";
//                Logger.println("GribPDSLevel: Table 3 level " + level.index + " is not implemented yet", Logger.WARNING);
                break;
        }
        return level;
    }

    /**
     * @return true if negative z-value
     */
    public boolean isDepth() {
        return index == 111 || index == 160;
    }

    /**
     * @return Index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return Level
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a unique ID for the given combination code-value combination.
     *
     * @return
     */
    public String getIdentifier() {
        String id = code;
        if (Float.isNaN(value1)) {
            return id;
        }
        id += ":";
        if (value1 % 1 == 0) {
            id += (int) value1;
        } else {
            id += value1;
        }
        return id;
    }

    /**
     *
     * @return Unit
     */
    public String getUnits() {
        return units;
    }

    public float[] getValues() {
        float[] values = {value1, value2};
        return values;
    }

    /**
     *
     * @return Value1
     */
    public float getValue1() {
        return value1;
    }

    /**
     *
     * @return Value2
     */
    public float getValue2() {
        return value2;
    }

    /**
     *
     * @return true/false if numeric
     */
    public boolean getIsNumeric() {
        return isNumeric;
    }

    /**
     *
     * @return true/false if increasing up
     */
    public boolean getIsIncreasingUp() {
        return isIncreasingUp;
    }

    /**
     *
     * @return true/false, if is 2D variable
     */
    public boolean getIsSingleLayer() {
        return isSingleLayer;
    }

    /**
     * Formats the class for output
     *
     * @return String holding description of the object parameters
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Level description:" + '\n'
                + "        \tparameter id: " + this.index + "\n"
                + "        \tname: " + this.name + "\n"
                + "        \tdescription: " + this.description + "\n"
                + "        \tunits: " + this.units + "\n"
                + "        \tshort descr: " + this.code + "\n"
                + "        \tincreasing up?: " + this.isIncreasingUp + "\n"
                + "        \tsingle layer?: " + this.isSingleLayer + "\n"
                + "        \tvalue1: " + this.value1 + "\n"
                + "        \tvalue2: " + this.value2 + "\n";
    }

    /**
     * rdg - added equals method didn't check everything as most are set in the
     * constructor
     *
     * @param obj - Object to check
     * @return true/false depends upon succes
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Grib1Level)) {
            return false;
        }

        // quick check to see if same object
        if (this == obj) {
            return true;
        }

        Grib1Level lvl = (Grib1Level) obj;
        if (index != lvl.getIndex()) {
            return false;
        }
        if (value1 != lvl.getValue1()) {
            return false;
        }
        if (value2 != lvl.getValue2()) {
            return false;
        }

        return true;
    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records.
     *
     * @param level
     *
     * @return - -1 if level is "less than" this, 0 if equal, 1 if level is
     * "greater than" this.
     *
     */
    public int compare(Grib1Level level) {
        if (this.equals(level)) {
            return 0;
        }

        // check if level is less than this
        if (index > level.getIndex()) {
            return -1;
        }
        if (value1 > level.getValue1()) {
            return -1;
        }
        if (value2 > level.getValue2()) {
            return -1;
        }

        return 1;
    }
}
