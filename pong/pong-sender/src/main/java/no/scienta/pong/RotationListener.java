package no.scienta.pong;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class RotationListener implements SensorEventListener {

    private static final float RADIANS_TO_DEGREES = (float) (180.0 / Math.PI);
    private final SensorManager sensorManager;
    private float[] magneticFieldValues;
    private float[] accelerationValues;
    private final float[] rotationMatrix = new float[9];


    public RotationListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void start() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerationValues = event.values.clone();
                break;
        }

        if (magneticFieldValues != null && accelerationValues != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerationValues, magneticFieldValues);

            // Correct if screen is in Landscape
            // TODO
//            float[] outR = new float[9];
//            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);

            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);


            float azimuth = orientation[0] * RADIANS_TO_DEGREES;
            float pitch = orientation[1] * RADIANS_TO_DEGREES;
            float roll = orientation[2] * RADIANS_TO_DEGREES;

            magneticFieldValues = null;
            accelerationValues = null;
            onRotationChanged(pitch, roll, azimuth);
        }
    }

    protected abstract void onRotationChanged(float pitch, float roll, float azimuth);
}
