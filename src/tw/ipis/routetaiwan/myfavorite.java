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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.gson.Gson;

public class myfavorite extends Activity {
	List<File> favorite_routes;
	List<Route> routes;
	private static final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan";
	String TAG = "--myfavorite--";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_favorite);
		Log.i(TAG, "in start projectdir= " + projectdir);

		favorite_routes = new ArrayList<File>();
		routes = new ArrayList<Route>();
		File folder = new File(projectdir);
		if (!folder.exists()) {
			Log.i(TAG, "make dir!");
			folder.mkdir();
		}
		else {
			favorite_routes = getListFiles(folder);
			for(File fd : favorite_routes) {
				try {
					Log.i(TAG, "decode file " + fd.getAbsolutePath());
					String buf = getStringFromFile(fd);
					Log.i(TAG, buf);
					Gson gson = new Gson();
					routes.add(gson.fromJson(buf, Route.class));
					Log.i(TAG, fd.getAbsolutePath() + " added");
				} catch (Exception e) {
					Log.e(TAG, "Cannot open file");
					e.printStackTrace();
				}
			}
			/* Display result */
			dump_details(routes);
		}
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

	private TableRow CreateTableRow(TableLayout parent, float weight, final int num){
		TableRow tr = new TableRow(this);	// 1st row

		OnClickListener popup = new OnClickListener() {
			@Override
			public void onClick(View onclick) {
				int childcount = ((ViewGroup) onclick).getChildCount();
				TextView act = (TextView)((ViewGroup) onclick).getChildAt(childcount - 1);

				if(act != null) {
					showPopup(myfavorite.this, act);
				}
			}
		};

		OnLongClickListener save_to_favorite = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				return true;
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
		TableLayout tl = new TableLayout(this);
		tl.setOrientation(TableLayout.VERTICAL);
		sv.removeAllViews();
		for (int i = 0; i < routes.size(); i++) {
			int transit = 0;
			for (int j = 0; j < routes.get(i).legs.length; j++)	{
				TableRow tr = null;
				TableRow time_row = CreateTableRow(tl, 0, i);	// 1st row

				String title = "";
				Time arrival_time, departure_time;
				arrival_time = routes.get(i).legs[j].arrival_time;
				departure_time = routes.get(i).legs[j].departure_time;
				long duration = 0;

				//				routes.get(i).legs[j].mark = new ArrayList<MarkP>();

				if(routes.get(i).legs[j].arrival_time != null && routes.get(i).legs[j].departure_time != null) {
					duration = arrival_time.value - departure_time.value;
				}
				else {
					departure_time = new Time(0);
					arrival_time = new Time(0);
					departure_time.value = System.currentTimeMillis() / 1000;
				}

				TableRow transit_times = CreateTableRow(tl, 0, i);	// 2nd row, leave it for later use

				tr = CreateTableRow(tl, 1.0f, i);
				createImageViewbyR(R.drawable.start, tr, 50, 50);
				createTextView(routes.get(i).legs[j].start_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map,current", 
						routes.get(i).legs[0].start_location, routes.get(i).legs[0].start_location);

				for (int k = 0; k < routes.get(i).legs[j].steps.length; k++) {
					Step step = routes.get(i).legs[j].steps[k];
					if(step.travel_mode.contentEquals("WALKING")) {
						String walk = new StringBuilder().append(step.html_instructions).append("\n(" + step.distance.text + ", " +step.duration.text + ")").toString();
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.walk, tr, 50, 50);
						createTextView(walk, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "map," + step.polyline.points, 
								step.start_location, step.end_location);
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
							createImageViewbyR(R.drawable.bus, tr, 50, 50);
							text = new StringBuilder().append(text).append("bus").toString();
							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
						}
						else if(type.contentEquals("SUBWAY")) {
							if(agencyname.contentEquals("台北捷運")) {
								createImageViewbyR(R.drawable.trtc, tr, 50, 50);
							}
							else if(agencyname.contentEquals("高雄捷運")) {
								createImageViewbyR(R.drawable.krtc, tr, 50, 50);
							}
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", (String)null, (String)null);
							text = new StringBuilder().append("map,").append(step.polyline.points).toString();
							headsign = new StringBuilder().append("(" + getResources().getString(R.string.go_to)).append(headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, step.start_location, step.end_location);
						}
						else if(type.contentEquals("HEAVY_RAIL")) {
							if(agencyname.contentEquals("台灣高鐵")) {
								createImageViewbyR(R.drawable.hsr, tr, 50, 50);
							}
							else if(agencyname.contentEquals("台灣鐵路管理局")) {
								createImageViewbyR(R.drawable.train, tr, 50, 50);
							}
							else
								createTextView("車", tr, Color.rgb(0,0,0), 0.1f, Gravity.CENTER, "transit,null", step.transit_details.departure_stop.name, step.transit_details.arrival_stop.name);
							headsign = new StringBuilder().append("(" + headsign + ")").toString();
							createTextView(trans + headsign + trans_to + time_taken, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT | Gravity.CENTER_VERTICAL, text, 
									step.start_location, step.end_location);
						}
					}
					if(k == routes.get(i).legs[j].steps.length - 1) {
						// Arrived
						tr = CreateTableRow(tl, 1.0f, i);
						createImageViewbyR(R.drawable.destination, tr, 50, 50);
						createTextView(routes.get(i).legs[j].end_address, tr, Color.rgb(0,0,0), 0.9f, Gravity.LEFT, "map,destination", 
								routes.get(i).legs[0].end_location, routes.get(i).legs[0].end_location);
					}
				}
				String str = getResources().getString(R.string.transit) + ": " + transit + "x";
				createTextView(str, transit_times, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL, "all," + routes.get(i).overview_polyline.points, 
						routes.get(i).legs[j].mark);

				String dur = String.format(" (%d" + getResources().getString(R.string.hour) + "%d" + getResources().getString(R.string.minute) + ")",
						TimeUnit.SECONDS.toHours(duration), TimeUnit.SECONDS.toMinutes(duration % 3600));
				title = new StringBuilder().append(convertTime(departure_time.value)).append(" - ").append(convertTime(arrival_time.value)).append(dur).toString();
				createTextView(title, time_row, Color.rgb(0,0,0), 1.0f, Gravity.LEFT | Gravity.CENTER_VERTICAL,
						"all," + routes.get(i).overview_polyline.points, routes.get(i).legs[j].mark);
			}
		}
		sv.addView(tl);
	}

	private List<File> getListFiles(File parentDir) {
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				inFiles.addAll(getListFiles(file));
			} else {
				if(file.getName().endsWith(".json")){
					Log.i(TAG, "file " + file.getName());
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
			Intent launchpop = new Intent(this, pop_transit.class);
			Bundle bundle=new Bundle();
			bundle.putString("poly", action);
			launchpop.putExtras(bundle);

			startActivity(launchpop);
		}
	}
}