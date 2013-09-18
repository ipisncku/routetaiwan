package tw.ipis.routetaiwan;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class planroute extends Activity {

	ProgressBar planning;
	String TAG = "~~planroute~~";
	private EditText from;
	private EditText to;
	private List<GeoPoint> _points = new ArrayList<GeoPoint>();
	private LocationManager locationMgr;
	private String provider;
	private boolean gps_fix = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.planroute);

		planning = (ProgressBar)findViewById(R.id.planning);
		planning.setVisibility(ProgressBar.GONE);

	}

	@Override
	protected void onStop() {
		locationMgr.removeUpdates(locationListener);
		super.onStop();
	}

	public void start_planing(View v) {
		foreground_cosmetic();

		Getroute();

	}

	private void foreground_cosmetic() {
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);

		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(from.getWindowToken(), 0);

		planning.setVisibility(ProgressBar.VISIBLE);
	}

	//	public List<GeoPoint> Getroute() {
	public void Getroute() {
		String request = "";
		String result = "";
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);
		String start = from.getText().toString();	// Get user input "From"
		String destination = to.getText().toString();	// Get user input "to"
		String Mapapi = "http://maps.googleapis.com/maps/api/directions/json?origin={0}&destination={1}&sensor={3}&departure_time={2}&mode={4}";
		long now = System.currentTimeMillis() / 1000;

		if(destination.isEmpty())
			destination = "Taipei 101";

		try {
			if (start.isEmpty()) {
				Location current = GetCurrentPosition();
				String curr = current.getLatitude() + "," + current.getLongitude();
				request = MessageFormat.format(Mapapi, URLEncoder.encode(curr, "UTF-8"), 
						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
			}
			else {
				request = MessageFormat.format(Mapapi, URLEncoder.encode(start, "UTF-8"),
						URLEncoder.encode(destination, "UTF-8"), URLEncoder.encode(new Long(now).toString(), "UTF-8"), 
						URLEncoder.encode("true", "UTF-8"), URLEncoder.encode("transit", "UTF-8"));
			}
			Log.d(TAG, request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}


		/* Use the url for http request */
		DownloadWebPageTask task = new DownloadWebPageTask();
		result = task.execute(new String[] {request}).toString();
	}
	
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader("accept", "application/json");
//				StringBuilder builder = new StringBuilder();
				HttpClient client = new DefaultHttpClient();
				try {
					HttpResponse result = client.execute(httpGet);
					StatusLine statusLine = result.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = result.getEntity();
//						InputStream content = entity.getContent();
//						BufferedReader reader = new BufferedReader(new InputStreamReader(content), 65728);
//						String line = null;
//						while ((line = reader.readLine()) != null) {
//							builder.append(line);
//						}
						
						response = EntityUtils.toString(entity);
						Log.i(TAG, "QQ...--->" + response);
					} else {
						Log.e(TAG, "Failed to download file");
					}
					return response;
				}
				catch (Exception e) {
					e.printStackTrace();
					return "";
				}
			}
			return "";
		}
	}

	public Location GetCurrentPosition() {
		initLocationProvider();
		if(locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {	// Using GPS as provider, wait for GPS_EVENT_FIRST_FIX
			Log.e(TAG, "GPS");
			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			Toast.makeText(this,"Getting current position..." , Toast.LENGTH_LONG).show();
			locationMgr.addGpsStatusListener(gpsListener);
			while (gps_fix == false) {
				Handler handler = new Handler(); 
				handler.postDelayed(new Runnable() { 
					public void run() {
						Log.i(TAG, "positioning...");
					} 
				}, 1000); 
			}
			Toast.makeText(this,"GPS fixed!" , Toast.LENGTH_LONG).show();
			return locationMgr.getLastKnownLocation(provider);
		}
		else {
			locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			Log.e(TAG, "network");
			return locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
	}

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

	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d(TAG, "GPS_EVENT_STARTED");
				Toast.makeText(planroute.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
				gps_fix = false;
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d(TAG, "GPS_EVENT_STOPPED");
				Toast.makeText(planroute.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
				gps_fix = false;
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
				Toast.makeText(planroute.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
				gps_fix = true;
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
				break;
			}
		}
	};
	LocationListener locationListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			Log.e(TAG, "location: " + location.getLatitude() + "," + location.getLongitude());
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO 自動產生的方法 Stub
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
}