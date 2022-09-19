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
import java.util.GregorianCalendar;
import java.util.TimeZone;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.Logger;

/**
 *
 * @author spidru
 */
public class Grib2RecordIDS
{
    public static class DataType
    {
        private int dataType;
        private final String acronym;
        private final String description;
        public DataType(int dataType)
        {
            switch (dataType)
            {
                case 0:
                    acronym = "ANL";
                    description = "Analysis products";
                    break;
                case 1:
                    acronym = "FCST";
                    description = "Forecast products";
                    break;
                case 2:
                    acronym = "ANL/FCST";
                    description = "Analysis and forecast products";
                    break;
                case 3:
                    acronym = "CFCST";
                    description = "Control forecast products";
                    break;
                default:
                    acronym = "???";
                    description = "Unsupported type";
            }
        }

        public String getAcronym() { return acronym; }
        public String getDescription() { return description; }
    }
    protected Calendar referenceTime;
    private int referenceTimeSignificance;
    private int length;
    private int number;
    private int origCentreId;
    private int origSubCentreId;
    private int masterTableVersion;
    private int localTableVersion;
    private int year;
    private int dataProdStatus;
    private DataType dataType;


    
    public static Grib2RecordIDS readFromStream(GribInputStream in) throws IOException
    {
        Grib2RecordIDS ids = new Grib2RecordIDS();
        
        /* [1-4] Section Length */
        ids.length = in.readUINT(4);
        
        /* [5] Section Number */
        ids.number = in.readUINT(1);
        
        /* [6-7] Originating Centre ID */
        ids.origCentreId = in.readUINT(2);
        
        /* [8-9] Originating Sub-centre ID */
        ids.origSubCentreId = in.readUINT(2);
        
        /* [10] Master Tables Version Number */
        ids.masterTableVersion = in.readUINT(1);
        
        /* [11] Local Tables Version Number */
        ids.localTableVersion = in.readUINT(1);
        
        /* [12] Reference Time Significance */
        ids.referenceTimeSignificance = in.readUINT(1);
        
        /* [13-14] Reference Year */
        int year = in.readUINT(2);
        
        /* [15] Reference Month */
        int month = in.readUINT(1);
        
        /* [16] Reference Day */
        int day = in.readUINT(1);
        
        /* [17] Hour */
        int hour = in.readUINT(1);
        
        /* [18] Minute */
        int minute = in.readUINT(1);
        
        /* [19] Second */
        int second = in.readUINT(1);
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ids.referenceTime = new GregorianCalendar(year, month-1, day, hour, minute, second);
        
        /* Data Production Status */
        ids.dataProdStatus = in.readUINT(1);
        
        /* Data Type */
        ids.dataType = new DataType(in.readUINT(1));
        
        /* Additional Data */
        if (ids.length > 21)
        {
            Logger.println("Length is greater than 21. Please review code.", Logger.ERROR);
        }
        
        return ids;
    }
    
    /**
     * Returns the ID of the originating centre.
     * @return 
     */
    public int getCentreId()
    {
        return origCentreId;
    }

    public int getReferenceTimeSignificance() { return referenceTimeSignificance; }
    
    /**
     * Returns the length of the {@link Grib2RecordIDS} instance.
     * @return the length of the IDS in bytes
     */
    public int getLength()
    {
        return length;
    }
    
    @Override
    public String toString()
    {
        String string = "\nGRIB IDS:\n";
        string += "Weather centre "+origCentreId+"\n";
        string += dataType.getDescription();
        return string;
    }
}
