package tw.ipis.routetaiwan;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
