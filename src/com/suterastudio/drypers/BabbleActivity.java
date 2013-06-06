package com.suterastudio.drypers;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.android.media.ImageHelper;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;
import com.suterastudio.drypers.ui.BabyHeadView;

public class BabbleActivity extends GenericActivity {
	private String mFileName;
	private BabyHeadView babyHeadView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.babble);

		createViews();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("com.package.KillStarter");
		sendBroadcast(broadcastIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case R.integer.capture_req:
			if (data != null) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				ImageHelper.saveImage(photo, mFileName);

				// rotate the photo since it's bugged...
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(),
						photo.getHeight(), matrix, true);

				babyHeadView.setBabyHead(photo, R.drawable.baby_mask);
			}
			break;

		case R.integer.gallery_req:
			if (data != null) {
				Uri selectedImageURI = data.getData();
				File imageFile = new File(ContextHelper.getRealPathFromURI(
						getContentResolver(), selectedImageURI));
				String stringedFile = (String) imageFile.toString();
				Bitmap babyBitmap = (Bitmap) BitmapFactory.decodeFile(stringedFile);
				babyHeadView.setBabyHead(babyBitmap, R.drawable.baby_mask);
			}
			break;
		}
	}

	private void createViews() {
		babyHeadView = (BabyHeadView) findViewById(R.id.baby_head);

		// Bind buttons
		Button choosePhoto = (Button) findViewById(R.id.choose_photo);
		Button takePhoto = (Button) findViewById(R.id.take_photo);
		Button saveBtn = (Button) findViewById(R.id.save_pic);
		Button skipBtn = (Button) findViewById(R.id.skip_pic);
		
		//set the default baby pic
//		Bitmap babyBitmap = (Bitmap) BitmapFactory.decodeResource(getResources(), R.drawable.default_babyface);
//		babyHeadView.setBabyHead(babyBitmap, R.drawable.baby_mask);

		// Listen for Gallery Selection
		choosePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("image/*");
				startActivityForResult(i, R.integer.gallery_req);
			}
		});

		// Listen for Camera selection
		takePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, R.integer.capture_req);
			}
		});

		// Listen for Save btn
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap resizedHead = ImageHelper.getRoundedShape(ImageHelper
						.getResizedImage((BabyHeadView) findViewById(R.id.baby_head)));
				ImageHelper.saveImage(resizedHead, DrypersResources.BabyHead.getAbsolutePath());
				
				//award points with connection check
				connectionDialog( ScoreType.PHOTO);
				
				startActivity(new Intent(BabbleActivity.this,
						GenderActivity.class));
				finish();
			}
		});

		// Listen for Skip Btn
		skipBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(DrypersResources.BabyHead.exists()){
					startActivity(new Intent(BabbleActivity.this, GenderActivity.class));
					finish();
				}
				else {
					//create blank image
					Bitmap resizedHead = ImageHelper.getRoundedShape(ImageHelper
							.getResizedImage((BabyHeadView) findViewById(R.id.baby_head)));
					ImageHelper.saveImage(resizedHead, DrypersResources.BabyHead.getAbsolutePath());
					
					startActivity(new Intent(BabbleActivity.this, GenderActivity.class));
					finish();
				}
			}
		});
	}
}