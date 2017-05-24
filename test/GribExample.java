import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import mt.edu.um.cf2.jgribx.GribFile;
import mt.edu.um.cf2.jgribx.GribRecord;
import mt.edu.um.cf2.jgribx.GribRecordGDS;
import mt.edu.um.cf2.jgribx.GribRecordLight;
import mt.edu.um.cf2.jgribx.NoValidGribException;
import mt.edu.um.cf2.jgribx.NotSupportedException;

/**
 *
 * @author Andrew Spiteri
 */
public class GribExample
{
    public static void main(String args[])
    {
        // define some params
        Calendar cal = new GregorianCalendar(2015, Calendar.JULY, 6, 20, 0, 0);
        String flightFilename = "9HAEI-history-20160324-0655Z-LMML-EGLL.csv";
        String[] levels = {"10.0m","200 hPa","300 hPa","500 hPa","700 hPa","850 hPa","925 hPa"};
        
        ///////////////////////
        /* FLIGHT DATA FILE FIELD DEFINITIONS */
        final int TEMP = 30;
        final int WIND_UGRD_10HGT = 31;
        final int WIND_VGRD_10HGT = 32;
        final int WIND_SPD_10HGT = 33;
        final int WIND_DIR_10HGT = 34;
        final int ALTITUDE = 35;
        ///////////////////////
        
        try
            (
                BufferedReader reader = new BufferedReader(new FileReader("test/"+flightFilename));
                BufferedWriter writer = new BufferedWriter(new FileWriter("test/new_"+flightFilename));
            )
        {
            GribFile grb = new GribFile("test/gfsanl_3_20160324_0600_000.grb");
//            GribFile grb = new GribFile("test/gfsanl_3_20170512_0000_000.grb2");
//            GribFile grb = new GribFile("CF2_20150706_092531.grb");
            grb.listParameters(System.out);
            
            String line;
            int lineCount = 0;
            Calendar flightUtc = null;
            while ((line = reader.readLine()) != null)
            {
                String[] fields = new String[72];
                String[] lineSplit = line.split(",");
                System.arraycopy(lineSplit, 0, fields, 0, lineSplit.length);
                int index_offset = 0;
                
                // Read existing data
                double latitude = Double.parseDouble(fields[1]);
                double longitude = Double.parseDouble(fields[2]);
                
                if (lineCount == 0)
                {
                    int flightUtcYear = Integer.parseInt(fields[11]);
                    int flightUtcMonth = Integer.parseInt(fields[12]) - 1;
                    int flightUtcDay = Integer.parseInt(fields[13]);
                    int flightUtcHour = Integer.parseInt(fields[14]);
                    flightUtc = new GregorianCalendar(flightUtcYear, flightUtcMonth, flightUtcDay, flightUtcHour, 0, 0);
                }
                
                float wind_ugrd = 0;
                float wind_vgrd = 0;
                
                for (int i_level = 0; i_level < levels.length; i_level++)
                {
                    GribRecord gr;
                    
                    if (i_level != 0)
                    {
                        gr = getRecord(grb, flightUtc, "HGT", levels[i_level]);
                        if (gr != null)
                            fields[ALTITUDE + index_offset] = Float.toString(getValue(gr, latitude, longitude));
                    }
                    
                    if (i_level == 0)
                    {
                        gr = getRecord(grb, flightUtc, "TMP", "2.0m");
                        if (gr != null)
                            fields[TEMP + index_offset] = Float.toString(getValue(gr, latitude, longitude) - (float) 273.15);
                    }
                    else
                    {
                        gr = getRecord(grb, flightUtc, "TMP", levels[i_level]);
                        if (gr != null)
                            fields[TEMP + index_offset] = Float.toString(getValue(gr, latitude, longitude) - (float) 273.15);
                    }
                    
                    gr = getRecord(grb, flightUtc, "UGRD", levels[i_level]);
                    if (gr != null)
                    {
                        wind_ugrd = getValue(gr, latitude, longitude);
                        fields[WIND_UGRD_10HGT + index_offset] = Float.toString(wind_ugrd);
                    }
                    
                    gr = getRecord(grb, flightUtc, "VGRD", levels[i_level]);
                    if (gr != null)
                    {
                        wind_vgrd = getValue(gr, latitude, longitude);
                        fields[WIND_VGRD_10HGT + index_offset] = Float.toString(wind_vgrd);
                    }
                    
                    if (fields[WIND_UGRD_10HGT + index_offset] != null && fields[WIND_VGRD_10HGT + index_offset] != null)
                    {
                        // source: http://mst.nerc.ac.uk/wind_vect_convs.html
                        fields[WIND_SPD_10HGT + index_offset] = Double.toString(Math.sqrt(Math.pow(wind_ugrd, 2) + Math.pow(wind_vgrd, 2)) * 1.9438444924574); // convert to knots
                        fields[WIND_DIR_10HGT + index_offset] = Double.toString((180/Math.PI) * Math.atan2(-wind_ugrd, -wind_vgrd));   // Meteorological wind direction
                    }
                    
                    if (i_level == 0)
                        index_offset += 5;
                    else
                        index_offset += 6;
                }
                
                for (String field : fields) {
                    if (field != null) {
                        System.out.print(field);
                        writer.write(field);
                    }
                    System.out.print(",");
                    writer.write(",");
                }
                System.out.println();
                writer.newLine();
                
                lineCount++;
            }
            
        }
        catch (FileNotFoundException e)
        {
            System.err.println("FileNotFoundException : " + e);
        }
        catch (IOException ioError)
        {
            System.err.println("IOException : " + ioError);
        }
        catch (NoValidGribException noGrib)
        {
            System.err.println("NoValidGribException : " + noGrib);
        }
        catch (NotSupportedException noSupport)
        {
            System.err.println("NotSupportedException : " + noSupport);
        }
        System.out.println("Done");
    }
    
