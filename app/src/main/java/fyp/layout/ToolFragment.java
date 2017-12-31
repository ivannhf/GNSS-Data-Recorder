package fyp.layout;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Ivan_Dktp on 1/11/2017.
 */

public class ToolFragment extends Fragment{

    View myView;

    TextView textX, textY, textZ;
    TextView tv_accX, tv_accY, tv_accZ, tv_mag;
    SensorManager gyroManager, accManager, magManager;
    Sensor gyroSensor, accSensor, magSensor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = inflater.inflate(R.layout.tool_layout, container, false);

        gyroManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = gyroManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        accManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accSensor = accManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        magManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        magSensor = magManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        textX = (TextView) myView.findViewById(R.id.textX);
        textY = (TextView) myView.findViewById(R.id.textY);
        textZ = (TextView) myView.findViewById(R.id.textZ);

        tv_accX = (TextView) myView.findViewById(R.id.accX);
        tv_accY = (TextView) myView.findViewById(R.id.accY);
        tv_accZ = (TextView) myView.findViewById(R.id.accZ);

        tv_mag = (TextView) myView.findViewById(R.id.magnet);


        return myView;


    }

    public void onResume() {
        super.onResume();
        gyroManager.registerListener(gyroListener, gyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        accManager.registerListener(accListener, accSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        gyroManager.registerListener(magListener, magSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        super.onStop();
        gyroManager.unregisterListener(gyroListener);
        accManager.unregisterListener(accListener);
        magManager.unregisterListener(magListener);

    }

    public SensorEventListener gyroListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            textX.setText("X : " + x + " rad/s");
            textY.setText("Y : " + y + " rad/s");
            textZ.setText("Z : " + z + " rad/s");
        }

        public void onAccuracyChanged(Sensor sensor, int acc) {

        }
    };

    public SensorEventListener accListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tv_accX.setText("X Acc : " + x + " m/s2");
            tv_accY.setText("Y Acc: " + y + " m/s2");
            tv_accZ.setText("Z Acc: " + z + " m/s2");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public SensorEventListener magListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];

            tv_mag.setText("Heading: " + x + " deg");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
