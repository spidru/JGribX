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
package test;

import java.io.FileNotFoundException;
import java.io.IOException;
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
        Logger.setLevel(Logger.WARNING);
        
        String gribFilename = "test/gfsanl_3_20160324_0600_000.grb";
//        String gribFilename = "test/gfsanl_3_20170512_0000_000.grb2";
        
        // Prepare format for reference times
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        try
        {
            GribFile gribFile = new GribFile(gribFilename);
            
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
            System.err.println("Cannot find " + gribFilename);
        }
        catch (IOException e)
        {
            System.err.println("Cannot access " + gribFilename);
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
