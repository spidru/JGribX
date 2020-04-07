package mt.edu.um.cf2.jgribx;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
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
    }

    @Test
    public void testGrib2Gfs3() throws IOException, NoValidGribException, NotSupportedException
    {
        /* TODO
         * Unsupported parameter: D:LAND_SURFACE C:4 N:2
         * Unsupported parameter: D:LAND_SURFACE C:0 N:2
         * Unsupported parameter: D:LAND_SURFACE C:0 N:192
         * Unsupported parameter: D:LAND_SURFACE C:0 N:201
         * Unsupported parameter: D:LAND_SURFACE C:3 N:203
         * Unsupported parameter: D:METEOROLOGICAL C:6 N:201
         * Unsupported parameter: D:METEOROLOGICAL C:7 N:192
         * Unsupported parameter: D:METEOROLOGICAL C:7 N:6
         * Unsupported parameter: D:METEOROLOGICAL C:7 N:7
         * Skipping GRIB record 274 (Record does not have a valid GRIB header)
         * Unsupported level of type 104
         * Skipping GRIB record 337 (Record does not have a valid GRIB header)
         * Unsupported parameter: D:OCEANOGRAPHIC C:2 N:0
         * Unsupported level of type 109
         */
        final String FILENAME = "/gfsanl_3_20170512_0000_000.grb2";

        // Define expected data
        final int N_RECORDS_EXPECTED = 0;
        final int EDITION = 2;
        final int[] WEATHER_CENTRES = {7};
        final int[] GENERATING_PROCESSES = {81};
        List<Calendar> refTimes = new ArrayList<Calendar>();
        refTimes.add(new GregorianCalendar(2017, Calendar.MAY, 12, 0, 0, 0));

        URL url = GribTest.class.getResource(FILENAME);
        GribFile file = new GribFile(url.openStream());

        assertEquals("GRIB edition", EDITION, file.getEdition());
        assertEquals("Reference time(s)", file.getReferenceTimes(), refTimes);
        assertArrayEquals("Weather centres", WEATHER_CENTRES, file.getCentreIDs());
        assertArrayEquals("Generating processes", GENERATING_PROCESSES, file.getProcessIDs());
    }

}
