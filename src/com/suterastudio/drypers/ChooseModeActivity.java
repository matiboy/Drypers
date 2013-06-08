package com.suterastudio.drypers;

import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper.Gender;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ChooseModeActivity extends Activity {
	private Button mBackButton;
	private Button mMenuButton;
	private Gender mGender;
	private OnClickListener mMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
				startActivity(new Intent(ChooseModeActivity.this,
						MenuCreatorActivity.class));
		}
	};
	private OnClickListener mBackClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
				finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.choose_mode);
		createViews();
	}

	private void createViews() {
		// Element Bindings
				mBackButton = (Button) findViewById(R.id.back_recording);
				mMenuButton = (Button) findViewById(R.id.menu_recording);
				ImageView babyHead = (ImageView) findViewById(R.id.male_baby);
				ImageView babyGender = (ImageView) findViewById(R.id.head_final);
				mGender = DrypersResources.ThawGender(this);
				if (mGender == Gender.BOY) {
					babyGender.setBackgroundResource(R.drawable.baby_whead);
				} else {
					babyGender.setBackgroundResource(R.drawable.babygirl2);
				}

				if (DrypersResources.BabyHead.exists()) {
					Bitmap savedHead = (Bitmap) BitmapFactory
							.decodeFile(DrypersResources.BabyHead.getAbsolutePath());
					Drawable headDrawable = new BitmapDrawable(getResources(),
							savedHead);
					babyHead.setBackgroundDrawable(headDrawable);
					babyHead.bringToFront();
				}
				// Button Click Listeners
				mBackButton.setOnClickListener(mBackClickListener);
				mMenuButton.setOnClickListener(mMenuClickListener);
				
				
				findViewById(R.id.button_babble).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity(new Intent(ChooseModeActivity.this, RecordingActivity.class));
						
					}
				});
				findViewById(R.id.button_learn).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity(new Intent(ChooseModeActivity.this, ChooseLanguageActivity.class));
							
					}
				});
		
	}
}
