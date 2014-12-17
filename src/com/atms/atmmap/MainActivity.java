package com.atms.atmmap;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atms.atmmap.MyLocation.LocationResult;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends BaseActivity implements OnMarkerDragListener {

	ImageButton menu_button;
	private AdView adView;
	public DatabaseWorker dw;

	public RelativeLayout popup;
	public RelativeLayout location_popup;
	int currentMarkerIndex = -1;
	public LatLng currentGPS;
	public String currentStreetGPS = "";
	public final static String EXTRA_MESSAGE = "com.atms.atmmap.MESSAGE";
	public LinearLayout adBlock;

	boolean ignoreMapMovement = false;
	public Menu menu;
	public LatLng ZoomArea = new LatLng(53.558, 9.927);
	public LatLng MyLoc = new LatLng(53.558, 9.927);

	public String nearestTown;
	public String locationDisplayText;
public LatLng compareLatLng;
	ArrayList<LatLng> decodedPoints;
	public Location location;
	private GoogleMap map;
	ArrayList<Marker> markers = new ArrayList<Marker>();
	ArrayList<MarkerDetails> marker_gps = new ArrayList<MarkerDetails>();

	
	public void setMapType(String tp) {
		if (tp.equals("hybrid"))
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		if (tp.equals("road"))
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		if (tp.equals("satellite"))
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		if (tp.equals("terrain"))
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

		v3EasyTracker.set(Fields.SCREEN_NAME, "Set Map Type: " + tp);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.exitApp:
			exitApp();
			return true;
		case R.id.rateApp:
			rateApp();
			return true;
		case R.id.terrain:
			setMapType("terrain");
			return true;
		case R.id.road:
			setMapType("road");
			return true;
		case R.id.satellite:
			setMapType("satellite");
			return true;
		case R.id.hybrid:
			setMapType("hybrid");
			return true;
		case R.id.moreApps:
			moreApps();
			return true;
		case R.id.playScrabattle:
			playScrabattle();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void playScrabattle() {
		Uri uri = Uri
				.parse("https://play.google.com/store/apps/details?id=com.wordgames.scabattle&hl=en");
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getBaseContext(), "Couldn't launch the market",
					Toast.LENGTH_LONG).show();
		}

		v3EasyTracker.set(Fields.SCREEN_NAME, "Play Scrabattle");
	}

	public void moreApps() {
		Uri uri = Uri
				.parse("https://play.google.com/store/apps/developer?id=EMurph");
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getBaseContext(), "Couldn't launch the market",
					Toast.LENGTH_LONG).show();
		}

		v3EasyTracker.set(Fields.SCREEN_NAME, "More Apps");

	}

	public void rateApp() {

		Uri uri = Uri
				.parse("https://play.google.com/store/apps/details?id=com.atms.atmmap");
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getBaseContext(), "Couldn't launch the market",
					Toast.LENGTH_LONG).show();
		}

		v3EasyTracker.set(Fields.SCREEN_NAME, "Rate App");
	}

	Tracker v3EasyTracker;
	
	public void setDecodedPoints(ArrayList<ATM> atms){
		System.out.println("DEcoding");
		for(int i=0;i<atms.size();i++){
			decodedPoints.add(new LatLng(atms.get(i).lat, atms.get(i).lng));
		}
	
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		decodedPoints = new ArrayList<LatLng>();
		v3EasyTracker = EasyTracker.getInstance(this);

		try {
			MapsInitializer.initialize(getApplicationContext());
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Problem with Google Play Services", Toast.LENGTH_LONG)
					.show();

		}

		
		adView = new AdView(this);

		String areaLoc = "";

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			areaLoc = extras.getString("areaLoc");
		}

		setContentView(R.layout.activity_main);

		dw = new DatabaseWorker(getBaseContext());

		
		adBlock = (LinearLayout) findViewById(R.id.adblock);

		DOIT();

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location loc){
		        System.out.println("GOT loc");
		        MainActivity.this.location=loc;
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
		// v3

		if (areaLoc.length() < 1) {

			if (location != null) {
				MyLoc = new LatLng(location.getLatitude(),
						location.getLongitude());
				ZoomArea = new LatLng(location.getLatitude(),
						location.getLongitude());
				// Set the screen name on the tracker so that it is used in all
				// hits sent from this screen.
				locationDisplayText = "Current Location";
				nearestTown = getNearestTown();
				v3EasyTracker.set(Fields.SCREEN_NAME, "Home Screen GEO :: "
						+ nearestTown);

			}

			else {
				MyLoc = new LatLng(53.344, -6.267);
				ZoomArea = new LatLng(53.344, -6.267);
				// Set the screen name on the tracker so that it is used in all
				// hits sent from this screen.
				locationDisplayText = "Dublin";
				v3EasyTracker.set(Fields.SCREEN_NAME, "Home Screen DEFAULT");

			}

		} else {

			MyLoc = new LatLng(Double.parseDouble(areaLoc.split(",")[0]),
					Double.parseDouble(areaLoc.split(",")[1]));
			// Set the screen name on the tracker so that it is used in all hits
			// sent from this screen.
			nearestTown = getNearestTown();
			if (!nearestTown.equals("")) {
				locationDisplayText = nearestTown;
			} else {
				locationDisplayText = "Current Location";
			}
			v3EasyTracker.set(Fields.SCREEN_NAME, "Home Screen SEARCH:: "
					+ nearestTown);

		}

		// Send a screenview.
		v3EasyTracker.send(MapBuilder.createAppView().build());

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// map.setOnMarkerClickListener((OnMarkerClickListener) this);
		displayMapOnScreen();

		final EditText searchTxt = (EditText) findViewById(R.id.searchTerm);
		final ImageButton xbut = (ImageButton) findViewById(R.id.x_button);
		final ImageButton search_button = (ImageButton) findViewById(R.id.search_button);
		final ImageButton gps_button = (ImageButton) findViewById(R.id.gps_button);
		final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.butlay);
		final LinearLayout hrline3 = (LinearLayout) findViewById(R.id.hrline3);
		final LinearLayout hrline4 = (LinearLayout) findViewById(R.id.hrline4);
		final RelativeLayout footerBlock = (RelativeLayout) findViewById(R.id.footer);
		popup = (RelativeLayout) findViewById(R.id.lay1);
		location_popup = (RelativeLayout) findViewById(R.id.lay2);
		// mainLayout.setBackgroundColor(Color.parseColor("#BBBF0000"));
		final Button enable_location_services_button = (Button) findViewById(R.id.enable_location_services_but);
		final ImageButton x_location_services_button = (ImageButton) findViewById(R.id.x_location_services_button);

		search_button.setVisibility(View.VISIBLE);
		searchTxt.setVisibility(View.INVISIBLE);
		xbut.setVisibility(View.INVISIBLE);
		mainLayout.setVisibility(View.INVISIBLE);
		popup.setVisibility(View.INVISIBLE);
		mainLayout.setVisibility(View.GONE);
		location_popup.setVisibility(View.GONE);

		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		search_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				location_popup.setVisibility(View.GONE);
				popup.setVisibility(View.GONE);

				search_button.setVisibility(View.INVISIBLE);
				searchTxt.setVisibility(View.VISIBLE);
				xbut.setVisibility(View.VISIBLE);
				gps_button.setVisibility(View.INVISIBLE);
				menu_button.setVisibility(View.INVISIBLE);
				footerBlock.setVisibility(View.INVISIBLE);
				mainLayout.setVisibility(View.VISIBLE);
				hrline3.setVisibility(View.INVISIBLE);
				hrline4.setVisibility(View.INVISIBLE);
				searchTxt.setFocusableInTouchMode(true);
				searchTxt.requestFocus();
				imm.showSoftInput(searchTxt, InputMethodManager.SHOW_IMPLICIT);

				searchTxt.setText("");
			}
		});

		x_location_services_button
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						location_popup.setVisibility(View.GONE);
						popup.setVisibility(View.GONE);

					}
				});
		xbut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search_button.setVisibility(View.VISIBLE);
				searchTxt.setVisibility(View.INVISIBLE);
				xbut.setVisibility(View.INVISIBLE);
				gps_button.setVisibility(View.VISIBLE);
				menu_button.setVisibility(View.VISIBLE);
				mainLayout.setVisibility(View.GONE);

				hrline3.setVisibility(View.VISIBLE);
				hrline4.setVisibility(View.VISIBLE);
				footerBlock.setVisibility(View.INVISIBLE);
				location_popup.setVisibility(View.GONE);
				popup.setVisibility(View.GONE);

				imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);

			}
		});

		enable_location_services_button
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
						location_popup.setVisibility(View.GONE);
						popup.setVisibility(View.GONE);

					}
				});

		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				popup.setVisibility(View.GONE);
				location_popup.setVisibility(View.GONE);

			}
		});
		
		compareLatLng = map.getCameraPosition().target; 
		map.setOnCameraChangeListener(new OnCameraChangeListener() {

		    @Override
		    public void onCameraChange(CameraPosition arg0) {
		    	if(!ignoreMapMovement){
		    		LatLng cop2 = map.getCameraPosition().target;
		    		if(Math.abs(cop2.latitude - compareLatLng.latitude) > 1 || Math.abs(cop2.longitude - compareLatLng.longitude) > 1  )
		    		loadMarkers();
		    	}
		    	MainActivity.this.compareLatLng = map.getCameraPosition().target; 
		    	ignoreMapMovement=false;
		    }
		});
		
		menu_button = (ImageButton) findViewById(R.id.menu);

		menu_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("IN THIS PLACE NPOW");
				openOptionsMenu();
			}
		});

		gps_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (location == null) {

					location_popup.setVisibility(View.VISIBLE);
					popup.setVisibility(View.GONE);

				} else {
					Intent intent = new Intent(getBaseContext(),
							MainActivity.class);

					startActivity(intent);

				}
			}
		});

		searchTxt.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

				mainLayout.removeAllViews();
				lastTextChange++;
				final int lsc = lastTextChange;
				
				
				if(lsc == lastTextChange && searchingInAction==0){
					searchingInAction=1;
				ArrayList<Town> showTowns = (getTowns(searchTxt.getText()));
				for (int i = 0; i < showTowns.size(); i++) {
					final Town showTown = showTowns.get(i);
					Button btn = new Button(getBaseContext());
					btn.setBackgroundColor(0xBB4742CA);
					btn.setTextSize(16);
					btn.setTextColor(Color.WHITE);
					
					if (showTown.name.length() > 1) {

						btn.setText(showTown.name);

						btn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								// Perform action on click
								Intent intent = new Intent(getBaseContext(),
										MainActivity.class);
								intent.putExtra("areaLoc", ""+showTown.lat+","+showTown.lng);
								startActivity(intent);
							}
						});

					} else {
						btn.setText("No Results Found");
					}

					mainLayout.addView(btn);

					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							btn.getLayoutParams());
					lp.setMargins(5, 1, 5, 0);
					btn.setLayoutParams(lp);
				}
				searchingInAction=0;
				        	}
					        
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}
		});

	}

	public int lastTextChange = 0;
	public int searchingInAction = 0;
	public void exitApp() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	void DOIT() {

		AdRequest adRequest = new AdRequest.Builder().build();

		// adView.setAdUnitId("a1528b49923ca27");
		adView.setAdUnitId("ca-app-pub-1818326317212789/2038714575");
		//
		adView.setAdSize(AdSize.BANNER);

		// Lookup your LinearLayout assuming it's been given
		// the attribute android:id="@+id/mainLayout".

		// Add the adView to it.
		adBlock.addView(adView);
		// Initiate a generic request.

		// Load the adView with the ad request.
		adView.loadAd(adRequest);

	}

	LocationListener onLocationChange = new LocationListener() {
		public void onLocationChanged(Location loc) {

		}

		public void onProviderDisabled(String provider) {
			// required for interface, not used
		}

		public void onProviderEnabled(String provider) {
			// required for interface, not used
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// required for interface, not used
		}
	};

	public void showStreet(String gps) {
		// if(exists("http://maps.googleapis.com/maps/api/streetview?size=20x20&location="+gps+"&fov=90&heading=235&pitch=10&sensor=false")){
		Intent streetView = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("google.streetview:cbll=" + gps));
		startActivity(streetView);
		// }
	}

	public static boolean exists(String URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) new URL(URLName)
					.openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {

			return false;
		}
	}

	public void initPage() {
		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		displayMapOnScreen();
	}
	public void goToMyLocation() {
		
		map.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
		
		map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
	        @Override
	        public void onMapLoaded() {
	        	
	        	com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
	    		
	    		for (LatLng point : decodedPoints) {
	    		    boundsBuilder.include(point);
	    		}
	    		LatLngBounds bounds = boundsBuilder.build();
	    		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
	    		map.moveCamera(cameraUpdate);	
	        }
	});
		
	}
	
	public String getNearestTown() {
		String townName = "";
		return dw.getNearestTown(MyLoc);
		/*
		 * try {
		 * 
		 * URL getTown = new
		 * URL("http://imaga.me/atms/api/getNearestTown.php?GPS="
		 * +MyLoc.toString().replace(" ","")); URLConnection yc =
		 * getTown.openConnection(); BufferedReader in = new BufferedReader(new
		 * InputStreamReader(yc .getInputStream())); String inputLine; while
		 * ((inputLine = in.readLine()) != null) { townName+=inputLine; }
		 * in.close(); } catch (Exception e) { Toast.makeText(getBaseContext(),
		 * "Please check your internet connection", Toast.LENGTH_LONG).show();
		 * 
		 * }
		 * 
		 * return townName;
		 */
	}

	@Override
	public void openOptionsMenu() {

		Configuration config = getResources().getConfiguration();

		if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_LARGE) {

			int originalScreenLayout = config.screenLayout;
			config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
			super.openOptionsMenu();
			config.screenLayout = originalScreenLayout;

		} else {
			super.openOptionsMenu();
		}
	}

	public void loadMarkers() {
		map.clear();
		map.addMarker(new MarkerOptions()
				.position(MyLoc)
				.title(locationDisplayText)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_icon))
				.draggable(true));

		ArrayList<ATM> closestAtms = dw.getClosestAtms(map.getCameraPosition().target);
		setDecodedPoints(closestAtms);
		
		int c = closestAtms.size();
		for (int i = 0; i < c; i++) {

			ATM atm = closestAtms.get(i);

			if (atm.owner.equals("AIB") || atm.owner.equals("FIRST TRUST BANK")) {
				Marker m = (map.addMarker(new MarkerOptions()
						.position(atm.latlng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.aib_atm))
						.draggable(false)));

				marker_gps.add(new MarkerDetails(i, atm.owner, atm.address,
						atm.latlng));
				markers.add(m);
			} else if (atm.owner.equals("ULSTER BANK")) {

				Marker m = (map.addMarker(new MarkerOptions()
						.position(atm.latlng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.ulster_atm))
						.draggable(false)));
				marker_gps.add(new MarkerDetails(i, atm.owner, atm.address,
						atm.latlng));

				markers.add(m);
			} else if (atm.owner.equals("PERMANENT TSB")) {

				Marker m = (map.addMarker(new MarkerOptions()
						.position(atm.latlng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.tsb_atm))
						.draggable(false)));

				marker_gps.add(new MarkerDetails(i, atm.owner, atm.address,
						atm.latlng));

				markers.add(m);
			} else if (atm.owner.equals("BANK OF IRELAND")) {

				Marker m = (map.addMarker(new MarkerOptions()
						.position(atm.latlng)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.boi_atm))
						.draggable(false)));

				marker_gps.add(new MarkerDetails(i, atm.owner, atm.address,
						atm.latlng));

				markers.add(m);
			}

		}

		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				ignoreMapMovement=true;
				arg0.hideInfoWindow();
				if (markers != null) {
					for (int i = 0; i < markers.size(); i++) {

						if (arg0.equals(markers.get(i))) { // if marker source
															// is clicked
							currentMarkerIndex = i;
							currentGPS = marker_gps.get(i).gps;

							showToast(marker_gps.get(i).ownerStr,
									marker_gps.get(i).addressStr);

							currentStreetGPS = marker_gps.get(i).gps.latitude+","+marker_gps.get(i).gps.longitude; 
							System.out.println("CURRENT GPS: "+currentStreetGPS);
						}
					
					}
				}

				return false;
			}

		});

		map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDragEnd(Marker marker) {
				if (markers != null) {
			//	MyLoc = map.getCenter();
					/*
					 for (int i = 0; i < markers.size(); i++) {
					 	if (markers.get(i) == marker) {

							try {
								URL posUrl = new URL(
										"http://imaga.me/atms/api/updateGPS.php?ID="
												+ marker_gps.get(i).id
												+ "&GPS="
												+ marker.getPosition()
														.toString()
														.replace(" ", ""));
								URLConnection yc = posUrl.openConnection();
								new BufferedReader(new InputStreamReader(yc
										.getInputStream()));
							} catch (Exception e) {
								Toast.makeText(
										getBaseContext(),
										"Please check your internet connection",
										Toast.LENGTH_LONG).show();

							}

						}
					}
					*/

				}
			}

			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	public void showToast(String title, String address) {
		// get your custom_toast.xml ayout
		// set a dummy image
		ImageButton image = (ImageButton) findViewById(R.id.image);
		image.setImageResource(R.drawable.pegman);

		ImageButton xbut = (ImageButton) findViewById(R.id.x_close_id);
		xbut.setImageResource(R.drawable.x_close);

		/*
		 * Button saveBut = (Button)findViewById(R.id.saveButton);
		 * saveBut.setHeight(100);
		 * 
		 * Button saveButLoc = (Button)findViewById(R.id.saveLoc);
		 * saveButLoc.setHeight(100);
		 * 
		 * Button saveButStreet = (Button)findViewById(R.id.saveStreet);
		 * saveButStreet.setHeight(100);
		 */

		// set a message
		TextView text = (TextView) findViewById(R.id.text);
		text.setText(title + "\r\n" + address);
		text.setWidth(200);
		text.setHeight(300);
		// Toast...

		popup.setVisibility(View.VISIBLE);
		location_popup.setVisibility(View.GONE);

		xbut.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				popup.setVisibility(View.INVISIBLE);
				return false;
			}
		});
		
		
		image.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				popup.setVisibility(View.INVISIBLE);
				showStreet(currentStreetGPS);

				return false;
			}
		});

	}

	public class MarkerDetails {
		int id;
		public String ownerStr;
		public String addressStr;
		public LatLng gps;

		public MarkerDetails(int id, String ownerStr, String addressStr,
				LatLng gps) {
			this.gps = gps;
			this.id = id;
			this.ownerStr = ownerStr;
			this.addressStr = addressStr;
		}

	}

	public void displayMapOnScreen() {

		//loadMarkers();

		// Move the camera instantly to hamburg with a zoom of 15.
		goToMyLocation();
		// Zoom in, animating the camera.
		//
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Town> getTowns(CharSequence charSequence) {

		return dw.getTowns(charSequence);
		/*
		 * String retStr = ""; try { Random rand = new Random();
		 * 
		 * URL getATMS = new
		 * URL("http://imaga.me/atms/api/getTownNames.php?hintText="
		 * +charSequence); URLConnection yc = getATMS.openConnection();
		 * 
		 * InputStream inz = yc.getInputStream(); BufferedReader in = new
		 * BufferedReader(new InputStreamReader(inz));
		 * 
		 * String inputLine; int yy=0; while ((inputLine = in.readLine()) !=
		 * null) {
		 * 
		 * retStr+=inputLine; } in.close(); } catch (Exception e) {
		 * Toast.makeText(getBaseContext(),
		 * "Please check your internet connection", Toast.LENGTH_LONG).show();
		 * 
		 * 
		 * }
		 * 
		 * return retStr;
		 */
	}

	@Override
	public void onPause() {
		dw.db.close();
		adView.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		dw.db.close();
		adView.resume();
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		dw.db.close();
		super.onDestroy();
	}

}
