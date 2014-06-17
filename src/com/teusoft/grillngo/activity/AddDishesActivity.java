package com.teusoft.grillngo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.teusoft.grillngo.R;
import com.teusoft.grillngo.dao.DishesDao;
import com.teusoft.grillngo.entity.MyDishes;
import com.teusoft.grillngo.fragment.MyDishesFragmentMenu;
import com.teusoft.grillngo.location.ErrorDialogFragment;
import com.teusoft.grillngo.location.LocationUtils;
import com.teusoft.grillngo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDishesActivity extends BaseDishesActivity implements
		OnClickListener, OnTouchListener, LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	Bitmap mBitmap;
	String mCurrentPhotoPath;
	LinearLayout backBtn;
	TextView saveBtn;
	TextView mTimeStamp;
	private DishesDao dishesDao;
	private MyDishes dishes;
	private long currentTime;
	private int _xDelta;
	private int _yDelta;
	RelativeLayout mRelativeLayout;
	ImageView mainImageView;
	ImageView imageView1;
	ImageView imageView2;
	TextView mLocation;
	EditText mTitle;
	int currentWidth;// Imageview current width
	int currentHeight;// Imageview current height
	// A request to connect to Location Services
	private Location currentLocation;
	private String addressText;
	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;
	String imageFileName;
	private boolean isSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveBtn = (TextView) findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		mTimeStamp = (TextView) findViewById(R.id.timestamp_tv);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.image_imgv);
		mainImageView = (ImageView) findViewById(R.id.main_imageView);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView2 = (ImageView) findViewById(R.id.imageView2);
		mLocation = (TextView) findViewById(R.id.location_tv);
		mTitle = (EditText) findViewById(R.id.title_ed);
		imageView1.setOnTouchListener(this);
		imageView2.setOnTouchListener(this);

		mRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						// Ensure you call it only once :
						mRelativeLayout.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						// Here you can get the size :)
						currentHeight = mRelativeLayout.getHeight();
						currentWidth = mRelativeLayout.getWidth();
						Log.e("height", currentHeight + "");
						// resizeView();
					}
				});
		init();
		// Create a new global location parameters object
		mLocationClient = new LocationClient(this, this, this);
		// Timestamp textview
		String timeStamp = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.US)
				.format(new Date(currentTime));
		mTimeStamp.setText(timeStamp);
	}

	public void init() {
		// currentTime = Calendar.getInstance().getTimeInMillis();
		currentTime = getIntent().getLongExtra("img_name", 0);
		mCurrentPhotoPath = getIntent().getStringExtra("img_path");
		mBitmap = getBitmapFromPath(mCurrentPhotoPath);
		// mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, 1080,
		// (mBitmap.getHeight() / currentHeight) * 1080);
		mainImageView.setImageBitmap(mBitmap);
		dishesDao = new DishesDao(this);
		if (getIntent().hasExtra("byteArray")) {
			Bitmap b = BitmapFactory.decodeByteArray(getIntent()
					.getByteArrayExtra("byteArray"), 0, getIntent()
					.getByteArrayExtra("byteArray").length);
			imageView2.setImageBitmap(b);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.slide_btn:
			finish();
			break;
		case R.id.save_btn:
			isSave = true;
			new SaveImageAsync(this).execute();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		if (!isSave) {
			deleteImageFile();
			Log.e("delete", "delete");
		}
		super.onDestroy();
	}

	/**
	 * Insert item to DB
	 */
	public void insertRecord() {
		dishes = new MyDishes();
		dishes.setTimeStamp(currentTime);
		dishes.setLocation(addressText);
		dishes.setImageName(imageFileName);
		dishes.setTitle(mTitle.getText().toString());
		dishesDao.insert(dishes);
	}

	private Bitmap getBitmapFromPath(String mCurrentPhotoPath) {
		/*
		 * There isn't enough memory to open up more than a couple camera photos
		 */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		// int targetW = mImageView.getWidth();
		// int targetH = mImageView.getHeight();
		int targetW = 1200;
		int targetH = 1600;

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		return bitmap;
	}

	/**
	 * Create image filename with Timestamp
	 * 
	 * @return
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		imageFileName = MyDishesFragmentMenu.JPEG_FILE_PREFIX + currentTime + "_n"
				+ MyDishesFragmentMenu.JPEG_FILE_SUFFIX;
		File albumF = Utils.getAlbumDir(this);
		File imageF = new File(albumF, imageFileName);
		return imageF;
	}

	public void moveImage(MotionEvent event, ImageView imageView) {
		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			_xDelta = (int) (X - imageView.getTranslationX());
			_yDelta = (int) (Y - imageView.getTranslationY());
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			break;
		case MotionEvent.ACTION_POINTER_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			imageView.setTranslationX(X - _xDelta);
			imageView.setTranslationY(Y - _yDelta);
			checkBound(imageView);
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.imageView1:
			moveImage(event, imageView1);
			return true;
		case R.id.imageView2:
			moveImage(event, imageView2);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Unable move image view to out of bound
	 * 
	 * @param imageView
	 */
	public void checkBound(ImageView imageView) {
		// Check bound left
		if (imageView.getX() <= 0) {
			imageView.setX(1);
		}
		// Check bound top
		if (imageView.getY() <= 0) {
			imageView.setY(1);
		}
		// Check bound right
		if (imageView.getX() + imageView.getWidth() >= mRelativeLayout
				.getWidth()) {
			imageView.setX(mRelativeLayout.getWidth() - imageView.getWidth());
		}
		if (imageView.getY() + imageView.getHeight() >= mRelativeLayout
				.getHeight()) {
			imageView.setY(mRelativeLayout.getHeight() - imageView.getHeight());
		}
	}

	/**
	 * Reset view with fit image capture
	 */
	public void resizeView() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRelativeLayout
				.getLayoutParams();
		double scale = Math.max((double) mBitmap.getHeight() / currentHeight,
				(double) mBitmap.getWidth() / currentWidth);
		params.width = (int) (mBitmap.getWidth() / scale);
		params.height = (int) (mBitmap.getHeight() / scale);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		mRelativeLayout.setLayoutParams(params);
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocationClient.connect();

	}

	public class SaveImageAsync extends AsyncTask<Void, String, Void> {
		private Context mContext;
		private ProgressDialog mProgressDialog;

		public SaveImageAsync(Context context) {
			mContext = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(mContext,
					AlertDialog.THEME_HOLO_LIGHT);
			mProgressDialog.setMessage("Saving...");
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... filePath) {
			saveViewToFile();
			return null;
		}

		@Override
		protected void onPostExecute(Void filename) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
			insertRecord();
			finish();
		}
	}

	/**
	 * Save image layout to file
	 * 
	 * @throws IOException
	 */
	public void saveViewToFile() {
		mBitmap = Utils
				.getBitmapFromViewWithColor(mRelativeLayout, Color.WHITE);
		try {
			Utils.exportBitmapToFile(mBitmap, createImageFile()
					.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void deleteImageFile() {
		try {
			File file;
			file = new File(createImageFile().getAbsolutePath().replace("_n",
					""));
			if (file.exists()) {
				file.delete();
			}
			file = new File(createImageFile().getAbsolutePath());
			if (file.exists()) {
				file.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shareViaFacebook() {
		saveViewToFile();
		Uri imgUri = null;
		try {
			imgUri = Uri
					.parse("file:///" + createImageFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		shareImageFromUri(imgUri);
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with
			// the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	/**
	 * Show a dialog returned by Google Play services for the connection error
	 * code
	 * 
	 * @param errorCode
	 *            An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getSupportFragmentManager(),
					LocationUtils.APPTAG);
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		currentLocation = mLocationClient.getLastLocation();
		(new GetAddressTask(this)).execute(currentLocation);
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
	}

	/**
	 * Report location updates to the UI.
	 * 
	 * @param location
	 *            The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {

	}

	/**
	 * An AsyncTask that calls getFromLocation() in the background. The class
	 * uses the following generic types: Location - A
	 * {@link android.location.Location} object containing the current location,
	 * passed as the input parameter to doInBackground() Void - indicates that
	 * progress units are not used by this subclass String - An address passed
	 * to onPostExecute()
	 */
	protected class GetAddressTask extends AsyncTask<Location, Void, String> {

		// Store the context passed to the AsyncTask when the system
		// instantiates it.
		Context localContext;

		// Constructor called by the system to instantiate the task
		public GetAddressTask(Context context) {

			// Required by the semantics of AsyncTask
			super();

			// Set a Context for the background task
			localContext = context;
		}

		/**
		 * Get a geocoding service instance, pass latitude and longitude to it,
		 * format the returned address, and return the address to the UI thread.
		 */
		@Override
		protected String doInBackground(Location... params) {
			/*
			 * Get a new geocoding service instance, set for localized
			 * addresses. This example uses android.location.Geocoder, but other
			 * geocoders that conform to address standards can also be used.
			 */
			Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

			// Get the current location from the input parameter list
			Location location = params[0];

			// Create a list to contain the result address
			List<Address> addresses = null;

			// Try to get an address for the current location. Catch IO or
			// network problems.
			try {

				/*
				 * Call the synchronous getFromLocation() method with the
				 * latitude and longitude of the current location. Return at
				 * most 1 address.
				 */
				if (location != null) {
					addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
				}

				// Catch network or other I/O problems.
			} catch (IOException exception1) {

				// Log an error and return an error message
				Log.e(LocationUtils.APPTAG,
						getString(R.string.IO_Exception_getFromLocation));

				// print the stack trace
				exception1.printStackTrace();

				// Return an error message
				return (getString(R.string.IO_Exception_getFromLocation));

				// Catch incorrect latitude or longitude values
			} catch (IllegalArgumentException exception2) {

				// Construct a message containing the invalid arguments
				String errorString = getString(
						R.string.illegal_argument_exception,
						location.getLatitude(), location.getLongitude());
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
				addressText = getString(
						R.string.address_output_string,

						// If there's a street address, add it
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) + "," : "",

						// Locality is usually a city
						// address.getLocality(),
						address.getAddressLine(1) + ", "
								+ address.getAddressLine(2) + ", ",

						// The country of the address
						// address.getCountryName());
						address.getAddressLine(3));

				// Return the text
				return addressText;

				// If there aren't any addresses, post a message
			} else {
				return getString(R.string.no_address_found);
			}
		}

		/**
		 * A method that's called once doInBackground() completes. Set the text
		 * of the UI element that displays the address. This method runs on the
		 * UI thread.
		 */
		@Override
		protected void onPostExecute(String address) {
			// Set the address in the UI
			mLocation.setText(address);
		}
	}

}
