package com.suterastudio.drypers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper;

public class LoginActivity extends FacebookActivity implements BabblerHandler {
	private Button mBackBtn;
	private Button mBabbleBtn;
	private BroadcastReceiver recieveeStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Call super
		super.onCreate(savedInstanceState);
	    
		// Set layout
		setContentView(R.layout.login_fb);
		
		// Create buttons with event handlers
		createViews();

		// Time to login
		doFacebookLogin();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		
		/**snip **/
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.package.KillStarter");
	    recieveeStart = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","kill start in progress");
                finish();
            }
        };
	    registerReceiver(recieveeStart, intentFilter);
	    //** snip **//
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(recieveeStart);
		}catch(RuntimeException er){
			Log.e(getCallingPackage(), er.getMessage());
		}
	}
	
	private void createViews() {		
		mBackBtn = (Button) findViewById(R.id.back_babblebox);

		// User Menu Buttons
		mBackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this,
						LoginNonFBActivity.class));
				
				finish();
			}
		});

		// Babble Button Binding and ClickEvent
		mBabbleBtn = (Button) findViewById(R.id.start_babble);

		toggleBabbleButton(false);

		mBabbleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this,
						BabbleActivity.class));
				finish();
			}
		});
	}

	private void doNormalLogin() {
		final String email = mUser.getUsername() + "@facebook.com";

		//triggerProgressDialog("Connecting", email);

		try {
			SessionHelper.LookupBabbler(email, this);
		} catch (final DrypersException e) {
			e.printStackTrace();
			showAlertMessage("Drypers Login Error", e.getMessage());
		}
	}

	private void toggleBabbleButton(final boolean truthiness) {
		runOnUiThread(new Runnable() {
			public void run() {
				mBabbleBtn.setEnabled(truthiness);
			}
		});
	}

	@Override
	public void onException(DrypersException exception) {
		showAlertMessage("Drypers Login Error", exception.getMessage());
	}

	@Override
	public void onProgress(String message) {
	}

	@Override
	public void onLogin(Babbler babbler) {
		if(babbler != null) {
			DrypersResources.setBabbler(babbler);
			DrypersResources.Freeze(this);
			
			toggleBabbleButton(true);
			dismissProgressDialog();
			// tell users login sucess...
			Toast.makeText(context, "Login Success!", Toast.LENGTH_SHORT).show();
		}
		
		
	}

	@Override
	public void onLogout(Babbler babbler) {
	}

	@Override
	public void onProfile(Babbler babbler) {
		if (babbler == null) {
			// Login user via Drypers API
			SessionHelper.Login(mUser.getUsername(), mUser.getUsername()
					+ "@facebook.com", Session.getActiveSession()
					.getAccessToken(), this);
		}
	}

	@Override
	public void onUpdate(Babbler babbler) {
	}

	@Override
	public void onRegister(Babbler babbler) {
	}

	@Override
	public void onLookup(Babbler babbler) {
		if (babbler != null) {
			// Login user via Drypers API
			SessionHelper.Login(mUser.getUsername(), mUser.getUsername()
					+ "@facebook.com", Session.getActiveSession()
					.getAccessToken(), this);

		} else {
			// Head on over to the register activity
			dismissProgressDialog();
			startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
		}

	}

	@Override
	protected void onUser() {
		doNormalLogin();		
	}
}
