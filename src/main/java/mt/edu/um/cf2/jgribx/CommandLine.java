
package mt.edu.um.cf2.jgribx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
        
        /* File Information */
        Option inputFile = new Option("i", "input", true, "Specify an input file");
        inputFile.setRequired(false);
        options.addOption(inputFile);
        
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
        }
        
    }
}
