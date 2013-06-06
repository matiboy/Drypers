package com.suterastudio.drypers;

import junit.framework.Assert;

import org.customsoft.stateless4j.StateMachine;
import org.customsoft.stateless4j.delegates.Action;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper.Gender;

public class GenderActivity extends GenericActivity {
	private StateMachine<State, Trigger> machine;

	private enum State {
		IDLING, PREPARING
	}

	private enum Trigger {
		IDLE, PREPARED
	}

	// Class constructor
	public GenderActivity() {
		machine = new StateMachine<State, Trigger>(State.IDLING);

		Action idler = new Action() {
			@Override
			public void doIt() {
			}
		};

		Action preparer = new Action() {
			@Override
			public void doIt() {
			}
		};

		try {
			machine.Configure(State.IDLING).OnEntry(idler)
					.Permit(Trigger.PREPARED, State.PREPARING);
			machine.Configure(State.PREPARING).OnEntry(preparer)
					.Permit(Trigger.IDLE, State.IDLING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set layout
		setContentView(R.layout.gender);

		createViews();
	}

	private void createViews() {
		// Bindings
		final ImageView babyHead = (ImageView) findViewById(R.id.gender_headshot);
		final Button imaboy = (Button) findViewById(R.id.gender_boybtn);
		final Button imagirl = (Button) findViewById(R.id.gender_girlbtn);
		final Button backbtn = (Button) findViewById(R.id.back_gender);
		final Button menubtn = (Button) findViewById(R.id.menu_gender);

		final TextView title = (TextView) findViewById(R.id.gender_title);
		
		babyPhotoCheck();
		
		// babble font
		title.setTypeface(font);

		if (machine.IsInState(State.PREPARING)) {
			Bitmap savedHead = (Bitmap) BitmapFactory
					.decodeFile(DrypersResources.BabyHead.getAbsolutePath());
			Drawable headDrawable = new BitmapDrawable(getResources(),
					savedHead);
			babyHead.setBackgroundDrawable(headDrawable);
		}

		backbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		menubtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(GenderActivity.this,
						MenuCreatorActivity.class));
			}
		});

		// Listen for gender btn
		imaboy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// setBabbleGender(Gender.BOY.toString());
				DrypersResources.FreezeGender(Gender.BOY.toString(), context);
				startActivity(new Intent(GenderActivity.this,
						RecordingActivity.class));
			}

		});

		// Listen for Skip Btn
		imagirl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// setBabbleGender(Gender.GIRL.toString());
				DrypersResources.FreezeGender(Gender.GIRL.toString(), context);
				startActivity(new Intent(GenderActivity.this,
						RecordingActivity.class));
			}
		});
	}

	public void babyPhotoCheck() {
		if (DrypersResources.BabyHead.exists()
				&& machine.CanFire(Trigger.PREPARED)) {
			try {
				machine.Fire(Trigger.PREPARED);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Assert.assertEquals(State.PREPARING, machine.getState());
		}
	}
}
