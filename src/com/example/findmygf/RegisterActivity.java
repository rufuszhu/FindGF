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
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";
	private final String TAG = "RegisterActivity"; 
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
		EditText edCountry = (EditText) findViewById(R.id.country);
		EditText edCity = (EditText) findViewById(R.id.city);

		edEmail.setText("");
		edPartnerEmail.setText("");
		edCountry.setText("");
		edCity.setText("");
	}

	public void postData(View vw) {

		EditText edEmail = (EditText) findViewById(R.id.email);
		EditText edPartnerEmail = (EditText) findViewById(R.id.partner_email);
		EditText edCountry = (EditText) findViewById(R.id.country);
		EditText edCity = (EditText) findViewById(R.id.city);

		String email = edEmail.getText().toString();
		String partner_email = edPartnerEmail.getText().toString();
		String country = edCountry.getText().toString();
		String city = edCity.getText().toString();

		if (email.equals("") || partner_email.equals("") || country.equals("") ||city.equals("")) {
			Toast.makeText(this, "Please enter in all required fields.",
					Toast.LENGTH_LONG).show();
			return;
		} 

		WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");
		
		Intent intent = getIntent();
		String userName = intent.getExtras().getString("user_name");
		if(userName == null)
			Toast.makeText(this, "username is null.",
					Toast.LENGTH_LONG).show();
		else {
		System.out.println("User Name: " + userName);
		wst.addNameValuePair("name", userName);
		wst.addNameValuePair("email", email);
		wst.addNameValuePair("partner_email", partner_email);
		wst.addNameValuePair("country", country);
		wst.addNameValuePair("city", city);
		wst.addNameValuePair("gender", "male");
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
		if(message.contains("partner_email")){
			if(CurrentUserManager.getCurrentUser() == null)
			{

				try {
					//store current user
					JSONObject jso = new JSONObject(message);

					String name = jso.getString("name");
					String city = jso.getString("city");
					String country = jso.getString("country");
					String partner_email = jso.getString("partner_email");
					String gender = jso.getString("gender");
					String email = jso.getString("email");    

					CurrentUser cu = new CurrentUser(name,email,partner_email,country,city,gender);
					CurrentUserManager.setCurrentUser(cu);

					Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_LONG).show();
					getPersonByEmail(partner_email);
				} catch (Exception e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}


			}
			else
			{
				try {

					JSONObject jso = new JSONObject(message);

					String name = jso.getString("name");
					String city = jso.getString("city");
					String country = jso.getString("country");
					String partner_email = jso.getString("partner_email");
					String gender = jso.getString("gender");
					String email = jso.getString("email");    

					CurrentUser cu = new CurrentUser(name,email,partner_email,country,city,gender);
					CurrentUserManager.getCurrentUser().setPartner(cu);
					Intent newActivity = new Intent(getApplicationContext(),
							PostActivity.class);
					startActivity(newActivity);
				} catch (Exception e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			}
		}
		//Current user's partner not found 
		else
		{


			Intent newActivity = new Intent(getApplicationContext(),
					WaitForPartnerActivity.class);
			startActivity(newActivity);
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

