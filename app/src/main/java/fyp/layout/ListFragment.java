package fyp.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import fyp.layout.util.GnssType;
import fyp.layout.util.GpsTestUtil;

public class ListFragment extends Fragment implements MainActivityListener {
    View myView;

    private static final String TAG = "ListFragment";
    Context context;
    Resources resources;

    TextView tv, mNumSats;

    /*
    private LocationManager locationManager = null;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssStatus mGnssStatus;
    */

    // SvGrid
    private SvGridAdapter mAdapter;
    private static final int COLUMN_COUNT = 6;
    private static final int PRN_COLUMN = 0;
    private static final int FLAG_IMAGE_COLUMN = 1;
    private static final int SNR_COLUMN = 2;
    private static final int ELEVATION_COLUMN = 3;
    private static final int AZIMUTH_COLUMN = 4;
    private static final int FLAGS_COLUMN = 5;
    private String mSnrCn0Title;

    private Drawable mFlagUsa, mFlagRussia, mFlagJapan, mFlagChina, mFlagGalileo;
    private int mSvCount, mUsedInFixCount, mPrns[], mConstellationType[];
    private float mSnrCn0s[], mSvElevations[], mSvAzimuths[];
    private boolean mHasEphemeris[], mHasAlmanac[], mUsedInFix[];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.list_layout, container, false);
        context = container.getContext();
        resources = getResources();

        //locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mNumSats = (TextView) myView.findViewById(R.id.num_sats);

        mFlagUsa = getResources().getDrawable(R.drawable.ic_flag_usa);
        mFlagRussia = getResources().getDrawable(R.drawable.ic_flag_russia);
        mFlagJapan = getResources().getDrawable(R.drawable.ic_flag_japan);
        mFlagChina = getResources().getDrawable(R.drawable.ic_flag_china);
        mFlagGalileo = getResources().getDrawable(R.drawable.ic_flag_galileo);

        GridView gridView = (GridView) myView.findViewById(R.id.SatList);
        mAdapter = new SvGridAdapter(getActivity());
        gridView.setAdapter(mAdapter);
        gridView.setFocusable(false);
        gridView.setFocusableInTouchMode(false);

        MainActivity.getInstance().addListener(this);

        return myView;
    }


    @Override
    public void gpsStart() {

    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {
        if (mPrns == null) {
            final int MAX_LENGTH = 255;
            mPrns = new int[MAX_LENGTH];
            mSnrCn0s = new float[MAX_LENGTH];
            mSvElevations = new float[MAX_LENGTH];
            mSvAzimuths = new float[MAX_LENGTH];
            mConstellationType = new int[MAX_LENGTH];
            mHasEphemeris = new boolean[MAX_LENGTH];
            mHasAlmanac = new boolean[MAX_LENGTH];
            mUsedInFix = new boolean[MAX_LENGTH];
        }

        mSnrCn0Title = "C/N0";

        final int length = status.getSatelliteCount();

        mSvCount = 0;
        mUsedInFixCount = 0;

        while (mSvCount < length) {
            int prn = status.getSvid(mSvCount);
            mPrns[mSvCount] = prn;
            mConstellationType[mSvCount] = status.getConstellationType(mSvCount);
            mSnrCn0s[mSvCount] = status.getCn0DbHz(mSvCount);
            mSvElevations[mSvCount] = status.getElevationDegrees(mSvCount);
            mSvAzimuths[mSvCount] = status.getAzimuthDegrees(mSvCount);
            mHasEphemeris[mSvCount] = status.hasEphemerisData(mSvCount);
            mHasAlmanac[mSvCount] = status.hasAlmanacData(mSvCount);
            mUsedInFix[mSvCount] = status.usedInFix(mSvCount);
            if (status.usedInFix(mSvCount)) {
                mUsedInFixCount++;
            }
            mSvCount++;
        }

        mNumSats.setText("No. of satellites (Fixed satellites  / Total satellites): " + resources.getString(R.string.gps_num_sats_value, mUsedInFixCount, mSvCount));

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGnssStarted() {

    }

    @Override
    public void onGnssStopped() {

    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Toast.makeText(context, "" + latLng, Toast.LENGTH_SHORT).show();
        //tv.setText("" + latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private class SvGridAdapter extends BaseAdapter {
        private Context context;

        public SvGridAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            // add 1 for header row
            return (mSvCount + 1) * COLUMN_COUNT;
        }

        public Object getItem(int position) {
            Log.d(TAG, "getItem(" + position + ")");
            return "foo";
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = null;
            ImageView imageView = null;

            int row = position / COLUMN_COUNT;
            int column = position % COLUMN_COUNT;

            if (convertView != null) {
                if (convertView instanceof ImageView) {
                    imageView = (ImageView) convertView;
                } else if (convertView instanceof TextView) {
                    textView = (TextView) convertView;
                }
            }

            CharSequence text = null;

            if (row == 0) {
                switch (column) {
                    case PRN_COLUMN:
                        //text = resources.getString(R.string.gps_prn_column_label);
                        text = "ID";
                        break;
                    case FLAG_IMAGE_COLUMN:
                        //text = resources.getString(R.string.gps_flag_image_label);
                        text = "GNSS";
                        break;
                    case SNR_COLUMN:
                        text = mSnrCn0Title;
                        break;
                    case ELEVATION_COLUMN:
                        //text = resources.getString(R.string.gps_elevation_column_label);
                        text = "Elev";
                        break;
                    case AZIMUTH_COLUMN:
                        //text = resources.getString(R.string.gps_azimuth_column_label);
                        text = "Azim";
                        break;
                    case FLAGS_COLUMN:
                        //text = resources.getString(R.string.gps_flags_column_label);
                        text = "Flags";
                        break;
                }
            } else {
                row--;
                switch (column) {
                    case PRN_COLUMN:
                        text = Integer.toString(mPrns[row]);
                        break;
                    case FLAG_IMAGE_COLUMN:
                        if (imageView == null) {
                            imageView = new ImageView(context);
                            //imageView.setScaleType(ImageView.ScaleType.FIT_START);
                            imageView.setLayoutParams(new GridView.LayoutParams(60, 40));
                        }
                        GnssType type;
                        type = GpsTestUtil.getGnssConstellationType(mConstellationType[row]);
                        /*if (GpsTestUtil.isGnssStatusListenerSupported() && !mUseLegacyGnssApi) {
                            type = GpsTestUtil.getGnssConstellationType(mConstellationType[row]);
                        } else {
                            type = GpsTestUtil.getGnssType(mPrns[row]);
                        }*/
                        switch (type) {
                            case NAVSTAR:
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageDrawable(mFlagUsa);
                                break;
                            case GLONASS:
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageDrawable(mFlagRussia);
                                break;
                            case QZSS:
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageDrawable(mFlagJapan);
                                break;
                            case BEIDOU:
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageDrawable(mFlagChina);
                                break;
                            case GALILEO:
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageDrawable(mFlagGalileo);
                                break;
                            case UNKNOWN:
                                imageView.setVisibility(View.INVISIBLE);
                                break;
                        }
                        return imageView;
                    case SNR_COLUMN:
                        if (mSnrCn0s[row] != 0.0f) {
                            text = Float.toString(mSnrCn0s[row]);
                        } else {
                            text = "";
                        }
                        break;
                    case ELEVATION_COLUMN:
                        if (mSvElevations[row] != 0.0f) {
                            text = resources.getString(R.string.gps_elevation_column_value,
                                    Float.toString(mSvElevations[row]));
                        } else {
                            text = "";
                        }
                        break;
                    case AZIMUTH_COLUMN:
                        if (mSvAzimuths[row] != 0.0f) {
                            text = resources.getString(R.string.gps_azimuth_column_value,
                                    Float.toString(mSvAzimuths[row]));
                        } else {
                            text = "";
                        }
                        break;
                    case FLAGS_COLUMN:
                        char[] flags = new char[3];
                        flags[0] = !mHasEphemeris[row] ? ' ' : 'E';
                        flags[1] = !mHasAlmanac[row] ? ' ' : 'A';
                        flags[2] = !mUsedInFix[row] ? ' ' : 'U';
                        text = new String(flags);
                        break;
                }
            }

            if (textView == null) {
                textView = new TextView(context);
            }

            textView.setText(text);
            return textView;
        }
    }
}

