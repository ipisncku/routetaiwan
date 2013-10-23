package tw.ipis.routetaiwan;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

public class favorite_points extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan";
		String TAG = "--fav_point--";
		setContentView(R.layout.favorite_point);
		
		File folder = new File(projectdir);
		if (!folder.exists()) {
			folder.mkdir();
		}
		else {
			/* Display result */
		}
	}
}