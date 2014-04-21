package no.scienta.sensortest.cast;

import java.io.IOException;

import no.scienta.sensortest.MainActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.CastOptions;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public abstract class CastSession {
	private final String TAG="CastSession";
//	private final CastDevice device;
//	private final String applicationId;
	private final String namespace;
	private GoogleApiClient apiClient;
	
	public CastSession(final CastDevice device, final String applicationId, final String namespace, final Context context) {
//		this.device=device;
//		this.applicationId=applicationId;
		this.namespace=namespace;
		
		final Cast.Listener castListener = new Cast.Listener() {
			@Override
			public void onApplicationDisconnected(int errorCode) {
				Log.d(TAG, "Application disconnected");
				onClose(CloseReason.APP_DISCONNECTED);
				// Local teardown?
			}
		};
		
		final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult result) {
				Log.e(TAG, "ConnectionFailed ");
				onClose(CloseReason.CONNECTION_FAILED);
				// Local teardown?
			}
		};
		
		final CastOptions apiOptions = Cast.CastOptions.builder(device, castListener).build();
		
		final GoogleApiClient.ConnectionCallbacks connectionCallbacks= new GoogleApiClient.ConnectionCallbacks() {
			private boolean connectionSuspended=false;
			
			@Override
			public void onConnected(Bundle bundle) {
				if (connectionSuspended) {
					connectionSuspended=false;
					if (receiverApplicationStopped(bundle)) {
						onClose(CloseReason.APP_DISCONNECTED);
					} else {
						establishChannel();
					}
				} else {
					Cast.CastApi.launchApplication(apiClient,applicationId, false)
						.setResultCallback(
							new ResultCallback<Cast.ApplicationConnectionResult>() {
								@Override
								public void onResult(ApplicationConnectionResult result) {
									Status status = result.getStatus();
									Log.d(TAG,"ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusCode());
									if (status.isSuccess()) {
										ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
										String sessionId = result.getSessionId();
										String applicationStatus = result.getApplicationStatus();
										boolean wasLaunched = result.getWasLaunched();
										Log.d(TAG,"application name: "+ applicationMetadata.getName()+ ", status: "+ applicationStatus+ ", sessionId: "+ sessionId+ ", wasLaunched: "+ wasLaunched);
										
										if (!isClosed()) {
											establishChannel();
										}
										
										if (!isClosed()) {
											onApplicationStarted();
										}
									} else {
										Log.e(TAG,"application could not launch");
										onClose(CloseReason.LAUNCH_FAILED);
									}
								}
							});					
				}				
			}
			
			private void establishChannel() {
				if (apiClient!=null) {
					try {
						MessageReceivedCallback channel=createNewMessageReceiver();
						if (channel!=null) {
							Cast.CastApi.setMessageReceivedCallbacks(apiClient,namespace,channel);
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception while creating channel", e);
						onClose(CloseReason.CHANNEL_ESTABLISH_FAILED);
					}
				} else {
					Log.e(TAG,"Session is not active");
				}
			}
			
			private boolean receiverApplicationStopped(Bundle bundle) {
				return (bundle != null) && bundle.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING);
			}
			
			@Override
			public void onConnectionSuspended(int arg0) {
				Log.d(TAG, "connectionSuspended");
				connectionSuspended = true;
			}			
		};
		
		this.apiClient = new GoogleApiClient.Builder(context)
			.addApi(Cast.API, apiOptions)
			.addConnectionCallbacks(connectionCallbacks)
			.addOnConnectionFailedListener(connectionFailedListener)
			.build();
		apiClient.connect();		
	}
	
	private void onClose(CloseReason reason) {
		Log.d(TAG, "Session closed, reason: "+reason.name());
		if (!isClosed()) {
			close();
			onClosed(reason);
		}
	}

	public void close() {
		if (!isClosed()) {
			if (apiClient.isConnected()) {
				Cast.CastApi.stopApplication(apiClient);
				try {Cast.CastApi.removeMessageReceivedCallbacks(apiClient, namespace);} catch (Exception e){}
				try {apiClient.disconnect();} catch (Exception e){};
			}
			apiClient=null;
		}
	}
	
	protected void onApplicationStarting() {
		if (!isClosed()) {
			onApplicationStarted();
		} else {
			Log.e(TAG,"Application started for inactive session");
		}
	}
	
	public boolean isClosed() {
		return apiClient==null;
	}	
	
	/**
	 * Send a text message to the receiver
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		if (!isClosed()) {
			try {
				Cast.CastApi.sendMessage(apiClient,namespace,message)
					.setResultCallback(new ResultCallback<Status>() {
						@Override
						public void onResult(Status result) {
							if (!result.isSuccess()) {
								Log.e(TAG, "Sending message failed");
							}
						}
				});
			} catch (Exception e) {
				Log.e(TAG, "Exception while sending message", e);
			}
		} else {
			Log.e(TAG,"Cannot send message:"+message+", session is closed");
		}
	}
	
	protected abstract void onApplicationStarted();
	protected abstract void onClosed(CloseReason reason);
	protected abstract MessageReceivedCallback createNewMessageReceiver();
	
	static public enum CloseReason {
		APP_DISCONNECTED, 
		CONNECTION_FAILED, 
		LAUNCH_FAILED,
		CHANNEL_ESTABLISH_FAILED;
	}
}
