package com.Helpme.mylocation;

import org.json.JSONObject;

import com.Helpme.mylocation.http.OKHttp;
import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Showdialog extends Activity {

	private UserManager mManager;
	private ApplicationStatus appStatus;
	private OKHttp okHttp;
	private String idClaim;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();
		okHttp = new OKHttp();
		Intent intent = getIntent();
		idClaim = intent.getStringExtra("idClaimer");
		mManager = new UserManager(this);
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("HelpMeClaim");
		alertDialog.setMessage("คุณได้รับการมอบหมายงานใหม่");
		alertDialog.setButton("ดู	", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mManager.setStatsend("1");
				endNoti();
				Intent job = new Intent(getApplicationContext(), MainActivity.class);
				job.putExtra("isInPage", true);
				startActivity(job);
				finish();
				dialog.cancel();
				appStatus.setShowDialog(false);
			}
		});

		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		alertDialog.show();

		int dividerId = alertDialog.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = alertDialog.findViewById(dividerId);
		divider.setBackgroundColor(this.getResources().getColor(
				R.color.default_pink));

		int textViewId = alertDialog.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) alertDialog.findViewById(textViewId);
		tv.setTextColor(this.getResources().getColor(R.color.default_pink));

	}

	protected void endNoti() {
		RequestBody formBody = new FormEncodingBuilder().add("id", idClaim).build();
		StrictMode.setThreadPolicy(policy);
		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost + "claimed.php", formBody));

		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
		
	}
	@Override
	protected void onPause() {
		appStatus.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (appStatus.checkStatus()) {
			appStatus.onResume();
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}
		super.onResume();
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

}
