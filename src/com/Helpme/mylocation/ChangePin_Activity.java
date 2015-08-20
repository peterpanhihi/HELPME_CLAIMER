package com.Helpme.mylocation;

import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ChangePin_Activity extends Activity {
	private UserManager mManager;

	private EditText etOldpin, etNewpin, etConfpin;
	private TextView txtError;
	private Button submit, cancel;

	private String idcus, oldpin, newpin, confpin;

	private StrictMode.ThreadPolicy policy;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepin);
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		mManager = new UserManager(this);
		idcus = mManager.getID();

		etOldpin = (EditText) findViewById(R.id.oldpin);
		etNewpin = (EditText) findViewById(R.id.newpin);
		etConfpin = (EditText) findViewById(R.id.confnewpin);
		submit = (Button) findViewById(R.id.btn_submit);
		cancel = (Button) findViewById(R.id.btn_cancel);
		txtError = (TextView) findViewById(R.id.error);
		txtError.setVisibility(View.INVISIBLE);

		etConfpin.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					submitPin();
				}
				return false;
			}
		});

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitPin();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				isCancel = true;
				finish();
			}
		});
	}

	public boolean checkPin() {
		if (oldpin.equals("") || newpin.equals("") || confpin.equals("")) {
			txtError.setText("*�ô�����������ú��ǹ");
			txtError.setVisibility(View.VISIBLE);
		} else if (!(oldpin.equals(mManager.getPin().toString()))) {
			txtError.setText("*���ʼ�ҹ���١��ͧ");
			txtError.setVisibility(View.VISIBLE);
		} else if (!(newpin.equals(confpin))) {
			txtError.setText("*���ʼ�ҹ�������ç�ѹ");
			txtError.setVisibility(View.VISIBLE);
		} else {
			return true;
		}
		return false;
	}

	public void submitPin() {
		oldpin = etOldpin.getText().toString();
		newpin = etNewpin.getText().toString();
		confpin = etConfpin.getText().toString();

		if (checkPin()) {
			mManager.changepass(oldpin, newpin);
			mManager.setPin(newpin);
			txtError.setVisibility(View.INVISIBLE);
			etOldpin.setText("");
			etNewpin.setText("");
			etConfpin.setText("");
			Toast.makeText(getApplicationContext(),
					"����¹���ʼ�ҹ���º��������", Toast.LENGTH_LONG).show();
			isCancel = true;
			finish();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isCancel) {
			appStatus.onPause();
		}
	}

	@Override
	public void onBackPressed() {
		isCancel = true;
		super.onBackPressed();
	}
}
