package tw.ipis.routetaiwan;

import java.io.File;
import java.io.FileWriter;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FileIntentService extends IntentService {

	public FileIntentService() {
		super("FileIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle data = intent.getExtras();
		String content = data.getString("content");
		String filename = data.getString("filename");

		File fp = new File(filename);
		try {
			FileWriter writer;
			writer = new FileWriter(fp.getAbsolutePath());
			writer.write(content);
			Log.i("SHIT", String.format("write %s to %s", content, fp.getName()));
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}