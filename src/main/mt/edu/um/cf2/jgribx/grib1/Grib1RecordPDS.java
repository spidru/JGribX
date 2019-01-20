/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * Adapted from JGRIB
 * 
 * Licensed under MIT (https://github.com/spidru/JGribX/blob/master/LICENSE)
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx.grib1;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import mt.edu.um.cf2.jgribx.Bytes2Number;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NotSupportedException;

/**
 * A class representing the product definition section (PDS) of a GRIB record.
 *
 */
public class Grib1RecordPDS
{
    /**
     * Length in bytes of this PDS.
     */
    protected int length;

    /**
     * Exponent of decimal scale.
     */
    protected int decscale;

    /**
     * ID of grid type.
     */
    protected int gridId;    // no pre-definied grids supported yet.

    /**
     * True, if GDS exists.
     */
    protected boolean gdsExists;

    /**
     * True, if BMS exists.
     */
    protected boolean bmsExists;

    /**
     * The parameter as defined in the Parameter Table
     */
    protected Grib1Parameter parameter;

    /**
     * Class containing the information about the level. This helps to actually
     * use the data, otherwise the string for level will have to be parsed.
     */
    protected Grib1Level level;

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
    private int tableVersion;

    /**
     * Identification of center e.g. 88 for Oslo
     */
    private int centreId;

    /**
     * Identification of subcenter
     */
    private int subcentreId;

    /**
     * Identification of Generating Process (i.e. the numerical model that 
     * created the data).
     */
    private int processId;

    /**
     * rdg - moved the Parameter table information and functionality into a
     * class. See GribPDSParamTable class for details.
     */
    private GribPDSParamTable parameter_table;
        
    /**
     * Constructs a {@link GribRecordPDS} object from a bit input stream.
     *
     * @param in bit input stream with PDS content
     *
     * @throws IOException if stream can not be opened etc.
     * @throws NotSupportedException
     */
    public Grib1RecordPDS(GribInputStream in) throws NotSupportedException, IOException
    {
        int offset = 0;
        int offset2 = 0;
        
        // All defined times are in UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        /* [1-3] Section Length */
        length = in.readUINT(3);
        
        /* [4] Table Version */
        tableVersion = in.readUINT(1);
        
        /* [5] Originating Centre ID */
        centreId = in.readUINT(1);
        
        /* [6] Generating Process */
        processId = in.readUINT(1);
        
        /* [7] Grid Definition */
        gridId = in.readUINT(1);
        
        /* [8] Flag (Presence of PDS and GDS) */
        int flag = in.readUINT(1);
        gdsExists = (flag & 128) == 128;
        bmsExists = (flag & 64) == 64;
        
        /* [9] Parameter Indicator */
        int parameterId = in.readUINT(1);
        
        /* [10] Level Type */
        int levelType = in.readUINT(1);
        
        /* [11-12] Level Data (height, pressure, etc.) */
        int levelData = in.readUINT(2);
        
        /* [13] Year of Century */
        int centuryYear = in.readUINT(1);
        
        /* [14] Month */
        int month = in.readUINT(1);
        
        /* [15] Day */
        int day = in.readUINT(1);
        
        /* [16] Hour */
        int hour = in.readUINT(1);
        
        /* [17] Minute */
        int minute = in.readUINT(1);
        
        /* [18] Time Unit */
        int timeUnit = in.readUINT(1);
        
        /* [19] Time Period P1 (time units) */
        int p1 = in.readUINT(1);
        
        /* [20] Time Period P2 (time units) */
        int p2 = in.readUINT(1);
        
        /* [21] Time Range */
        int timeRangeId = in.readUINT(1);
        
        /* [22-23] Number included in average */
        int number = in.readUINT(2);
        
        /* Number missing from averages */
        int numberMissing = in.readUINT(1);
        
        /* [25] Reference Time Century */
        int century = in.readUINT(1);
        
        /* [26] Originating Sub-centre ID */
        subcentreId = in.readUINT(1);
        
        /* [27-28] Decimal Scale Factor */
        decscale = in.readINT(2, Bytes2Number.INT_SM);
        
        /*********************************************************************/
        /* Data Processing */

        /* Parameter */
        if (tableVersion == 255)
        {
            parameter_table = null;
            parameter = new Grib1Parameter(255, "missing", "missing parameter", "");
        } else
        {
            // Before getting parameter table values, must get the appropriate table for this center, subcenter (not yet implemented) and parameter table.
//            parameter_table = GribPDSParamTable.getParameterTable(center_id, subcenter_id, table_version);
//            parameter = parameter_table.getParameter(data[5]);
//            parameter = GribPDSParamTable.getParameterFromFile(centreId, subcenter_id, table_version, parameterId);
            parameter = Grib1Parameter.getParameter(tableVersion, parameterId, centreId);
        }
        if (parameter == null)
            throw new NotSupportedException("Unsupported Parameter "+ parameterId + " in Table " + tableVersion);

        /* Level */
//      this.level = GribTables.getLevel(data[6], data[7], data[8]);
        level = Grib1Level.getLevel(levelType, levelData);

        // octets 13-17 (base time of forecast in UTC)
        baseTime = new GregorianCalendar(100 * (century - 1) + centuryYear,
                month - 1, day, hour, minute);

        // get info for forecast time

        /*  RDG - added this code obtained from the sourceforce forum for jgrib*/
        /* changes x3/x6/x12 to hours */
        // rdg - changed indices to match indices used here
        // octet 18
        switch (timeUnit) {
            case 10: //3 hours
                p1 *= 3;
                p2 *= 3;
                timeUnit = 1;
                break;
            case 11: // 6 hours
                p1 *= 6;
                p2 *= 6;
                timeUnit = 1;
                break;
            case 12: // 12 hours
                p1 *= 12;
                p2 *= 12;
                timeUnit = 1;
                break;
        }
        /* RDG - end of code added 4 Aug 02 */

        // octet 21 (time range indicator)
        switch (timeRangeId) {
            case 0:
                offset = p1;
                offset2 = 0;
                break;
            case 1: // analysis product - valid at reference time
                offset = 0;
                offset2 = 0;
                break;
            case 2:
                timeRange = "product valid from ";
                connector = " to ";
                offset = p1;
                offset2 = p2;
                break;
            case 3:
                timeRange = "product is an average between ";
                connector = " and ";
                offset = p1;
                offset2 = p2;
                break;
            case 4:
                timeRange = "product is an accumulation from ";
                connector = " to ";
                offset = p1;
                offset2 = p2;
                break;
            case 5:
                timeRange = "product is the difference of ";
                connector = " minus ";
                offset = p2;
                offset2 = p1;
                break;
//            case 6:
//                timeRange = "product is an average from ";
//                connector = " to ";
//                offset = -data[15];
//                offset2 = -data[16];
//                break;
//            case 7:
//                timeRange = "product is an average from ";
//                connector = " to ";
//                offset = -data[15];
//                offset2 = data[16];
//                break;
            case 10:
                offset = Bytes2Number.uint2(p1, p2);
                break;
            default:
                Logger.println("GribRecordPDS: Time Range Indicator "
                        + timeRangeId + " is not yet supported - continuing, but time "
                        + "of data is not valid", Logger.ERROR);
        }

        // prep for adding the offset - get the base values
        int minute1 = minute;
        int minute2 = minute;
        int hour1 = hour;
        int hour2 = hour;
        int day1 = day;
        int day2 = day;
        int month1 = month;
        int month2 = month;
        int year1 = centuryYear;
        int year2 = centuryYear;

        // octet 18 (again) - this time adding offset to get forecast/valid time
        switch (timeUnit) {
            case 0:
                minute1 += offset;
                minute2 += offset2;
                break;  // minute
            case 1:
                hour1 += offset;
                hour2 += offset2;
                break;  // hour
            case 2:
                day1 += offset;
                day2 += offset;
                break;  // day
            case 3:
                month1 += offset;
                month2 += offset2;
                break;  // month
            case 4:
                year1 += offset;
                year2 += offset2;
                break;  // year
            default:
                Logger.println("GribRecordPDS: Forecast time unit, index of "
                        + timeUnit + ", is not yet supported - continuing, but time "
                        + "of data is not valid", Logger.ERROR);
        }

        // octets 13-17 (time of forecast)
        this.forecastTime = new GregorianCalendar(year1 + 100 * (century - 1),
                month1 - 1, day1, hour1, minute1);
        this.forecastTime2 = new GregorianCalendar(year2 + 100 * (century - 1),
                month2 - 1, day2, hour2, minute2);
    }

    /**
     * Get the byte length of this section.
     * @return length in bytes of this section
     */
    public int getLength() {

        return length;
    }

    /**
     * Check if GDS exists.
     *
     * @return true, if GDS exists
     */
    public boolean gdsExists() {

        return this.gdsExists;
    }

    /**
     * Check if BMS exists.
     *
     * @return true, if BMS exists
     */
    public boolean bmsExists() {

        return this.bmsExists;
    }

    /**
     * Get the exponent of the decimal scale used for all data values.
     *
     * @return exponent of decimal scale
     */
    public int getDecimalScale() {

        return this.decscale;
    }

    /**
     * Get the type of the parameter.
     *
     * @return type of parameter
     */
    public String getParameterAbbreviation() {
        if (parameter == null)
            return null;
        return parameter.getAbbreviation();
    }

    /**
     * Get a descritpion of the parameter.
     *
     * @return descritpion of parameter
     */
    public String getParameterDescription()
    {
        if (parameter == null)
            return "";
        else
            return this.parameter.getDescription();
    }

    /**
     * Get the name of the unit of the parameter.
     *
     * @return name of the unit of the parameter
     */
    public String getParameterUnits() {

        return this.parameter.getUnits();
    }

    /**
     * Get the level of the forecast/analysis.
     *
     * @return the level (height or pressure)
     */
    public Grib1Level getLevel()
    {

        return level;
    }
// rdg - added the following getters for level information though they are
//       just convenience methods.  You could do the same by getting the
//       GribPDSLevel (with getPDSLevel) then calling its methods directly
    /**
     * Get the name for the type of level for this forecast/analysis.
     *
     *
     * @return name of level (height or pressure)
     */
    public String getLevelName() {
        return this.level.getName();
    }

    /**
     * Get the long description for this level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public String getLevelDesc() {
        return this.level.getDescription();
    }

    /**
     * Get the units for the level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public String getLevelUnits() {
        return this.level.getUnits();
    }

    /**
     * Get the numeric value for this level.
     *
     * @return name of level (height or pressure)
     */
    public float getLevelValue() {
        return this.level.getValue1();
    }

    /**
     * Get value 2 (if it exists) for this level.
     *
     * @return name of level (height or pressure)
     */
    public float getLevelValue2() {
        return this.level.getValue2();
    }

    /**
     * Get the level of the forecast/analysis.
     *
     * @return name of level (height or pressure)
     */
    public Grib1Level getPDSLevel() {
        return this.level;
    }

    /**
     *
     * @return the ID of the originating centre
     */
    public int getCentreId() {
        return centreId;
    }

    /**
     *
     * @return subcenter_id
     */
    public int getSubcenterId() {
        return subcentreId;
    }

    /**
     *
     * @return table version
     */
    public int getTableVersion() {
        return tableVersion;
    }

    /**
     *
     * @return process_id
     */
    public int getProcessId() {
        return processId;
    }

    /**
     * Get the Parameter Table that defines this parameter.
     *
     * @return GribPDSParamTable containing parameter table that defined this
     * parameter
     */
    public GribPDSParamTable getParamTable() {
        return this.parameter_table;
    }

    /**
     * Get the reference time (in UTC) of the forecast.
     *
     * @return date and time
     */
    public Calendar getReferenceTime()
    {
        return this.baseTime;
    }

    /**
     * Get the time of the forecast in local time zone.
     *
     * @return date and time
     */
    public Calendar getLocalForecastTime() {
        return this.forecastTime;
    }

    /**
     * Get the parameter for this pds.
     *
     * @return date and time
     */
    public Grib1Parameter getParameter() {
        return this.parameter;
    }

    /**
     * Get the base (analysis) time of the forecast in GMT.
     *
     * @return date and time
     */
    public Calendar getGMTBaseTime() {

        Calendar gmtTime = (Calendar) baseTime.clone();
        // hopefully this DST offset adjusts to DST automatically
        int dstOffset = gmtTime.get(Calendar.DST_OFFSET) / 3600000;
        int gmtOffset = gmtTime.get(Calendar.ZONE_OFFSET) / 3600000;//ms to hours
        Logger.println("offset is " + gmtOffset, Logger.DEBUG);
        Logger.println("dst offset is " + dstOffset, Logger.DEBUG);
        //put offset back
        gmtTime.set(Calendar.HOUR, gmtTime.get(Calendar.HOUR) - gmtOffset - dstOffset);
        Logger.println("new time is " + gmtTime.getTime(), Logger.DEBUG);
        return gmtTime;
    }

    /**
     * Get the time of the forecast. FIXME calls to this method somehow affect
     * forecastTime (should be solved now, confirm)
     *
     * @return date and time
     */
    public Calendar getGMTForecastTime() {
        Calendar gmtTime = (Calendar) forecastTime.clone();
        Logger.println("forecast time = " + gmtTime.getTime(), Logger.DEBUG);
        // hopefully this DST offset adjusts to DST automatically
        int dstOffset = gmtTime.get(Calendar.DST_OFFSET) / 3600000;
        int gmtOffset = gmtTime.get(Calendar.ZONE_OFFSET) / 3600000;//ms to hours
        Logger.println("offset is " + gmtOffset, Logger.DEBUG);
        Logger.println("dst offset is " + dstOffset, Logger.DEBUG);
        gmtTime.set(Calendar.HOUR, gmtTime.get(Calendar.HOUR) - gmtOffset - dstOffset);//put offset back
        Logger.println("new time is " + gmtTime.getTime(), Logger.DEBUG);
        return gmtTime;
    }

    /**
     * Get a string representation of this PDS.
     *
     * @return string representation of this PDS
     */
    @Override
    public String toString() {
        return headerToString()
                + "        Type: " + this.getParameterAbbreviation() + "\n"
                + "        Description: " + this.getParameterDescription() + "\n"
                + "        Unit: " + this.getParameterUnits() + "\n"
                + "        table version: " + this.tableVersion + "\n"
                + "        " + this.level
                + // now formatted in GribPDSLevel
                "        dec.scale: " + this.decscale
                + (this.gdsExists ? "\n        GDS exists" : "")
                + (this.bmsExists ? "\n        BMS exists" : "");
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
                + "        center: " + this.centreId + "\n"
                + "        subcenter: " + this.subcentreId + "\n"
                + "        table: " + this.tableVersion + "\n"
                + "        grid_id: " + this.gridId + "\n"
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
        if (!(obj instanceof Grib1RecordPDS)) {
            return false;
        }
        if (this == obj) {
            // Same object
            return true;
        }
        Grib1RecordPDS pds = (Grib1RecordPDS) obj;

        if (gridId != pds.gridId) {
            return false;
        }
        if (baseTime != pds.baseTime) {
            return false;
        }
        if (forecastTime != pds.forecastTime) {
            return false;
        }
        if (centreId != pds.centreId) {
            return false;
        }
        if (subcentreId != pds.subcentreId) {
            return false;
        }
        if (tableVersion != pds.tableVersion) {
            return false;
        }
        if (decscale != pds.decscale) {
            return false;
        }
        if (length != pds.length) {
            return false;
        }

        if (!(parameter.equals(pds.getParameter()))) {
            return false;
        }
        if (!(level.equals(pds.getPDSLevel()))) {
            return false;
        }

        return true;

    }

    /**
     * rdg - added this method to be used in a comparator for sorting while
     * extracting records. Not currently used in the JGrib library, but is used
     * in a library I'm using that uses JGrib. Compares numerous features from
     * the PDS information to sort according to a time, level, level-type,
     * y-axis, x-axis order
     *
     * @param pds - GribRecordPDS object
     * @return - -1 if pds is "less than" this, 0 if equal, 1 if pds is "greater
     * than" this.
     *
     */
    public int compare(Grib1RecordPDS pds) {

        int check;

        if (this.equals(pds)) {
            return 0;
        }

        // not equal, so either less than or greater than.
        // check if pds is less; if not, then pds is greater
        if (gridId > pds.gridId) {
            return -1;
        }
        if (baseTime.getTime().getTime() > pds.baseTime.getTime().getTime()) {
            return -1;
        }
        if (forecastTime.getTime().getTime() > pds.forecastTime.getTime().getTime()) {
            return -1;
        }
        if (forecastTime2.getTime().getTime() > pds.forecastTime2.getTime().getTime()) {
            return -1;
        }
        if (centreId > pds.centreId) {
            return -1;
        }
        if (subcentreId > pds.subcentreId) {
            return -1;
        }
        if (tableVersion > pds.tableVersion) {
            return -1;
        }
        if (decscale > pds.decscale) {
            return -1;
        }
        if (length > pds.length) {
            return -1;
        }

        check = parameter.compare(pds.getParameter());
        if (check < 0) {
            return -1;
        }
        check = level.compare(pds.getPDSLevel());
        if (check < 0) {
            return -1;
        }

        // if here, then something must be greater than something else - doesn't matter what
        return 1;
    }

}
