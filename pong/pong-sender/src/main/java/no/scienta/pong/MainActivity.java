package no.scienta.pong;

import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import no.scienta.pong.cast.CastSession;
import no.scienta.pong.cast.DiscoveryAndSelectHandler;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DiscoveryAndSelectHandler discoveryAndSelectHandler;
    private CastSession castSession;
    private RotationListener rotationListener;
    private int pitchDegrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView pitchView = (TextView) findViewById(R.id.pitchView);
        final TextView azimuthView = (TextView) findViewById(R.id.azimuthView);
        final TextView rollView = (TextView) findViewById(R.id.rollView);


//        ActionBar actionBar = getSupportActionBar();
//		actionBar.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        final String applicationId = getResources().getString(R.string.app_id);
        final String namespace = getResources().getString(R.string.namespace);

        // Handler for Cast device discovery
        this.discoveryAndSelectHandler = new DiscoveryAndSelectHandler(getApplicationContext(), applicationId) {
            @Override
            public void onConnected(CastDevice device) {
                if (castSession != null) {
                    castSession.close();
                }
                castSession = createSession(device, applicationId, namespace, getApplicationContext());
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, "DiscoveryAndSelectHandler disconnected");
                if (castSession != null) {
                    castSession.close();
                    castSession = null;
                }
            }
        };

        rotationListener = new RotationListener((SensorManager) getSystemService(SENSOR_SERVICE)) {
            @Override
            protected void onRotationChanged(float pitch, float roll, float azimuth) {
                int newPitchDegrees = Math.round(pitch);
                pitchView.setText("Pitch " + newPitchDegrees);
                rollView.setText("Roll " + Math.round(roll));
                azimuthView.setText("Azi " + Math.round(azimuth));

                if (castSession != null && MainActivity.this.pitchDegrees != pitchDegrees){
                    MainActivity.this.pitchDegrees = newPitchDegrees;
                    castSession.sendMessage("{pitch: " + newPitchDegrees + "}");
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        discoveryAndSelectHandler.startDiscovery();
        rotationListener.start();
    }

    @Override
    protected void onPause() {
        rotationListener.stop();
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
        if (castSession != null) {
            castSession.close();
        }
        if (discoveryAndSelectHandler != null) {
            discoveryAndSelectHandler.close();
        }
        castSession = null;
        discoveryAndSelectHandler = null;
        super.onDestroy();
    }


    /**
     * Start the receiver app
     */
    private CastSession createSession(CastDevice device, String applicationId, String namespace, Context context) {
        try {
            return new CastSession(device, applicationId, namespace, context) {

                @Override
                protected void onClosed(CloseReason reason) {
                    Log.i(TAG, "onClosedSession");

                }

                @Override
                protected void onApplicationStarted() {
                    Log.i(TAG, "onApplicationStarted");
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
}
