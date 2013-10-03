package tw.ipis.routetaiwan;

import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class pop_transit extends Activity {
	private static String TAG = "--pop_transit--";
	private ProgressBar process;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.pop_transit);
		
		Bundle Data = this.getIntent().getExtras();
		String type = Data.getString("type");
		String line = null, agency = null, car_class = null;
		long time = 0;
		if(type != null && !type.contentEquals("null")) {
			line = Data.getString("line");
			if(type.contentEquals("bus"))
				agency = Data.getString("agency");
			else if(type.contentEquals("tra")) {
				car_class = Data.getString("class");
				time = Data.getLong("time");
			}
		}
		else {
			Log.d(TAG, "Unknown type, finished...");
			finish();
		}
		
		process = new ProgressBar(this, null, android.R.attr.progressBarStyleInverse);
		process.setIndeterminate(true);
		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
		RelativeLayout.LayoutParams process_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		process_param.addRule(RelativeLayout.CENTER_IN_PARENT);
		process.setLayoutParams(process_param);
		
		/* 台鐵 */
		/* If type = "tra", then open webview for ex: http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate=2013/10/03&traincode=117 */
		if(type.contentEquals("tra")) {
			Date date = new Date(time);
			Format format = new SimpleDateFormat("yyyy/MM/dd");
			String str_date = format.format(date).toString();
			String tra_real_time_url = "http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate={0}&traincode={1}";
			String url = MessageFormat.format(tra_real_time_url, str_date, line);
			
			Log.i(TAG, url);
			create_webview_by_url(url);
			
			/* 設定activity title, ex: 自強 123 */
			this.setTitle(car_class + " " + line);
			
			/* 資料由台鐵提供 */
			TextView tv = new TextView(this);
			tv.setText(getResources().getString(R.string.provide_by_tra));
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(16);
			tv.setGravity(Gravity.RIGHT);
			tv.setHorizontallyScrolling(false);
			
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			tv.setLayoutParams(param);
			
			rl.addView(tv);
		}
		/* 高鐵 */
		else if(type.contentEquals("hsr")) {
			Log.i(TAG, "hsr");
			
			rl.addView(process);
			
			/* 設定activity title, ex: 自強 123 */
			this.setTitle(getResources().getString(R.string.hsr_status));
			
			DownloadWebPageTask task = new DownloadWebPageTask();
			task.execute(new String[] {"http://www.thsrc.com.tw/tw/Operation"});
		}
		/* 公車 客運 */
		/* 其他狀況: 施工中... */
		else {
			TextView tv = new TextView(this);
			tv.setText(getResources().getString(R.string.realtime_under_construction));
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(16);
			tv.setGravity(Gravity.CENTER);
			tv.setHorizontallyScrolling(false);
			
			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			param.addRule(RelativeLayout.CENTER_IN_PARENT);
			tv.setLayoutParams(param);
			
			rl.addView(tv);
		}
	}
	
	public void thsrc_current_status(String result) {
		if(result != null) {
			ImageView iv = new ImageView(this);
			iv.setId(0x12345001);
			iv.setImageBitmap(null);
			
			iv.setAdjustViewBounds(true);
			RelativeLayout.LayoutParams ivparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			ivparam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			iv.setLayoutParams(ivparam);
			
			TextView tv = new TextView(this);
			tv.setText(getResources().getString(R.string.hsr_normal));
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(20);
			tv.setGravity(Gravity.CENTER);
			tv.setHorizontallyScrolling(false);
			RelativeLayout.LayoutParams tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			tvparam.addRule(RelativeLayout.RIGHT_OF, iv.getId());
			tvparam.addRule(RelativeLayout.CENTER_VERTICAL);
			tv.setLayoutParams(tvparam);
			if(result.contains("show_ok")) {
				/* 正常運行 */
				iv.setImageResource(R.drawable.allok);
				tv.setText(getResources().getString(R.string.hsr_normal));
			}
			else {
				/* 未知狀況 */
				iv.setImageResource(R.drawable.warning);
				tv.setText(getResources().getString(R.string.hsr_warning));
			}
			RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
			rl.removeView(process);
			rl.addView(iv);
			rl.addView(tv);
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public boolean create_webview_by_url(String url) {
		WebView wv = new WebView(this);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setUseWideViewPort(false);
		
		wv.loadUrl(url);
		
		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
		RelativeLayout.LayoutParams webview_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		webview_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		wv.setLayoutParams(webview_param);
		
		rl.addView(wv);
		
		return true;
	}
	
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				HttpGet httpGet = new HttpGet(url);
				HttpClient client = new DefaultHttpClient();
				try {
					HttpResponse result = client.execute(httpGet);
					StatusLine statusLine = result.getStatusLine();
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = result.getEntity();

						response = EntityUtils.toString(entity);
					} else {
						Log.e(TAG, "Failed to download file");
					}
					return response;
				}
				catch (Exception e) {
					e.printStackTrace();
					return "";
				}
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			thsrc_current_status(result);
		}
	}
}