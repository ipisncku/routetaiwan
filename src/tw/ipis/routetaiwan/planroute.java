package tw.ipis.routetaiwan;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

public class planroute extends Activity {
	
	ProgressBar planning;
	String TAG = "~~planroute~~";
	private EditText from;
	private EditText to;
	
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
		
		foreground_cosmetic();
	}
	
	private void foreground_cosmetic() {
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(from.getWindowToken(), 0);
		
		planning.setVisibility(ProgressBar.VISIBLE);
	}
}