package tw.ipis.routetaiwan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class planroute extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String TAG = "~~planroute~~";
		
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.planroute);
	}
}