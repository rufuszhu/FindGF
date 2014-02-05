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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.example.user.CurrentUser;
import com.example.user.CurrentUserManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;


import android.widget.TextView;
import android.widget.Toast;

/**
 * Example of signing in a user with Google+, and how to make a call to a Google+ API endpoint.
 */
public class SignInActivity extends FragmentActivity
implements View.OnClickListener, PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
PlusClient.OnAccessRevokedListener {

    private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	//private TextView mSignInStatus;
    private TextView mSignInStatus;
	private PlusClient mPlusClient;
    private SignInButton mSignInButton;
    private View mSignOutButton;
    private View mRevokeAccessButton;
    private ConnectionResult mConnectionResult;
	private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";
	private String userName = "Sylvia";
	private String photo = "https://lh3.googleusercontent.com/-TTwPfV0-ukc/UrJ8mEXkeNI/AAAAAAAAACA/bVVzrV649Y4/w676-h675-no/%25E4%25BC%258F%25E5%2587%25BB+060.JPG";
	private final String TAG = "SingInActivity"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mPlusClient = new PlusClient.Builder(this, this, this)
        .setActions(MomentUtil.ACTIONS)
        .build();
		
		mSignInStatus = (TextView) findViewById(R.id.sign_in_status);
		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mSignOutButton = findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(this);
        mRevokeAccessButton = findViewById(R.id.revoke_access_button);
        mRevokeAccessButton.setOnClickListener(this);

		findViewById(R.id.sign_in_button).setOnClickListener(this);
	}
	   @Override
	    public void onStart() {
	        super.onStart();
	        mPlusClient.connect();
	    }

	    @Override
	    public void onStop() {
	        mPlusClient.disconnect();
	        super.onStop();
	    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
        case R.id.sign_in_button:
//            int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//            if (available != ConnectionResult.SUCCESS) {
//                showDialog(DIALOG_GET_GOOGLE_PLAY_SERVICES);
//                return;
//            }
//
//            try {
//                mSignInStatus.setText(getString(R.string.signing_in_status));
//                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
//            } catch (IntentSender.SendIntentException e) {
//                // Fetch a new result to start.
//                mPlusClient.connect();
//            }
        	getPersonByName(userName);
            break;
            case R.id.sign_out_button:
                if (mPlusClient.isConnected()) {
                    mPlusClient.clearDefaultAccount();
                    mPlusClient.disconnect();
                    mPlusClient.connect();
                }
                break;
            case R.id.revoke_access_button:
                if (mPlusClient.isConnected()) {
                    mPlusClient.revokeAccessAndDisconnect(this);
                    updateButtons(false /* isSignedIn */);
                }
                break;
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
            return super.onCreateDialog(id);
        }

        int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            return null;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
            return GooglePlayServicesUtil.getErrorDialog(
                    available, this, REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
        }
        return new AlertDialog.Builder(this)
                .setMessage(R.string.plus_generic_error)
                .setCancelable(true)
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SIGN_IN
                || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
            if (resultCode == RESULT_OK && !mPlusClient.isConnected()
                    && !mPlusClient.isConnecting()) {
                // This time, connect should succeed.
                mPlusClient.connect();
            }
        }
    }

    @Override
    public void onAccessRevoked(ConnectionResult status) {
        if (status.isSuccess()) {
            mSignInStatus.setText(R.string.revoke_access_status);
        } else {
            mSignInStatus.setText(R.string.revoke_access_error_status);
            mPlusClient.disconnect();
        }
        mPlusClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        String currentPersonName = mPlusClient.getCurrentPerson() != null
                ? mPlusClient.getCurrentPerson().getDisplayName()
                : getString(R.string.unknown_person);
        
        updateButtons(true /* isSignedIn */);
        
        userName = currentPersonName;
		photo = mPlusClient.getCurrentPerson().getImage().getUrl();
		if (!userName.equals("")) {
			mSignInStatus.setText(getString(R.string.signed_in_status, currentPersonName));
			//mSignInStatus.setText(greeting);

			getPersonByName(userName);
			//getPersonByName("abc");
			//sendPersonToServer(currentPerson, "Calgary", "Canada", "sylvialoverufus@gmail.com","rufus.zhu@hotmail.com", "Male");
			//
		}
    }

    @Override
    public void onDisconnected() {
        mSignInStatus.setText(R.string.loading_status);
        mPlusClient.connect();
        updateButtons(false /* isSignedIn */);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mConnectionResult = result;
        updateButtons(false /* isSignedIn */);
    }

    private void updateButtons(boolean isSignedIn) {
        if (isSignedIn) {
            mSignInButton.setVisibility(View.INVISIBLE);
            mSignOutButton.setEnabled(true);
            mRevokeAccessButton.setEnabled(true);
        } else {
            if (mConnectionResult == null) {
                // Disable the sign-in button until onConnectionFailed is called with result.
                mSignInButton.setVisibility(View.INVISIBLE);
                mSignInStatus.setText(getString(R.string.loading_status));
            } else {
                // Enable the sign-in button since a connection result is available.
                mSignInButton.setVisibility(View.VISIBLE);
                mSignInStatus.setText(getString(R.string.signed_out_status));
            }

            mSignOutButton.setEnabled(false);
            mRevokeAccessButton.setEnabled(false);
        }
    }


	private void resetAccountState() {
		//mSignInStatus.setText(getString(R.string.signed_out_status));
		CurrentUserManager.clearPartner();
		CurrentUserManager.clearCurrentUser();

	}

	private void getPersonByName(String name)
	{
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "Verifying your info...");
		wst.addNameValuePair("name", name);
		wst.execute(new String[] { SERVICE_URL+"/get_person_by_name/"+name });
	}

	private void getPersonByEmail(String email)
	{
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "Verifying your Partner's info...");
		wst.addNameValuePair("email", email);
		wst.execute(new String[] { SERVICE_URL+"/get_person_by_email/"+email });
	}

	//    private void checkPartner(String partner)
	//    {
	//    	WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Verifying your info...");
	//    	wst.addNameValuePair("name", person.getName().getGivenName());
	//    	wst.execute(new String[] { SERVICE_URL+"/check_exist" });
	//    }



	public void handleResponse(String message) {
		//mSignInStatus.setText(message);
		//person found
		if(message.contains("name")){

			//store current user
			if(CurrentUserManager.getCurrentUser() == null)
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
					CurrentUserManager.setCurrentUser(p);
					//mSignInStatus.setText("current user: " + CurrentUserManager.getCurrentUser().getName());
					Toast.makeText(SignInActivity.this, "Already Registered", Toast.LENGTH_LONG).show();
					getPersonByEmail(partner_email);
				} catch (Exception e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			}
			//store partner
			else
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
//					Intent newActivity = new Intent(getApplicationContext(),
//							MapActivity.class);
//					startActivity(newActivity);
					Intent newActivity = new Intent(getApplicationContext(),
							MapActivity.class);
					startActivity(newActivity);					
				} catch (Exception e) {
					Log.e(TAG, e.getLocalizedMessage(), e);
				}
			}
		}
		//Current user not Found, register
		else if(CurrentUserManager.getCurrentUser() == null){
			//mSignInStatus.setText("Please Register");
			Toast.makeText(SignInActivity.this, "Please Register", Toast.LENGTH_LONG).show();
			Intent newActivity = new Intent(getApplicationContext(),
					RegisterActivity.class);
			newActivity.putExtra("user_name", userName);
			newActivity.putExtra("photo_uri", photo);
			startActivity(newActivity);
		}
		//Current user's partner not found 
		else
		{
			//mSignInStatus.setText("current user: " + CurrentUserManager.getCurrentUser().getName());
			Toast.makeText(SignInActivity.this, "Your partner with the email: \"" +
					CurrentUserManager.getCurrentUser().getPartner_email() +"\" has not register yet, please try to sign in again after your partner is registered", Toast.LENGTH_LONG).show();
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
