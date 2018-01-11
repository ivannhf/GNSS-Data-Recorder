package fyp.recorder;

import android.Manifest;
//import android.app.FragmentManager;
//import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import fyp.layout.R;
import fyp.recorder.util.GpsTestUtil;
import fyp.recorder.util.MathUtils;

import static com.google.common.base.Preconditions.checkArgument;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener, SensorEventListener {

    private static final String TAG = "MainActivity";
    private static MainActivity sInstance;
    SharedPreferences setting;
    private bkgdService sv;

    double gyroX = 0, gyroY = 0, gyroZ = 0, accelX = 0, accelY = 0, accelZ = 0, heading = 0;
    double orientation = Double.NaN;
    double tilt = Double.NaN;

    FragmentManager fragmentManager;
    PositionFragment positionFragment;
    ListFragment listFragment;
    RadarFragment radarFragment;
    LogFragment logFragment;
    MapFragment mapFragment;
    ToolFragment toolFragment;

    LoggerFile loggerFile;
    LoggerFileRINEX loggerFileRINEX;
    LoggerUI loggerUI;
    int logFileType = 1;

    // Listeners for Fragments
    private ArrayList<MainActivityListener> mMainActivityListeners = new ArrayList<MainActivityListener>();
    boolean mStarted;
    boolean logging = false;

    private LocationManager locationManager;
    private Location mLastLocation;
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener;
    private GnssNavigationMessage.Callback mGnssNavigationMessageListener;
    boolean mWriteGnssMeasurementToLog;

    // Sensor Event
    private SensorManager mSensorManager;
    private Sensor gyroSensor, accSensor, magSensor;
    private static boolean mTruncateVector = false;
    private static float[] mRotationMatrix = new float[16];
    private static float[] mRemappedMatrix = new float[16];
    private static float[] mValues = new float[3];
    private static float[] mTruncatedRotationVector = new float[4];
    boolean mFaceTrueNorth;
    private GeomagneticField mGeomagneticField;

    FloatingActionButton fab, fab_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        setting = PreferenceManager.getDefaultSharedPreferences(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        positionFragment = new PositionFragment();
        listFragment = new ListFragment();
        radarFragment = new RadarFragment();
        logFragment = new LogFragment();
        mapFragment = new MapFragment();
        toolFragment = new ToolFragment();

        navigationView.setCheckedItem(R.id.nav_position);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content_frame, positionFragment).
                add(R.id.content_frame, listFragment).add(R.id.content_frame, radarFragment).
                add(R.id.content_frame, logFragment).add(R.id.content_frame, mapFragment).
                add(R.id.content_frame, toolFragment).commit();
        fragmentManager.beginTransaction().hide(positionFragment).hide(listFragment).hide(radarFragment).hide(logFragment).hide(mapFragment).hide(toolFragment).commit();
        fragmentManager.beginTransaction().show(positionFragment).commit();

        sInstance = this;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);
            gpsStart();
            addGnssStatusListener();
            addGnssMeasurementsListener();
            addGnssGnssNavigationMessageListener();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1001);
        }

        loggerFile = new LoggerFile(this);
        loggerFileRINEX = new LoggerFileRINEX(this);
        loggerUI = new LoggerUI();
        logFragment.setLoggerFile(loggerFile);
        logFragment.setUILogger(loggerUI);

        //FloatingActionButton
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_stop = (FloatingActionButton) findViewById(R.id.fab_stop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Logging Started", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startedLogButton(true);
                startNewLogging();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Click to start logging.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Logging Stopped", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startedLogButton(false);
                stopLogging();
            }
        });

        fab_stop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Click to stop logging.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //startService(new Intent(this, bkgdService.class));
    }

    public void startedLogButton(boolean started) {
        if (started) {
            fab.setVisibility(View.INVISIBLE);
            fab_stop.setVisibility(View.VISIBLE);
            logFragment.startedLog(true);
        } else {
            fab.setVisibility(View.VISIBLE);
            fab_stop.setVisibility(View.INVISIBLE);
            logFragment.startedLog(false);
        }
    }

    public void hideFloatButton(boolean hide) {
        if (hide) {
            fab.setVisibility(View.INVISIBLE);
            fab_stop.setVisibility(View.INVISIBLE);
        } else {
            startedLogButton(logging);
        }
    }

    public void startNewLogging() {
        SharedPreferences setting = this.getSharedPreferences("settings", MODE_PRIVATE);
        logFileType = Integer.parseInt(setting.getString(getString(R.string.pref_key_log_type), "1"));
        logging = true;
        if (logFileType == 1) {
            loggerFile.startNewLog();
        } else if (logFileType == 2) {
            loggerFileRINEX.startNewLog();
        }
    }

    public void stopLogging() {
        logging = false;
        if (logFileType == 1) {
            loggerFile.send();
        } else if (logFileType == 2) {
            loggerFileRINEX.send();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (logging) {
                Toast.makeText(this, "Setting is not allowed when logging", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, settings.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void closeSetting() {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //FragmentManager fragmentManager = getSupportFragmentManager();

        /*fragmentManager.beginTransaction()
                    .replace(R.id.content_frame
                            , listFragment)
                    .commit();*/

        hideFloatButton(false);

        if (id != R.id.nav_info)
            fragmentManager.beginTransaction().hide(positionFragment).hide(listFragment).hide(radarFragment).hide(logFragment).hide(mapFragment).hide(toolFragment).commit();

        if (id == R.id.nav_position) {
            fragmentManager.beginTransaction()
                    .show(positionFragment)
                    .commit();
        } else if (id == R.id.nav_list) {
            fragmentManager.beginTransaction()
                    .show(listFragment)
                    .commit();
        } else if (id == R.id.nav_radar) {
            fragmentManager.beginTransaction().detach(radarFragment).attach(radarFragment).commit();
            fragmentManager.beginTransaction()
                    .show(radarFragment)
                    .commit();
        } else if (id == R.id.nav_log) {
            hideFloatButton(true);
            fragmentManager.beginTransaction()
                    .show(logFragment)
                    .commit();
        } else if (id == R.id.nav_map) {
            fragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit();
        } else if (id == R.id.nav_tool) {
            fragmentManager.beginTransaction()
                    .show(toolFragment)
                    .commit();
        } else if (id == R.id.nav_info) {
            InfoFragment infoFragment = new InfoFragment();
            infoFragment.show(getSupportFragmentManager(), "info");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);
            addGnssStatusListener();
            addGnssMeasurementsListener();
            addGnssGnssNavigationMessageListener();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1001);
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);
            addGnssStatusListener();
            addGnssMeasurementsListener();
            addGnssGnssNavigationMessageListener();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1001);
        }

        addOrientationSensorListener();
        //Toast.makeText(this, "App resume", Toast.LENGTH_SHORT).show();

        startService(new Intent(this, bkgdService.class));

        super.onResume();

    }

    @Override
    protected void onPause() {
        //locationManager.removeUpdates(this);
        //Toast.makeText(this, "App pause", Toast.LENGTH_SHORT).show();
        super.onPause();

    }

    @Override
    protected void onStop() {
        //locationManager.removeUpdates(this);
        //Toast.makeText(this, "App stop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //locationManager.removeUpdates(this);
        //Toast.makeText(this, "App destroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    static MainActivity getInstance() {
        return sInstance;
    }

    void addListener(MainActivityListener listener) {
        mMainActivityListeners.add(listener);
    }

    private void addOrientationSensorListener() {
        if (GpsTestUtil.isRotationVectorSensorSupported(this)) {
            // Use the modern rotation vector sensors
            Sensor vectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, vectorSensor, 16000); // ~60hz

            mSensorManager.registerListener(this, gyroSensor, mSensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, accSensor, mSensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, magSensor, mSensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // Use the legacy orientation sensors
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (sensor != null) {
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    // location listener
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        updateGeomagneticField();

        for (MainActivityListener listener : mMainActivityListeners) {
            listener.onLocationChanged(mLastLocation);
        }
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
    // location listener

    private synchronized void gpsStart() {
        if (!mStarted) {
            mStarted = true;
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);
        }
        for (MainActivityListener listener : mMainActivityListeners) {
            listener.gpsStart();
        }
    }

    private synchronized void gpsStop() {
        if (mStarted) {
            locationManager.removeUpdates(this);
            mStarted = false;
            // Stop progress bar
            setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);

            // Reset the options menu to trigger updates to action bar menu items
            invalidateOptionsMenu();
        }
        for (MainActivityListener listener : mMainActivityListeners) {
            listener.gpsStop();
        }
    }

    private void addGnssStatusListener() {
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onStarted() {
                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onGnssStarted();
                }
            }

            @Override
            public void onStopped() {
                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onGnssStopped();
                }
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onGnssFirstFix(ttffMillis);
                }
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;

                // Stop progress bar after the first status information is obtained
                setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);

                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onSatelliteStatusChanged(mGnssStatus);
                }
            }
        };
        locationManager.registerGnssStatusCallback(mGnssStatusListener);
    }

    private void addGnssMeasurementsListener() {
        mGnssMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onGnssMeasurementsReceived(event);
                }
                if (mWriteGnssMeasurementToLog) {
                    for (GnssMeasurement m : event.getMeasurements()) {
                        GpsTestUtil.writeGnssMeasurementToLog(m);
                    }
                }
            }

            @Override
            public void onStatusChanged(int status) {
                final String statusMessage;
                switch (status) {
                    case STATUS_LOCATION_DISABLED:
                        statusMessage = getString(R.string.gnss_measurement_status_loc_disabled);
                        break;
                    case STATUS_NOT_SUPPORTED:
                        statusMessage = getString(R.string.gnss_measurement_status_not_supported);
                        break;
                    case STATUS_READY:
                        statusMessage = getString(R.string.gnss_measurement_status_ready);
                        break;
                    default:
                        statusMessage = getString(R.string.gnss_status_unknown);
                }
                Log.d(TAG, "GnssMeasurementsEvent.Callback.onStatusChanged() - " + statusMessage);
                if (GpsTestUtil.canManageDialog(MainActivity.this)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(MainActivity.this, statusMessage, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, statusMessage);
                        }
                    });
                }
            }
        };
        locationManager.registerGnssMeasurementsCallback(mGnssMeasurementsListener);
    }

    private void addGnssGnssNavigationMessageListener() {
        mGnssNavigationMessageListener = new GnssNavigationMessage.Callback() {
            @Override
            public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                for (MainActivityListener listener : mMainActivityListeners) {
                    listener.onGnssNavigationMessageReceived(event);
                }
            }

            @Override
            public void onStatusChanged(int status) {

            }
        };
        locationManager.registerGnssNavigationMessageCallback(mGnssNavigationMessageListener);
    }

    private void addNmeaMessageListener() {
        OnNmeaMessageListener nmeaListener =
                new OnNmeaMessageListener() {
                    @Override
                    public void onNmeaMessage(String s, long l) {
                        for (MainActivityListener listener : mMainActivityListeners) {
                            listener.onNmeaReceived(l, s);
                        }
                    }
                };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // orientation = Double.NaN;
        //double tilt = Double.NaN;
        //double gyroX = 0, gyroY = 0, gyroZ = 0, accelX = 0, accelY = 0, accelZ = 0, heading = 0;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                // Modern rotation vector sensors
                if (!mTruncateVector) {
                    try {
                        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    } catch (IllegalArgumentException e) {
                        // On some Samsung devices, an exception is thrown if this vector > 4 (see #39)
                        // Truncate the array, since we can deal with only the first four values
                        Log.e(TAG, "Samsung device error? Will truncate vectors - " + e);
                        mTruncateVector = true;
                        // Do the truncation here the first time the exception occurs
                        getRotationMatrixFromTruncatedVector(event.values);
                    }
                } else {
                    // Truncate the array to avoid the exception on some devices (see #39)
                    getRotationMatrixFromTruncatedVector(event.values);
                }

                int rot = getWindowManager().getDefaultDisplay().getRotation();
                switch (rot) {
                    case Surface.ROTATION_0:
                        // No orientation change, use default coordinate system
                        SensorManager.getOrientation(mRotationMatrix, mValues);
                        // Log.d(TAG, "Rotation-0");
                        break;
                    case Surface.ROTATION_90:
                        // Log.d(TAG, "Rotation-90");
                        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y,
                                SensorManager.AXIS_MINUS_X, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    case Surface.ROTATION_180:
                        // Log.d(TAG, "Rotation-180");
                        SensorManager
                                .remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_X,
                                        SensorManager.AXIS_MINUS_Y, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    case Surface.ROTATION_270:
                        // Log.d(TAG, "Rotation-270");
                        SensorManager
                                .remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Y,
                                        SensorManager.AXIS_X, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    default:
                        // This shouldn't happen - assume default orientation
                        SensorManager.getOrientation(mRotationMatrix, mValues);
                        // Log.d(TAG, "Rotation-Unknown");
                        break;
                }
                orientation = Math.toDegrees(mValues[0]);  // azimuth
                tilt = Math.toDegrees(mValues[1]);
                break;
            case Sensor.TYPE_ORIENTATION:
                // Legacy orientation sensors
                orientation = event.values[0];
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroX = event.values[0];
                gyroY = event.values[1];
                gyroZ = event.values[2];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelX = event.values[0];
                accelY = event.values[1];
                accelZ = event.values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                heading = event.values[0];
                break;
            default:
                // A sensor we're not using, so return
                return;
        }

        // Correct for true north, if preference is set
        if (mFaceTrueNorth && mGeomagneticField != null) {
            orientation += mGeomagneticField.getDeclination();
            // Make sure value is between 0-360
            orientation = MathUtils.mod((float) orientation, 360.0f);
        }

        for (MainActivityListener listener : mMainActivityListeners) {
            listener.onOrientationChanged(orientation, tilt);
            listener.sensorValue(gyroX, gyroY, gyroZ, accelX, accelY, accelZ, heading);
        }
    }

    private void getRotationMatrixFromTruncatedVector(float[] vector) {
        System.arraycopy(vector, 0, mTruncatedRotationVector, 0, 4);
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, mTruncatedRotationVector);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateGeomagneticField() {
        mGeomagneticField = new GeomagneticField((float) mLastLocation.getLatitude(),
                (float) mLastLocation.getLongitude(), (float) mLastLocation.getAltitude(),
                mLastLocation.getTime());
    }

}
