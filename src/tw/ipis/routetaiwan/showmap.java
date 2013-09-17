package tw.ipis.routetaiwan;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class showmap extends Activity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {

	private static final String TAG = "~~showmap~~";
	GoogleMap googleMap;
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private boolean first_read = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showmap);

		first_read = true;
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		// Showing status
		if(status != ConnectionResult.SUCCESS){ // Google Play Services are not available
			int requestCode = 10;
			Log.d(TAG, "Google play service unavailable");
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
			Toast.makeText(this, "Google play service unavailable", Toast.LENGTH_LONG).show();
		}else {    // Google Play Services are available
			Log.d(TAG, "Google play service available");
			
			locationclient = new LocationClient(this,this,this);
			locationclient.connect();
			
			// Getting reference to the SupportMapFragment of activity_main.xml
			MapFragment fm = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapv2));

			// Getting GoogleMap object from the fragment
			googleMap = fm.getMap();

			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(true);

			// Setting event handler for location change
//			googleMap.setOnMyLocationChangeListener((OnMyLocationChangeListener) this);
			
			if(locationclient != null && locationclient.isConnected()){
				locationrequest = LocationRequest.create();
				locationrequest.setInterval(100);
				locationclient.requestLocationUpdates(locationrequest, this);
				Location last = locationclient.getLastLocation();
				focus_on_me(last);
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		first_read = true;
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected....", Toast.LENGTH_LONG).show();
		if(locationclient != null && locationclient.isConnected()) {
			locationrequest = LocationRequest.create();
			locationrequest.setInterval(100);
			locationclient.requestLocationUpdates(locationrequest, this);
			Location last = locationclient.getLastLocation();
			first_read = true;
			focus_on_me(last);
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationclient!=null)
			locationclient.disconnect();
		first_read = true;
	}
	
	LocationListener locationListener1 = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			focus_on_me(location);
		}
	};

	public void focus_on_me(Location location) {
		Log.i(TAG, "focus on me!");
		
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Getting current speed
		float speed = location.getSpeed();
		
		// Getting current time
		long time = location.getTime();
		
		if(first_read) {
			Log.e(TAG, "changing camera...");
			// Creating a LatLng object for the current location
			LatLng latLng = new LatLng(latitude, longitude);
			// Zoom in the Google Map
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
			// Showing the current location in Google Map
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			first_read = false;
		}
		
		show_info(longitude, latitude, speed, time);
	}
	
	private void show_info(double lon, double lat, float speed, long time) {
		TextView longtitude = (TextView) findViewById(R.id.longitude);
		TextView latitude = (TextView) findViewById(R.id.latitude);
		TextView volecity = (TextView) findViewById(R.id.speed);
		TextView nowtime = (TextView) findViewById(R.id.time);
		
		longtitude.setText("經度: " + String.valueOf(lon));
		latitude.setText("緯度: " + String.valueOf(lat));
		volecity.setText("速度: " + String.valueOf(speed));
		nowtime.setText("時間: " + getTimeString(time));
	}
	
	private String getTimeString(long timeInMilliseconds){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(timeInMilliseconds);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		focus_on_me(arg0);
	}
}