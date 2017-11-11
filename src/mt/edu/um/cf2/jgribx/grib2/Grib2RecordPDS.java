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

import java.io.IOException;
import java.util.Calendar;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.GribRecordIS;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NotSupportedException;

/**
 * A class representing the product definition section (PDS) of a GRIB record.
 */
public class Grib2RecordPDS
{
    /**
     * Length in bytes of this PDS.
     */
    protected int length;

    /**
     * The parameter in the form of {@link Grib2Parameter}
     */
    protected Grib2Parameter parameter;

    /**
     * Model Run/Analysis/Reference time.
     *
     */
    protected Calendar baseTime;

    /**
     * Forecast time. Also used as starting time when times represent a period
     */
    protected Calendar forecastTime;

    /**
     * Ending time when times represent a period
     */
    protected Calendar forecastTime2;

    /**
     * String used in building a string to represent the time(s) for this PDS
     * See the decoder for octet 21 to get an understanding
     */
    protected String timeRange = null;

    /**
     * String used in building a string to represent the time(s) for this PDS
     * See the decoder for octet 21 to get an understanding
     */
    protected String connector = null;

    /**
     * Parameter Table Version number, currently 3 for international exchange.
     */
    private int table_version;
    
    private int genProcessId;
    
    /**
     * Type of generating process
     */
    private int genProcessType;
    
    private Grib2Level level;
    
//    private String levelDescription;
    
//    private float[] levelValues;
    
    private int nCoords;
    
    private int paramCategory;
    
    /**
     * Parameter Number (used when the appropriate parameter is not found)
     */
    private int paramNumber;
    
    private int templateId;
    
    private String forecastTimeDesc;
    
    // *** constructors *******************************************************
    /**
     * Constructs a {@link GribRecordPDS} object from a bit input stream.
     *
     * @param in bit input stream with PDS content
     * @param is
     *
     * @throws IOException if stream can not be opened etc.
     * @throws NotSupportedException
     */
    public Grib2RecordPDS(GribInputStream in, GribRecordIS is) throws NotSupportedException, IOException
    {
        /* [1-4] Section Length */
        length = in.readUINT(4);

        /* [5] Section Number */
        int section = in.readUINT(1);
        if (section != 4)
            Logger.println("PDS section contains an invalid section number", Logger.WARNING);
        
        /* [6-7] Number of coordinate values after template */
        nCoords = in.readUINT(2);
        if (nCoords > 0)
        {
            throw new NotSupportedException("Hybrid coordinate values are not yet supported");
        }
        
        /* [8-9] Template number */
        templateId = in.readUINT(2);
        
        switch (templateId)
        {
            // Analysis or forecast at a horizontal level or layer at a point in time
            case 0:
                /* [10] Parameter category */
                paramCategory = in.readUINT(1);
                
                /* [11] Parameter number */
                paramNumber = in.readUINT(1);
                
                if (!Grib2Parameter.isDefaultLoaded())
                    Grib2Parameter.loadDefaultParameters();
                
                parameter = Grib2Parameter.getParameter(is.getDiscipline(), paramCategory, paramNumber);
                
                /* [12] Type of generating process */
                genProcessType = in.readUINT(1);
                
                /* [13] Background generating process identifier (defined by originating centre) */
                int backgroundGeneratingProcessId = in.readUINT(1);
                
                /* [14] Analysis or forecast generating process identifier (see Code ON388 Table A) */
                genProcessId = in.readUINT(1);
                
                /* [15-16] Hours of observational data cutoff after reference time (see Note) */
                int observationalHours = in.readUINT(2);
                
                /* [17] Minutes of observational data cutoff after reference time (see Note) */
                in.readUINT(1);
                
                /* [18] Indicator of unit of time range (see Code table 4.4) */
                int timeRangeUnitIndicator = in.readUINT(1);
                
                /* [19-22] */
                int forecastTime = in.readUINT(4);
                
                switch (timeRangeUnitIndicator)
                {
                    case 0:
                        forecastTimeDesc = forecastTime + " minutes";
                        break;
                    case 1:
                        forecastTimeDesc = forecastTime + " hours";
                        break;
                    default:
                        forecastTimeDesc = "Unknown";
                }
                
                /* [23] Type of first fixed surface (see Code table 4.5) */
                int level1Type = in.readUINT(1);
                
                /* [24] Scale factor of first fixed surface */
                int level1ScaleFactor = in.readUINT(1);
                
                /* [25-28] Scaled value of first fixed surface */
                int level1ScaledValue = in.readUINT(4);
                
                /* [29] Type of second fixed surface */
                int level2Type = in.readUINT(1);
                
                /* [30] Scale factor of second fixed surface */
                int level2ScaleFactor = in.readUINT(1);
                
                /* [31-34] Scaled value of second fixed surface */
                int level2ScaledValue = in.readUINT(4);
                
                ///////////////////////////////////////////////////////////////
                /* PROCESSING */
                float level1Value = level1ScaledValue / (float) Math.pow(10, level1ScaleFactor);
                float level2Value = level2ScaledValue / (float) Math.pow(10, level2ScaleFactor);
                
                level = new Grib2Level(level1Type, level1Value);
                
                if (level2Type != 255)
                    Logger.println("Second surface is not yet supported", Logger.ERROR);
                
//                levelValues = new float[2];
//                levelValues[0] = level1Value;
                
                break;
            default:
                Logger.println("Unsupported template number", Logger.ERROR);
        }        
    }
    
