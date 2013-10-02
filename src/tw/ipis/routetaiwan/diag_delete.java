package tw.ipis.routetaiwan;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


public class diag_delete extends Activity {
	String TAG = "--diag_delete--";
	String filename;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diag_delete);
		Bundle Data = this.getIntent().getExtras();
		filename = Data.getString("filename");
	}
	
	public void del_yes(View v) {
		File file = new File(filename);
		file.delete();
		finish();
	}
	
	public void del_no(View v) {
		finish();
	}
}