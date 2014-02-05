/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.findmygf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.location.LocationUtils;
import com.example.location.MyLocationService;

import com.example.user.CurrentUser;
import com.example.user.CurrentUserManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This shows how to change the camera position for the map.
 */
public class MapActivity extends FragmentActivity {

	/**
	 * The amount by which to scroll the camera. Note that this amount is in raw pixels, not dp
	 * (density-independent pixels).
	 */
	private String TAG = "MapActivity";

	private static final String SERVICE_URL = "http://my-gf-server.appspot.com/resources/person";

	private AlarmManagerBroadcastReceiver alarm;
	private MyLocationService locService;

	private Boolean toogle;

	private GoogleMap mMap;
	
	private String p_condition;
	
	final String yahooPlaceApisBase = "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=";
	final String yahooapisFormat = "&format=xml";
	private String yahooPlaceAPIsQuery;
	
	private String p_country="";
	private String p_city="";
	private String my_country="";
	private String my_city="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_demo);
		    
		setUpMapIfNeeded();
		UiSettings uiSettings = mMap.getUiSettings();
		uiSettings.setMyLocationButtonEnabled(true);
		toogle=false;
		//get partner info
		updatePartner();
		
		Button button = (Button) findViewById(R.id.find_partner_btn);
		if(CurrentUserManager.getPartner().getGender().equals("Male")){
			button.setText("Find my Boyfriend");
		}
		else{
			button.setText("Find my Girlfriend");
		}
			

		//start myLocationService
		locService = new MyLocationService(MapActivity.this);
		alarm = new AlarmManagerBroadcastReceiver();

		TextView my_name = (TextView) findViewById(R.id.my_name);
		TextView p_name = (TextView) findViewById(R.id.p_name);

		// getting photo
		new GetPartnerPhotoTask().execute(new String[] { CurrentUserManager.getPartner().getPhoto() });


		new GetMyPhotoTask().execute(new String[] { CurrentUserManager.getCurrentUser().getPhoto() });


		my_name.setText(CurrentUserManager.getCurrentUser().getName());
		p_name.setText(CurrentUserManager.getPartner().getName());
		
		getMyAddress(locService.getLocation());


		Location location = new Location("partner location");
		location.setLatitude(CurrentUserManager.getPartner().getLatitude());
		location.setLongitude(CurrentUserManager.getPartner().getLongitude());
		getPartnerAddress(location);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updatePartner();
		
		Location location = new Location("partner location");
		location.setLatitude(CurrentUserManager.getPartner().getLatitude());
		location.setLongitude(CurrentUserManager.getPartner().getLongitude());
		getPartnerAddress(location);
		
