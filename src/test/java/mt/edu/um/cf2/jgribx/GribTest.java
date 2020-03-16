package mt.edu.um.cf2.jgribx;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GribTest
{
    @Test
    public void testVersion()
    {
        // Check that version string format conforms to SemVer (taken from: https://semver.org/)
        Pattern pattern = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
        String version = JGribX.getVersion();
        Matcher match = pattern.matcher(version);
        assertTrue("Version string format is valid: " + version, match.find());
    }

}
