package no.scienta.pong;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorStateSender implements SensorEventListener {
	private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    float aa=0;
    float bb=0;
    
    
    public SensorStateSender(SensorManager sensorManager) {
    	this.mSensorManager=sensorManager;
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	}
    
    public void start() {
    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    public void stop() {
    	mSensorManager.unregisterListener(this);
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
    	final float a = event.values[0];
    	final float b = event.values[1];
    	//final float c = event.values[2];
    	aa+=a;
    	bb+=b;
    	
    	aa*=0.95f;
    	bb*=0.95f;
    	
    	int x=((int)(aa*10f)+300) % 600;
    	int y=((int)(bb*10f)+300) % 600;
    	
    	String msg="{\"x\": "+x+",\"y\":"+y+"}";
    	send(msg);
    }
    
    protected abstract void send(String msg);
}
