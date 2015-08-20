package com.Helpme.mylocation;

import org.json.JSONObject;

import com.Helpme.mylocation.http.OKHttp;
import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;

//import android.widget.Toast;

public class Myservice extends Service {

	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private double lat, lng;
	private LocationManager mLocationManager = null;
	private GPSTracker myGPSTracker;
	private String status;
	private UserManager mManager;
	private String idclamer;
	private OKHttp okHttp;
	private ApplicationStatus appStatus;

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mManager = new UserManager(this);
		idclamer = mManager.getID().toString();
		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(myGPSTracker);
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		myGPSTracker = new GPSTracker();
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		}
		if (isNetworkOnline()) {
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 5000, 1, myGPSTracker);
		}

		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 1, myGPSTracker);

		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isNetworkOnline() {
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				return true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	// Always at GPS Find location
	private class GPSTracker implements android.location.LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			lat = loc.getLatitude();
			lng = loc.getLongitude();
			send();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private void send() {
		String lati = String.valueOf(lat);
		String lngti = String.valueOf(lng);

		RequestBody formBody = new FormEncodingBuilder()
				.add("claimer_id", idclamer).add("claimer_latitude", lati)
				.add("claimer_longtitude", lngti).build();

		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost
					+ "upclaimer.php", formBody));
			status = (json_data.getString("status"));

			if (status.equals("go")) {
				mManager.setStatsend("1");
				appStatus.setShowDialog(true);
				Log.e("ตรวจสอบงาน", "มีงานต้องทำ");
				Intent intent = new Intent(getApplicationContext(), Showdialog.class);
				intent.putExtra("idClaimer", idclamer);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else {
				Log.e("ตรวจสอบงาน", "ยังไม่มีงานต้องทำ");
			}
		} catch (Exception e) {
		}
	}

	public void onTaskRemoved(Intent rootIntent) {

		Intent restartServiceIntent = new Intent(getApplicationContext(),
				this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(
				getApplicationContext(), 1, restartServiceIntent,
				PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 1000,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);

	}

}
