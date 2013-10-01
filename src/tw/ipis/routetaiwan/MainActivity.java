package tw.ipis.routetaiwan;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "~main~";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		File folder = new File(Environment.getExternalStorageDirectory() + "/.routetaiwan");
		if (!folder.exists()) {
		    folder.mkdir();
		}
	}
	public void my_favorite(View v) {
		Toast.makeText(this,"施工中...",Toast.LENGTH_LONG).show();
		//		if(checkGooglePlayServices() > 0) {
		//			Toast.makeText(this,"無法連接上Google Play",Toast.LENGTH_LONG).show();
		//			return;
		//		}
		//
		//		Intent launchmap = new Intent(this, map.class);
		//		startActivity(launchmap);
	}

	public void show_map_v2(View v) {
		Intent launchmap = new Intent(this, showmap.class);
		startActivity(launchmap);
	}

	public void plan_route(View v) {
		Intent route = new Intent(this, planroute.class);
		startActivity(route);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
