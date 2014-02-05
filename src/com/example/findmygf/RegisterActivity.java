package com.example.findmygf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;


import com.example.user.CurrentUser;
import com.example.user.CurrentUserManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";
	private final String TAG = "RegisterActivity"; 
	String gender = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	public void clearControls(View vw) {

		EditText edEmail = (EditText) findViewById(R.id.email);
		EditText edPartnerEmail = (EditText) findViewById(R.id.partner_email);

		edEmail.setText("");
		edPartnerEmail.setText("");
		gender = "";
	}

	public void postData(View vw) {

		EditText edEmail = (EditText) findViewById(R.id.email);
		EditText edPartnerEmail = (EditText) findViewById(R.id.partner_email);
		RadioGroup radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);

		String email = edEmail.getText().toString();
		String partner_email = edPartnerEmail.getText().toString();
		
		// get selected radio button from radioGroup
		int selectedId = radioSexGroup.getCheckedRadioButtonId();
		 
		// find the radiobutton by returned id
		RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
		gender = radioSexButton.getText().toString();

		if (email.equals("") || partner_email.equals("") || gender.equals("")) {
			Toast.makeText(this, "Please enter in all required fields.",
					Toast.LENGTH_LONG).show();
			return;
		} 

		WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");

		Intent intent = getIntent();
		String userName = intent.getExtras().getString("user_name");
		String photo = intent.getExtras().getString("photo_uri");
		if(userName == null)
			Toast.makeText(this, "username is null.",
					Toast.LENGTH_LONG).show();
		else {
			CurrentUser cu = new CurrentUser(userName,email,partner_email,gender,photo,0.0,0.0,"");
			CurrentUserManager.setCurrentUser(cu);
			System.out.println("User Name: " + userName);
			wst.addNameValuePair("name", userName);
			wst.addNameValuePair("email", email);
			wst.addNameValuePair("partner_email", partner_email);
			wst.addNameValuePair("gender", gender);
			wst.addNameValuePair("photo", photo);
			// the passed String is the URL we will POST to
			wst.execute(new String[] { SERVICE_URL + "/register" });
		}
	}

	private void getPersonByEmail(String email)
	{
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "Verifying your Partner's info...");
		wst.addNameValuePair("email", email);
		wst.execute(new String[] { SERVICE_URL+"/get_person_by_email/"+email });
	}

	public void handleResponse(String message) {

		//person found
		if(message.contains("register")){
			//this is finding the current user

			Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_LONG).show();
			getPersonByEmail(message.substring(8));
		}
		//this is find the cu's partner
		else if(message.contains("kind"))
		{
			try {

				JSONObject jso = new JSONObject(message);

				String name = jso.getString("name");
				String partner_email = jso.getString("partner_email");
				String gender = jso.getString("gender");
				String email = jso.getString("email");   
				String photo = jso.getString("photo");    
				String update_at = jso.getString("update_at");
				double lat = Double.parseDouble(jso.getString("latitude"));
				double lon = Double.parseDouble(jso.getString("longitude"));
				
				//System.out.println("handle response lat lon" + lat + " " + lon);
				
				CurrentUser p = new CurrentUser(name,email,partner_email,gender,photo,lat,lon,update_at);
				CurrentUserManager.setPartner(p);
				Intent newActivity = new Intent(getApplicationContext(),
						MapActivity.class);
				startActivity(newActivity);
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
		}

		//Current user's partner not found 
		else if(message.equals("") || message==null)
		{
			Intent newActivity = new Intent(getApplicationContext(),
					WaitForPartnerActivity.class);
			startActivity(newActivity);
		}

		else if(message.contains("Error"))
		{
			Toast.makeText(RegisterActivity.this, "getting this error: " + message, Toast.LENGTH_LONG).show();
		}

	}


	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;

		private static final String TAG = "WebServiceTask";

		// connection timeout, in milliseconds (waiting to connect)
		private static final int CONN_TIMEOUT = 30000;

		// socket timeout, in milliseconds (waiting for data)
		private static final int SOCKET_TIMEOUT = 50000;

		private int taskType = GET_TASK;
		private Context mContext = null;
		private String processMessage = "Processing...";
		private CurrentUserManager cum = null;

		private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		private ProgressDialog pDlg = null;

		public WebServiceTask(int taskType, Context mContext, String processMessage) {

			this.taskType = taskType;
			this.mContext = mContext;
			this.processMessage = processMessage;
		}

		public void addNameValuePair(String name, String value) {

			params.add(new BasicNameValuePair(name, value));
		}

		@SuppressWarnings("deprecation")
		private void showProgressDialog() {

			pDlg = new ProgressDialog(mContext);
			pDlg.setMessage(processMessage);
			pDlg.setProgressDrawable(mContext.getWallpaper());
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.show();

		}

		@Override
		protected void onPreExecute() {

			showProgressDialog();

		}

		protected String doInBackground(String... urls) {

			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);
			System.out.println("response:"+ response);

			if (response == null ) {
				return result;
			} else {

				try {

					HttpEntity entity = response.getEntity();
					if(entity==null)
						result="";
					else{
						InputStream stream = entity.getContent();            	
						result = inputStreamToString(stream);
					}

					System.out.println("result: "+ result);

				} catch (IllegalStateException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);

				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}

			}

			return result;
		}

		@Override
		protected void onPostExecute(String response) {

			handleResponse(response);
			pDlg.dismiss();


		}

		// Establish connection and socket (data retrieval) timeouts
		private HttpParams getHttpParams() {

			HttpParams htpp = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
			HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

			return htpp;
		}

		private HttpResponse doResponse(String url) {

			// Use our connection and data timeouts as parameters for our
			// DefaultHttpClient
			HttpClient httpclient = new DefaultHttpClient(getHttpParams());

			HttpResponse response = null;

			try {
				switch (taskType) {

				case POST_TASK:
					HttpPost httppost = new HttpPost(url);
					// Add parameters
					httppost.setEntity(new UrlEncodedFormEntity(params));

					response = httpclient.execute(httppost);
					break;
				case GET_TASK:
					HttpGet httpget = new HttpGet(url);
					response = httpclient.execute(httpget);
					break;
				}
			} catch (Exception e) {

				Log.e(TAG, e.getLocalizedMessage(), e);

			}

			return response;
		}

		private String inputStreamToString(InputStream is) {

			String line = "";
			StringBuilder total = new StringBuilder();

			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));


			try {
				// Read response until the end
				while ((line = rd.readLine()) != null) {
					total.append(line);

				}

			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}

			// Return full string
			return total.toString();
		}


	}
}

