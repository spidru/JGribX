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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The GribFile class represents a GRIB file containing any number of records,
 * stored within the class as a {@link List}{@code <}{@link GribRecord}{@code >}.
 * 
 * This class can be considered as a top-level class which does not deal with
 * the underlying format of the records within the file. This means that this
 * class remains valid for all formats such as GRIB-1 and GRIB-2. 
 *
 */

public class GribFile
{
    private String filename;
    private int nRecordsSkipped;
    
    /**
     * List of GRIB records
     */
    private List<GribRecord> records;

   /**
    * Constructs a {@link GribFile} object from a file.
    *
    * @param filename name of the GRIB file
    *
    * @throws FileNotFoundException if file cannot be found
    * @throws IOException           if file cannot be opened etc.
    * @throws NotSupportedException if file contains features not yet supported
    * @throws NoValidGribException  if file is no valid GRIB file
    */
    public GribFile(String filename) throws FileNotFoundException,
        IOException, NotSupportedException, NoValidGribException
    {
        this(new FileInputStream(filename));
        this.filename = filename;
    }

   /**
    * Constructs a {@link GribFile} object from an input stream.
    *
    * @param in input stream with GRIB content
    *
    * @throws IOException           if stream cannot be opened etc.
    * @throws NotSupportedException if file contains features not yet supported
    * @throws NoValidGribException  if stream does not contain a valid GRIB file
    */
   public GribFile(InputStream in) throws IOException,
          NotSupportedException, NoValidGribException
   {
       // note: the BufferedInputStream enables mark/reset functionality
       this(new GribInputStream(new BufferedInputStream(in)));
   }

   /**
    * Constructs a {@link GribFile} object from a bit input stream.
    *
    * @param in bit input stream with GRIB content
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NotSupportedException if file contains features not yet supported
    * @throws NoValidGribException  if stream does not contain a valid GRIB file
    */
    public GribFile(GribInputStream in) throws IOException,
          NotSupportedException, NoValidGribException
    {
        // Initialise fields
        nRecordsSkipped = 0;
        records = new ArrayList();

        /**
        * Initialise the Parameter Tables with the information in the parameter
        * table lookup file.  See GribPDSParamTable for details
        */
        //GribPDSParamTable.readParameterTableLookup(); done in static initializer

        int count = 0;
        while (in.available() > 0)
        {
            count++;
            GribRecord record;
            try
            {
                record = GribRecord.readFromStream(in);
            }
            catch (NotSupportedException|NoValidGribException e)
            {
                Logger.println("Skipping GRIB record "+count+" ("+e.getMessage()+")", Logger.WARNING);
                nRecordsSkipped++;
                GribRecordES.seekNext(in);
                continue;
            }

            Logger.println("GRIB Record "+count, Logger.INFO);
            Logger.println("\tReference Time: "+record.getReferenceTime().getTime().toString(), Logger.INFO);
            Logger.println("\tParameter: "+record.getParameterCode()+" ("+record.getParameterDescription()+")", Logger.INFO);
            Logger.println("\tLevel: "+record.getLevelCode()+" ("+record.getLevelDescription()+")", Logger.INFO);
            
            records.add(record);
        }
        
        Logger.println("Reached end of file: "+records.size()+" of "+count+" records read successfully", Logger.INFO);
        in.close();

        if (records.isEmpty())
           Logger.println("No GRIB file or no records found.", Logger.WARNING);
    }
    
    /**
     * Returns the different originating centre IDs found in the GRIB file.
     * @return the different originating centre IDs found in the GRIB file
     */
    public int[] getCentreIDs()
    {
        List<Integer> idList = new ArrayList();
        for (GribRecord record : records)
        {
            int id = record.getCentreId();
            if (!idList.contains(id))
                idList.add(id);
        }
        int[] ids = new int[idList.size()];
        for (int i = 0; i < idList.size(); i++)
            ids[i] = idList.get(i);
        return ids;
    }
    
