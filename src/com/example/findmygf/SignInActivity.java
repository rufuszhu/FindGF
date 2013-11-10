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

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.example.findmygf.PlusClientFragment.OnSignedInListener;
import com.example.user.CurrentUser;
import com.example.user.CurrentUserManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
        implements View.OnClickListener, OnSignedInListener {

    public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;

    private TextView mSignInStatus;
    private PlusClientFragment mSignInFragment;
    private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";
    private String userName;
    private final String TAG = "SingInActivity"; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInFragment =
                PlusClientFragment.getPlusClientFragment(this, MomentUtil.VISIBLE_ACTIVITIES);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.revoke_access_button).setOnClickListener(this);
        mSignInStatus = (TextView) findViewById(R.id.sign_in_status);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.sign_out_button:
                resetAccountState();
                mSignInFragment.signOut();
                break;
            case R.id.sign_in_button:
                mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
                
                break;
            case R.id.revoke_access_button:
                resetAccountState();
                mSignInFragment.revokeAccessAndDisconnect();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        mSignInFragment.handleOnActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public void onSignedIn(PlusClient plusClient) {
        mSignInStatus.setText(getString(R.string.signed_in_status));

        // We can now obtain the signed-in user's profile information.
        Person currentPerson = plusClient.getCurrentPerson();
        userName = currentPerson.getName().getGivenName();
        if (currentPerson != null) {
            String greeting = getString(R.string.greeting_status, currentPerson.getDisplayName());            
            mSignInStatus.setText(greeting);
     
            getPersonByName(currentPerson.getName().getGivenName());
            //getPersonByName("abc");
            //sendPersonToServer(currentPerson, "Calgary", "Canada", "sylvialoverufus@gmail.com","rufus.zhu@hotmail.com", "Male");
            //
        } else {
            resetAccountState();
        }
    }

    private void resetAccountState() {
        mSignInStatus.setText(getString(R.string.signed_out_status));
        
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
    
    private void sendPersonToServer(Person person, String city, String country, String partner_email, String email, String gender ){
    	
    	//CurrentUser cu = new CurrentUser(person.getName().getGivenName(), partner_email, country, city, gender);
    	//CurrentUserManager.setCurrentUser(cu);
    	
    	WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Verifying your info...");
    	
        wst.addNameValuePair("name", person.getName().getGivenName());
        wst.addNameValuePair("city", city);
        wst.addNameValuePair("country", country);
        wst.addNameValuePair("partner_email", partner_email);
        wst.addNameValuePair("gender", gender);
        wst.addNameValuePair("email", email);
 
        // the passed String is the URL we will POST to
        wst.execute(new String[] { SERVICE_URL+"/register" });
    }
    
	public void handleResponse(String message) {
		mSignInStatus.setText(message);
		//person found
		if(message.contains("name")){
			
       	 	//store current user
       	 	if(CurrentUserManager.getCurrentUser() == null)
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
		             CurrentUserManager.setCurrentUser(cu);
		             mSignInStatus.setText("current user: " + CurrentUserManager.getCurrentUser().getName());
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
		//Current user not Found
		else if(CurrentUserManager.getCurrentUser() == null){
			mSignInStatus.setText("Please Register");
       	 	Toast.makeText(SignInActivity.this, "Please Register", Toast.LENGTH_LONG).show();
       	 	Intent newActivity = new Intent(getApplicationContext(),
					RegisterActivity.class);
       	 	newActivity.putExtra("user_name", userName);
       	 	startActivity(newActivity);
		}
		//Current user's partner not found 
		else
   	 	{
			 mSignInStatus.setText("current user: " + CurrentUserManager.getCurrentUser().getName());
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
