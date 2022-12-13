package mt.edu.um.cf2.jgribx.grib2;

import mt.edu.um.cf2.jgribx.Bytes2Number;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.Logger;

import java.io.IOException;

public class Grib2RecordGdsLambertConformal extends Grib2RecordGDS
{
    private int gridNx;
    private int gridNy;

    public Grib2RecordGdsLambertConformal(GribInputStream in) throws IOException {
        super(in);

        /* [15] Shape of the Earth */
        earthShape = in.readUINT(1);

        /* [16] Scale factor of radius of spherical Earth */
        int radiusScaleFactor = in.readUINT(1);

        /* [17-20] Scale value of radius of spherical Earth */
        int radiusScaledValue = in.readUINT(4);

        /* [21] Scale factor of major axis of oblate spheroid Earth */
        int majorScaleFactor = in.readUINT(1);

        /* [22-25] Scale value of major axis of oblate spheroid Earth */
        int majorScaledValue = in.readUINT(4);

        /* [26] Scale factor of minor axis of oblate spheroid Earth */
        int minorScaleFactor = in.readUINT(1);

        /* [27-30] Scale value of minor axis of oblate spheroid Earth */
        int minorScaledValue = in.readUINT(4);

        /* [31-34] Nx - Number of points along x-axis */
        gridNx = in.readUINT(4);

        /* [35-38] Ny - Number of points along y-axis */
        gridNy = in.readUINT(4);

        /* [39-42] La1 - Latitude of first grid point */
        lat1 = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        /* [43-46] Lo1 - Longitude of first grid point */
        lon1 = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        /* [47] Resolution and component flags */
        int flags = in.readUINT(1);

        /* [48-51] LaD - Latitude where Dx and Dy are specified */
        double lad = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        /* [52-55] LoV - Longitude of meridian parallel to y-axis along which latitude increases as the y-coordinate increases */
        double lov = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        /* [56-59] Dx - x-direction grid length */
        double dx = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        /* [60-63] Dy - y-direction grid length */
        double dy = in.readINT(4, Bytes2Number.INT_SM) / 1e6;

        Logger.println("lat1 = " + lat1, Logger.INFO);
        Logger.println("LaD = " + lad, Logger.INFO);
        Logger.println("LoV = " + lov, Logger.INFO);
        Logger.println("Dx = " + dx, Logger.INFO);
        Logger.println("Dy = " + dy, Logger.INFO);
    }

    @Override
    protected double[][] getGridCoords() {
        return new double[0][];
    }

    @Override
    protected double[] getGridXCoords() {
        return new double[0];
    }

    @Override
    protected double[] getGridYCoords() {
        return new double[0];
    }

    @Override
    protected double getGridDeltaX() {
        return 0;
    }

    @Override
    protected double getGridDeltaY() {
        return 0;
    }

    @Override
    protected double getGridLatStart() {
        return 0;
    }

    @Override
    protected double getGridLonStart() {
        return 0;
    }

    @Override
    protected int getGridSizeX() {
        return 0;
    }

    @Override
    protected int getGridSizeY() {
        return 0;
    }
}
