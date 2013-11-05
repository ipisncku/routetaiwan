package tw.ipis.routetaiwan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class favorite_points extends Activity {
	ImageView empty;
	private static final String TAG = "--fav_points--";
	final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan";
	List<File> favorite_points;
	List<FavPoint> points;
	List<TableLayout> btn_table;
	TextView textv;
	private int num_of_points = 0;
	private int img_base_pixel = 48;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_point);

		File folder = new File(projectdir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		else {
			favorite_points = new ArrayList<File>();
			points = new ArrayList<FavPoint>();
			/* Display result */
			favorite_points = getListFiles(folder);
			if(favorite_points.isEmpty()) {
				info_empty_folder();
			}
			for(File fd : favorite_points) {
				try {
					String buf = getStringFromFile(fd);
					FavPoint fp = decode_str_to_points(buf);
					if(fp == null && fd.exists())
						fd.delete();
					else if(fp != null) {
						fp.set_filename(fd);
						points.add(fp);
					}
				} catch (Exception e) {
					Log.e(TAG, "Cannot open file " + fd.getName());
					e.printStackTrace();
				}
			}
			num_of_points = points.size();
			Log.i(TAG, "num = " + num_of_points);
			if(num_of_points > 0)
				display();
			else
				info_empty_folder();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		File folder = new File(projectdir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		else {
			favorite_points = new ArrayList<File>();
			points = new ArrayList<FavPoint>();
			/* Display result */
			favorite_points = getListFiles(folder);
			if(favorite_points.isEmpty()) {
				info_empty_folder();
			}
			for(File fd : favorite_points) {
				try {
					String buf = getStringFromFile(fd);
					FavPoint fp = decode_str_to_points(buf);
					if(fp == null && fd.exists())
						fd.delete();
					else if(fp != null) {
						fp.set_filename(fd);
						points.add(fp);
					}
				} catch (Exception e) {
					Log.e(TAG, "Cannot open file " + fd.getName());
					e.printStackTrace();
				}
			}
			if(num_of_points != points.size()){
				Log.i(TAG, "repaint! " + points.size());
				num_of_points = points.size();
				if(num_of_points > 0)
					display();
				else
					info_empty_folder();
			}
		}
		
	}
	
	public String getPhotoByNumber(String number) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String photoURI = null;

	    ContentResolver contentResolver = getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, null, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            photoURI = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_URI));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return photoURI;
	}

	private void display() {
		if(points.isEmpty())
			return;

		btn_table = new ArrayList<TableLayout>();

		final ScrollView sv = (ScrollView)findViewById(R.id.fav_points);
		TableLayout tl = new TableLayout(this);
		tl.setOrientation(TableLayout.VERTICAL);
		sv.removeAllViews();
		sv.addView(tl);

		final Rect scrollBounds = new Rect();
		sv.getHitRect(scrollBounds);

		for(int num = 0; num<points.size(); num++) {
			final FavPoint fp = points.get(num);
			final TableRow tr = CreateTableRow(tl);
			tr.setBackgroundResource(R.drawable.fav_bg);

			ImageView iv = new ImageView(this);
			iv.setImageBitmap(null);
			if(fp.phonenum == null)
				iv.setImageResource(R.drawable.favorite_32);
			else {
				String PhotoURI = getPhotoByNumber(fp.phonenum);
				if(PhotoURI != null) {
					Log.i(TAG, "found contact! ");
					iv.setImageURI(Uri.parse(PhotoURI));
					iv.setMaxWidth((int) (img_base_pixel * getResources().getDisplayMetrics().density));
					iv.setMaxHeight((int) (img_base_pixel * getResources().getDisplayMetrics().density));
				}
				else
					iv.setImageResource(R.drawable.friend);
			}
			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.2f));
			tr.addView(iv);

			TableLayout tl_text = new TableLayout(this);
			tl_text.setOrientation(TableLayout.VERTICAL);
			tl_text.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.8f));
			tr.addView(tl_text);
			TableRow tr_text = new TableRow(this);
			tl_text.addView(tr_text);
			tr_text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			TextView tv = new TextView(this);
			tr_text.addView(tv);
			tv.setTextSize(20);
			tv.setTextColor(Color.WHITE);
			tv.setTypeface(null, Typeface.BOLD);
			tv.setText(fp.name);

			if(fp.phonenum != null) {
				tr_text = new TableRow(this);
				tl_text.addView(tr_text);
				tr_text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				tv = new TextView(this);
				tr_text.addView(tv);
				tv.setTextSize(16);
				tv.setTextColor(Color.WHITE);
				tv.setText(String.format("%s: %s", getResources().getString(R.string.sendfrom),	contact_by_number(fp.phonenum)));
			}

			tr_text = new TableRow(this);
			tl_text.addView(tr_text);
			tr_text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			final TextView description = new TextView(this);
			tr_text.addView(description);
			description.setTextColor(Color.WHITE);
			description.setTextSize(16);

			final TableLayout expand_tl = new TableLayout(this);
			tl.addView(expand_tl);
			btn_table.add(expand_tl);
			expand_tl.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			expand_tl.setGravity(Gravity.CENTER);
			expand_tl.setOrientation(TableLayout.VERTICAL);
			TableRow btn_tr = CreateTableRow(expand_tl);
			btn_tr.setBackgroundResource(R.drawable.fav_btn_bg);

			iv = new ImageView(this);
			iv.setImageBitmap(null);
			iv.setImageResource(R.drawable.button_direction_32);
			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.2f));
			iv.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {
					Intent launchpop = new Intent(favorite_points.this, planroute.class);
					Bundle bundle=new Bundle();

					bundle.putString("end", String.format("%s<%s>", fp.name
							, new DecimalFormat("###.######").format(fp.location.latitude) + "," + new DecimalFormat("###.######").format(fp.location.longitude)));
					launchpop.putExtras(bundle);

					startActivity(launchpop);
				}
			});
			btn_tr.addView(iv);

			iv = new ImageView(this);
			iv.setImageBitmap(null);
			iv.setImageResource(R.drawable.friend);
			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.2f));
			iv.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {
					Intent launchpop = new Intent(favorite_points.this, sms_send.class);
					Bundle bundle=new Bundle();

					bundle.putString("title", fp.name);
					bundle.putString("latlng"
							, new DecimalFormat("###.######").format(fp.location.latitude) + "," + new DecimalFormat("###.######").format(fp.location.longitude));
					launchpop.putExtras(bundle);

					startActivity(launchpop);
				}
			});
			btn_tr.addView(iv);

			iv = new ImageView(this);
			iv.setImageBitmap(null);
			iv.setImageResource(R.drawable.button_map_32);
			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.2f));
			iv.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {
					Intent launchpop = new Intent(favorite_points.this, pop_map.class);
					Bundle bundle=new Bundle();

					bundle.putString("poly", "map,marker");
					bundle.putString("departure", new DecimalFormat("###.######").format(fp.location.latitude) + "," + new DecimalFormat("###.######").format(fp.location.longitude));
					bundle.putString("title", fp.name);
					launchpop.putExtras(bundle);

					startActivity(launchpop);
				}
			});
			btn_tr.addView(iv);

			iv = new ImageView(this);
			iv.setImageBitmap(null);
			iv.setImageResource(R.drawable.delete);
			iv.setAdjustViewBounds(true);
			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.2f));
			iv.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {
					Intent launchpop = new Intent(favorite_points.this, diag_delete.class);
					Bundle bundle=new Bundle();
					bundle.putString("filename", fp.file.getAbsolutePath());
					Log.i(TAG, "file " + fp.file.getAbsolutePath() + " delete!");
					launchpop.putExtras(bundle);

					startActivity(launchpop);
				}
			});
			btn_tr.addView(iv);

			expand_tl.setVisibility(View.GONE);

			tr.setOnClickListener(new OnClickListener(){  
				public void onClick(View v) {
					for(TableLayout table : btn_table) {
						if(table != expand_tl)
							table.setVisibility(View.GONE);
					}

					expand_tl.setVisibility(expand_tl.isShown() ? View.GONE : View.VISIBLE);

					if(!expand_tl.getLocalVisibleRect(scrollBounds)) {
						sv.post(new Runnable() {
							@Override
							public void run() {
								sv.smoothScrollTo(0, tr.getTop());
							} 
						});
					}
				}
			});
			tr.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					for(TableLayout table : btn_table)
						if(table != expand_tl)
							table.setVisibility(View.GONE);

					expand_tl.setVisibility(expand_tl.isShown() ? View.GONE : View.VISIBLE);

					if(!expand_tl.getLocalVisibleRect(scrollBounds)) {
						sv.post(new Runnable() {
							@Override
							public void run() {
								sv.smoothScrollTo(0, tr.getBottom());
							} 
						});
					}
					return true;
				}
			});


			if(fp.description != null) {
				description.setText(fp.description);
			}
			else {
				Log.i(TAG, "no description! go ask google");
				Geocoder_get_address_by_location task = new Geocoder_get_address_by_location(new AnalysisResult() {
					@Override
					public void show_result(final String p) {
						if(p != null) {
							Log.i(TAG, "google description=" + p);
							Intent intent = new Intent(favorite_points.this, FileIntentService.class);
							intent.putExtra("content", p);
							intent.putExtra("filename", fp.file.getAbsolutePath());
							startService(intent);

							fp.set_description(p);
							description.setText(p);
						}
					}
				});
				task.execute(new LatLng[] {fp.location});
			}
		}
		CreateTableRow(tl).setBackgroundColor(Color.TRANSPARENT);
		CreateTableRow(tl).setBackgroundColor(Color.TRANSPARENT);
	}

	public String contact_by_number(String phoneNumber) {
		String name = null;
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor idCursor = getContentResolver().query(uri, null, null, null, null);
		if(idCursor.moveToNext())
			name = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		idCursor.close();
		Log.i(TAG, "name="+ name);
		if(name != null)
			return name;
		else
			return phoneNumber;
	}

	public interface AnalysisResult {
		public void show_result(String p);
	}

	private class Geocoder_get_address_by_location extends AsyncTask<LatLng, Void, List<Address>>{
		private AnalysisResult cb = null;
		public Geocoder_get_address_by_location(AnalysisResult analysisResult) {
			cb = analysisResult;
		}

		@Override
		protected List<Address> doInBackground(LatLng... location) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			try {
				// Getting a maximum of 20 Address that matches the input text
				addresses = geocoder.getFromLocation(location[0].latitude, location[0].longitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}			
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {
			if(addresses != null && addresses.size() > 0) {
				Address addr = addresses.get(0);
				String local = addr.getAdminArea() != null ? addr.getAdminArea() : "";
				local = new StringBuilder().append(local).append(addr.getLocality() != null ? addr.getLocality() : "").toString();
				cb.show_result(local);
			}
		}
	}

	private TableRow CreateTableRow(TableLayout parent){
		TableRow tr = new TableRow(this);	// 1st row

		tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		tr.setGravity(Gravity.CENTER_VERTICAL);
		tr.setClickable(true);

		parent.addView(tr);
		return tr;
	}

	public FavPoint decode_str_to_points(String buf) {
		if(buf == null)
			return null;

		String[] results = buf.split(",");
		if(results.length >= 4 && results[0].contentEquals("save")) {
			/* 格式範例: save,地名,23.xxxxxx,125,xxxxxx,台北市中山區(option) */
			LatLng location = new LatLng(Double.parseDouble(results[2]), Double.parseDouble(results[3]));
			FavPoint fp = new FavPoint(results[1], null, location, results.length >= 5 ? results[4] : null);
			return fp;
		}
		else if(results.length >= 5 && results[0].contentEquals("phone")) {
			/* 格式範例: phone,09xxxxxxxx,地名,23.xxxxxx,125,xxxxxx,台北市中山區(option) */
			LatLng location = new LatLng(Double.parseDouble(results[3]), Double.parseDouble(results[4]));
			FavPoint fp = new FavPoint(results[2], results[1], location, results.length >= 6 ? results[5] : null);
			return fp;
		}
		else
			return null;
	}

	public void info_empty_folder() {
		LinearLayout ll = (LinearLayout)findViewById(R.id.ll_fav_points);

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

	private List<File> getListFiles(File parentDir) {
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				inFiles.addAll(getListFiles(file));
			} else {
				if(file.getName().endsWith(".point")){
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
}