package tw.ipis.routetaiwan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {

	private static final String TAG = "~!!!!~";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}
		public void start_positioning(View v) {
			
			if(checkGooglePlayServices() > 0) {
				Toast.makeText(this,"無法連接上Google Play",Toast.LENGTH_LONG).show();
				return;
			}
			
			Intent launchmap = new Intent(this, map.class);
			startActivity(launchmap);
		}
		
		public void show_map_v2(View v) {
			Intent launchmap = new Intent(this, showmap.class);
			startActivity(launchmap);
		}
		
		public void plan_route(View v) {
			Intent route = new Intent(this, planroute.class);
			startActivity(route);
		}
		
		private int checkGooglePlayServices(){
		    int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		    switch (result) {
		        case ConnectionResult.SUCCESS:
		            Log.d(TAG, "SUCCESS");
		            break;

		        case ConnectionResult.SERVICE_INVALID:
		            Log.d(TAG, "SERVICE_INVALID");
		            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_INVALID, this, 0).show();
		            break;

		        case ConnectionResult.SERVICE_MISSING:
		            Log.d(TAG, "SERVICE_MISSING");
		            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, this, 0).show();
		            break;

		        case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
		            Log.d(TAG, "SERVICE_VERSION_UPDATE_REQUIRED");
		            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0).show();
		            break;

		        case ConnectionResult.SERVICE_DISABLED:
		            Log.d(TAG, "SERVICE_DISABLED");
		            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_DISABLED, this, 0).show();
		            break;
		    }
		    return result;
		}
//	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
