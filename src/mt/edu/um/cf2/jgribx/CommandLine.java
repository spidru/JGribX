
package mt.edu.um.cf2.jgribx;

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
        
        if (cmd.hasOption("v"))
        {
            System.out.println("JGribX " + JGribX.getVersion());
        }
        
    }
}