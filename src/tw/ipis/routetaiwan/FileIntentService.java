package tw.ipis.routetaiwan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

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
			String buf = getStringFromFile(fp);
			buf = buf + "," + content;
			FileWriter writer;
			writer = new FileWriter(fp.getAbsolutePath());
			writer.write(buf);
			writer.close();
		} catch (Exception e) {
			// TODO 自動產生的 catch 區塊
			e.printStackTrace();
		}
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	public static String getStringFromFile (File fl) throws Exception {
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		//Make sure you close all streams.
		fin.close();        
		return ret;
	}
}