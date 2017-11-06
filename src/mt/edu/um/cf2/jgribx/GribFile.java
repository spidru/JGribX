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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map gridMap = new HashMap();
        List typeList = new ArrayList();
        List descList = new ArrayList();
        List lightRecList = new ArrayList();
        
        records = new ArrayList();

        /**
        * Initialize the Parameter Tables with the information in the parameter
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
                GribRecordES.seekNext(in);
                continue;
            }
            GribRecordIS is = record.getIS();
            
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
    * Get the number of records this GRIB file contains.
    *
    * @return number of records in this GRIB file
    */
   public int getRecordCount()
   {
      return records.size();
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
