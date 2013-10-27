package tw.ipis.routetaiwan;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class sms_send extends Activity {
	private static final String TAG = "--SMS--";
	// List view
	private ListView lv;
	ArrayAdapter<String> adapter;
	EditText inputSearch, pos_title;
	ArrayList<HashMap<String, String>> productList;
	String title, latlng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_search_contact);
		
		Bundle Data = this.getIntent().getExtras();
		title = Data.getString("title");
		latlng = Data.getString("latlng");

		String contact[] = get_all_contact();

		lv = (ListView) findViewById(R.id.all_contacts);
		inputSearch = (EditText) findViewById(R.id.inputSearch);
		pos_title = (EditText) findViewById(R.id.point_name);
		pos_title.setText(title);

		// Adding items to listview
		adapter = new ArrayAdapter<String>(this, R.layout.contact_list, R.id.contact_name, contact);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String selectedFromList =(String) (lv.getItemAtPosition(position));
				inputSearch.setText(selectedFromList.replaceAll("[^+0-9]", ""));

			}
		});

		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				sms_send.this.adapter.getFilter().filter(cs);   
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

	}
	
	public void start_sending(View v) {
		String phoneNo = inputSearch.getText().toString();
		if(!phoneNo.matches("[+0-9]+")) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.info_illegal_num),
					Toast.LENGTH_LONG).show();
			return;
		}
		String title_name = pos_title.getText().toString();
		
		String sms = String.format("rtw,%s,%s", title_name, latlng);
		
		Log.i(TAG, "sms="+sms);
		
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNo, null, sms, null, null);
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.sms_sent),
				Toast.LENGTH_LONG).show();
		
		finish();
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
	
	public void cancel(View v) {
		finish();
	}

	public String[] get_all_contact() {
		ArrayList<String> contact = new ArrayList<String>();
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(
						cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
									new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//						Toast.makeText(this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
						
						contact.add(String.format("%s %s", name, phoneNo));
					}
					pCur.close();
				}
			}
		}
		if(contact.size() > 0) {
			String list[] = new String[contact.size()];
			for(int i=0; i<contact.size(); i++) {
				list[i] = contact.get(i);
			}
			return list;
		}
		
		return new String[0];
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}