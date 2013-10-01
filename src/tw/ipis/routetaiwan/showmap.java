package tw.ipis.routetaiwan;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class showmap extends Activity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {
	/* Define area */
	private static final int TEXT_INSTRUCTION = 0x12300001;
	private static final int BUTTON_PLAN_ROUTE = 0x12300002;
	/* Define area end */
	
	private static final String TAG = "~~showmap~~";
	GoogleMap googleMap;
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private boolean first_read = true;
	private boolean button_exist = false;
	MarkerOptions opt_start, opt_destination;
	Marker start, dest;
	Point p = new Point(0, 0);

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
			View cover = findViewById(R.id.mapcover);

			/* Check if it is the first time to use this app, If yes, show some instruction */
			File chk_fist_use = new File(Environment.getExternalStorageDirectory() + "/.routetaiwan/.first_showmap");
			if(chk_fist_use.exists() == false) {
				
				try {
					chk_fist_use.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				cover.setBackgroundColor(Color.argb(0x80, 0xC, 0xC, 0xC));
				TextView instruction = new TextView(this);
				instruction.setId(TEXT_INSTRUCTION);
				instruction.setText(getResources().getString(R.string.instruction));
				instruction.setTextColor(Color.WHITE);
				instruction.setTextSize(20);
				instruction.setGravity(Gravity.CENTER);

				Button ok = new Button(this);
				ok.setText(getResources().getString(R.string.understand));
				ok.setGravity(Gravity.CENTER);
				ok.setTextColor(Color.WHITE);
				
				RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
				RelativeLayout.LayoutParams textLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				textLayoutParameters.addRule(RelativeLayout.CENTER_IN_PARENT);
				instruction.setLayoutParams(textLayoutParameters);

				RelativeLayout.LayoutParams buttonLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				buttonLayoutParameters.addRule(RelativeLayout.BELOW, instruction.getId());
				buttonLayoutParameters.addRule(RelativeLayout.CENTER_IN_PARENT, instruction.getId());
				ok.setLayoutParams(buttonLayoutParameters);

				ll.addView(instruction);
				ll.addView(ok);

				ok.setOnClickListener(new OnClickListener(){  
					public void onClick(View v) {  
						View cover = findViewById(R.id.mapcover);
						RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
						TextView instruction = (TextView) findViewById(TEXT_INSTRUCTION);

						ll.removeView(instruction);
						ll.removeView(v);

						final Animation animTrans = AnimationUtils.loadAnimation(showmap.this, R.anim.anim_alpha_out);
						cover.setAnimation(animTrans);

						/* Make the cover fully transparent after 500ms */
						Handler reset_view = new Handler();
						reset_view.postDelayed(new Runnable()
						{
							public void run()
							{
								View cover = findViewById(R.id.mapcover);
								cover.setBackgroundColor(Color.argb(0x0, 0x0, 0x0, 0x0));
							}
						}, 500);
					}  
				});
			}

			opt_start = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
			opt_destination  = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination));

			locationclient = new LocationClient(this,this,this);
			locationclient.connect();

			// Getting reference to the SupportMapFragment of activity_main.xml
			MapFragment fm = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapv2));

			// Getting GoogleMap object from the fragment
			googleMap = fm.getMap();

			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(true);

			googleMap.getUiSettings().setCompassEnabled(true);

			cover.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					p.x = (int)event.getX();
					p.y = (int)event.getY();
					return false;
				}
			});


			googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
				@Override
				public void onMapLongClick(LatLng position) {
					Log.i(TAG, "lat=" + position.latitude + ",lnt=" + position.longitude);
					showPopup(showmap.this, position);
				}
			});

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
		locationclient.disconnect();
		first_read = true;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
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

		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();


		if(first_read) {
			CameraPosition camPosition = new CameraPosition.Builder()
			.target(new LatLng(latitude, longitude))
			.zoom(16)
			.build();

			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
			first_read = false;
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		focus_on_me(arg0);
	}

	public void create_button() {
		if(button_exist == false) {
			Button gotoplan = new Button(this);
			gotoplan.setId(BUTTON_PLAN_ROUTE);
			gotoplan.setText(getResources().getString(R.string.goto_planroute));

			RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
			RelativeLayout.LayoutParams buttonLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			buttonLayoutParameters.setMargins(0, 0, 0, 0);

			// Add Rule to Layout
			buttonLayoutParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			// Setting the parameters on the Button
			gotoplan.setLayoutParams(buttonLayoutParameters); 
			ll.addView(gotoplan);
			final Animation animTrans = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
			gotoplan.startAnimation(animTrans);

			button_exist = true;
			gotoplan.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {  
					Intent route = new Intent(showmap.this, planroute.class);
					Bundle bundle=new Bundle();

					if(start != null) {
						bundle.putString("start", new DecimalFormat("###.######").format(start.getPosition().latitude) + "," + new DecimalFormat("###.######").format(start.getPosition().longitude));
					}
					if(dest != null) {
						bundle.putString("end", new DecimalFormat("###.######").format(dest.getPosition().latitude) + "," + new DecimalFormat("###.######").format(dest.getPosition().longitude));
					}

					route.putExtras(bundle);
					startActivity(route);
				}  
			});
		}
	}
	
	public void remove_button() {
		if(button_exist == true) {
			Button gotoplan = (Button) findViewById(BUTTON_PLAN_ROUTE);
			
			final Animation animTrans = AnimationUtils.loadAnimation(showmap.this, R.anim.anim_translate_out);
			gotoplan.setAnimation(animTrans);

			/* Make the cover fully transparent after 500ms */
			Handler reset_view = new Handler();
			reset_view.postDelayed(new Runnable()
			{
				public void run()
				{
					Button gotoplan = (Button) findViewById(BUTTON_PLAN_ROUTE);
					RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
					ll.removeView(gotoplan);
					button_exist = false;
				}
			}, 500);
		}
	}

	// The method that displays the popup.
	private void showPopup(final Activity context, final LatLng position) {
		int popupWidth = 250;
		int popupHeight = 250;

		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.menu1);
		LayoutInflater layoutInflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.menu_route, viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth(popupWidth);
		popup.setHeight(popupHeight);
		popup.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		int OFFSET_X = 30;
		int OFFSET_Y = 30;

		// Clear the default translucent background
		//		popup.setBackgroundDrawable(new BitmapDrawable());
		popup.setOutsideTouchable(true);

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

		Button set_start = (Button) layout.findViewById(R.id.add_to_departure);
		set_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(start != null)
					start.remove();
				opt_start.position(position);
				start = googleMap.addMarker(opt_start);
				popup.dismiss();
				create_button();
			}
		});

		Button set_end = (Button) layout.findViewById(R.id.add_to_arrival);
		set_end.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(dest != null)
					dest.remove();
				opt_destination.position(position);
				dest = googleMap.addMarker(opt_destination);
				popup.dismiss();
				create_button();
			}
		});
		// Getting a reference to Close button, and close the popup when clicked.
		Button clearall = (Button) layout.findViewById(R.id.close);
		clearall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(start != null)
					start.remove();
				if(dest != null)
					dest.remove();
				remove_button();
				popup.dismiss();
			}
		});
	}

}