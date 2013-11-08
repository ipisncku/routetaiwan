package tw.ipis.routetaiwan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import tw.ipis.routetaiwan.myfavorite.Route.Leg.Step;
import tw.ipis.routetaiwan.myfavorite.Route.Leg.Step.Poly;
import tw.ipis.routetaiwan.myfavorite.Route.Leg.Step.ValueText;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.gson.Gson;

public class myfavorite extends Activity {
	TextView textv;
	ImageView empty;
	List<File> favorite_routes;
	List<Route> routes;
	private static final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan";
	String TAG = "--myfavorite--";
	private int basic_pixel = 36;
	private int basic_btn_pixel = 16;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_favorite);

		favorite_routes = new ArrayList<File>();
		routes = new ArrayList<Route>();
		File folder = new File(projectdir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		else {
			favorite_routes = getListFiles(folder);
			if(favorite_routes.isEmpty()) {
				info_empty_folder();
			}
			for(File fd : favorite_routes) {
				try {
					String buf = getStringFromFile(fd);
					Gson gson = new Gson();
					Route newroute = gson.fromJson(buf, Route.class);
					newroute.filename = fd.getAbsolutePath();
					routes.add(newroute);
				} catch (Exception e) {
					Log.e(TAG, "Cannot open file");
					e.printStackTrace();
				}
			}
			/* Display result */
			dump_details(routes);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		favorite_routes = new ArrayList<File>();
		routes = new ArrayList<Route>();
		File folder = new File(projectdir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		else {
			favorite_routes = getListFiles(folder);
			if(favorite_routes.isEmpty()) {
				info_empty_folder();
			}
			for(File fd : favorite_routes) {
				try {
					String buf = getStringFromFile(fd);
					Gson gson = new Gson();
					Route newroute = gson.fromJson(buf, Route.class);
					newroute.filename = fd.getAbsolutePath();
					routes.add(newroute);
				} catch (Exception e) {
					Log.e(TAG, "Cannot open file");
					e.printStackTrace();
				}
			}
			/* Display result */
			dump_details(routes);
		}
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
	
	public void info_empty_folder() {
		LinearLayout ll = (LinearLayout)findViewById(R.id.ll_favorites);
		
		if(empty != null)
			ll.removeView(empty);
		else
			empty = null;
		if(textv != null)
			ll.removeView(textv);
		else
			textv = null;

		empty = new ImageView(this);
		empty.setImageBitmap(null);
		empty.setImageResource(R.drawable.empty);
		empty.setAdjustViewBounds(true);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.gravity = Gravity.CENTER;
		empty.setLayoutParams(param);
		
		textv = new TextView(this);
		textv.setText(getResources().getString(R.string.no_data));
		textv.setTextColor(Color.BLACK);
		textv.setTextSize(20);
		textv.setGravity(Gravity.CENTER);
		textv.setHorizontallyScrolling(false);
		
		ll.addView(empty);
		ll.addView(textv);
	}

	public String convertTime(long time){
		time = time * 1000;	// Change to milli-seconds
		Date date = new Date(time);
		Format format = new SimpleDateFormat("HH:mm");
		return format.format(date).toString();
	}

	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, LatLng_s departure, LatLng_s destination) {
		TextView tv = new TextView(this);
		tv.setText(content);
		tv.setTextColor(textcolor);
		tv.setTextSize(16);
		tv.setHorizontallyScrolling(false);
		tv.setWidth(0);
		tv.setGravity(gravity);
		tv.setTag(R.id.tag_zero, text);
		String dept = String.valueOf(departure.lat) + "," + String.valueOf(departure.lng);
		String det = String.valueOf(destination.lat) + "," + String.valueOf(destination.lng);
		tv.setTag(R.id.tag_first, dept);
		tv.setTag(R.id.tag_second, det);
		if(weight != 0)
			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		parent.addView(tv);
		return tv;
	}

	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, String departure, String destination) {
		TextView tv = new TextView(this);
		tv.setText(content);
		tv.setTextColor(textcolor);
		tv.setTextSize(16);
		tv.setHorizontallyScrolling(false);
		tv.setWidth(0);
		tv.setGravity(gravity);
		tv.setTag(R.id.tag_zero, text);
		tv.setTag(R.id.tag_first, departure);
		tv.setTag(R.id.tag_second, destination);
		if(weight != 0)
			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		parent.addView(tv);
		return tv;
	}

	private TextView createTextView(String content, TableRow parent, int textcolor, float weight, int gravity, String text, List<MarkP> allP) {
		TextView tv = new TextView(this);
		tv.setText(content);
		tv.setTextColor(textcolor);
		tv.setTextSize(16);
		tv.setHorizontallyScrolling(false);
		tv.setWidth(0);
		tv.setGravity(gravity);
		tv.setTag(R.id.tag_zero, text);
		tv.setTag(R.id.tag_first, allP);
		if(weight != 0)
			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		parent.addView(tv);
		return tv;
	}

	private ImageView createImageViewbyAnim(TableRow parent, int height, int width) {
		ImageView iv = new ImageView(this);
		iv.setBackgroundResource(R.anim.btn_anim_moreinfo);
		AnimationDrawable startAnimation = (AnimationDrawable) iv.getBackground(); 
		iv.setLayoutParams(new LayoutParams((int) (width * getResources().getDisplayMetrics().density), (int) (width * getResources().getDisplayMetrics().density)));
		iv.setAdjustViewBounds(true);
		parent.addView(iv);
		startAnimation.start();
		return iv;
	}
	
	private ImageView createImageViewbyR(int R, TableRow parent, int height, int width) {
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(null);
		iv.setImageResource(R);
		iv.setMaxHeight((int) (height * getResources().getDisplayMetrics().density));
		iv.setMaxWidth((int) (width * getResources().getDisplayMetrics().density));
		iv.setAdjustViewBounds(true);
		parent.addView(iv);
		return iv;
	}

	private TableRow CreateTableRow(TableLayout parent, float weight, final int num){
		TableRow tr = new TableRow(this);	// 1st row

		OnClickListener popup = new OnClickListener() {
			@Override
			public void onClick(View onclick) {
				int childcount = ((ViewGroup) onclick).getChildCount();
				TextView act = (TextView)((ViewGroup) onclick).getChildAt(childcount - 2);

				if(act != null) {
					showPopup(myfavorite.this, act);
				}
			}
		};

		OnLongClickListener save_to_favorite = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				/* Pop up a diaglog to ask whether to remove this item */
				Intent launchpop = new Intent(myfavorite.this, diag_delete.class);
				Bundle bundle=new Bundle();
				bundle.putString("filename", routes.get(num).filename);
				launchpop.putExtras(bundle);
				
				startActivity(launchpop);
				return true;
			}
		};
		
		tr.setBackgroundResource(R.drawable.seletor_trans);

		if(weight != 0)
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, weight));
		else
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		tr.setGravity(Gravity.CENTER_VERTICAL);
		tr.setClickable(true);
		/* Short click: open a popup activity for MAP or REALTIME INFO */
		tr.setOnClickListener(popup);
		/* Long click: open a dialog menu asking for saving to My Favorite */
		tr.setOnLongClickListener(save_to_favorite);

		parent.addView(tr);
		return tr;
	}

	private void dump_details(List<Route> routes) {
		ScrollView sv = (ScrollView) this.findViewById(R.id.favorites);

		// Create a LinearLayout element
		sv.removeAllViews();
		TableLayout tl_host = new TableLayout(this);
		tl_host.setOrientation(TableLayout.VERTICAL);
		for (int i = 0; i < routes.size(); i++) {
			int transit = 0;
			TableLayout tl = new TableLayout(this);
			tl.setOrientation(TableLayout.VERTICAL);
			tl.setBackgroundResource(R.drawable.fav_btn_bg);
			for (int j = 0; j < routes.get(i).legs.length; j++)	{
				TableRow tr = null;
				TableRow time_row = CreateTableRow(tl, 0, i);	// 1st row

				String title = "";
				Time arrival_time, departure_time;
				arrival_time = routes.get(i).legs[j].arrival_time;
				departure_time = routes.get(i).legs[j].departure_time;
				long duration = 0;

				if(routes.get(i).legs[j].arrival_time != null && routes.get(i).legs[j].departure_time != null) {
					duration = arrival_time.value - departure_time.value;
				}
				else {
					departure_time = new Time(0);
					arrival_time = new Time(0);
					departure_time.value = System.currentTimeMillis() / 1000;
				}

//				TableRow transit_times = CreateTableRow(tl, 0, i);	// 2nd row, leave it for later use

				tr = CreateTableRow(tl, 1.0f, i);
				createImageViewbyR(R.drawable.start, tr, basic_pixel, basic_pixel);
				createTextView(routes.get(i).legs[j].start_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map,current", 
						routes.get(i).legs[0].start_location, routes.get(i).legs[0].start_location);
				createImageViewbyAnim(tr, basic_btn_pixel, basic_btn_pixel);

				for (int k = 0; k < routes.get(i).legs[j].steps.length; k++) {
					Step step = routes.get(i).legs[j].steps[k];
					if(step.travel_mode.contentEquals("WALKING")) {
						String walk = new StringBuilder().append(step.html_instructions).append("\n(" + step.distance.text + ", " +step.duration.text + ")").toString();
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.walk, tr, basic_pixel, basic_pixel);
						createTextView(walk, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + step.polyline.points, 
								step.start_location, step.end_location);
						createImageViewbyAnim(tr, basic_btn_pixel, basic_btn_pixel);
					}
					else if(step.travel_mode.contentEquals("TRANSIT")) {
						String type = step.transit_details.line.vehicle.type;
						String agencyname = step.transit_details.line.agencies[0].name;
						String text = "transit,";

						String trans = new StringBuilder().append(getResources().getString(R.string.taketransit)).append(step.transit_details.line.short_name).toString();

						String headsign = step.transit_details.headsign;

						String trans_to = new StringBuilder().append(getResources().getString(R.string.to)).append(step.transit_details.arrival_stop.name).toString();

						String time_taken = new StringBuilder().append("\n(" + step.transit_details.num_stops + getResources().getString(R.string.stops) + ", " +step.duration.text + ")").toString();
						transit++;

						tr = CreateTableRow(tl, 1.0f, i);
						if(type.contentEquals("BUS")) {
							createImageViewbyR(R.drawable.bus, tr, basic_pixel, basic_pixel);
							text = new StringBuilder().append(text).append("bus,").append(step.transit_details.line.short_name + ",").append(step.transit_details.line.agencies[0].name + ",")
									.append(step.transit_details.departure_stop.name + ",").append(step.transit_details.arrival_stop.name + ",")
									.append(step.transit_details.line.name).toString();
							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
						}
						else if(type.contentEquals("SUBWAY")) {
							if(agencyname.contentEquals("台北捷運")) {
								createImageViewbyR(R.drawable.trtc, tr, basic_pixel, basic_pixel);
							}
							else if(agencyname.contentEquals("高雄捷運")) {
								createImageViewbyR(R.drawable.krtc, tr, basic_pixel, basic_pixel);
							}
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", (String)null, (String)null);
							text = new StringBuilder().append("map,").append(step.polyline.points).toString();
							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.start_location, step.end_location);
						}
						else if(type.contentEquals("HEAVY_RAIL")) {
							if(agencyname.contentEquals("台灣高鐵")) {
								createImageViewbyR(R.drawable.hsr, tr, basic_pixel, basic_pixel);
								text = new StringBuilder().append(text).append("hsr,").append(train_num(step.transit_details.headsign)+",")
										.append(step.transit_details.departure_time.value+",")
										.append(step.transit_details.departure_stop.name+",")
										.append(step.transit_details.arrival_stop.name).toString();
							}
							else if(agencyname.contentEquals("台灣鐵路管理局")) {
								createImageViewbyR(R.drawable.train, tr, basic_pixel, basic_pixel);
								text = new StringBuilder().append(text).append("tra,").append(train_num(step.transit_details.headsign) + ",").append(step.transit_details.line.short_name).toString();
							}
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
							headsign = new StringBuilder().append("(" + headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, 
									step.start_location, step.end_location);
						}
						else if(type.contentEquals("DRIVING")) {
							createImageViewbyR(R.drawable.drive, tr, basic_pixel, basic_pixel);
							createTextView(new StringBuilder().append(step.html_instructions).append("\n(" + step.distance.text + ", " +step.duration.text + ")").toString()
									, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, 
									step.start_location, step.end_location);
						}
						createImageViewbyAnim(tr, basic_btn_pixel, basic_btn_pixel);
					}
					if(k == routes.get(i).legs[j].steps.length - 1) {
						// Arrived
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.destination, tr, basic_pixel, basic_pixel);
						createTextView(routes.get(i).legs[j].end_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT, "map,destination", 
								routes.get(i).legs[0].end_location, routes.get(i).legs[0].end_location);
						createImageViewbyAnim(tr, basic_btn_pixel, basic_btn_pixel);
					}
				}
				String str = getResources().getString(R.string.transit) + ": " + transit + "x";
