package com.Helpme.mylocation.fragment;

import com.Helpme.mylocation.ChangePass;
import com.Helpme.mylocation.ChangePin_Activity;
import com.Helpme.mylocation.MainActivity;
import com.Helpme.mylocation.R;
import com.Helpme.mylocation.model.User;
import com.Helpme.mylocation.persistence.UserManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Setting extends Fragment {

	private UserManager mManager;
	private TextView name, phone, address;
	private Button submit, cancel;
	private View rootView;
	private String idcus;
	private LinearLayout changepass, changepin;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private User user = new User();
	private Context context;
	private Builder builder;

	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mManager = new UserManager(getActivity());
		idcus = mManager.getID();
		context = getActivity();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater
				.inflate(R.layout.setting_fragment, container, false);
		changepass = (LinearLayout) rootView.findViewById(R.id.changepass);
		changepin = (LinearLayout) rootView.findViewById(R.id.changepin);
		cancel = (Button) rootView.findViewById(R.id.cancel);
		Start();
		return rootView;
	}

	private void Start() {
		name = (TextView) rootView.findViewById(R.id.name);
		phone = (TextView) rootView.findViewById(R.id.phone);
		address = (TextView) rootView.findViewById(R.id.address);
		user = mManager.getuser();
		name.setText(user.getname().toString());
		phone.setText(user.getphone().toString());
		address.setText(user.getaddress().toString());
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				back();
			}
		});

		changepass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent changepass = new Intent(getActivity(), ChangePass.class);
				startActivity(changepass);
			}
		});
		
		changepin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent changepin = new Intent(getActivity(), ChangePin_Activity.class);
				startActivity(changepin);
			}
		});

	}
	
	public void back() {
		builder = new AlertDialog.Builder(context);
		builder.setTitle("HelpMe");
		builder.setMessage("ต้องการยกเลิกการทำงาน ? ");
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent cancel = new Intent(getActivity(), MainActivity.class);
				cancel.putExtra("isInPage", true);
				startActivity(cancel);
			}
		});
		builder.setPositiveButton("ไม่ใช่", null);

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
