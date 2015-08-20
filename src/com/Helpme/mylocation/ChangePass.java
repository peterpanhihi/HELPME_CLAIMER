package com.Helpme.mylocation;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import com.Helpme.mylocation.fragment.Setting;
import com.Helpme.mylocation.http.OKHttp;
import com.Helpme.mylocation.model.ApplicationStatus;
import com.Helpme.mylocation.persistence.UserManager;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ChangePass extends Activity {
	private EditText oldpass, newpass, confnewpass;
	private TextView error;
	private Button submit, cancel;
	private UserManager mManager;
	private String idcus, oldpassword, newpassword, confnewpassword;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private OKHttp okHttp;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changpassword);

		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		mManager = new UserManager(this);
		idcus = mManager.getID();
		oldpass = (EditText) findViewById(R.id.oldpass);
		newpass = (EditText) findViewById(R.id.newpass);
		confnewpass = (EditText) findViewById(R.id.confnewpass);
		submit = (Button) findViewById(R.id.submit);
		cancel = (Button) findViewById(R.id.Button01);
		error = (TextView) findViewById(R.id.error);
		error.setVisibility(View.INVISIBLE);

		confnewpass.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					submitPass();
				}
				return false;
			}
		});

		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitPass();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
	}

	public void submitPass() {
		oldpassword = oldpass.getText().toString();
		newpassword = newpass.getText().toString();
		confnewpassword = confnewpass.getText().toString();
		if (oldpassword.equals("") || newpassword.equals("")
				|| confnewpassword.equals("")) {
			error.setText("*�ô�����������ú��ǹ");
			error.setVisibility(View.VISIBLE);
		} else if (!(oldpassword.equals(mManager.getPassword().toString()))) {
			error.setText("*���ʼ�ҹ���١��ͧ");
			error.setVisibility(View.VISIBLE);
		} else if (!(newpassword.equals(confnewpassword))) {
			error.setText("*���ʼ�ҹ�������ç�ѹ");
			error.setVisibility(View.VISIBLE);
		} else {
			mManager.changepass(oldpassword, newpassword);
			send();
			error.setVisibility(View.INVISIBLE);
			oldpass.setText("");
			newpass.setText("");
			confnewpass.setText("");
			Toast.makeText(getApplicationContext(),
					"����¹���ʼ�ҹ���º��������", Toast.LENGTH_LONG).show();
			isCancel = true;
			finish();
		}
	}

	void send() {
		try {
			RequestBody formBody = new FormEncodingBuilder()
					.add("customer_id", idcus).add("newpasswordd", newpassword)
					.build();
			okHttp.POST(Login.nameHost + "cpasswordclaim.php", formBody);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StrictMode.setThreadPolicy(policy);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
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
		if (!isCancel)
			appStatus.onPause();
	}

	@Override
	public void onBackPressed() {
		isCancel = true;
		super.onBackPressed();
	}

}