//				createTextView(str, transit_times, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "all," + routes.get(i).overview_polyline.points, 
//						routes.get(i).legs[j].mark);

				String dur = String.format(" (%d" + getResources().getString(R.string.hour) + "%d" + getResources().getString(R.string.minute) + ")",
						TimeUnit.SECONDS.toHours(duration), TimeUnit.SECONDS.toMinutes(duration % 3600));
				title = new StringBuilder().append(convertTime(departure_time.value)).append(" - ")
										.append(convertTime(arrival_time.value))
										.append(dur)
										.append("\n" + str).toString();
				createTextView(title, time_row, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL,
						"all," + routes.get(i).overview_polyline.points, routes.get(i).legs[j].mark);
				createImageViewbyAnim(time_row, basic_btn_pixel, basic_btn_pixel);
			}
			tl_host.addView(tl);
		}
		sv.addView(tl_host);
	}
	
	private String train_num(String ori) {
		// ori example: 往苗栗,車次1183,山線 or 往左營 ,車次151
		return ori.replaceAll("[^0-9]", "");
	}

	private List<File> getListFiles(File parentDir) {
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				inFiles.addAll(getListFiles(file));
			} else {
				if(file.getName().endsWith(".json")){
					inFiles.add(file);
				}
			}
		}
		return inFiles;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	public static String getStringFromFile (File fl) throws Exception {
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		//Make sure you close all streams.
		fin.close();        
		return ret;
	}

	public class Route {
		String filename;
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
			LatLng_s start_location, end_location;
			List<MarkP> mark;

			public class Step {
				String travel_mode;
				LatLng_s start_location, end_location;
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
						LatLng_s location;
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
			LatLng_s southwest, northeast;
		}
	}

	public class LatLng_s {
		double lat;
		double lng;
	}

	public class Time {
		long value;

		public Time(long v) {
			value = v;
		}
	}

	public class MarkP {
		String type;
		String title;
		String description;
		LatLng_s location;

		public MarkP(String s, String t, String d, LatLng_s l) {
			type = s;
			title = t;
			description = d;
			location = l;
		}
	}
	private void showPopup(final Activity context, TextView act) {

		String action = (String) act.getTag(R.id.tag_zero);
		Log.i(TAG, "tag=" + action);
		if(action.regionMatches(0, "map", 0, 3)) {
			Intent launchpop = new Intent(this, pop_map.class);
			Bundle bundle=new Bundle();

			bundle.putString("poly", action);
			bundle.putString("departure", (String) act.getTag(R.id.tag_first));
			bundle.putString("destination", (String) act.getTag(R.id.tag_second));
			launchpop.putExtras(bundle);

			startActivity(launchpop);
		}
		else if(action.regionMatches(0, "all", 0, 3)) {
			Intent launchpop = new Intent(this, pop_map.class);
			Bundle bundle=new Bundle();
			ArrayList<String> types = new ArrayList<String>();
			ArrayList<String> title = new ArrayList<String>();
			ArrayList<String> description = new ArrayList<String>();
			ArrayList<String> locations = new ArrayList<String>();

			@SuppressWarnings("unchecked")
			List<MarkP> allP = (List<MarkP>)act.getTag(R.id.tag_first);

			Iterator<MarkP> mark =  allP.iterator();
			while(mark.hasNext()){
				MarkP e = mark.next();
				types.add(e.type);
				title.add(e.title);
				description.add(e.description);
				locations.add(e.location.lat + "," + e.location.lng);
			}

			bundle.putString("poly", action);
			bundle.putStringArrayList("types", types);
			bundle.putStringArrayList("title", title);
			bundle.putStringArrayList("descriptions", description);
			bundle.putStringArrayList("locations", locations);
			launchpop.putExtras(bundle);
			
			startActivity(launchpop);
		}
		else if(action.regionMatches(0, "transit", 0, 7)) {
			String[] transit_detail = action.split(",");
			
			Intent launchpop = new Intent(this, pop_transit.class);
			Bundle bundle=new Bundle();

			if(transit_detail[1].contentEquals("tra")) {
				bundle.putString("type", transit_detail[1]);
				bundle.putString("line", transit_detail[2]);
				bundle.putString("class", transit_detail[3]);
				bundle.putLong("time", System.currentTimeMillis());
			}
			else if(transit_detail[1].contentEquals("hsr")) {
				bundle.putString("type", transit_detail[1]);
				bundle.putString("line", transit_detail[2]);
				bundle.putLong("time", Long.parseLong(transit_detail[3]));
				bundle.putString("dept", transit_detail[4]);
				bundle.putString("arr", transit_detail[5]);
			}
			else if(transit_detail[1].contentEquals("bus")) {
				bundle.putString("type", transit_detail[1]);
				bundle.putString("line", transit_detail[2]);
				bundle.putString("agency", transit_detail[3]);
				bundle.putString("dept", transit_detail[4]);
				bundle.putString("arr", transit_detail[5]);
				bundle.putString("headname", transit_detail[6]);
			}
			else {
				bundle.putString("type", transit_detail[1]);	// type = null
			}
			
			launchpop.putExtras(bundle);

			startActivity(launchpop);
		}
	}
}