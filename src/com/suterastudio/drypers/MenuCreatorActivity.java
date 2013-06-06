package com.suterastudio.drypers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;

public class MenuCreatorActivity extends GenericActivity implements
		BabblerHandler, OnTouchListener {
	View dialogPointer = null;
	Activity thisActivity = this;
	Context context = this;
	Rect rectf = new Rect();
	Integer rectLeft = 0;
	Integer rectRight = 0;
	Integer rectTop = 0;
	Integer rectBottom = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_menu);

		createViews();
	}

	private void createViews() {
		// Bindings
		final Button umBB = (Button) findViewById(R.id.um_bb);
		final Button umPro = (Button) findViewById(R.id.um_pro);
		final Button umPoints = (Button) findViewById(R.id.um_points);
		final Button umLogout = (Button) findViewById(R.id.um_logout);
		final Button umDrypers = (Button) findViewById(R.id.um_drypers);
		View dialog = (View) findViewById(R.id.um_r1);
		View fullScreen = (View) findViewById(R.id.main_user_menu);
		dialogPointer = fullScreen;
		dialog.getLocalVisibleRect(rectf);

		// Define the rectangle
		rectLeft = rectf.left;
		rectRight = rectf.right;
		rectTop = rectf.top;
		rectBottom = rectf.bottom;
		fullScreen.setOnTouchListener(this);

		// Create Click Listeners for user menu buttons
		// User Menu Buttons
		umBB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connectionDialog(BabbleBoxActivity.class);

				if (safeToFinish) {
					finish();
				}
			}

		});

		// User Profile Button
		umPro.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connectionDialog(ProfileActivity.class);
			}

		});

		// User Points Button
		umPoints.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(context, "This feature is coming soon.",
				// Toast.LENGTH_LONG).show();
				// finish(); // <-- IMPORTANT!! KILL THIS IF SWITCHING
				// REDEMPTION ON !!
				connectionDialog(RedemptionStartActivity.class);
			}

		});

		// User Logout Button
		umLogout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction("com.package.ACTION_LOGOUT");
				sendBroadcast(broadcastIntent);

				Intent intent = new Intent(context, LoginMasterActivity.class);
				startActivity(intent);

				Toast.makeText(context,
						"You have been successfully logged out.",
						Toast.LENGTH_LONG).show();
				finish();
			}

		});
		// User Drypers Button
		umDrypers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// web browser intent
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.drypers.com.my"));

				connectionDialog(browserIntent);
			}

		});
	}

	@Override
	public boolean onTouch(View view, MotionEvent mev) {
		int width = view.getWidth();
		int height = view.getHeight();
		float xCoord = mev.getX();
		float yCoord = mev.getY();
		if (xCoord > rectLeft && xCoord < rectRight && yCoord > rectTop
				&& yCoord < rectBottom) {
			// NOOP
		} else {
			finish();
		}

		return false;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdate(Babbler babbler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegister(Babbler babbler) {
		// TODO Auto-generated method stub

	}

}
