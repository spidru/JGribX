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
    protected float value;

    /**
     * Constructor for an instance of {@link Grib2Level} matching the specified type and value
     * Reference: https://fossies.org/linux/gdal/frmts/grib/degrib/data/grib2_table_4_5.csv
     */
    public Grib2Level()
    {
        this.units = "";
    }
    
    public static Grib2Level getLevel(int type, float value)
    {
        Grib2Level level = new Grib2Level();
        level.index = type;
        switch (level.index)
        {
            case 1:
                level.code = "SFC";
                level.description = level.name = "ground or water surface";
                break;
            case 2:
                level.code = "CBL";
                level.description = level.name = "cloud base level";
                break;
            case 3:
                level.code = "CTL";
                level.description = level.name = "level of cloud tops";
                break;
            case 4:
                level.description = level.name = "level of 0 degC isotherm";
                level.code = "0DEG";
                break;
            case 5:
                level.code = "ADCL";
                level.description = level.name = "level of adiabatic condensation lifted from the surface";
              break;
            case 6:
                level.code = "MWSL";
                level.description = level.name = "maximum wind level";
                break;
            case 7:
                level.code = "TRO";
                level.description = level.name = "tropopause";
                break;
            case 100:
                level.code = "ISBL";
                level.name = "isobaric surface";
                level.units = "hPa";
                level.value = value / 100;
                level.description = "pressure at " + (int) level.value + " " + level.units;
                break;
            case 101:
                level.code = "MSL";
                level.description = level.name = "mean sea level";
                break;
            case 102:
                level.code = "GPML";
                level.name = "specified altitude above MSL";
                level.units = "m";
                level.value = value;
                level.description = level.value + " " + level.units + " above MSL";
                break;
            case 103:
                level.code = "HTGL";
                level.name = "Specified height level above ground";
                level.units = "m";
                level.value = value;
                level.description = (int) level.value + " " + level.units + " above ground";
                break;
            case 104:
                level.code = "SIGL";
                level.description = level.name = "Sigma Level";
                level.units = "%";
                level.value = value;
                break;
            case 105:
                level.code = "HYBL";
                level.name = "Hybrid level";
                level.value = value;
                level.description = (int) value + " hybrid level(s)";
                break;
            case 106:
                level.code = "DBLL";
                level.name = "Depth below land surface";
                level.value = value;
                level.units = "m";
                level.description = level.value + " " + level.units + " depth below land surface";
                break;
            case 108:
                level.code = "SPDL";
                level.name = "level at specified pressure difference from ground to level";
                level.units = "hPa";
                level.value = value;
                level.description = level.value + " " + level.units + " pressure difference from ground";
                break;
            case 109:
                level.code = "PVL";
                level.name = "Potential vorticity surface";
                level.units = "K m^2 kg^-1 s^-1";
                level.value = value;
                level.description = level.name + " " + level.value + " " + level.units;
                break;
            case 200:
                level.code = "EATM";
                level.description = level.name = "entire atmosphere (considered as a single layer)";
                break;
            case 204:
                level.code = "HTFL";
                level.description = level.name = "highest tropospheric freezing level";
                break;
            case 220:
                level.code = "PBL";
                level.description = level.name = "planetary boundary layer";
                break;
            default:
                level = null;
                break;
        }
        return level;
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
        return code+":"+ value;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getUnits()
    {
        return units;
    }
    
    public float getValue()
    {
        return value;
    }
}
