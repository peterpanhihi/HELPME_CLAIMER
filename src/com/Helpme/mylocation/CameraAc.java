package com.Helpme.mylocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import com.Helpme.mylocation.http.HttpFileUpload;
import com.Helpme.mylocation.http.OKHttp;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraAc extends Activity {
	private static final String TAG = "CallCamera";
	private int TAKE_PHOTO_CODE = 0;
	public static int count = 0;
	private ImageView imageview1, imageview2, imageview3;
	private File file, file2, file3;
	private EditText detail;
	private Uri fileUri = null;
	private InputStream is = null;
	private String detail2, id, status, result, line;
	private double lng, lat;
	private int picture;
	private OKHttp okHttp;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameralayout);

		okHttp = new OKHttp();

		Intent get = getIntent();
		id = get.getStringExtra("CusID").toString();
		lat = get.getDoubleExtra("lati", lat);
		lng = get.getDoubleExtra("long", lng);
		status = get.getStringExtra("Status").toString();

		imageview1 = (ImageView) findViewById(R.id.imageView1);
		imageview2 = (ImageView) findViewById(R.id.imageView2);
		imageview3 = (ImageView) findViewById(R.id.imageView3);
		detail = (EditText) findViewById(R.id.multiAutoCompleteTextView1);
		detail2 = detail.getText().toString();
		imageview2.setVisibility(View.INVISIBLE);
		imageview3.setVisibility(View.INVISIBLE);

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		file = getOutputPhotoFile();
		fileUri = Uri.fromFile(file);
		picture = 1;
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

		imageview2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				file2 = getOutputPhotoFile();
				fileUri = Uri.fromFile(file2);
				picture = 2;
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
			}
		});
		imageview3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				file3 = getOutputPhotoFile();
				fileUri = Uri.fromFile(file3);
				picture = 3;
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
			}
		});
		Button sendpic = (Button) findViewById(R.id.button1);
		sendpic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				send();
				if (picture == 1) {
					UploadFile(file);
				} else if (picture == 2) {
					UploadFile(file);
					UploadFile(file2);
				} else if (picture == 3) {
					UploadFile(file);
					UploadFile(file2);
					UploadFile(file3);
				}
				finish();
			}
		});

	}

	void send() {

		String lati = String.valueOf(lat);
		String longti = String.valueOf(lng);

		RequestBody formBody = new FormEncodingBuilder().add("customer_id", id)
				.add("lati", lati).add("longti", longti).add("Text", longti)
				.add("Status", status).build();

		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login.nameHost + "insert.php", formBody));
			String code1 = (json_data.getString("code"));
			if (code1.equals("1")) {
				Log.e("Insert", "Inserted Successfully");
			} else {
				Log.e("Insert", "Sorry Try Again");

			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}

	private File getOutputPhotoFile() {
		File directory = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				getPackageName());
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				Log.e(TAG, "Failed to create storage directory.");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK)
				.format(new Date());
		return new File(directory.getPath() + File.separator + "IMG_"
				+ timeStamp + ".jpg");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PHOTO_CODE) {
			if (resultCode == RESULT_OK) {
				Uri photoUri = null;
				if (data == null) {
					// A known bug here! The image should have saved in fileUri
					Toast.makeText(this, "Image saved successfully",
							Toast.LENGTH_LONG).show();
					photoUri = fileUri;
				} else {
					photoUri = data.getData();
					Toast.makeText(this,
							"Image saved successfully in: " + data.getData(),
							Toast.LENGTH_LONG).show();
				}
				if (picture == 1) {
					imageview1.setImageDrawable(showPhoto(photoUri));
					imageview2.setVisibility(View.VISIBLE);
				} else if (picture == 2) {
					imageview2.setImageDrawable(showPhoto(photoUri));
					imageview3.setVisibility(View.VISIBLE);
				} else if (picture == 3) {
					imageview3.setImageDrawable(showPhoto(photoUri));
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Callout for image capture failed!",
						Toast.LENGTH_LONG).show();
			}
			Log.d("CameraDemo", "Pic saved");
		}

	}

	public void UploadFile(File f) {
		try {
			// Set your file path here
			FileInputStream fstrm = new FileInputStream(f);

			// Set your server page url (and the file title/description)
			HttpFileUpload hfu = new HttpFileUpload(Login.nameHost
					+ "insertpicture.php", "my file title", detail2);

			hfu.Send_Now(fstrm, id);

		} catch (FileNotFoundException e) {
			// Error: File not found
		}
	}

	private BitmapDrawable showPhoto(Uri photoUri) {
		/*
		 * Bitmap bitmap; BitmapDrawable drawable = null; String filePath =
		 * photoUri.getEncodedPath(); File imageFile = new File(filePath); if
		 * (imageFile.exists()){
		 * //((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();
		 * bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
		 * drawable = new BitmapDrawable(this.getResources(),
		 * Bitmap.createScaledBitmap(bitmap, 200, 250, true));
		 * 
		 * } return drawable;
		 */
		Bitmap bitmap;
		BitmapDrawable drawable = null;
		String filePath = photoUri.getEncodedPath();
		int height = 200;
		int width = 150;
		File imageFile = new File(filePath);
		if (imageFile.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, width, height);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),
					options);
			drawable = new BitmapDrawable(this.getResources(), bitmap);
		}
		return drawable;
	}

	public int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}
