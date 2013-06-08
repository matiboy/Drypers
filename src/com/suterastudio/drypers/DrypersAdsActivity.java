package com.suterastudio.drypers;

import com.suterastudio.android.helpers.ContextHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class DrypersAdsActivity extends Activity {
	private Button mBackButton;
	private Button mMenuButton;
	private ImageView mMainImage;
	private Tab[] mTabs;
	
	private OnClickListener mMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(DrypersAdsActivity.this,
					MenuCreatorActivity.class));
		}
	};
	private OnClickListener mBackClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnClickListener mMainImageClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			for(Tab view : mTabs)
			{
				if (view.isActive())
				{
					// web browser intent
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(view.getLink()));
					startActivity(i);
				}
				
			}
		}
	};

	private OnClickListener mButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			for(Tab view : mTabs)
			{
				if (view.getId()==v.getId())
				{
					view.setActive(true);
					mMainImage.setImageResource(view.getMainImage());
				}
				else
				{
					view.setActive(false);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.drypers_ads);
		createViews();
	}

	private void createViews() {
		// Element Bindings
		mBackButton = (Button) findViewById(R.id.back_recording);
		mMenuButton = (Button) findViewById(R.id.menu_recording);
		mMainImage  = (ImageView) findViewById(R.id.dypers_image_main);
		// Button Click Listeners
		mBackButton.setOnClickListener(mBackClickListener);
		mMenuButton.setOnClickListener(mMenuClickListener);
		mMainImage.setOnClickListener(mMainImageClickListener);
		
		mTabs = new Tab[] {
				new Tab(R.id.drypers_btn_facebook,
						R.drawable.drypers_facebook_active,
						R.drawable.drypers_facebook_inactive,
						R.drawable.drypers_facebook, mButtonClickListener,
						"http://www.drypers.com.my"),
				new Tab(R.id.drypers_btn_happyfamily,
						R.drawable.drypers_happyfamily_active,
						R.drawable.drypers_happyfamily_inactive,
						R.drawable.drypers_happyfamilt, mButtonClickListener,
						"http://www.drypers.com.my"),
				new Tab(R.id.drypers_btn_promo,
						R.drawable.drypers_promo_active,
						R.drawable.drypers_promo_inactive,
						R.drawable.drypers_promo, mButtonClickListener,
						"http://www.drypers.com.my"),
				new Tab(R.id.drypers_btn_web, R.drawable.drypers_web_active,
						R.drawable.drypers_web_inactive,
						R.drawable.drypers_web, mButtonClickListener,
						"http://www.drypers.com.my") };
		mTabs[3].setActive(true);
	}
	private class Tab
	{
		private Button button;
		private int activeImageResource;
		private int inactiveImageResource;
		private int mainImageResource;
		private boolean active;
		private String link;
		
		public boolean isActive()
		{
			return active;
		}
		
		public String getLink() {
			return link;
		}

		public int getMainImage()
		{
			return mainImageResource;
		}
		
		public int getId() {
			return button.getId();
		}
		
		public Tab(int buttonId, int activeImageResource,
				int inactiveImageResource, int mainImageResource,View.OnClickListener listener,String link) {
			super();
			this.button =(Button) findViewById(buttonId);
			this.activeImageResource = activeImageResource;
			this.inactiveImageResource = inactiveImageResource;
			this.mainImageResource = mainImageResource;
			this.link=link;
			active=false;
			button.setOnClickListener(listener);
		}
		
		public void setActive(boolean active)
		{
			if (active)
				button.setBackgroundResource(activeImageResource);
			else 
				button.setBackgroundResource(inactiveImageResource);
			this.active=active;
		}
		
		
	}
}
