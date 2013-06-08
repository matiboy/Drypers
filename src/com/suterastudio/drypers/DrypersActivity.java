package com.suterastudio.drypers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.suterastudio.android.media.ImageHelper;
import com.suterastudio.drypers.data.DrypersResources;
import com.tapjoy.TapjoyConnect;

public class DrypersActivity extends GenericActivity {
	private Class TargetActivity;
	private Intent mReferralIntent;
	private Uri mReferralURI;
	private static final String TJ_APP_ID = "83d4d396-b4cb-4034-96cc-cc661090c59b";
	private static final String TJ_APP_SECRET = "I9gnFrhGW3dQTgzvHdyx";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// For debugging purpose, fake a REFERRAL intent
//		fireReferral();
		
		// Get Referral data
		prepareCampaign();
		// Put app in correct state
		prepareApp();
		// Create the loader
		createViews();
	}
	
	// DEBUG only
	private void fireReferral()
	{
		String url = "utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_term%3Dspiral%26utm_content%3DdisplayAd1%26utm_campaign%3Dshoe%252Bcampaign";
		Intent intent = new Intent();
		intent.setAction("com.android.vending.INSTALL_REFERRER");
		intent.putExtra("referrer", url);
		sendBroadcast(intent);
	}

	private void createViews() {
		// TODO See if we can make the funky loader less funky
		final ImageView loader = (ImageView) findViewById(R.id.loading_spinner);

		/** set time to splash out */
		final int welcomeScreenDisplay = 3000;

		/** create a thread to show splash up to splash time */
		Thread welcomeThread = new Thread() {
			@Override
			public void run() {
				try {
					super.run();
					ImageHelper.rotator(DrypersActivity.this, loader, true,
							800, 4000);
					sleep(welcomeScreenDisplay);
				} catch (Exception e) {
					Log.i(this.getClass().toString(), "EXc=" + e);
				} finally {
					runOnUiThread(new Runnable() {
						public void run() {
							startActivity(new Intent(DrypersActivity.this,
									TargetActivity));
							if (TargetActivity == BabbleActivity.class) {
								Toast.makeText(
										context,
										"Logged in, please provide a baby photo.",
										Toast.LENGTH_SHORT).show();
							}
							if (TargetActivity == ChooseModeActivity.class) {
								Toast.makeText(
										context,
										"Logged in using registered account...",
										Toast.LENGTH_SHORT).show();
							}
							finish();
						}
					});
				}
			}
		};
		welcomeThread.start();
	}

	private void prepareApp() {
		// Set up Tapjoy and Flurry
		TapjoyConnect.requestTapjoyConnect(this, TJ_APP_ID, TJ_APP_SECRET);
		DrypersResources.Thaw(this);

		if (DrypersResources.getBabbler() != null) {
			TargetActivity = ChooseModeActivity.class;
			if (!DrypersResources.BabyHead.exists()) {
				TargetActivity = BabbleActivity.class;
			}
		} else {
			TargetActivity = LoginMasterActivity.class;
		}

	}

	private void prepareCampaign() {
		mReferralIntent = this.getIntent();
		mReferralURI = mReferralIntent.getData();
		
	    // Call setContext() here so that we can access EasyTracker
	    // to update campaign information before activityStart() is called.
		EasyTracker.getInstance().setContext(this);

		if (mReferralURI != null) {
			// Use campaign parameters if available.
			if (mReferralURI.getQueryParameter("utm_source") != null) {
				EasyTracker.getTracker().setCampaign(mReferralURI.getPath());
				// Otherwise, try to find a referrer parameter.
			} else if (mReferralURI.getQueryParameter("referrer") != null) {
				EasyTracker.getTracker().setReferrer(
						mReferralURI.getQueryParameter("referrer"));
			}
		}
	}

}