package tw.ipis.routetaiwan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "--SMSRECV--";
	final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan/";

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//---get the SMS message passed in---
		Bundle bundle = intent.getExtras();        
		SmsMessage[] msgs = null;
		String str = "";            
		if (bundle != null)
		{
			//---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];            
			for (int i=0; i<msgs.length; i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]); 

				String phone_num = msgs[i].getOriginatingAddress();    
				String sms_msg = msgs[i].getMessageBody().toString();

				str += "SMS from " + phone_num;                     
				str += " :";
				str += sms_msg;
				str += "\n";

				Log.i(TAG, "sms="+str);

				if(sms_msg.regionMatches(0, "rtw,", 0, 4)) {
					String list[] = sms_msg.split(",");

					if(list.length > 3) {
						/* 格式範例: phone,09xxxxxxxx,地名,23.xxxxxx,125,xxxxxx,,台北市中山區(option) */
						String fav_point = String.format("phone,%s,%s,%s,%s", phone_num, 
								list[1].isEmpty() ? context.getResources().getString(R.string.fav_points) : list[1], 
										list[2], list[3]);
						String filename = projectdir + getMD5EncryptedString(fav_point) + ".point";

						File file = new File(filename);
						if (!file.exists()) {
							Log.i(TAG, String.format("file:%s content:%s", filename, fav_point));

							FileWriter writer;
							try {
								writer = new FileWriter(filename);
								writer.write(fav_point);
								writer.close();
								showNotification(context);
							} catch (IOException e) {
								e.printStackTrace();
								Toast.makeText(context, context.getResources().getString(R.string.info_internal_error) , Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			}
		}                         
	}

	private void showNotification(Context context) {
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.routetw_logo_v2)
		.setContentTitle(context.getResources().getString(R.string.app_name))
		.setContentText(context.getResources().getString(R.string.sms_received));
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setDefaults(Notification.DEFAULT_ALL);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());
	}  

	public static String getMD5EncryptedString(String encTarget){
		MessageDigest mdEnc = null;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception while encrypting to md5");
			e.printStackTrace();
		} // Encryption algorithm
		mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
		String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
		return md5;
	}
}

