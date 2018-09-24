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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import mt.edu.um.cf2.jgribx.GribCodes.Discipline;
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
     * The time at which the forecast applies.
     */
    private Calendar forecastTime;
    
    /**
     * Length in bytes of this PDS.
     */
    protected int length;

    /**
     * The parameter in the form of {@link Grib2Parameter}.
     */
    protected Grib2Parameter parameter;
    
    /**
     * Analysis or forecast generating process identified.
     * <br>
     * e.g. 81 (Analysis from GFS)
     */
    private int genProcessId;
    
    /**
     * Type of generating process.
     * <br>
     * e.g. 2 (Forecast)
     */
    private int genProcessType;
    
    private Grib2Level level;
    
    private int nCoords;
    
    private int paramCategory;
    
    /**
     * Parameter Number (used when the appropriate parameter is not found)
     */
    private int paramNumber;
    
    private int templateId;
    
    /**
     * Constructs a {@link GribRecordPDS} object from a bit input stream.
     *
     * @param in bit input stream with PDS content
     * @param discipline
     * @param referenceTime
     *
     * @throws IOException if stream can not be opened etc.
     * @throws NotSupportedException
     */
    public Grib2RecordPDS(GribInputStream in, Discipline discipline, Calendar referenceTime) throws NotSupportedException, IOException
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
                
                parameter = Grib2Parameter.getParameter(discipline, paramCategory, paramNumber);
                
                /* [12] Type of generating process */
                genProcessType = in.readUINT(1);
                
                /* [13] Background generating process identifier (defined by originating centre) */
                int backgroundGeneratingProcessId = in.readUINT(1);
                
                /* [14] Analysis or forecast generating process identifier */
                genProcessId = in.readUINT(1);
                
                /* [15-16] Hours of observational data cutoff after reference time (see Note) */
                int observationalHours = in.readUINT(2);
                
                /* [17] Minutes of observational data cutoff after reference time (see Note) */
                in.readUINT(1);
                
                /* [18] Indicator of unit of time range (see Code table 4.4) */
                int timeRangeUnitIndicator = in.readUINT(1);
                
                /* [19-22] Forecast time in units defined in octet 18 */
                int forecastTimeAhead = in.readUINT(4);
                
                forecastTime = (Calendar) referenceTime.clone();
                switch (timeRangeUnitIndicator)
                {
                    case 0:
                        // Minute
                        forecastTime.add(Calendar.MINUTE, forecastTimeAhead);
                        break;
                    case 1:
                        // Hour
                        forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead);
                        break;
                    case 2:
                        // Day
                        forecastTime.add(Calendar.DAY_OF_MONTH, forecastTimeAhead);
                        break;
                    case 3:
                        // Month
                        forecastTime.add(Calendar.MONTH, forecastTimeAhead);
                        break;
                    case 4:
                        // Year
                        forecastTime.add(Calendar.YEAR, forecastTimeAhead);
                        break;
                    case 5:
                        // Decade
                        forecastTime.add(Calendar.YEAR, forecastTimeAhead*10);
                        break;
                    case 6:
                        // Normal (30 years)
                        forecastTime.add(Calendar.YEAR, forecastTimeAhead*30);
                        break;
                    case 7:
                        // Century
                        forecastTime.add(Calendar.YEAR, forecastTimeAhead*100);
                        break;
                    case 10:
                        // 3 Hours
                        forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead*3);
                        break;
                    case 11:
                        // 6 Hours
                        forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead*6);
                        break;
                    case 12:
                        // 12 Hours
                        forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead*12);
                        break;
                    case 13:
                        // Second
                        forecastTime.add(Calendar.SECOND, forecastTimeAhead);
                        break;
                    default:
                        throw new NotSupportedException("Time range " + timeRangeUnitIndicator + " is not supported yet");
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
                
                level = Grib2Level.getLevel(level1Type, level1Value);
                
                if (level2Type != 255)
                    Logger.println("Second surface is not yet supported", Logger.ERROR);
                
//                levelValues = new float[2];
//                levelValues[0] = level1Value;
                
                break;
            default:
                throw new NotSupportedException("Unsupported template number");
        }        
    }
    
    /**
     * Returns the forecast time.
     * @return 
     */
    public Calendar getForecastTime()
    {
        return forecastTime;
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
            return parameter.getCode();
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
        return parameter.getUnits();
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
        return "        Abbrev: " + getParameterAbbrev() + "\n"
                + "        Description: " + getParameterDescription() + "\n"
                + "        Units: " + getParameterUnits() + "\n"
                + "        Type: " + getGeneratingProcessType() + "\n";
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

        if (length != pds.length) {
            return false;
        }

        if (!(parameter.equals(pds.getParameter()))) {
            return false;
        }

        return true;

    }

}
