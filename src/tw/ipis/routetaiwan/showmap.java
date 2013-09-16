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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showmap);

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
			
			Log.e(TAG, "before");
			if(locationclient != null && locationclient.isConnected()){
				Log.e(TAG, "locationclient connected");
				locationrequest = LocationRequest.create();
				locationrequest.setInterval(100);
				locationclient.requestLocationUpdates(locationrequest, this);
				Location last = locationclient.getLastLocation();
				focus_on_me(last);
			}
		}
	}
	
	LocationListener locationListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			focus_on_me(location);
		}
	};
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connect failed....", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected....", Toast.LENGTH_LONG).show();
		if(locationclient != null && locationclient.isConnected()) {
			Log.e(TAG, "locationclient connected");
			locationrequest = LocationRequest.create();
			locationrequest.setInterval(100);
			locationclient.requestLocationUpdates(locationrequest, this);
			Location last = locationclient.getLastLocation();
			focus_on_me(last);
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connect disconnected....", Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationclient!=null)
			locationclient.disconnect();
	}
	

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
		
		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		// Showing the current location in Google Map
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		
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