package com.suterastudio.drypers;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.drypers.data.Redemption;
import com.suterastudio.drypers.data.RedemptionHandler;
import com.suterastudio.drypers.network.SessionHelper;

public class RedemptionMyGiftsActivity extends GenericActivity implements RedemptionHandler{
	Context context = this;
	private LinearLayout redemptionContainer;

	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private DateFormat df2 = new SimpleDateFormat("dd MMM yyyy");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.redemption_my_gifts);
		
		createViews();
	}
	
	private void createViews() {
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
		redemptionContainer = (LinearLayout) findViewById(R.id.Redemption_Listings);
		// Set fonts
		titleText.setTypeface(font);
		infoText1.setTypeface(font);
		infoText2.setTypeface(font);
		
		//get listing
		getMyListing();
		
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		menuBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(RedemptionMyGiftsActivity.this,
						MenuCreatorActivity.class));
			}
		});
	}
	
	public void getMyListing(){
		// alert downloading... msg
		triggerProgressDialog("Loading", "Getting your redemptions..");
		SessionHelper.GetMyRedemptions(RedemptionMyGiftsActivity.this);
	}
	
	public void createRedemption(Integer index, Redemption redemption) {
		// Create Single Gift LL to insert in ScrollView
		View thisRedemption = LayoutInflater.from(this).inflate(
				R.layout.redemption_one_status, null);
		
		// Set layout params here
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		lp.setMargins(0, 0, 0, 3);
		thisRedemption.setLayoutParams(lp);
		// Add the view
		redemptionContainer.addView(thisRedemption);
		
		// Bind view elements
		final ImageView itemImage = (ImageView) thisRedemption
				.findViewById(R.id.img_gift_item);
		final TextView itemName = (TextView) thisRedemption
				.findViewById(R.id.status_item_name);
		final TextView itemDate = (TextView) thisRedemption
				.findViewById(R.id.status_date);
		final ImageView giftButton = (ImageView) thisRedemption.findViewById(R.id.status_btn);
		
		itemName.setTypeface(font);
		itemDate.setTypeface(font);
		
		//set the product listing
		//new DownloadImageTask(itemImage).execute(SessionHelper.hostUrl
		//		+ redemption.image_url);
		itemName.setText(redemption.award_title);
		
		//date format
		String date = "";
		try {
			  date = df2.format(df1.parse(redemption.created_at));
		}
		catch (java.text.ParseException e) {
				e.printStackTrace();
		}
		itemDate.setText(date);
		//itemDate.setText(redemption.created_at.split("T")[0]);
		
		
		if(redemption.approved.equals("1")){
			giftButton.setImageResource(R.drawable.success);
		}
		else{
			giftButton.setImageResource(R.drawable.pending);
		}
	}

	@Override
	public void onRedemptionUploaded(Redemption redemption) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMyRedemptions(List<Redemption> redemption) {
		
		if(redemption != null)
		{
			//create redemptions
			int i = 0;
			for (Redemption listMe : redemption) {
					createRedemption(i++, listMe);
			}
		}
		if (redemption.size() == 0){
			Toast.makeText(context,
					"You have no redemptions.", Toast.LENGTH_LONG).show();
		}
		dismissProgressDialog();
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
