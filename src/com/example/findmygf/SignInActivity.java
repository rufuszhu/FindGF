package com.example.findmygf;

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.example.findmygf.PlusClientFragment.OnSignedInListener;
import com.example.rest.WebServiceTask;
import com.example.user.CurrentUser;
import com.example.user.CurrentUserManager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Example of signing in a user with Google+, and how to make a call to a Google+ API endpoint.
 */
public class SignInActivity extends FragmentActivity
        implements View.OnClickListener, OnSignedInListener {

    public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;

    private TextView mSignInStatus;
    private PlusClientFragment mSignInFragment;
    private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";

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
        if (currentPerson != null) {
            String greeting = getString(R.string.greeting_status, currentPerson.getDisplayName());            
            mSignInStatus.setText(greeting);
            sendPersonToServer(currentPerson, "Vancouver", "Canada", "sylvialoverufus@gmail.com","rufus.zhu@hotmail.com", "Male");
            Intent i = new Intent(getApplicationContext(),
            		postActivity.class);
			startActivity(i);
        } else {
            resetAccountState();
        }
    }

    private void resetAccountState() {
        mSignInStatus.setText(getString(R.string.signed_out_status));
    }
    
    private void sendPersonToServer(Person person, String city, String country, String partner_email, String email, String gender ){
    	
    	CurrentUser cu = new CurrentUser(person.getName().getGivenName(), partner_email, country, city, gender);
    	CurrentUserManager.setCurrentUser(cu);
    	
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
}
