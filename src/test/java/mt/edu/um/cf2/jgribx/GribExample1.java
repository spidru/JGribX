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
package mt.edu.um.cf2.jgribx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import mt.edu.um.cf2.jgribx.GribCodes;
import mt.edu.um.cf2.jgribx.GribFile;
import mt.edu.um.cf2.jgribx.JGribX;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NoValidGribException;
import mt.edu.um.cf2.jgribx.NotSupportedException;

/**
 * This example demonstrates how a quick summary of the contents of the GRIB
 * file can be obtained.
 */
public class GribExample1
{
    public static void main(String[] args)
    {
        System.out.println("JGribX version " + JGribX.getVersion());
        Logger.setLoggingMode(Logger.LoggingMode.CONSOLE);
        Logger.setLevel(Logger.INFO);
        
        /**
         * type: GRIB1 NCEP GFS
         * issues:
         *  - Table 3 level 220 is not implemented yet
         *  - Unsupported Parameter 219 in Table 130
         *  - Unsupported Parameter 220 in Table 130
         *  - Unsupported Parameter 191 in Table 133
         */
//        String gribFilename = "test/gfsanl_3_20160324_0600_000.grb";

        /**
         * type: GRIB1 NCEP GFS
         * issues:
         *  - Table 3 level 220 is not implemented yet
         *  - Unsupported Parameter 219 in Table 130
         *  - Unsupported Parameter 220 in Table 130
         *  - Unsupported Parameter 191 in Table 133
         */
//        String gribFilename = "test/gfs_3_20170101_0000_000.grb";

        /**
         * type: GRIB2 NCEP GFS
         * issues:
         *  - BMS bitmap not yet supported
         *  - second surface not yet supported
         */
//        String gribFilename = "test/gfsanl_3_20170512_0000_000.grb2";
        
//        String gribFilename = "test/200601010000.pgbh06.gdas.20051226-20051231.grb2";
        
        /**
         * type: GRIB2 NCEP GFS
         * issues:
         *  - Data Representation type 0 not supported
         *  - Missing Value Management is not supported
         *  - causes out of memory error
         */
//        String gribFilename = "test/gfs_4_20171112_0000_024.grb2";

//        String gribFilename = "test/A_HWXE85ECEM210000_C_ECMF_20160721000000_24h_em_ws_850hPa_global_0p5deg_grib2.bin";     // testing files from ECMWF 
        
//        String gribFilename = "cached_gfs_4_20180520_0000_000_m.grb2";
        final String gribFilename = "gfsanl_3_20170512_0000_000.grb2";

        // Prepare format for reference times
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        try
        {
//            System.out.println(GribExample1.class.getClassLoader().getResource(gribFilename));
            GribFile gribFile = new GribFile("src/test/resources/" + gribFilename);
            
            // Get info
            int nRecords = gribFile.getRecordCount();
            List<Calendar> refDates = gribFile.getReferenceTimes();
            List<Calendar> forecastDates = gribFile.getForecastTimes();
            List<String> params = gribFile.getParameterCodes();
            int[] centreIds = gribFile.getCentreIDs();
            int[] processIds = gribFile.getProcessIDs();
            
            // Print out generic GRIB file info
            System.out.println("---------------------------------------");
            System.out.println("Reading file: " + gribFilename);
            System.out.println("GRIB Edition: " + gribFile.getEdition());
            System.out.println("Records successfully read: " + nRecords + " of "
                    + (nRecords + gribFile.getRecordsSkippedCount()));
            System.out.println("---------------------------------------");
            
            // Print out originating centre info
            System.out.print("Weather Centre(s): ");
            for (int i = 0; i < centreIds.length; i++)
            {
                System.out.print(centreIds[i]
                        + " [" + GribCodes.getCentreName(centreIds[i]) + "]");
                if (i != centreIds.length - 1) System.out.print(",");
            }
            System.out.println();
            
            // Print out generating process info
            System.out.print("Generating Process(es): ");
            for (int i = 0; i < processIds.length; i++)
            {
                System.out.print(processIds[i]
                        + " [" + GribCodes.getProcessName(processIds[i]) + "]");
                if (i != processIds.length - 1) System.out.print(",");
            }
            System.out.println();
            
            // Get forecast times
            System.out.println("Forecast Time(s): ");
            for (Calendar date : forecastDates)
            {
                System.out.println("\t" + sdf.format(date.getTime()));
            }
            
            // Get reference time
            System.out.println("Reference Time: ");
            for (Calendar date : refDates)
            {
                System.out.println("\t" + sdf.format(date.getTime()));
            }
            
            System.out.println("Data: ");
            for (String paramCode : params)
            {
                System.out.print("\t" + paramCode + ": ");
                List<String> descList = gribFile.getParameterLevelIdentifiers(paramCode);
                for (int i = 0; i < descList.size(); i++)
                {
                    System.out.print(descList.get(i));
                    if (i != descList.size()-1)
                        System.out.print(", ");
                }
                System.out.println();
            }
            
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Cannot find file: " + gribFilename);
        }
        catch (IOException e)
        {
            System.err.println("Cannot access file: " + gribFilename);
        }
        catch (NotSupportedException e)
        {
            System.err.println("GRIB file contains unsupported features: " + e);
        }
        catch (NoValidGribException e)
        {
            System.err.println("GRIB file is invalid: " + e);
        }
    }
}
