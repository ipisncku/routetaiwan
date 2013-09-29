package tw.ipis.routetaiwan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class pop_map extends Activity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {

	private static final String TAG = "--popup_map--";
	GoogleMap googleMap;
	private List<LatLng> points;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pop_map);

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		// Showing status
		if(status != ConnectionResult.SUCCESS){ // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
			Toast.makeText(this, "Google play service unavailable", Toast.LENGTH_LONG).show();
		}else {    // Google Play Services are available
			Log.d(TAG, "Google play service available");
			
			// Getting reference to the SupportMapFragment of activity_main.xml
			MapFragment fm = ((MapFragment)getFragmentManager().findFragmentById(R.id.smallmap));

			// Getting GoogleMap object from the fragment
			googleMap = fm.getMap();

			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(true);
			
			// Draw poly line and make markers.
			Bundle Data = this.getIntent().getExtras();
			String poly = Data.getString("poly");
			String start, det;
			ArrayList<String> types, locations;
			start = Data.getString("departure");
			if(start != null) {
				det = Data.getString("destination");
				poly = poly.substring(4);
				if(poly.contentEquals("current")) {
					LatLng p = decode_latlng(start);
					add_marker(p, R.drawable.start);
					focus_on_me(p);
				}
				else if(poly.contentEquals("destination")) {
					LatLng p = decode_latlng(det);
					add_marker(p, R.drawable.destination);
					focus_on_me(p);
				}
				else
					draw_polyline(poly, decode_latlng(start), decode_latlng(det));
			}
			else {
				types = Data.getStringArrayList("types");
				locations = Data.getStringArrayList("locations");
				poly = poly.substring(4);
				draw_polyline(poly, types, locations);
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	// input string should be "LAT,LNG"
	private LatLng decode_latlng(String str) {
		String location[] = str.split(",", 2);
		
		LatLng ret = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
		
		return ret;
	}
	
	public void draw_polyline(String poly, ArrayList<String> types, ArrayList<String> locations) {
		Log.i(TAG, "poly=" + poly);
		points = decodePoly(poly);
		
		for (int i = 0; i < locations.size(); i++) {
			LatLng p = decode_latlng(locations.get(i));
			String t = types.get(i);
			
			if (t.contentEquals("start"))
				add_marker(p, R.drawable.start);
			else if (t.contentEquals("walk"))
				add_marker(p, R.drawable.map_walk);
			else if (t.contentEquals("bus"))
				add_marker(p, R.drawable.map_bus);
			else if (t.contentEquals("trtc"))
				add_marker(p, R.drawable.map_trtc);
			else if (t.contentEquals("krtc"))
				add_marker(p, R.drawable.map_krtc);
			else if (t.contentEquals("thsrc"))
				add_marker(p, R.drawable.map_thsrc);
			else if (t.contentEquals("tra"))
				add_marker(p, R.drawable.map_tra);
			else if (t.contentEquals("end"))
				add_marker(p, R.drawable.destination);
			else
				add_marker(p, 0);
		}
		
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
		
		for (int i = 0; i < points.size(); i++) {
			builder.include(points.get(i));
		}
		
		googleMap.addPolyline(new PolylineOptions().addAll(points).width(5).color(Color.BLUE));
		
		googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

		    @Override
		    public void onCameraChange(CameraPosition arg0) {
		        // Move camera.
		    	googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
		        // Remove listener to prevent position reset on camera move.
		    	googleMap.setOnCameraChangeListener(null);
		    }
		});
		
	}
	
	public void draw_polyline(String poly, LatLng start, LatLng det) {
		Log.i(TAG, "poly=" + poly);
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
		
		if(!poly.contentEquals("current"))
			points = decodePoly(poly);
		
		points.add(0, start);
		points.add(points.size(), det);
		add_marker(start, R.drawable.start);
		add_marker(det, R.drawable.destination);
		
		for (int i = 0; i < points.size(); i++) {
			builder.include(points.get(i));
		}
		
		googleMap.addPolyline(new PolylineOptions().addAll(points).width(5).color(Color.BLUE));
		
		googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

		    @Override
		    public void onCameraChange(CameraPosition arg0) {
		        // Move camera.
		    	googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
		        // Remove listener to prevent position reset on camera move.
		    	googleMap.setOnCameraChangeListener(null);
		    }
		});
		
	}
	
	public void add_marker(LatLng _point, int icon) {
		
		MarkerOptions markerOpt = new MarkerOptions();
		markerOpt.position(_point);
		if (icon != 0)
			markerOpt.icon(BitmapDescriptorFactory.fromResource(icon));
		googleMap.addMarker(markerOpt);
	}

	public void focus_on_me(LatLng location) {
		
		CameraPosition camPosition = new CameraPosition.Builder()
		.target(location)
		.zoom(16)
		.build();

		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
		
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Method to decode polyline points
	 * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
	 * */
	private List<LatLng> decodePoly (String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}
	
}