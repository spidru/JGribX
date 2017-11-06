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
package mt.edu.um.cf2.jgribx.grib2;

public class Grib2Level
{
    
    protected String code;          // provides a unique codename for the level
    protected String description;   // describes the level together with any specified values
    protected String name;          // provides a generic name for the level
    protected int index;
    protected String units;
    protected float value1;
    protected float value2;
    
    /**
     * Constructor for an instance of {@link Grib2Level} matching the specified type and value
     * @param type  the type designator of the level
     * @param value the value associated with the specified level
     */
    protected Grib2Level(int type, float value)
    {
        index = type;
        switch (index)
        {
            case 1:
                code = "SFC";
                description = name = "ground or water surface";
                break;
            case 2:
                code = "CBL";
                description = name = "cloud base level";
                break;
            case 3:
                code = "CTL";
                description = name = "level of cloud tops";
                break;
            case 4:
                description = name = "level of 0 degC isotherm";
                code = "0DEG";
                break;
            case 5:
                description = name = "level of adiabatic condensation lifted from the surface";
                code = "ADCL";
              break;
            case 6:
                code = "MWSL";
                description = name = "maximum wind level";
                break;
            case 7:
                code = "TRO";
                description = name = "tropopause";
                break;
            case 100:
                code = "ISBL";
                name = "isobaric surface";
                units = "hPa";
                value1 = value / 100;
                description = "pressure at " + (int) value1 + " " + units;
                break;
            case 101:
                code = "MSL";
                description = name = "mean sea level";
                break;
            case 102:
                code = "GPML";
                name = "specified altitude above MSL";
                units = "m";
                value1 = value;
                description = value1 + " " + units + " above MSL";
                break;
            case 103:
                code = "TGL";
                name = "Specified height level above ground";
                units = "m";
                value1 = value;
                description = (int) value1 + " " + units + " above ground";
                break;
            case 106:
                code = "DBLL";
                description = "depth below land surface";
                units = "m";
                break;
            case 108:
                code = "SPDL";
                name = "level at specified pressure difference from ground to level";
                units = "hPa";
                value1 = value;
                description = value1 + " " + units + " pressure difference from ground";
            case 200:
                code = "EATM";
                description = name = "entire atmosphere (considered as a single layer)";
                break;
            case 204:
                code = "HTFL";
                description = name = "highest tropospheric freezing level";
                break;
            case 220:
                code = "PBL";
                description = "planetary boundary layer";
                break;
            default:
                code = "???";
                description = "unknown";
        }
    }

    public String getCode()
    {
        return code;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public String getLevelIdentifier()
    {
        return code+":"+value1;
    }
    
    public float[] getValues()
    {
        float[] values = {value1, value2};
        return values;
    }
}
