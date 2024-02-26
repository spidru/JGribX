package mt.edu.um.cf2.jgribx;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GribTest
{
    @BeforeClass
    public static void setUpBeforeClass()
    {
        // Prepare format for reference times
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Configure logging
        Logger.setLoggingMode(Logger.LoggingMode.CONSOLE);
        JGribX.setLoggingLevel(Logger.DEBUG);
    }

    @Test
    public void testVersion()
    {
        // Check that version string format conforms to SemVer (taken from: https://semver.org/)
        Pattern pattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
        String version = JGribX.getVersion();
        Matcher match = pattern.matcher(version);
        assertTrue("Version string format is valid: " + version, match.find());
    }

    @Test
    public void testGrib1Gfs3() throws NotSupportedException, IOException, NoValidGribException
    {
        final String FILENAME = "/CF2_20150706_092531.grb";

        // Define expected data
        final int N_RECORDS_EXPECTED = 304;
        final int GRIB_EDITION = 1;
        final int[] WEATHER_CENTRES = {7};
        final int[] GENERATING_PROCESSES = {81, 96};

        URL url = GribTest.class.getResource(FILENAME);
        System.out.println("Path to file: " + url);
        GribFile gribFile = new GribFile(url.openStream());

        assertEquals("Records read successfully", N_RECORDS_EXPECTED, gribFile.getRecordCount());
        assertEquals("GRIB edition", GRIB_EDITION, gribFile.getEdition());
        assertArrayEquals("Weather centres", WEATHER_CENTRES, gribFile.getCentreIDs());
        assertArrayEquals("Generating processes", GENERATING_PROCESSES, gribFile.getProcessIDs());

        // Compare values in each record against a "gold standard" (wgrib)
        for (int i_record = 0; i_record < gribFile.getRecordCount(); i_record++)
        {
            // Get values from GRIB file using JGribX
            GribRecord record = gribFile.getRecords().get(i_record);
            float[] obtainedValues = record.getValues();

            String gribFilepath = "";
            try {
                gribFilepath = new File(url.toURI()).getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ProcessBuilder pb = new ProcessBuilder(
                    System.getenv("WGRIB"),
                    gribFilepath,
                    "-d", String.valueOf(i_record + 1),
                    "-text",
                    "-nh"
            );
            String cmd = "";
            for (String word : pb.command())
            {
                cmd += word + " ";
            }
            File wgribDir = pb.directory();
            String wgribCwd;
            if (wgribDir == null)
            {
                wgribCwd = System.getProperty("user.dir");
            }
            else
            {
                wgribCwd = wgribDir.getAbsolutePath();
            }
            System.out.println(String.format("Executing in %s: %s", wgribCwd, cmd));
            try
            {
                Process process = pb.start();
                boolean exited = process.waitFor(2, TimeUnit.SECONDS);
                assertTrue("wgrib has not exited", exited);
                assertTrue(String.format("wgrib has returned error code %d", process.exitValue()), process.exitValue() == 0);
            } catch (IOException | InterruptedException e)
            {
                System.err.println("Exception: " + e.getMessage());
            }

            // Read dump file
            try (BufferedReader reader = new BufferedReader(new FileReader("dump"));)
            {
                String line;

                int i = 0;
                while ((line = reader.readLine()) != null)
                {
		    // Calculate the tolerance based on the maximum data value
                    double tolerance = 0;
                    double maxValue = GribTest.getMaxValue(obtainedValues);
                    if (maxValue >= 100e3) { tolerance = 0.9; }
                    else if (maxValue >= 10e3) { tolerance = 0.5; }
                    else if (maxValue >= 1e3) { tolerance = 0.05; }
                    else if (maxValue >= 100) { tolerance = 0.005; }
                    else if (maxValue >= 10) { tolerance = 0.0005; }
                    else { tolerance = 0.00005; }

                    float expectedValue = Float.parseFloat(line);

                    assertEquals(String.format("Record %d entry %d", i_record, i),
                            expectedValue, obtainedValues[i], tolerance);
                    i++;
                }
            } catch (IOException e) {
                System.out.println("Exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testGrib2Gfs3() throws IOException, NoValidGribException, NotSupportedException
    {
        final String FILENAME = "/gfsanl_3_20170512_0000_000.grb2";

        // Define expected data
        final int N_RECORDS_EXPECTED = 352;
        final int EDITION = 2;
        final int[] WEATHER_CENTRES = {7};
        final int[] GENERATING_PROCESSES = {81};
        List<Calendar> refTimes = new ArrayList<Calendar>();
        refTimes.add(new GregorianCalendar(2017, Calendar.MAY, 12, 0, 0, 0));

        URL url = GribTest.class.getResource(FILENAME);
        GribFile file = new GribFile(url.openStream());

        assertEquals("GRIB edition", EDITION, file.getEdition());
        assertEquals("Reference time(s)", refTimes, file.getReferenceTimes());
        assertArrayEquals("Weather centres", WEATHER_CENTRES, file.getCentreIDs());
        assertArrayEquals("Generating processes", GENERATING_PROCESSES, file.getProcessIDs());
        assertEquals("Records read successfully", N_RECORDS_EXPECTED, file.getRecordCount());
    }

    @Test
    public void testGrib2QuasiRegularGrid() throws IOException, NoValidGribException, NotSupportedException
    {
        /* TODO
         * - Resolve warnings: Record contains multiple PDS's
         */
        final String FILENAME = "/fh.000_tl.press_ar.octanti";

        // Define expected data
        final int EDITION = 2;
        final int[] WEATHER_CENTRES = {7};

        URL url = GribTest.class.getResource(FILENAME);
        GribFile file = new GribFile(url.openStream());

        assertEquals("GRIB edition", EDITION, file.getEdition());
        assertArrayEquals("Weather centres", WEATHER_CENTRES, file.getCentreIDs());
    }

    private static float getMaxValue(float[] values)
    {
        float max = values[0];
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] > max) { max = values[i]; }
        }
        return max;
    }
}
