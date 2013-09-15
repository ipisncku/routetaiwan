package tw.ipis.routetaiwan;

import java.text.SimpleDateFormat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;

import android.location.LocationListener;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class map extends Activity {

	protected static final String TAG = "~~map~~";
	private TextView longitude;
	private TextView latitude;
	private TextView speed;
	private TextView time;
	private GoogleMap mMap;
	private Marker markerMe = null;

	private LocationManager locationMgr;
	private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "start...");
		initMap();
		Log.d(TAG, "initMap finished...");
		initView();
		Log.d(TAG, "start initLocationProvider...");
		if (initLocationProvider()) {
			whereAmI();
		}else{
			Toast.makeText(this,"無法定位...",Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onStop() {
		locationMgr.removeUpdates(locationListener);
		super.onStop();
	}

	private void initMap() {
		mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		GoogleMapOptions options = new GoogleMapOptions();
		options.compassEnabled(false);
	}

	LocationListener locationListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO 自動產生的方法 Stub
			updateWithNewLocation(null);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO 自動產生的方法 Stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO 自動產生的方法 Stub

		}
	};
	private void cameraFocusOnMe(double lat, double lng){
		CameraPosition camPosition = new CameraPosition.Builder()
		.target(new LatLng(lat, lng))
		.zoom(16)
		.build();

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
	}

	private void initView(){
		longitude = (TextView) findViewById(R.id.longitude);
		latitude = (TextView) findViewById(R.id.latitude);
		speed = (TextView) findViewById(R.id.speed);
		time = (TextView) findViewById(R.id.time);
	}

	/**
	 * GPS初始化，取得可用的位置提供器
	 * @return
	 */
	private boolean initLocationProvider() {
		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		//1.選擇最佳提供器
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		provider = locationMgr.getBestProvider(criteria, true);

		Toast.makeText(this,"使用" + provider + "定位..." , Toast.LENGTH_LONG).show();

		if (provider != null) {
			return true;
		}

		//2.選擇使用GPS提供器
		//		if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		//			provider = LocationManager.GPS_PROVIDER;
		//			Toast.makeText(this,"使用" + provider + "定位..." , Toast.LENGTH_LONG).show();
		//			return true;
		//		}

		//3.選擇使用網路提供器
		// if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
		//  provider = LocationManager.NETWORK_PROVIDER;
		//  return true;
		// }

		Toast.makeText(this,"無法取得定位..." , Toast.LENGTH_LONG).show();
		return false;
	}

	/**
	 * 執行"我"在哪裡
	 * 1.建立位置改變偵聽器
	 * 2.預先顯示上次的已知位置
	 */
	private void whereAmI(){
		//取得上次已知的位置
		Location location = locationMgr.getLastKnownLocation(provider);
		updateWithNewLocation(location);

		//GPS Listener
		locationMgr.addGpsStatusListener(gpsListener);


		//Location Listener
		long minTime = 5000;//ms
		float minDist = 5;//meter
		locationMgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
	}


	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d(TAG, "GPS_EVENT_STARTED");
				Toast.makeText(map.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d(TAG, "GPS_EVENT_STOPPED");
				Toast.makeText(map.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
				Toast.makeText(map.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
				break;
			}
		}
	};

	/**
	 * 顯示"我"在哪裡
	 * @param lat
	 * @param lng
	 */
	private void showMarkerMe(double lat, double lng){
		if (markerMe != null) {
			markerMe.remove();
		}

		MarkerOptions markerOpt = new MarkerOptions();
		markerOpt.position(new LatLng(lat, lng));
		markerOpt.title("我在這裡");
		markerMe = mMap.addMarker(markerOpt);

		Toast.makeText(this, "緯度:" + lat + ",經度:" + lng, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 更新並顯示新位置
	 * @param location
	 */
	private void updateWithNewLocation(Location location) {
		double lng = 0, lat = 0;
		float volecity = 0;
		long now;
		String timeString="";
		if (location != null) {
			//經度
			lng = location.getLongitude();
			//緯度
			lat = location.getLatitude();
			//速度
			volecity = location.getSpeed();
			//時間
			now = location.getTime();
			timeString = getTimeString(now);


			//"我"
			showMarkerMe(lat, lng);
			cameraFocusOnMe(lat, lng);

		}
		//顯示資訊
		longitude.setText("經度: " + String.valueOf(lng));
		latitude.setText("緯度: " + String.valueOf(lat));
		speed.setText("速度: " + String.valueOf(volecity));
		time.setText("時間: " + timeString);
	}

	private String getTimeString(long timeInMilliseconds){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(timeInMilliseconds);
	}
}
