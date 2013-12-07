package tw.ipis.routetaiwan;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String version = "0.9.05";
	Locale myLocale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		File folder = new File(Environment.getExternalStorageDirectory() + "/.routetaiwan");
		if (!folder.exists()) {
			folder.mkdir();
		}

		/* 設定版本號 */
		TextView ver = (TextView)findViewById(R.id.ver);
		try {
			ver.setText(getResources().getString(R.string.version) + ":" + this.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (Exception e) {
			ver.setText(getResources().getString(R.string.version) + ":" + version);
		} 

		/* Create shortcut on desktop */
		Intent shortcutIntent;
		shortcutIntent = new Intent();
		shortcutIntent.setComponent(new ComponentName(
				getApplicationContext().getPackageName(), ".classname"));

		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		final Intent putShortCutIntent = new Intent();
		putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
				shortcutIntent);

		// Sets the custom shortcut's title
		putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
		putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(
				this, R.drawable.routetw_logo_v2));
		putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		sendBroadcast(putShortCutIntent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void my_favorite(View v) {
		Intent myfavorite = new Intent(this, myfavorite.class);
		startActivity(myfavorite);
	}

	public void show_map_v2(View v) {
		Intent launchmap = new Intent(this, showmap.class);
		startActivity(launchmap);
	}

	public void plan_route(View v) {
		Intent route = new Intent(this, planroute.class);
		startActivity(route);
	}

	public void fav_points(View v) {
		Intent route = new Intent(this, favorite_points.class);
		startActivity(route);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//		Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int item_id = item.getItemId();

		switch (item_id){
		case R.id.language_settings:
			openOptionsDialog();
			break;
		default: 
			return false;
		}
		return true;
	}

	public void openOptionsDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeWithCorners));
		dialog.setTitle(getResources().getString(R.string.language_settings));
		dialog.setItems(R.array.country_arrays, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					setLocale(Locale.TRADITIONAL_CHINESE);
					break;
				case 1:
					setLocale(Locale.SIMPLIFIED_CHINESE);
					break;
				case 2:
					setLocale(Locale.ENGLISH);
					break;
				}
			}
		});
		dialog.show();
	}

	public void setLocale(Locale lang) {
		myLocale = lang;
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		Intent refresh = new Intent(this, MainActivity.class);
		startActivity(refresh);
		this.finish();
	}
}
