package fyp.layout;


public interface SatelliteDataProvider {
    public final static int maxSatellites = 15;

    public void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
                                   float[] elevations, float[] azimuths, int ephemerisMask,
                                   int almanacMask, int usedInFixMask);

    public int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
                                  float[] azimuths, int ephemerisMask,
                                  int almanacMask, int[] usedInFixMask);
}
