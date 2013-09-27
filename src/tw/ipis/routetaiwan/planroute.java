package tw.ipis.routetaiwan;

import java.net.URLEncoder;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Bound;
import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step;
import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.Poly;
import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Leg.Step.ValueText;
import tw.ipis.routetaiwan.planroute.DirectionResponseObject.Route.Time;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.gson.Gson;

public class planroute extends Activity {

	//	ProgressBar planning;
	String TAG = "~~planroute~~";
	private ProgressBar planning;
	private EditText from;
	private EditText to;
	private List<GeoPoint> _points = new ArrayList<GeoPoint>();
	private LocationManager locationMgr;
	private DownloadWebPageTask task = null;
	private boolean isrequested = false;
	public DirectionResponseObject dires = null;
	private int textid = 0;
	String provider = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.planroute);

		ConnectivityManager connMgr = (ConnectivityManager) 
		getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			Toast.makeText(this, getResources().getString(R.string.info_network_using) + networkInfo.getTypeName() , Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, getResources().getString(R.string.warninig_no_network) , Toast.LENGTH_LONG).show();
		}

		start_positioning();
	}

	@Override
	protected void onResume() {

		super.onResume();

		start_positioning();
	}

	@Override
	protected void onStop() {
		locationMgr.removeUpdates(locationListener);
		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
			task.cancel(true);
		super.onStop();
	}

	public void start_planing(View v) {
		if(task != null && task.getStatus() != DownloadWebPageTask.Status.FINISHED)
			task.cancel(true);
		foreground_cosmetic();

		isrequested = true;

		from = (EditText)findViewById(R.id.from);
		String start = from.getText().toString();	// Get user input "From"
		Location currentloc = GetCurrentPosition();
		if (!start.isEmpty() || currentloc.getProvider().contentEquals("network")) // Wait for positioning
			Getroute();
	}

	public void start_positioning() {
		String locprovider = 	initLocationProvider();
		if(locprovider == null) {
			Toast.makeText(this, getResources().getString(R.string.warning_no_loc_provider) , Toast.LENGTH_LONG).show();
			return;
		}
		locationMgr.requestLocationUpdates(locprovider, 0, 0, locationListener);
		if(locprovider.contentEquals("gps")) {
			Toast.makeText(this, getResources().getString(R.string.info_positioning_by_gps) , Toast.LENGTH_SHORT).show();
			locationMgr.addGpsStatusListener(gpsListener);
		}
		else
			Toast.makeText(this, getResources().getString(R.string.info_positioning_by_network) , Toast.LENGTH_SHORT).show();
	}

	private void foreground_cosmetic() {
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);

		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(from.getWindowToken(), 0);

		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);
		sv.removeAllViews();	// Clear screen
		planning = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
		planning.setIndeterminate(true);
		sv.addView(planning);	// Add processbar

		planning.setVisibility(ProgressBar.VISIBLE);
	}

	public void Getroute() {
		String request = "";
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);
		String start = from.getText().toString();	// Get user input "From"
		String destination = to.getText().toString();	// Get user input "to"
		String Mapapi = "https://maps.googleapis.com/maps/api/directions/json?origin={0}&destination={1}&sensor={3}&departure_time={2}&mode={4}&alternatives=true&region=tw";

		isrequested = false;

		if(Locale.getDefault().getDisplayLanguage().contentEquals("中文"))
			Mapapi = new StringBuilder().append(Mapapi).append("&language=zh-tw").toString();

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
		task = new DownloadWebPageTask();
		task.execute(new String[] {request});
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

						response = EntityUtils.toString(entity);
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

		@Override
		protected void onPostExecute(String result) {
			decode(result);
		}
	}

	public String convertTime(long time){
		time = time * 1000;	// Change to milli-seconds
		Date date = new Date(time);
		Format format = new SimpleDateFormat("HH:mm");
		return format.format(date).toString();
	}

	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text) {
		TextView tv = new TextView(this);
		tv.setText(content);
		tv.setTextColor(textcolor);
		tv.setTextSize(16);
		tv.setHorizontallyScrolling(false);
		tv.setWidth(0);
		tv.setGravity(gravity);
		tv.setId(textid);
		tv.setTag(text);
		//		textview_extra.add(textid, text);
		//		Log.i(TAG, textid + ". " + text);
		//		textid++;
		if(weight != 0)
			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		parent.addView(tv);
		return tv;
	}

	private ImageView createImageViewbyR(int R, TableRow parent, int height, int width) {
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(null);
		iv.setImageResource(R);
		iv.setMaxHeight(height);
		iv.setMaxWidth(width);
		iv.setAdjustViewBounds(true);
		parent.addView(iv);
		return iv;
	}

	private TableRow CreateTableRow(TableLayout parent, float weight, int num){
		TableRow tr = new TableRow(this);	// 1st row

		OnClickListener popup = new OnClickListener() {
			@Override
			public void onClick(View onclick) {
				int childcount = ((ViewGroup) onclick).getChildCount();
				Log.i(TAG, "childcount=" + childcount);
				TextView act = (TextView)((ViewGroup) onclick).getChildAt(childcount - 1);

				if(act != null) {
					showPopup(planroute.this, act);
				}
			}
		};

		if(num % 2 == 0)
			tr.setBackgroundColor(Color.WHITE);
		else
			tr.setBackgroundColor(Color.LTGRAY);
		if(weight != 0)
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		else
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		tr.setGravity(Gravity.CENTER_VERTICAL);
		tr.setClickable(true);
		tr.setOnClickListener(popup);

		parent.addView(tr);
		return tr;
	}

	public boolean dumpdetails(DirectionResponseObject dires) {
		ScrollView sv = (ScrollView) this.findViewById(R.id.routes);

		// Create a LinearLayout element
		TableLayout tl = new TableLayout(this);
		tl.setOrientation(TableLayout.VERTICAL);
		sv.removeAllViews();

		if(!dires.status.contentEquals("OK")) {
			Toast.makeText(this, getResources().getString(R.string.info_no_result) , Toast.LENGTH_LONG).show();
			return false;
		}


		// Add text
		for (int i = 0; i < dires.routes.length; i++) {
			int transit = 0;
			for (int j = 0; j < dires.routes[i].legs.length; j++)	{
				TableRow tr = CreateTableRow(tl, 0, i);	// 1st row

				String title = "";
				Time arrival_time, departure_time;
				arrival_time = dires.routes[i].legs[j].arrival_time;
				departure_time = dires.routes[i].legs[j].departure_time;
				int duration = arrival_time.value - departure_time.value;

				String dur = String.format(" (%d" + getResources().getString(R.string.hour) + "%d" + getResources().getString(R.string.minute) + ")",
						TimeUnit.SECONDS.toHours(duration), TimeUnit.SECONDS.toMinutes(duration % 3600));

				title = new StringBuilder().append(convertTime(departure_time.value)).append(" - ").append(convertTime(arrival_time.value)).append(dur).toString();

				createTextView(title, tr, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + dires.routes[i].overview_polyline.points);

				TableRow transit_times = CreateTableRow(tl, 0, i);	// 2nd row, leave it for later use

				tr = CreateTableRow(tl, 1.0f, i);
				createImageViewbyR(R.drawable.start, tr, 50, 50);
				createTextView(dires.routes[i].legs[j].start_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map,current");

				for (int k = 0; k < dires.routes[i].legs[j].steps.length; k++) {
					Step step = dires.routes[i].legs[j].steps[k];
					if(step.travel_mode.contentEquals("WALKING")) {
						String walk = new StringBuilder().append(step.html_instructions).append("\n(" + step.distance.text + ", " +step.duration.text + ")").toString();
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.walk, tr, 50, 50);
						createTextView(walk, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + step.polyline.points);
					}
					else if(step.travel_mode.contentEquals("TRANSIT")) {
						String type = step.transit_details.line.vehicle.type;
						String agencyname = step.transit_details.line.agencies[0].name;
						// TODO: filled the text
						String text = "transit,";

						String trans = new StringBuilder().append(getResources().getString(R.string.taketransit)).append(step.transit_details.line.short_name).toString();

						trans = new StringBuilder().append(trans).append(getResources().getString(R.string.to)).append(step.transit_details.arrival_stop.name).toString();

						trans = new StringBuilder().append(trans).append("\n(" + step.transit_details.num_stops + getResources().getString(R.string.stops) + ", " +step.duration.text + ")").toString();
						transit++;

						tr = CreateTableRow(tl, 1.0f, i);
						if(type.contentEquals("BUS")) {
							createImageViewbyR(R.drawable.bus, tr, 50, 50);
							text = new StringBuilder().append(text).append("bus").toString();
						}
						else if(type.contentEquals("SUBWAY")) {
							if(agencyname.contentEquals("台北捷運"))
								createImageViewbyR(R.drawable.trtc, tr, 50, 50);
							else if(agencyname.contentEquals("高雄捷運"))
								createImageViewbyR(R.drawable.krtc, tr, 50, 50);
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null");
							text = new StringBuilder().append("map,").append(step.polyline.points).toString();
						}
						else if(type.contentEquals("HEAVY_RAIL")) {
							if(agencyname.contentEquals("台灣高鐵"))
								createImageViewbyR(R.drawable.hsr, tr, 50, 50);
							else if(agencyname.contentEquals("台灣鐵路管理局"))
								createImageViewbyR(R.drawable.train, tr, 50, 50);
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null");
						}
						createTextView(trans, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text);
					}
					if(k == dires.routes[i].legs[j].steps.length - 1) {
						// Arrived
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.destination, tr, 50, 50);
						createTextView(dires.routes[i].legs[j].end_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT, "map," + dires.routes[i].overview_polyline.points );
					}
				}
				String str = getResources().getString(R.string.transit) + ": " + transit + "x";
				createTextView(str, transit_times, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + dires.routes[i].overview_polyline.points );
			}
		}
		// Add the LinearLayout element to the ScrollView
		sv.addView(tl);

		return true;
	}

	private void decode(String result) {
		try {
			Gson gson = new Gson();
			dires = gson.fromJson(result,	DirectionResponseObject.class);
			Log.i(TAG, "Total routes = " + dires.routes.length);
			planning.setVisibility(ProgressBar.GONE);
			dumpdetails(dires);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
		}
	}

	public Location GetCurrentPosition() {
		if(provider != null) {
			Log.i(TAG, "Current provider is " + provider);
			return locationMgr.getLastKnownLocation(provider);
		}
		else
			return locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}

	private String initLocationProvider() {
		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		//1.選擇最佳提供器
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		provider = locationMgr.getBestProvider(criteria, true);

		if (provider != null) {
			return provider;
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

		return null;
	}

	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d(TAG, "GPS_EVENT_STARTED");
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d(TAG, "GPS_EVENT_STOPPED");
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d(TAG, "GPS_EVENT_FIRST_FIX");
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
			if (isrequested)
				Getroute();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	public class DirectionResponseObject {
		public String status;
		public Route[] routes;
		String copyrights;
		Poly overview_polyline;
		String[] warnings;
		int[] waypoint_order;
		Bound bounds;

		public class Route {
			String summary;
			String[] warnings;
			Leg[] legs;
			Poly overview_polyline;
			String copyrights;

			public class Leg {
				public Step[] steps;
				ValueText duration;
				Time arrival_time, departure_time;
				String start_address, end_address;

				public class Step {
					String travel_mode;
					LatLng start_location, end_location;
					Poly polyline;
					ValueText duration;
					String html_instructions;
					ValueText distance;
					Transit transit_details;

					public class Transit {
						Stop arrival_stop, departure_stop;
						Time arrival_time, departure_time;
						String headsign;
						int num_stops;
						TransitLine line;

						public class Stop {
							LatLng location;
							String name;
						}
						public class TransitLine {
							Agency agencies[];
							String name;
							String short_name;
							Vehicle vehicle;

							public class Vehicle {
								String icon;
								String name;
								String type;
							}

							public class Agency {
								String name;
								String url;
							}
						}
					}

					public class Poly {
						public String points;
					}

					public class ValueText {
						int value;
						String text;
					}
				}
			}

			public class Bound {
				LatLng southwest, northeast;
			}

			public class Time {
				int value;
			}
		}
	}

	private void showPopup(final Activity context, TextView act) {

		String action = (String) act.getTag();
		Log.i(TAG, "tag=" + action);
		if(action.regionMatches(0, "map", 0, 3)) {
			Intent launchpop = new Intent(this, pop_map.class);
			Bundle bundle=new Bundle();
			bundle.putString("poly", (String) act.getTag());
			launchpop.putExtras(bundle);
			
			startActivity(launchpop);
		}
		else if(action.regionMatches(0, "transit", 0, 7)) {
			Intent launchpop = new Intent(this, pop_transit.class);
			startActivity(launchpop);
		}
	}
}