    private static GribRecord getRecord(GribFile grib, Calendar date, String type, String level)
    {
        GribRecord record = null;
        GribRecordLight[] headers = grib.getRecordForType(type);
        
        if (headers.length == 0)
            return null;
        
        // Find closest time in GRIB to the one specified in argument list
        long diff_ms = Math.abs(date.getTimeInMillis() - headers[0].getPDS().getLocalForecastTime().getTimeInMillis());
        Calendar closestDate = headers[0].getPDS().getLocalForecastTime();
        for (int i = 1; i < headers.length; i++)
        {
            long compareValue = Math.abs(date.getTimeInMillis() - headers[i].getPDS().getLocalForecastTime().getTimeInMillis());
            if (compareValue < diff_ms)
            {
                diff_ms = compareValue;
                closestDate = headers[i].getPDS().getLocalForecastTime();
            }
        }
        System.out.println("Closest date found to "+date.getTime()+": "+closestDate.getTime().toString());
        
        GribRecordGDS[] gdsRecords = grib.getGridsForType(type);
        if (gdsRecords.length > 1)
            System.err.println("Unsupported scenario occured. Please review this section.");
        for (GribRecordGDS gds : gdsRecords)
        {
            try {
                record = grib.getRecord(type, gds, level, closestDate.getTime());
            } catch (IOException | NoValidGribException | NotSupportedException ex) {
                Logger.getLogger(GribExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return record;
    }
    
    private static float getValue(GribRecord gr, double lat, double lon) throws NoValidGribException
    {

        double[] xcoords = gr.getGDS().getXCoords();
        double[] ycoords = gr.getGDS().getYCoords();
            
        int index_closest_latitude = (int) Math.round((lat - gr.getGDS().getGridLat1()) / gr.getGDS().getGridDY());
        int index_closest_longitude = (int)Math.round((lon - gr.getGDS().getGridLon1()) / gr.getGDS().getGridDX());

        double closest_latitude = ycoords[index_closest_latitude];
        double closest_longitude = xcoords[index_closest_longitude];
        
        return gr.getValue(index_closest_longitude, index_closest_latitude);
    }
}