		getMyAddress(locService.getLocation());
		
		
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if (item.getItemId() == R.id.start_alarm) {
			startUpdates();
		}
		if (item.getItemId() == R.id.stop_alarm) {
			stopUpdates();
		}
		return super.onOptionsItemSelected(item);
	}

	public void startUpdates() {

		Context context = this.getApplicationContext();
		if(alarm != null){
			Toast.makeText(context, "Starting alarm", Toast.LENGTH_SHORT).show();
			alarm.SetAlarm(context);
		}else{
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}

	}

	public void stopUpdates() {

		Context context = this.getApplicationContext();
		if(alarm != null){
			Toast.makeText(context, "Stoping alarm", Toast.LENGTH_SHORT).show();
			alarm.CancelAlarm(context);

		}else{
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}

	}

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
		}
	}

	/**
	 * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
	 * all entry points that call methods on the Google Maps API.
	 */
	private boolean checkReady() {
		if (mMap == null) {
			Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * Called when the Go To My Location button is clicked.
	 */
	public void onGoToMyLocation(View view) {
		if (!checkReady()) {
			return;
		}


		final Location myLocation = locService.getLocation();
		LatLng myPos = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
		CameraPosition myPosition =
				new CameraPosition.Builder().target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
				.zoom(15.5f)
				.bearing(0)
				.tilt(25)
				.build();

		changeCamera(CameraUpdateFactory.newCameraPosition(myPosition));
		mMap.addMarker(new MarkerOptions()
		.position(myPos)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));
	}

	/**
	 * Called when the Animate To Partner button is clicked.
	 */
	public void onGoToPartnerLocation(View view) {
		toogle = true;
		updatePartner();
	}

	private void changeCamera(CameraUpdate update) {
		changeCamera(update, null);
	}

	/**
	 * Change the camera position by moving or animating the camera depending on the state of the
	 * animate toggle button.
	 */
	private void changeCamera(CameraUpdate update, CancelableCallback callback) {

		mMap.animateCamera(update, callback);

	}

	private void updatePartner(){
		WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "getting your Partner's location...");
		wst.execute(new String[] { SERVICE_URL+"/get_person_by_email/"+CurrentUserManager.getCurrentUser().getPartner_email() });
	}

	private void handleResponse(String message){
		if(message.contains("name")){


			try {

				JSONObject jso = new JSONObject(message);

				String name = jso.getString("name");
				String city = jso.getString("city");
				String country = jso.getString("country");
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
				
				if(toogle == true)
				{
					toogle= false;
					Location location = new Location("partner location");
					location.setLatitude(CurrentUserManager.getPartner().getLatitude());
					location.setLongitude(CurrentUserManager.getPartner().getLongitude());
					getPartnerAddress(location);

					LatLng partnerPos = new LatLng(lat,lon);
					
					

					mMap.addMarker(new MarkerOptions()
					.position(partnerPos)
					.title(CurrentUserManager.getPartner().getName())
					.snippet("Updated at: " + update_at)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.dot)));

					if (!checkReady()) {
						return;
					}

					if(CurrentUserManager.getPartner()==null || CurrentUserManager.getPartner().getLatitude() == null || CurrentUserManager.getPartner().getLongitude() == null){
						Toast.makeText(MapActivity.this, "Failed to get your partner's information, please try again", Toast.LENGTH_LONG).show();
					}
					final Location myLocation = locService.getLocation();

					System.out.println(lat + "and, " + lon);

					CameraPosition partnerPosition =
							new CameraPosition.Builder().target(new LatLng(CurrentUserManager.getPartner().getLatitude(), CurrentUserManager.getPartner().getLongitude()))
							.zoom(15.5f)
							.bearing(0)
							.tilt(25)
							.build();

					changeCamera(CameraUpdateFactory.newCameraPosition(partnerPosition), new CancelableCallback() {
						@Override
						public void onFinish() {
							String message = "";
							if(CurrentUserManager.getPartner().getGender() == "Male")
							{
								message += "He is ";
							}
							else
							{
								message += "She is ";
							}

							float distance = myLocation.distanceTo(CurrentUserManager.getPartner().getLocation());

							if(distance < 1000)
								message += String.valueOf(distance) + "M from you.";
							else
								message += String.valueOf(distance/1000) + "KM from you.";	


							Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT)
							.show();
						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub

						}
					});
				}
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
		}

		
	}

	public void getMyAddress(Location currentLocation) {

		// In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
			// No geocoder is present. Issue an error message
			Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
			return;
		}


		// Start the background task
		(new MapActivity.GetMyAddressTask(this)).execute(currentLocation);


	}

	public void getPartnerAddress(Location currentLocation) {

		// In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
			// No geocoder is present. Issue an error message
			Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
			return;
		}


		// Start the background task
		(new MapActivity.GetPartnerAddressTask(this)).execute(currentLocation);


	}

	/**
	 * An AsyncTask that calls getFromLocation() in the background.
	 * The class uses the following generic types:
	 * Location - A {@link android.location.Location} object containing the current location,
	 *            passed as the input parameter to doInBackground()
	 * Void     - indicates that progress units are not used by this subclass
	 * String   - An address passed to onPostExecute()
	 */
	protected class GetMyAddressTask extends AsyncTask<Location, Void, String> {

		// Store the context passed to the AsyncTask when the system instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetMyAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		/**
		 * Get a geocoding service instance, pass latitude and longitude to it, format the returned
		 * address, and return the address to the UI thread.
		 */
		@Override
		protected String doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized addresses. This example uses
			 * android.location.Geocoder, but other geocoders that conform to address standards
			 * can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

			// Get the current location from the input parameter list
			Location location = params[0];

			// Create a list to contain the result address
			List <Address> addresses = null;

			// Try to get an address for the current location. Catch IO or network problems.
			try {

				/*
				 * Call the synchronous getFromLocation() method with the latitude and
				 * longitude of the current location. Return at most 1 address.
				 */
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1
						);

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();

				// Return an error message
				return (getString(R.string.IO_Exception_getFromLocation));

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(
						R.string.illegal_argument_exception,
						location.getLatitude(),
						location.getLongitude()
						);
				// Log the error and print the stack trace
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();

				//
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {

				// Get the first address
				Address address = addresses.get(0);

				// Format the first line of address
				String addressText = getString(R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) : "",

								// Locality is usually a city
								address.getLocality(),

								// The country of the address
								address.getCountryName()
						);
				
				my_city = address.getLocality();
				my_country = address.getCountryName();

				// Return the text
				return addressText;

				// If there aren't any addresses, post a message
			} else {
				return getString(R.string.no_address_found);
			}
		}

		/**
		 * A method that's called once doInBackground() completes. Set the text of the
		 * UI element that displays the address. This method runs on the UI thread.
		 */
		@Override
		protected void onPostExecute(String result) {
			TextView my_address = (TextView) findViewById(R.id.my_address);
			my_address.setText("Address: "+result);
			new MyQueryYahooPlaceForMeTask().execute();
		}
	}

	/**
	 * An AsyncTask that calls getFromLocation() in the background.
	 * The class uses the following generic types:
	 * Location - A {@link android.location.Location} object containing the current location,
	 *            passed as the input parameter to doInBackground()
	 * Void     - indicates that progress units are not used by this subclass
	 * String   - An address passed to onPostExecute()
	 */
	protected class GetPartnerAddressTask extends AsyncTask<Location, Void, String> {

		// Store the context passed to the AsyncTask when the system instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetPartnerAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		/**
		 * Get a geocoding service instance, pass latitude and longitude to it, format the returned
		 * address, and return the address to the UI thread.
		 */
		@Override
		protected String doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized addresses. This example uses
			 * android.location.Geocoder, but other geocoders that conform to address standards
			 * can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

			// Get the current location from the input parameter list
			Location location = params[0];

			// Create a list to contain the result address
			List <Address> addresses = null;

			// Try to get an address for the current location. Catch IO or network problems.
			try {

				/*
				 * Call the synchronous getFromLocation() method with the latitude and
				 * longitude of the current location. Return at most 1 address.
				 */
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1
						);

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();

				// Return an error message
				return (getString(R.string.IO_Exception_getFromLocation));

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(
						R.string.illegal_argument_exception,
						location.getLatitude(),
						location.getLongitude()
						);
				// Log the error and print the stack trace
				Log.e(LocationUtils.APPTAG, errorString);
				exception2.printStackTrace();

				//
				return errorString;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {

				// Get the first address
				Address address = addresses.get(0);

				// Format the first line of address
				String addressText = getString(R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ?
								address.getAddressLine(0) : "",

								// Locality is usually a city
								address.getLocality(),

								// The country of the address
								address.getCountryName()
						);
				
				p_city = address.getLocality();
				p_country = address.getCountryName();

				// Return the text
				return addressText;

				// If there aren't any addresses, post a message
			} else {
				return getString(R.string.no_address_found);
			}
		}

		/**
		 * A method that's called once doInBackground() completes. Set the text of the
		 * UI element that displays the address. This method runs on the UI thread.
		 */
		@Override
		protected void onPostExecute(String result) {
			TextView p_address = (TextView) findViewById(R.id.p_address);
			p_address.setText("Address: "+result);
			new MyQueryYahooPlaceForPartnerTask().execute();
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


	private class GetMyPhotoTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap map = null;
			for (String url : urls) {
				map = downloadImage(url);
			}
			return map;
		}

		// Sets the Bitmap returned by doInBackground
		@Override
		protected void onPostExecute(Bitmap result) {
			ImageView my_image = (ImageView) findViewById(R.id.my_photo);
			my_image.setImageBitmap(result);
		}

		// Creates Bitmap from InputStream and returns it
		private Bitmap downloadImage(String url) {
			Bitmap bitmap = null;
			InputStream stream = null;
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;

			try {
				stream = getHttpConnection(url);
				bitmap = BitmapFactory.
						decodeStream(stream, null, bmOptions);
				stream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return bitmap;
		}

		// Makes HttpURLConnection and returns InputStream
		private InputStream getHttpConnection(String urlString)
				throws IOException {
			InputStream stream = null;
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					stream = httpConnection.getInputStream();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return stream;
		}
	}

	private class GetPartnerPhotoTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap map = null;
			for (String url : urls) {
				map = downloadImage(url);
			}
			return map;
		}

		// Sets the Bitmap returned by doInBackground
		@Override
		protected void onPostExecute(Bitmap result) {
			ImageView p_image = (ImageView) findViewById(R.id.p_photo);
			p_image.setImageBitmap(result);
		}

		// Creates Bitmap from InputStream and returns it
		private Bitmap downloadImage(String url) {
			Bitmap bitmap = null;
			InputStream stream = null;
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;

			try {
				stream = getHttpConnection(url);
				bitmap = BitmapFactory.
						decodeStream(stream, null, bmOptions);
				stream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return bitmap;
		}

		// Makes HttpURLConnection and returns InputStream
		private InputStream getHttpConnection(String urlString)
				throws IOException {
			InputStream stream = null;
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					stream = httpConnection.getInputStream();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return stream;
		}
	}
	
	private class MyQueryYahooPlaceForPartnerTask extends AsyncTask<Void, Void, Void>{

		ArrayList<String> l;

		@Override
		protected Void doInBackground(Void... arg0) {
			l = QueryYahooPlaceForPartnerAPIs();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			new MyQueryYahooWeatherForPartnerTask(l.get(0)).execute();
			super.onPostExecute(result);
		}

	}
	
	private class MyQueryYahooPlaceForMeTask extends AsyncTask<Void, Void, Void>{

		ArrayList<String> l;

		@Override
		protected Void doInBackground(Void... arg0) {
			l = QueryYahooPlaceForMeAPIs();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			new MyQueryYahooWeatherForMeTask(l.get(0)).execute();
			super.onPostExecute(result);
		}

	}
	
	private ArrayList<String> QueryYahooPlaceForMeAPIs(){
		String uriPlace = Uri.encode(my_country + " " + my_city );

		yahooPlaceAPIsQuery = yahooPlaceApisBase
				+ "%22" + uriPlace + "%22"
				+ yahooapisFormat;

		String woeidString = QueryYahooWeather(yahooPlaceAPIsQuery);
		Document woeidDoc = convertStringToDocument(woeidString);
		return  parseWOEID(woeidDoc);
	}
	
	private ArrayList<String> QueryYahooPlaceForPartnerAPIs(){
		String uriPlace = Uri.encode(p_country + " " + p_city );

		yahooPlaceAPIsQuery = yahooPlaceApisBase
				+ "%22" + uriPlace + "%22"
				+ yahooapisFormat;

		String woeidString = QueryYahooWeather(yahooPlaceAPIsQuery);
		Document woeidDoc = convertStringToDocument(woeidString);
		return  parseWOEID(woeidDoc);
	}
	private ArrayList<String> parseWOEID(Document srcDoc){
		ArrayList<String> listWOEID = new ArrayList<String>();

		NodeList nodeListDescription = srcDoc.getElementsByTagName("woeid");
		if(nodeListDescription.getLength()>=0){
			for(int i=0; i<nodeListDescription.getLength(); i++){
				listWOEID.add(nodeListDescription.item(i).getTextContent());	
			}	
		}else{
			listWOEID.clear();	
		}

		return listWOEID;
	}

	private Document convertStringToDocument(String src){
		Document dest = null;

		DocumentBuilderFactory dbFactory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));	
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();	
		} catch (SAXException e) {
			e.printStackTrace();	
		} catch (IOException e) {
			e.printStackTrace();	
		}

		return dest;	
	}

	private String QueryYahooWeather(String queryString){
		String qResult = "";

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null){
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");	
				}

				qResult = stringBuilder.toString();	
			}	
		} catch (ClientProtocolException e) {
			e.printStackTrace();;	
		} catch (IOException e) {
			e.printStackTrace();	
		}

		return qResult;	
	}
	
	private class MyQueryYahooWeatherForPartnerTask extends AsyncTask<Void, Void, Void>{

		String woeid;
		String weatherString;
		String condition = "";
		String code = "";
		int snow[] = {5,7,13,14,15,16,17,41,42,43,46};
		int rain[] = {6,10,35,11,12,9,18,8,40};
		int sunny[] = {32,34};
		int storm[] = {1,3,4,37,38,39,45,47};
		int windy[] = {0,2,24,23};
		int foggy[] = {20,21,22};
		int cloudy[] = {26,27,28,29,30,44};
		int night[] = {31,33};
		TextView p_condition = (TextView) findViewById(R.id.p_condition);
		
		
		MyQueryYahooWeatherForPartnerTask(String w){
			woeid = w;
		}
		
		private boolean contains(int a[],int b)
		{
			
			for(int i =0; i < a.length; i++)
			{
				if(a[i]==b)
					return true;
			}
			return false;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			weatherString = QueryYahooWeather();
			Document weatherDoc = convertStringToDocument(weatherString);
			
			if(weatherDoc != null){
				parseWeather(weatherDoc);
			}else{
				p_condition.setText("Cannot convertStringToDocument!");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			p_condition.setText(condition);

			if(!code.equals(""))
			{
				RelativeLayout rLayout = (RelativeLayout) findViewById (R.id.p_layout);
				Resources res = getResources(); //resource handle
				   
				int c = Integer.parseInt(code);
				
				if(contains(snow,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.snow); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(rain,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.rain); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(sunny,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.sunny); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(storm,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.storm); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(windy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.windy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(foggy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.foggy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(cloudy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.cloudy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(c==19)
				{
				    rLayout.setBackgroundColor(Color.YELLOW);
				}
				if(c==25)
				{
				    rLayout.setBackgroundColor(Color.BLUE);
				}
				if(c==36)
				{
				    rLayout.setBackgroundColor(Color.RED);
				}
				if(contains(night,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.stars); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				
				
				
			}
		
			
			super.onPostExecute(result);
		}
		
		private String QueryYahooWeather(){
			String qResult = "";
			  String queryString = "http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=c";
			  
			  HttpClient httpClient = new DefaultHttpClient();
			  HttpGet httpGet = new HttpGet(queryString);
			  
			  try {
				  HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
				  
				  if (httpEntity != null){
					  InputStream inputStream = httpEntity.getContent();
					  Reader in = new InputStreamReader(inputStream);
					  BufferedReader bufferedreader = new BufferedReader(in);
					  StringBuilder stringBuilder = new StringBuilder();
					  
					  String stringReadLine = null;
					  
					  while ((stringReadLine = bufferedreader.readLine()) != null) {
						  stringBuilder.append(stringReadLine + "\n");  
					  }
					  
					  qResult = stringBuilder.toString();  
				  }  
			  } catch (ClientProtocolException e) {
				  e.printStackTrace(); 
			  } catch (IOException e) {
				  e.printStackTrace(); 
			  }
			  return qResult;	  
		}
		
		private Document convertStringToDocument(String src){
			Document dest = null;
			
			DocumentBuilderFactory dbFactory =
					DocumentBuilderFactory.newInstance();
			DocumentBuilder parser;

			try {
				parser = dbFactory.newDocumentBuilder();
				dest = parser.parse(new ByteArrayInputStream(src.getBytes()));	
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();	
			} catch (SAXException e) {
				e.printStackTrace();	
			} catch (IOException e) {
				e.printStackTrace();	
			}
			
			return dest;	
		}
		
		private void parseWeather(Document srcDoc){
			
			
			//<yweather:condition text="Fair" code="33" temp="60" date="Fri, 23 Mar 2012 8:49 pm EDT"/>
			NodeList conditionNodeList = srcDoc.getElementsByTagName("yweather:condition");
			if(conditionNodeList != null && conditionNodeList.getLength() > 0){
				Node conditionNode = conditionNodeList.item(0);
				NamedNodeMap conditionNamedNodeMap = conditionNode.getAttributes();
				
				condition = "Current Weather: " + conditionNamedNodeMap.getNamedItem("text").getNodeValue().toString() + "\nTemperature: ";
				condition += conditionNamedNodeMap.getNamedItem("temp").getNodeValue().toString() + " Celsius";
				
				code = conditionNamedNodeMap.getNamedItem("code").getNodeValue().toString();
				
			}else{
				condition = "EMPTY";
			}
			
			
		}
		
	}
	
	private class MyQueryYahooWeatherForMeTask extends AsyncTask<Void, Void, Void>{

		String woeid;
		String weatherString;
		String condition = "";
		String code = "";
		int snow[] = {5,7,13,14,15,16,17,41,42,43,46};
		int rain[] = {6,10,35,11,12,9,18,8,40};
		int sunny[] = {32,34};
		int storm[] = {1,3,4,37,38,39,45,47};
		int windy[] = {0,2,24,23};
		int foggy[] = {20,21,22};
		int cloudy[] = {26,27,28,29,30,44};
		int night[] = {31,33};
		TextView my_condition = (TextView) findViewById(R.id.my_condition);
		
		
		MyQueryYahooWeatherForMeTask(String w){
			woeid = w;
		}
		
		private boolean contains(int a[],int b)
		{
			
			for(int i =0; i < a.length; i++)
			{
				if(a[i]==b)
					return true;
			}
			return false;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			weatherString = QueryYahooWeather();
			Document weatherDoc = convertStringToDocument(weatherString);
			
			if(weatherDoc != null){
				parseWeather(weatherDoc);
			}else{
				my_condition.setText("Cannot convertStringToDocument!");
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			my_condition.setText(condition);

			if(!code.equals(""))
			{
				RelativeLayout rLayout = (RelativeLayout) findViewById (R.id.my_layout);
				Resources res = getResources(); //resource handle
				   
				int c = Integer.parseInt(code);
				
				if(contains(snow,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.snow); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(rain,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.rain); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(sunny,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.sunny); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(storm,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.storm); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(windy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.windy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(foggy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.foggy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(contains(cloudy,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.cloudy); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				if(c==19)
				{
				    rLayout.setBackgroundColor(Color.YELLOW);
				}
				if(c==25)
				{
				    rLayout.setBackgroundColor(Color.BLUE);
				}
				if(c==36)
				{
				    rLayout.setBackgroundColor(Color.RED);
				}
				if(contains(night,c))
				{
					Drawable drawable = res.getDrawable(R.drawable.stars); //new Image that was added to the res folder
				    rLayout.setBackground(drawable);
				}
				
				
				
			}
		
			
			super.onPostExecute(result);
		}
		
		private String QueryYahooWeather(){
			String qResult = "";
			  String queryString = "http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=c";
			  
			  HttpClient httpClient = new DefaultHttpClient();
			  HttpGet httpGet = new HttpGet(queryString);
			  
			  try {
				  HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
				  
				  if (httpEntity != null){
					  InputStream inputStream = httpEntity.getContent();
					  Reader in = new InputStreamReader(inputStream);
					  BufferedReader bufferedreader = new BufferedReader(in);
					  StringBuilder stringBuilder = new StringBuilder();
					  
					  String stringReadLine = null;
					  
					  while ((stringReadLine = bufferedreader.readLine()) != null) {
						  stringBuilder.append(stringReadLine + "\n");  
					  }
					  
					  qResult = stringBuilder.toString();  
				  }  
			  } catch (ClientProtocolException e) {
				  e.printStackTrace(); 
			  } catch (IOException e) {
				  e.printStackTrace(); 
			  }
			  return qResult;	  
		}
		
		private Document convertStringToDocument(String src){
			Document dest = null;
			
			DocumentBuilderFactory dbFactory =
					DocumentBuilderFactory.newInstance();
			DocumentBuilder parser;

			try {
				parser = dbFactory.newDocumentBuilder();
				dest = parser.parse(new ByteArrayInputStream(src.getBytes()));	
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();	
			} catch (SAXException e) {
				e.printStackTrace();	
			} catch (IOException e) {
				e.printStackTrace();	
			}
			
			return dest;	
		}
		
		private void parseWeather(Document srcDoc){
			
			
			//<yweather:condition text="Fair" code="33" temp="60" date="Fri, 23 Mar 2012 8:49 pm EDT"/>
			NodeList conditionNodeList = srcDoc.getElementsByTagName("yweather:condition");
			if(conditionNodeList != null && conditionNodeList.getLength() > 0){
				Node conditionNode = conditionNodeList.item(0);
				NamedNodeMap conditionNamedNodeMap = conditionNode.getAttributes();
				
				condition = "Current Weather: " + conditionNamedNodeMap.getNamedItem("text").getNodeValue().toString() + "\nTemperature: ";
				condition += conditionNamedNodeMap.getNamedItem("temp").getNodeValue().toString() + " Celsius";
				
				code = conditionNamedNodeMap.getNamedItem("code").getNodeValue().toString();
				
			}else{
				condition = "EMPTY";
			}
			
			
		}
		
	}

}
