
package mt.edu.um.cf2.jgribx;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import jj2000.j2k.JJ2KInfo;

/**
 *
 * @author spidru
 */
public class CommandLine {
    public static void main(String[] args)
    {
        Options options = new Options();
        
        /* Version Information */
        Option version = new Option("v", "version", false, "Show version information");
        version.setRequired(false);
        options.addOption(version);
        
        /* Input File */
        Option inputFile = new Option("i", "input", true, "Specify an input file");
        inputFile.setRequired(false);
        options.addOption(inputFile);
        
        /* Logging Level */
        Option logLevel = new Option("l", "loglevel", true, "Specify the logging level");
        logLevel.setRequired(false);
        options.addOption(logLevel);

        /* Inventory file */
        Option invFile = new Option("g", "generate-inventory", false, "Generate inventory file");
        invFile.setRequired(false);
        options.addOption(invFile);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        org.apache.commons.cli.CommandLine cmd = null;
        
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("JGribX", options);
            System.exit(1);
        }

        /* If no arguments have been specified, display help */
        if (cmd.getOptions().length == 0)
        {
            System.err.println("No arguments specified");
            formatter.printHelp("JGribX", options);
            System.exit(1);
        }
        
        /* Must be first option processed due to logging */
        if (cmd.hasOption("l"))
        {
            int level = Integer.parseInt(cmd.getOptionValue("l"));
            if (level > 0 && level < 6)
            {
                Logger.setLoggingMode(Logger.LoggingMode.CONSOLE);
                JGribX.setLoggingLevel(level - 1);
            }
        }

        if (cmd.hasOption("i"))
        {
            String inputFilePath = cmd.getOptionValue("i");
            try
            {
                GribFile gribFile = new GribFile(inputFilePath);
            
                // Print out generic GRIB file info
                gribFile.getSummary(System.out);
                
                List<String> params = gribFile.getParameterCodes();
                System.out.println("Parameters:");
                for (String param : params)
                {
                    System.out.print(param + " ");
                }
                System.out.println();

                // Generate inventory file if specified
                // Reference: https://ftp.cpc.ncep.noaa.gov/wd51we/wgrib/readme
                if (cmd.hasOption("g"))
                {
                    Path path = Paths.get(inputFilePath);
                    String invFilePath = path.getFileName() + ".inv";
                    System.out.println("Generating inventory file: " + invFilePath);
                    List<String> lines = new ArrayList<>();
                    for (int iRecord = 0; iRecord < gribFile.getRecordCount(); iRecord++)
                    {
                        GribRecord record = gribFile.getRecords().get(iRecord);
                        long byteOffset = record.getIS().getByteOffset();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
                        Date forecastDate = record.getReferenceTime().getTime();
                        long forecastDelta_s = (record.getForecastTime().getTimeInMillis() - record.getReferenceTime().getTimeInMillis()) / 1000;
                        int forecastDelta_h = Math.round((float) forecastDelta_s / 3600);
                        String genProcessTypeAcronym = record.getGeneratingProcess().getAcronym();
                        if (record.getGeneratingProcess().getType() == GeneratingProcess.Type.FORECAST)
                        {
                            genProcessTypeAcronym += " " + forecastDelta_h + "h";
                        }

                        String line = String.format(
                            "%d:%d:d=%s:%s:%s:%s",
                            iRecord + 1,
                            byteOffset,
                            dateFormat.format(forecastDate),
                            record.getParameterCode(),
                            record.getLevelCode(),
                            genProcessTypeAcronym
                        );

                        lines.add(line);
                    }

                    // Write to file
                    Files.write(Paths.get(invFilePath), lines, StandardCharsets.UTF_8);
                }
            }
            catch (FileNotFoundException e)
            {
                System.err.println("Cannot find file: " + inputFilePath);
            }
            catch (IOException e)
            {
                System.err.println("Could not open file: " + inputFilePath);
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
        
        if (cmd.hasOption("v"))
        {
            System.out.println("JGribX " + JGribX.getVersion());
            System.out.println("\nExternal Modules:");
            System.out.println("jj2000 " + JJ2KInfo.version + " (JPEG 2000 decoder)");
        }
    }
}
