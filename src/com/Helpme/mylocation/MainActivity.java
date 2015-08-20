package com.Helpme.mylocation;

import org.json.JSONObject;

import com.Helpme.mylocation.adapter.MenuListAdapter;
import com.Helpme.mylocation.fragment.Main_Fragment;
import com.Helpme.mylocation.fragment.Setting;
import com.Helpme.mylocation.http.OKHttp;
import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.GravityCompat;

public class MainActivity extends SherlockFragmentActivity {
	Controller aController;
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String idcus;
	private AlertDialog.Builder builder;
	private Context mContext;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuListAdapter mMenuAdapter;
	private UserManager mManager;
	private String[] title;
	private String[] subtitle;
	private int[] icon;
	private CharSequence mTitle;
	private int oldposition = 5;
	private TextView mytitle;
	private ImageView myicon;
	private ApplicationStatus appStatus;
	private OKHttp okHttp;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		okHttp = new OKHttp();
		mManager = new UserManager(this);
		idcus = mManager.getID();
		mContext = this;
		// Get the view from drawer_main.xml
		setContentView(R.layout.activity_menu);

		// Generate title
		title = new String[] { "Helpme Claimer", "ตั้งค่า", "ออกจากระบบ" };

		// Generate icon
		icon = new int[] { R.drawable.menu_1_helpme, R.drawable.menu_7_setting,
				R.drawable.menu_8_logout };

		// Locate DrawerLayout in drawer_main.xml
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// Locate ListView in drawer_main.xml
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		// Set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Pass string arrays to MenuListAdapter
		mMenuAdapter = new MenuListAdapter(MainActivity.this, title, subtitle,
				icon);

		// Set the MenuListAdapter to the ListView
		mDrawerList.setAdapter(mMenuAdapter);

		// Capture listview menu item click
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator.inflate(R.layout.titleview, null);

		// if you need to customize anything else about the text, do it here.
		// I'm using a custom TextView with a custom font in my layout xml so
		// all I need to do is set title
		mytitle = (TextView) v.findViewById(R.id.title);
		myicon = (ImageView) v.findViewById(R.id.iconmenu);
		// assign the view to the actionbar
		this.getActionBar().setCustomView(v);

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);

		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		Intent intent = getIntent();
		appStatus.setIsInPage(intent.getBooleanExtra("isInPage", false));

		if (!appStatus.isOnline(this)) {
			appStatus.setNetwork(this);
		}

		if (savedInstanceState == null) {
			mytitle.setText(title[0]);
			myicon.setImageResource(icon[0]);
			getSupportActionBar().setIcon(R.drawable.icon_mainmenu);
			selectItem(0);
		}

		aController = (Controller) getApplicationContext();

		// Check if Internet Connection present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(MainActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);

			// stop executing code by return
			return;
		}

		// Check if GCM configuration is set
		if (Config.YOUR_SERVER_URL == null || Config.GOOGLE_SENDER_ID == null
				|| Config.YOUR_SERVER_URL.length() == 0
				|| Config.GOOGLE_SENDER_ID.length() == 0) {

			// GCM sernder id / server url is missing
			aController.showAlertDialog(MainActivity.this,
					"Configuration Error!",
					"Please set your Server URL and GCM Sender ID", false);

			// stop executing code by return
			return;
		}

		// Check if Internet present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(MainActivity.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			// stop executing code by return
			return;
		}

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest permissions was properly set
		GCMRegistrar.checkManifest(this);

		// Register custom Broadcast receiver to show messages on activity
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {

			// Register with GCM
			GCMRegistrar.register(this, Config.GOOGLE_SENDER_ID);

		} else {

			// Device is already registered on GCM Server
			if (GCMRegistrar.isRegisteredOnServer(this)) {

				// Skips registration.
				Toast.makeText(getApplicationContext(),
						"Already registered with GCM Server", Toast.LENGTH_LONG)
						.show();

			} else {

				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						// Register on our server
						// On server creates a new user
						aController.register(context, idcus, regId);

						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};

				// execute AsyncTask
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {

			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	// ListView click listener in the navigation drawer
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mytitle.setText(title[position]);
			myicon.setImageResource(icon[position]);
			getSupportActionBar().setIcon(R.drawable.icon_mainmenu);
			selectItem(position);

			final ProgressDialog ringProgressDialog = ProgressDialog.show(
					MainActivity.this, "Please wait ...",
					"กำลังโหลดข้อมูล ...", true);
			ringProgressDialog.setCancelable(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						ringProgressDialog.dismiss();
					} catch (Exception e) {

					}
				}
			}).start();

		}
	}

	private void selectItem(int position) {
		switch (position) {
		case 0:
			if (position != oldposition) {
				Fragment mapfragment = new Main_Fragment();
				mapfragment.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, mapfragment);
				ft.commit();
			}
			oldposition = position;
			break;
		case 1:
			if (position != oldposition) {
				Setting set = new Setting();
				set.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
			}
			oldposition = position;
			break;
		case 2:
			if (position != oldposition) {
				mManager.resetStat();
				mManager.resetPin();
				Intent logout = new Intent(getApplicationContext(), Login.class);
				startActivity(logout);
			}
			oldposition = position;
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onStart() {
		stopService(new Intent(getBaseContext(), Myservice.class));
		super.onStart();
	}
	
	public void onStop() {
		super.onStop();
		startService(new Intent(getBaseContext(), Myservice.class));
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		Log.d("Check onResume MainActivity ",
				"isFillPin: " + appStatus.isFillPin() + " isInApp: "
						+ appStatus.isInApp() + " isInPage: "
						+ appStatus.isInPage());
		if (appStatus.checkStatus()) {
			appStatus.onResume();
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		appStatus.onPause();
		Log.d("Check onPause MainActivity ",
				"isFillPin: " + appStatus.isFillPin() + " isInApp: "
						+ appStatus.isInApp() + " isInPage: "
						+ appStatus.isInPage());
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
				appStatus.setIsInPage(true);
			}
		}
	}

	@Override
	public void onBackPressed() {
	}

	// Create a broadcast receiver to get message and show on screen
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = "";
			try {
				newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}

			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());

			if (newMessage != null) {
				if(newMessage.equals("Work") && !appStatus.isShowDialog()){
					appStatus.setShowDialog(true);
					
					builder =  new AlertDialog.Builder(mContext);
					builder.setTitle("HelpMeClaim");
					builder.setMessage("คุณได้รับการมอบหมายงานใหม่");
					builder.setPositiveButton("ดู	", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mManager.setStatsend("1");
							RequestBody formBody = new FormEncodingBuilder().add("id", idcus).build();
							StrictMode.setThreadPolicy(policy);
							try {
								JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost + "claimed.php", formBody));

							} catch (Exception e) {
								Log.e("Fail 3", e.toString());
							}
							dialog.cancel();
							appStatus.setShowDialog(false);
						}
					});
					
					setColorDialog();
				} else {
					Toast.makeText(getApplicationContext(),
							"Got Message: " + newMessage, Toast.LENGTH_LONG).show();
				}
			}

			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};
	
	public void setColorDialog() {
		Dialog d = builder.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(this.getResources().getColor(
				R.color.default_pink));

		int textViewId = d.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(this.getResources().getColor(R.color.default_pink));
	}
}
