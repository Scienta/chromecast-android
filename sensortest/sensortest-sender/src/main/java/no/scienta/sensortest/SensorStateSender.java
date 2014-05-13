package no.scienta.sensortest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorStateSender implements SensorEventListener {
	private final SensorManager sensorManager;
    private final float[] r = new float[9];

    public SensorStateSender(SensorManager sensorManager) {
    	this.sensorManager=sensorManager;
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
        SensorManager.getRotationMatrixFromVector(r, event.values);
        String msg="{"+
                "\"r0\": "+r[0]+"," +
                "\"r1\": "+r[1]+"," +
                "\"r2\": "+r[2]+"," +
                "\"r3\": "+r[3]+"," +
                "\"r4\": "+r[4]+"," +
                "\"r5\": "+r[5]+"," +
                "\"r6\": "+r[6]+"," +
                "\"r7\": "+r[7]+"," +
                "\"r8\": "+r[8]+"}";
        //System.out.println("msg="+msg);

                send(msg);
    }
    
    protected abstract void send(String msg);
}
