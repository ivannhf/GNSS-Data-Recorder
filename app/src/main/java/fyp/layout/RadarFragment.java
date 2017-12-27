package fyp.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// from sample
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.IGpsStatusListener;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
// from sample


public class RadarFragment extends Fragment implements SatelliteDataProvider {

    View myView;


    //public final static String TAG = "SatelliteStatusActivity";

    int mSatellites = 0;
    int[] mPnrs = new int[maxSatellites];
    float[] mSnrs = new float[maxSatellites];
    float[] mElevation = new float[maxSatellites];
    float[] mAzimuth = new float[maxSatellites];
    int ephemerisMask = 0;
    int almanacMask = 0;
    int mUsedInFixMask = 0;

    public void setSatelliteStatus(int svCount, int[] prns, float[] snrs, float[] elevations,
                                   float[] azimuths, int ephemerisMask,
                                   int almanacMask, int usedInFixMask){
        synchronized(this){
            mSatellites = svCount;
            System.arraycopy(prns, 0, mPnrs, 0, mSatellites);
            System.arraycopy(snrs, 0, mSnrs, 0, mSatellites);
            System.arraycopy(elevations, 0, mElevation, 0, mSatellites);
            System.arraycopy(azimuths, 0, mAzimuth, 0, mSatellites);
            mUsedInFixMask = usedInFixMask;
        }
        mSatelliteView.postInvalidate();
        mSignalView.postInvalidate();
    }

    public int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
                                  float[] azimuths, int ephemerisMask,
                                  int almanacMask, int[] usedInFixMask){
        synchronized(this){
            if (prns != null){
                System.arraycopy(mPnrs, 0, prns, 0, mSatellites);
            }
            if (snrs != null){
                System.arraycopy(mSnrs, 0, snrs, 0, mSatellites);
            }
            if (azimuths != null){
                System.arraycopy(mAzimuth, 0, azimuths, 0, mSatellites);
            }
            if (elevations != null){
                System.arraycopy(mElevation, 0, elevations, 0, mSatellites);
            }
            if (usedInFixMask != null){
                usedInFixMask[0] = mUsedInFixMask;
            }
            return mSatellites;
        }
    }

    private SatelliteSkyView mSatelliteView = null;
    private SatelliteSignalView mSignalView = null;
    private LocationManager mLocationManager = null;
    private ILocationManager mILM;
    private IGpsStatusListener mGpsListener = null;
    private LocationListener mLocListener = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.radar_layout, container, false);


        mSatelliteView = (SatelliteSkyView) myView.findViewById(R.id.skyview);
        mSatelliteView.setDataProvider(this);
        mSignalView = (SatelliteSignalView) myView.findViewById(R.id.signalview);
        mSignalView.setDataProvider(this);
        try {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Class c = Class.forName(mLocationManager.getClass().getName());
            Field f = c.getDeclaredField("mService");
            f.setAccessible(true);
            mILM = (ILocationManager) f.get(mLocationManager);

            mGpsListener = new GpsListener();
            mILM.addGpsStatusListener(mGpsListener);
            mLocListener = (LocationListener) mGpsListener;
            mLocationManager.requestLocationUpdates("gps", 0, 0, mLocListener);

        } catch (SecurityException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        } catch (RemoteException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }


        return myView;
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean supRetVal = super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, getString(R.string.menu_copyright));
        return supRetVal;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ComponentName comp;
        switch (item.getItemId()) {
            case 0:
                comp = new ComponentName(this.getPackageName(), CopyrightInfo.class.getName());
                startActivity(new Intent().setComponent(comp));
                return true;
        }
        return false;
    }


    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocListener);
        try {
            mILM.removeGpsStatusListener(mGpsListener);
        } catch (RemoteException e) {
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mLocationManager.requestLocationUpdates("gps", 0, 0, mLocListener);
        try {
            mILM.addGpsStatusListener(mGpsListener);
        } catch (RemoteException e){
        }

    }



    private class GpsListener extends IGpsStatusListener.Stub implements LocationListener {
        @Override
        public void onFirstFix(int ttff) throws RemoteException {
            CharSequence txt = new String("GPS Fix afer " + ttff + "ms");
            Toast t = Toast.makeText(SatelliteStatus.this, txt, Toast.LENGTH_LONG);
            t.show();
        }

        @Override
        public void onGpsStarted() throws RemoteException {
            CharSequence txt = new String("GPS started");
            Toast t = Toast.makeText(SatelliteStatus.this, txt, Toast.LENGTH_SHORT);
            t.show();
        }

        @Override
        public void onGpsStopped() throws RemoteException {
            CharSequence txt = new String("GPS stopped");
            Toast t = Toast.makeText(SatelliteStatus.this, txt, Toast.LENGTH_SHORT);
            t.show();
        }

        @Override
        public void onSvStatusChanged(int svCount, int[] prns, float[] snrs,
                                      float[] elevations, float[] azimuths, int ephemerisMask,
                                      int almanacMask, int usedInFixMask) throws RemoteException {
            setSatelliteStatus(svCount, prns, snrs, elevations, azimuths,
                    ephemerisMask, almanacMask, usedInFixMask);
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }


}
