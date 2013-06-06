package com.suterastudio.drypers;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.android.helpers.ApplicationHelper;
import com.suterastudio.drypers.data.Award;
import com.suterastudio.drypers.data.AwardHandler;
import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.Redemption;
import com.suterastudio.drypers.data.RedemptionHandler;
import com.suterastudio.drypers.network.SessionHelper;

public class RedemptionStartActivity extends GenericActivity implements
		AwardHandler, RedemptionHandler, BabblerHandler {
	Context context = this;
	List<Award> allAwards;
	ScrollView giftsScroller;
	LinearLayout giftsContainer;
	LinearLayout giftsContainer2;
	AlertDialog redemptionAlert;
	ProgressBar progressBar;
	private boolean isRedeeming;

	// pointers
	Drawable drawPointer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.redemption_main);

		createViews();
	}

	private void createViews() {

		updateBabblerInfo();

		// set the default value
		isRedeeming = false;

		// Bindings
		// Title
		final TextView titleText = (TextView) findViewById(R.id.txt_title);
		// Info line 1
		final TextView infoText1 = (TextView) findViewById(R.id.info1);
		// Info line 2
		final TextView infoText2 = (TextView) findViewById(R.id.info2);
		// Back Button
		final TextView backBtn = (TextView) findViewById(R.id.back_redemption);
		// Menu Button
		final TextView menuBtn = (TextView) findViewById(R.id.menu_redemption);
		// Set containers
		giftsScroller = (ScrollView) findViewById(R.id.line_o_giftbox);
		giftsContainer = (LinearLayout) findViewById(R.id.gifts_container);
		giftsContainer2 = (LinearLayout) findViewById(R.id.gifts_container2);
		// Set ProgressBar
		progressBar = (ProgressBar) findViewById(R.id.getting_redemptions_progress);
		// Set fonts
		titleText.setTypeface(font);
		infoText1.setTypeface(font);
		infoText2.setTypeface(font);

		// Get Possible Awards
		populateGifts();

		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		menuBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RedemptionStartActivity.this,
						MenuCreatorActivity.class));
			}
		});
	}

	public void getPoints(final TextView textView) {
		try {
			// textView.setText(DrypersResources.getBabbler().points);
			double parsedPoints = Double.parseDouble(DrypersResources
					.getBabbler().points);
			textView.setText(NumberFormat.getNumberInstance(Locale.US).format(
					parsedPoints));
		} catch (Exception e) {
			Toast.makeText(this, "Are you connected and logged in?",
					Toast.LENGTH_LONG).show();
		}
	}

	public void populateGifts() {
		// get the awards
		try {
			SessionHelper.GetAwards(RedemptionStartActivity.this);
			progressBar.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			Toast.makeText(this, "Are you connected and logged in?",
					Toast.LENGTH_LONG).show();
		}
	}

	public void updateBabblerInfo() {
		// alert downloading... msg
		triggerProgressDialog("Update", "Getting latest info..");
		// update babbler info
		try {
			if (DrypersResources.getBabbler() != null) {
				SessionHelper.LookupBabbler(
						DrypersResources.getBabbler().email,
						RedemptionStartActivity.this);
			} else {
				Toast.makeText(context, "Connection Failure", Toast.LENGTH_LONG)
				.show();
				startActivity(new Intent(RedemptionStartActivity.this,
						LoginMasterActivity.class));
				finish();
				
			}
		} catch (DrypersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createAwards(Integer index, LinearLayout container, Award award) {
		// Create Single Gift LL to insert in ScrollView
		View thisAward = LayoutInflater.from(this).inflate(
				R.layout.redemption_one_gift, null);
		// Set layout params here
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		thisAward.setLayoutParams(lp);
		// Add the view
		container.addView(thisAward);
		// Bind view elements
		final ImageView giftImage = (ImageView) thisAward
				.findViewById(R.id.img_gift_item);
		final Button giftButton = (Button) thisAward
				.findViewById(R.id.btn_view);

		// finalised copy of award
		final Award mAward = award;
		// Set gift image
		if (mAward.image_url != null && giftImage != null) {
			new DownloadImageTask(giftImage).execute(SessionHelper.hostUrl
					+ award.image_url);
		}
		if (mAward.id != null && giftButton != null) {
			// Set Button Action
			giftButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					drawPointer = giftImage.getDrawable();
					confirmDialog(mAward);
				}

			});
		}
	}

	public void createMyGifts(Integer index, LinearLayout container) {
		// Create Single Gift LL to insert in ScrollView
		View thisAward = LayoutInflater.from(this).inflate(
				R.layout.redemption_one_gift, null);
		// Set layout params here
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		thisAward.setLayoutParams(lp);
		// Add the view
		container.addView(thisAward);
		// Bind view elements
		final ImageView giftImage = (ImageView) thisAward
				.findViewById(R.id.img_gift_item);
		final Button giftButton = (Button) thisAward
				.findViewById(R.id.btn_view);

		// Set gift image
		giftImage.setImageResource(R.drawable.mygift);
		giftButton.setBackgroundResource(R.drawable.abtn_mygifts);

		// Set Button Action
		giftButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connectionDialog( RedemptionMyGiftsActivity.class);
			}

		});
	}

	public void confirmDialog(Award award) {
		Dialog confirmDialog = new Dialog(RedemptionStartActivity.this,
				R.style.MyActivityDialogTheme);
		confirmDialog.setTitle("Claim " + award.title + "?");
		setupConfirmDialog(confirmDialog, award);
		confirmDialog.show();
	}

	public void setupConfirmDialog(final Dialog dialog, final Award award) {
		// Create inflatable programmatic layout for dialog
		View dialogLayout = LayoutInflater.from(this).inflate(
				R.layout.redemption_view_gift, null);

		// Set layout params here
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		dialogLayout.setLayoutParams(lp);
		// Add the view
		// container.addView(dialogLayout);
		// Set the dialog content view
		dialog.setContentView(dialogLayout);
		// Element bindings
		ImageView productPhoto = (ImageView) dialogLayout
				.findViewById(R.id.img_product);
		TextView productDesc = (TextView) dialogLayout
				.findViewById(R.id.txt_product);
		TextView points = (TextView) dialogLayout.findViewById(R.id.txt_points);
		Button redeemBtn = (Button) dialogLayout.findViewById(R.id.btn_redeem);
		Button againBtn = (Button) dialogLayout.findViewById(R.id.btn_again);
		
		productDesc.setTypeface(font);
		points.setTypeface(font);
		
		// Add live photo
		// check for null images and not assign null images!
		if (!award.image_url.equals("null")) {
			// resuse the downloaded image on the gift listing
			productPhoto.setAdjustViewBounds(true);
			productPhoto.setImageDrawable(drawPointer);
		}

		// add live product Description
		String description = "You've chosen to redeem: ";
		if (award.title != null) {
			description += award.title;
		}
		productDesc.setText(description);
		// add live points
		if (award.points != null) {
			points.setText(award.points);
		}
		// Add click listener for redeem button
		redeemBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isRedeeming) {
					isRedeeming = true;
					if (attemptPurchase(award)) {
						dialog.dismiss();
					} else {
						isRedeeming = false;
						Toast.makeText(context, "Insufficient points!",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context,
							"Currently redeeming other gift...",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
		// Add click listener for again button
		againBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}

		});
	}

	private boolean attemptPurchase(Award award) {
		int myPoints = Integer.parseInt(DrypersResources.getBabbler().points);
		int claimPoints = Integer.parseInt(award.points);
		if (myPoints > claimPoints) {
			// delete points for claimed product
			connectionDialog( award.title, award.points);

			triggerProgressDialog("Claiming Gift", "Please hold..");
			SessionHelper.UploadRedemption(award.id,
					RedemptionStartActivity.this);
			return true;
		}
		return false;
	}

	@Override
	public void onRedemptionUploaded(Redemption redemption) {
		// dismiss progress dialog
		dismissProgressDialog();
		// reset boolean
		isRedeeming = false;

		// feedback dialog
		redemptionAlert = new AlertDialog.Builder(RedemptionStartActivity.this)
				.create();
		redemptionAlert.setTitle("Thank You");
		redemptionAlert
				.setMessage("Your redemption is being processed.\n" +
						"For details and confirmation please check the email " +
						"you used to sign up for this application. You can also " +
						"email babybabble@drypersmalaysia.com for help or enquiries.");
		redemptionAlert.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						redemptionAlert.dismiss();
					}
				});
		redemptionAlert.show();
	}

	@Override
	public void onMyRedemptions(List<Redemption> redemption) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAwards(List<Award> awards) {
		// create the mygift icon 1st
		createMyGifts(0, giftsContainer);

		// create gifts
		allAwards = awards;
		int i = 1;
		for (Award award : allAwards) {
			if (ApplicationHelper.isEven(i)) {
				createAwards(i++, giftsContainer, award);
			} else {
				createAwards(i++, giftsContainer2, award);
			}
		}
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onException(DrypersException exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgress(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLogin(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLogout(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProfile(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLookup(Babbler babbler) {
		DrypersResources.setBabbler(babbler);

		// Get User's Points
		// Points total
		final TextView pointsText = (TextView) findViewById(R.id.txt_points);
		getPoints(pointsText);
		pointsText.setKeyListener(null);

		dismissProgressDialog();
	}

	@Override
	public void onUpdate(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegister(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	// class to download images and update the imageView async-ly
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

}
