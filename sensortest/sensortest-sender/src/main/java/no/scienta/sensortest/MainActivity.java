package no.scienta.sensortest;


import no.scienta.sensortest.cast.CastSession;
import no.scienta.sensortest.cast.DiscoveryAndSelectHandler;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private DiscoveryAndSelectHandler discoveryAndSelectHandler;
	private CastSession castSession;	
	private SensorStateSender sensorStateSender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

		final String applicationId=getResources().getString(R.string.app_id);
		final String namespace=getResources().getString(R.string.namespace);
		
		// Handler for Cast device discovery
		this.discoveryAndSelectHandler=new DiscoveryAndSelectHandler(getApplicationContext(),applicationId) {
			@Override
			public void onConnected(CastDevice device) {
				if (castSession!=null) {
					castSession.close();
				}
				castSession=createSession(device, applicationId, namespace, getApplicationContext());
			}
			
			@Override
			public void onDisconnected() {
				Log.i(TAG,"DiscoveryAndSelectHandler disconnected");
				if (castSession!=null) {
					castSession.close();
					castSession=null;
				}
			}
		};

        // Create sensor state sender
		sensorStateSender=new SensorStateSender((SensorManager)getSystemService(SENSOR_SERVICE)) {
			@Override
			protected void send(String msg) {
				if (castSession!=null && !castSession.isClosed()) {
					Log.d(TAG,"Sending "+msg);
					castSession.sendMessage(msg);
				}
			}
		};
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		discoveryAndSelectHandler.startDiscovery();
		sensorStateSender.start();
		
	}

	@Override
	protected void onPause() {
		sensorStateSender.stop();
		if (isFinishing()) {
			discoveryAndSelectHandler.stopDiscovery();
		}
		super.onPause();
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
		
		// Set the MediaRouteActionProvider selector for device discovery.
		mediaRouteActionProvider.setRouteSelector(discoveryAndSelectHandler.getRouteSelector());
		return true;
	}

	
	@Override
	public void onDestroy() {
		if (castSession!=null) {
			castSession.close();
		}
		if (discoveryAndSelectHandler!=null) {
			discoveryAndSelectHandler.close();
		}
		castSession=null;
		discoveryAndSelectHandler=null;
		super.onDestroy();
	}


	/**
	 * Start the receiver app
	 */
	private CastSession createSession(CastDevice device, String applicationId, String namespace, Context context) {
		try {
			return new CastSession(device,applicationId,namespace,context) {
				
				@Override
				protected void onClosed(CloseReason reason) {
					Log.i(TAG,"onClosedSession");
				}
				
				@Override
				protected void onApplicationStarted() {
					Log.i(TAG,"onApplicationStarted");
				}
				
				@Override
				protected MessageReceivedCallback createNewMessageReceiver() {
                    return null;
				}
			};
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
			return null;
		}
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void resetOrientation(View view) {
        if (sensorStateSender!=null) {
            sensorStateSender.updateCalibration();
        }
    }

    public void selectLogo(View view) {
        selectObject(0);
    }

    public void selectAndroid(View view) {
        selectObject(1);
    }

    public void selectCube(View view) {
        selectObject(2);
    }

    public void selectObject(int objId) {
        if (sensorStateSender!=null) {
            sensorStateSender.setObjectId(objId);
        }
    }
}
