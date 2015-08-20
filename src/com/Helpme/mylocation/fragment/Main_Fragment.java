package com.Helpme.mylocation.fragment;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;
import org.w3c.dom.Document;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.Helpme.mylocation.Login;
import com.Helpme.mylocation.R;
import com.Helpme.mylocation.Showdialog;
import com.Helpme.mylocation.http.GMapV2Direction;
import com.Helpme.mylocation.http.OKHttp;
import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

public class Main_Fragment extends GMap_Fragment {
	private GoogleMap mMap;
	private Marker mMarker, mMarker2;
	private double lat, lng, lat2, lng2;
	private AlertDialog.Builder builder;
	private InputStream is = null;
	private String result, line = null;
	private LatLng coordinate;
	private String status = "rd";
	private int code;
	private String idclamer;
	private View rootView;
	private UserManager mManager;
	private OKHttp okHttp;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mManager = new UserManager(getActivity());
		idclamer = mManager.getID().toString();
		okHttp = new OKHttp();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.mainlay, container, false);
		setUpMapIfNeeded();
		return rootView;
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				updateUI();
			}
		}
	}

	@Override
	public void updateUI() {
		if (mCurrentLocation != null) {
			lat = mCurrentLocation.getLatitude();
			lng = mCurrentLocation.getLongitude();
			coordinate = new LatLng(lat, lng);
			send();
			if (mMarker != null)
				mMarker.remove();
			Log.i("GETSTATSEND", mManager.getStatsend());
			if (mManager.getStatsend().equals("1")) {
				MarkCus();
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 16));
			} else {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 16));
				mMarker = mMap.addMarker(new MarkerOptions()
						.position(new LatLng(lat, lng))
						.title("ID : " + mManager.getID())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.marker_claimer)));
			}

			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker arg0) {
					if (mManager.getStatsend().equals("1"))
						Claimed();
					return false;
				}
			});
		}
		super.updateUI();
	}

	public void Claimed() {
		builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("ให้ความช่วยเหลือแล้ว ใช่ หรือ ไม่ ?");
		builder.setMessage("กรุณากดยืนยันเมื่อทำงานเรียบร้อยแล้ว");
		builder.setCancelable(true);
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				endClaim();
				mMap.clear();
				mMarker = mMap.addMarker(new MarkerOptions().position(
						new LatLng(lat, lng)).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.marker_claimer)));
				mManager.setStatsend("0");
				dialog.cancel();
			}
		});
		builder.setPositiveButton("ไม่ใช่",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		setColorDialog();
	}

	protected void endClaim() {
		String id = mManager.getID().toString();
		RequestBody formBody = new FormEncodingBuilder().add("id", id).build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost
					+ "claimed.php", formBody));
			code = (json_data.getInt("code"));
			if (code == 1) {
				Log.e("Insert", "Inserted Successfully");
			} else {
				Log.e("Insert", "Sorry Try Again");

			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}

	public void MarkCus() {
		mMap.clear();
		getCus();
		mMarker = mMap.addMarker(new MarkerOptions().position(
				new LatLng(lat, lng))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.marker_claimer)));
		mMarker2 = mMap.addMarker(new MarkerOptions().position(
				new LatLng(lat2, lng2)).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.marker_user)));

		GMapV2Direction md = new GMapV2Direction();
		Document doc = md.getDocument((new LatLng(lat, lng)), (new LatLng(lat2,
				lng2)), GMapV2Direction.MODE_DRIVING);
		ArrayList<LatLng> directionPoint = md.getDirection(doc);
		PolylineOptions rectLine = new PolylineOptions().width(10).color(
				Color.BLUE);
		for (int i = 0; i < directionPoint.size(); i++) {
			rectLine.add(directionPoint.get(i));
		}

		@SuppressWarnings("unused")
		Polyline polylin = this.mMap.addPolyline(rectLine);
	}

	protected void getCus() {
		String id = mManager.getID().toString();
		RequestBody formBody = new FormEncodingBuilder().add("claimer_id", id)
				.build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost
					+ "routing.php", formBody));
			lat2 = (json_data.getDouble("lati"));
			lng2 = (json_data.getDouble("longi"));

			if (lat2 != 0 && lng2 != 0) {
				Log.e("Claimer", "Get Loacation Success");
				Log.e("Claimer", "Get Loacation Latitude = " + lat2
						+ "Longtitude = " + lng2);
			} else {
				Log.e("Claimer", "Get Loacation Fail");
			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}

	}

	// ฟังก์ชั่น ส่งค่าไป Server
	private void send() {
		String id = mManager.getID().toString();
		String lati = String.valueOf(lat);
		String lngti = String.valueOf(lng);

		RequestBody formBody = new FormEncodingBuilder().add("claimer_id", id)
				.add("claimer_latitude", lati).add("claimer_longtitude", lngti)
				.build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost
					+ "upclaimer.php", formBody));
			String stat = (json_data.getString("status"));
			if (stat.equals("rd")) {
				Log.e("ตรวจสอบงาน", "ยังไม่มีงานต้องทำ");
				mMap.clear();
				mMarker = mMap.addMarker(new MarkerOptions().position(
						new LatLng(lat, lng)).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.marker_claimer)));

			} else if (stat.equals("go")) {
				Log.e("ตรวจสอบงาน", "มีงานต้องทำ");
				mManager.setStatsend("1");
			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}

	public void onDestroyView() {
		super.onDestroyView();
		try {
			SupportMapFragment fragment = (SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map);
			if (fragment != null)
				getFragmentManager().beginTransaction().remove(fragment)
						.commit();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

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