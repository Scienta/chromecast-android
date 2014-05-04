package no.scienta.pong;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class RotationListener implements SensorEventListener {

    private final SensorManager sensorManager;
    private final float[] rotationMatrix = new float[9];


    public RotationListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void start() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] orientation = new float[3];

        // Convert the rotation-vector to a 4x4 matrix.
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);

        float azimuth = (float) Math.toDegrees(orientation[0]);
        float pitch = (float) Math.toDegrees(orientation[1]);
        float roll = (float) Math.toDegrees(orientation[2]);

        onRotationChanged(pitch, roll, azimuth);

    }

    protected abstract void onRotationChanged(float pitch, float roll, float azimuth);
}