    public int getEdition()
    {
        int edition = records.get(0).getIS().getGribEdition();
        
        // Check if GRIB file contains different editions
        // TODO not sure if different editions within one file should be allowed
        for (int i = 1; i < records.size(); i++)
        {
            if (records.get(i).getIS().getGribEdition() != edition)
            {
                Logger.println("GRIB file contains different editions", Logger.WARNING);
                break;
            }
        }
        
        return edition;
    }
    
    /**
     * Returns the GRIB filename.
     * @return the GRIB filename
     */
    public String getFilename()
    {
        return filename;
    }
    
    public List<Calendar> getForecastTimes()
    {
        boolean matchFound;
        List<Calendar> forecastTimeList = new ArrayList();
        for (GribRecord record : records)
        {
            // Compare dates
            matchFound = false;
            for (Calendar forecastTime : forecastTimeList)
            {
                if (forecastTime.compareTo(record.getForecastTime()) == 0)
                {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound)
                forecastTimeList.add(record.getForecastTime());
            
        }
        Collections.sort(forecastTimeList);
        return forecastTimeList;
    }
    
    /**
     * Returns a sorted list of different parameter codes present within the
     * GRIB file.
     * 
     * @return a sorted list of different parameter codes present within the
     * GRIB file.
     */
    public List<String> getParameterCodes()
    {
            /**
     * List of different parameters present in the GRIB file
     */
        List<String> parameterList = new ArrayList();
        for (GribRecord record : records)
        {
            if (!parameterList.contains(record.getParameterCode()))
            {
                // Add parameter to list
                parameterList.add(record.getParameterCode());
            }
        }
        Collections.sort(parameterList);
        return parameterList;
    }
    
    /**
     * Returns a sorted list of different parameter levels as textual
     * descriptions.
     * 
     * @return a sorted list of different parameter levels as textual
     * descriptions
     */
    public List<String> getParameterLevelDescriptions()
    {
        List<String> levelDescList = new ArrayList();
        for (GribRecord record : records)
        {
            if (!levelDescList.contains(record.getLevelDescription()))
            {
                // Add level to list
                levelDescList.add(record.getLevelDescription());
            }
        }
        Collections.sort(levelDescList);
        return levelDescList;
    }
    
    public List<String> getParameterLevelDescriptions(String paramCode)
    {
        List<String> descList = new ArrayList();
        for (GribRecord record : records)
        {
            if (record.getParameterCode().equals(paramCode))
            {
                if (descList.contains(record.getLevelIdentifier()))
                    System.err.println("Record contains duplicate level IDs");
                else
                    descList.add(record.getLevelDescription());
            }
        }
        return descList;
    }
    
    public List<String> getParameterLevelIdentifiers(String paramCode)
    {
        List<String> idList = new ArrayList();
        for (GribRecord record : records)
        {
            if (record.getParameterCode().equals(paramCode))
            {
                if (idList.contains(record.getLevelIdentifier()))
                    ;//System.err.println("Record contains duplicate level IDs");
                else
                    idList.add(record.getLevelIdentifier());
            }
        }
        return idList;
    }
    
    /**
     * Returns the different generating process IDs found in the GRIB file.
     * @return the different generating process IDs found in the GRIB file
     */
    public int[] getProcessIDs()
    {
        List<Integer> idList = new ArrayList();
        for (GribRecord record : records)
        {
            int id = record.getProcessId();
            if (!idList.contains(id))
                idList.add(id);
        }
        int[] ids = new int[idList.size()];
        for (int i = 0; i < idList.size(); i++)
            ids[i] = idList.get(i);
        return ids;
    }
        
    /**
    * Get the number of records this GRIB file contains.
    *
    * @return number of records in this GRIB file
    */
   public int getRecordCount()
   {
      return records.size();
   }
   
    /**
     * Returns the number of records skipped due to them being invalid or not
     * supported.
     * @return the number of records skipped
     */
    public int getRecordsSkippedCount()
    {
        return nRecordsSkipped;
    }
   
    /**
     * Get all the records successfully read.
     * @return 
     */
    public List<GribRecord> getRecords()
    {
        return records;
    }
    
    public GribRecord getRecord(Calendar time, String parameterAbbrev, String levelCode)
    {
        // Find closest time
        long delta_ms;
        long deltaMin_ms = Long.MAX_VALUE;
        Calendar closestTime = null;
        for (GribRecord record : records)
        {
            delta_ms = Math.abs(time.getTimeInMillis() - record.getReferenceTime().getTimeInMillis());
            if (delta_ms < deltaMin_ms)
            {
                deltaMin_ms = delta_ms;
                closestTime = record.getReferenceTime();
            }
        }
        
        Pattern pattern = Pattern.compile("(\\w+):(\\d+)?");
        Matcher matcher;
        for (GribRecord record : records)
        {
            matcher = pattern.matcher(levelCode);
            if (matcher.find())
            {
                String code = matcher.group(1);
                int value = 0;
                if (matcher.groupCount() == 2)
                    value = Integer.parseInt(matcher.group(2));
                if (
                        record.getReferenceTime().equals(closestTime) && 
                        record.getParameterCode().equals(parameterAbbrev) && 
                        record.getLevelCode().equals(code) &&
                        (
                            matcher.groupCount() == 1 ||
                            (
                                matcher.groupCount() == 2 && record.getLevelValues()[0] == value
                            )
                        )
                    )
                {
                    return record;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Return a List of different reference times present in the GRIB file.
     * @return a sorted list of different reference times
     */
    public List<Calendar> getReferenceTimes()
    {
        List<Calendar> referenceTimeList = new ArrayList();
        for (GribRecord record : records)
        {
            if (!referenceTimeList.contains(record.getReferenceTime()))
            {
                // Add reference time to list
                referenceTimeList.add(record.getReferenceTime());
            }
        }
        Collections.sort(referenceTimeList);
        return referenceTimeList;
    }
    
    /**
     * Prints out a summary of the GRIB file.
     * @param out 
     */
    public void getSummary(PrintStream out)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        int[] centreIds = this.getCentreIDs();
        int[] processIds = this.getProcessIDs();
        List<Calendar> refDates = this.getReferenceTimes();
        List<Calendar> forecastDates = this.getForecastTimes();
        
        // Print out generic GRIB file info
        out.println("---------------------------------------");
        out.println("Reading file: " + this.filename);
        out.println("GRIB Edition: " + this.getEdition());
        out.println("Records successfully read: " + this.getRecordCount() + " of "
                + (this.getRecordCount() + this.getRecordsSkippedCount()));
        out.println("---------------------------------------");
        
        // Print out originating centre info
        out.print("Weather Centre(s): ");
        for (int i = 0; i < centreIds.length; i++)
        {
            out.print(centreIds[i]
                    + " [" + GribCodes.getCentreName(centreIds[i]) + "]");
            if (i != centreIds.length - 1) out.print(",");
        }
        out.println();
        
        // Print out generating process info
        out.print("Generating Process(es): ");
        for (int i = 0; i < processIds.length; i++)
        {
            out.print(processIds[i]
                    + " [" + GribCodes.getProcessName(processIds[i]) + "]");
            if (i != processIds.length - 1) out.print(",");
        }
        out.println();
        
        // Get reference time
        System.out.println("Reference Time: ");
        for (Calendar date : refDates)
        {
            System.out.println("\t" + sdf.format(date.getTime()));
        }
        
        // Get forecast times
        System.out.println("Forecast Time(s): ");
        for (Calendar date : forecastDates)
        {
            System.out.println("\t" + sdf.format(date.getTime()));
        }
    }
    
   /**
    * Get a string representation of the GRIB file.
    *
    * @return NoValidGribException   if record is no valid GRIB record
    */
    @Override
    public String toString()
    {
        return "GRIB file (" + records.size() + " records)";
    }
    


}
