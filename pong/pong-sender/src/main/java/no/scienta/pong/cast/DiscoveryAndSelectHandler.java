package no.scienta.pong.cast;

import android.content.Context;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;

public abstract class DiscoveryAndSelectHandler {
	private final String TAG="DiscoveryAndSelectHandler";
	
	private MediaRouter mediaRouter;
	private final MediaRouter.Callback mediaRouterCallback;
	private final MediaRouteSelector mediaRouteSelector;
	private final String castCategory;
	
	
	public DiscoveryAndSelectHandler(Context applicationContext, String applicationId) {
		this.mediaRouter=MediaRouter.getInstance(applicationContext);
		this.mediaRouterCallback=new MediaRouter.Callback() {
			
			@Override
			public void onRouteSelected(MediaRouter router, RouteInfo info) {
				Log.d(TAG, "onRouteSelected: "+info);
				if (!isClosed()) { 
					onConnected(CastDevice.getFromBundle(info.getExtras()));
				}
			}

			@Override
			public void onRouteUnselected(MediaRouter router, RouteInfo info) {
				Log.d(TAG, "onRouteUnselected: " + info);
				if (!isClosed()) {
					onDisconnected();
				}
			}
		};
		this.castCategory = CastMediaControlIntent.categoryForCast(applicationId);
		this.mediaRouteSelector = new MediaRouteSelector.Builder()
		    .addControlCategory(castCategory)
		    .build();
	}
	
	public MediaRouteSelector getRouteSelector() {
		return mediaRouteSelector;
	}
	
	public void startDiscovery() {
		mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}
	
	public void stopDiscovery() {
		mediaRouter.removeCallback(mediaRouterCallback);
	}
	
	public void close() {
		if (!isClosed()) {
			stopDiscovery();
			mediaRouter=null;
		}
	}
	
	public boolean isClosed() {
		return mediaRouter==null;
	}
		
	public abstract void onConnected(CastDevice device);
	public abstract void onDisconnected();
}
