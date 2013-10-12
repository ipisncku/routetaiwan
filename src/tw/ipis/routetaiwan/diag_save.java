package tw.ipis.routetaiwan;

import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class diag_save extends Activity {
	String TAG = "--diag_save--";
	String filename, content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diag_save);
		Bundle Data = this.getIntent().getExtras();
		filename = Data.getString("filename");
		content = Data.getString("content");
		
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
	
	public void save_yes(View v) {
		try {
			//write converted json data to a file named "file.json"
			FileWriter writer = new FileWriter(filename);
			Log.i(TAG, filename);
			writer.write(content);
			writer.close();
			Toast.makeText(this, getResources().getString(R.string.saved) , Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finish();
	}
	
	public void save_no(View v) {
		finish();
	}
}