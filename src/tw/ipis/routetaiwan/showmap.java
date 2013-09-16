package tw.ipis.routetaiwan;

import java.text.SimpleDateFormat;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class showmap extends FragmentActivity implements OnMyLocationChangeListener {

	private static final String TAG = "~~showmap~~";
	GoogleMap googleMap;

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
		}else {    // Google Play Services are available
			Log.d(TAG, "Google play service available");
			// Getting reference to the SupportMapFragment of activity_main.xml
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapv2);

			// Getting GoogleMap object from the fragment
			googleMap = fm.getMap();

			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(true);

			// Setting event handler for location change
			googleMap.setOnMyLocationChangeListener((OnMyLocationChangeListener) this);
		}
	}
	
	@Override
	public void onMyLocationChange(Location location) {
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
}