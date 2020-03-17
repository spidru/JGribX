package mt.edu.um.cf2.jgribx;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GribTest
{
    protected void setUp()
    {
        // Prepare format for reference times
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
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

}
