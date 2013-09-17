package tw.ipis.routetaiwan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class planroute extends Activity {
	
	ProgressBar planning;
	String TAG = "~~planroute~~";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		
		setContentView(R.layout.planroute);
		
		planning = (ProgressBar)findViewById(R.id.planning);
		planning.setVisibility(ProgressBar.GONE);
	}
	
	public void start_planing(View v) {
		Log.e(TAG, "start_planing");
		planning.setVisibility(ProgressBar.VISIBLE);
	}
}