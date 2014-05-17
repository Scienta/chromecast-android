package no.scienta.sensortest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorStateSender implements SensorEventListener {
	private final SensorManager sensorManager;
    private final float[] r = new float[9];
    private float[] calibration=new float[]{1,0,0,0,1,0,0,0,1};

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
    public synchronized void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(r, event.values);

        float[] m=matrixMultiply(calibration,r);

        String msg="{"+
                "\"r0\": "+m[0]+"," +
                "\"r1\": "+m[1]+"," +
                "\"r2\": "+m[2]+"," +
                "\"r3\": "+m[3]+"," +
                "\"r4\": "+m[4]+"," +
                "\"r5\": "+m[5]+"," +
                "\"r6\": "+m[6]+"," +
                "\"r7\": "+m[7]+"," +
                "\"r8\": "+m[8]+"}";
                send(msg);
    }

    private float[] matrixMultiply(float[] a, float[] b) {
        return new float[] {a[0]*b[0]+a[1]*b[3]+a[2]*b[6],  a[0]*b[1]+a[1]*b[4]+a[2]*b[7],  a[0]*b[2]+a[1]*b[5]+a[2]*b[8],
                            a[3]*b[0]+a[4]*b[3]+a[5]*b[6],  a[3]*b[1]+a[4]*b[4]+a[5]*b[7],  a[3]*b[2]+a[4]*b[5]+a[5]*b[8],
                            a[6]*b[0]+a[7]*b[3]+a[8]*b[6],  a[6]*b[1]+a[7]*b[4]+a[8]*b[7],  a[6]*b[2]+a[7]*b[5]+a[8]*b[8]};
    }

    private float[] matrixTranspose(float[] m) {
        return new float[]{m[0],m[3],m[6],m[1],m[4],m[7],m[2],m[5],m[8]};
    }

    public synchronized void updateCalibration() {
        calibration=matrixTranspose(r);
    }
    
    protected abstract void send(String msg);
}