    public String getGeneratingProcessType()
    {
        switch (genProcessType)
        {
            case 0:
                return "Analysis";
            case 1:
                return "Initialisation";
            case 2:
                return "Forecast";
            case 3:
                return "Bias Corrected Forecast";
            case 4:
                return "Ensemble Forecast";
            default:
                return "Unsupported";
        }
    }

    /**
     * Get the byte length of this section.
     *
     * @return length in bytes of this section
     */
    public int getLength()
    {

        return this.length;
    }
    
    /**
     * Get the number of coordinates.
     * @return 
     */
    public int getNumberOfCoordinates()
    {
        return nCoords;
    }
    
    /**
     * Get the abbreviation representing the parameter.
     * @return the abbreviation
     */
    public String getParameterAbbrev()
    {
        if (parameter == null)
            return "";
        else
            return parameter.getName();
    }
    
    /**
     * Get a description of the parameter.
     *
     * @return description of parameter
     */
    public String getParameterDescription()
    {
        if (parameter == null)
            return "Unknown Parameter: Category "+paramCategory+" Number "+this.paramNumber;
        return this.parameter.getDescription();
    }
    
    public String getParameterUnits()
    {
        return parameter.getUnit();
    }
    
    public Grib2Level getLevel()
    {
        return level;
    }
    
    public String getLevelCode()
    {
        return level.code;
    }
    
    public String getLevelDescription()
    {
        return level.description;
    }
    
    public String getLevelIdentifier()
    {
        return level.getLevelIdentifier();
    }

    /**
     *
     * @return table version
     */
    public int getTableVersion() {
        return table_version;
    }

    /**
     * Get the parameter for this pds.
     *
     * @return date and time
     */
    public Grib2Parameter getParameter() {
        return this.parameter;
    }
    
    public int getProcessId()
    {
        return genProcessId;
    }

    /**
     * Get a string representation of this PDS.
     *
     * @return string representation of this PDS
     */
    @Override
    public String toString() {
        return headerToString()
                + "        Abbrev: " + getParameterAbbrev() + "\n"
                + "        Description: " + getParameterDescription() + "\n"
                + "        Units: " + getParameterUnits() + "\n"
                + "        Type: " + getGeneratingProcessType() + "\n"
                + "        table: " + this.table_version + "\n"
                + "        table version: " + this.table_version + "\n";
    }

    /**
     * Get a string representation of this Header information for this PDS.
     *
     * @return string representation of the Header for this PDS
     */
    public String headerToString() {
        String time1 = this.forecastTime.get(Calendar.DAY_OF_MONTH) + "."
                + (this.forecastTime.get(Calendar.MONTH) + 1) + "."
                + this.forecastTime.get(Calendar.YEAR) + "  "
                + this.forecastTime.get(Calendar.HOUR_OF_DAY) + ":"
                + this.forecastTime.get(Calendar.MINUTE);
        String time2 = this.forecastTime2.get(Calendar.DAY_OF_MONTH) + "."
                + (this.forecastTime.get(Calendar.MONTH) + 1) + "."
                + this.forecastTime.get(Calendar.YEAR) + "  "
                + this.forecastTime.get(Calendar.HOUR_OF_DAY) + ":"
                + this.forecastTime.get(Calendar.MINUTE);
        String timeStr;
        if (timeRange == null) {
            timeStr = "time: " + time1;
        } else {
            timeStr = timeRange + time1 + connector + time2;
        }

        return "    PDS header:" + '\n'
                + "        table: " + this.table_version + "\n"
                + "        " + timeStr + " (dd.mm.yyyy hh:mm) \n";
    }

    /**
     * rdg - added an equals method here
     *
     * @param obj - to test
     * @return true/false
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Grib2RecordPDS)) {
            return false;
        }
        if (this == obj) {
            // Same object
            return true;
        }
        Grib2RecordPDS pds = (Grib2RecordPDS) obj;

        if (baseTime != pds.baseTime) {
            return false;
        }
        if (forecastTime != pds.forecastTime) {
            return false;
        }
        if (table_version != pds.table_version) {
            return false;
        }
        if (length != pds.length) {
            return false;
        }

        if (!(parameter.equals(pds.getParameter()))) {
            return false;
        }

        return true;

    }

}
