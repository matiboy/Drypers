package com.suterastudio.drypers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper;

public class LoginNonFBActivity extends GenericActivity implements
		BabblerHandler {
	private final Context context = this;
	private String mEmail;
	private String mPassword;
	private BroadcastReceiver recieveeStart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login_normal);

		createViews();
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
	public void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(recieveeStart);
		}catch(RuntimeException er){
			Log.e(getCallingPackage(), er.getMessage());
		}
	}

	private void createViews() {
		// Bindings
		final Button fbBtn = (Button) findViewById(R.id.btn_fb);
		final Button loginBtn = (Button) findViewById(R.id.btn_login);
		final Button backBtn = (Button) findViewById(R.id.back_babblebox);
		final EditText nameField = (EditText) findViewById(R.id.txt_email);
		final EditText passField = (EditText) findViewById(R.id.txt_password);

		// User Menu Buttons
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginNonFBActivity.this,
						LoginMasterActivity.class));
				
				finish();
			}
		});

		// Startup Buttons
		fbBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Ensure user isn't redundantly logging in
				if (DrypersResources.getBabbler() != null) {
					startActivity(new Intent(LoginNonFBActivity.this,
							BabbleActivity.class));
					
					finish();
				} else {
					startActivity(new Intent(LoginNonFBActivity.this,
							LoginActivity.class));
					
					finish();
				}
			}
		});

		loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// bindings
				final EditText txtEmail = (EditText) findViewById(R.id.txt_email);
				final EditText txtPassword = (EditText) findViewById(R.id.txt_password);

				// Live Attributes
				mEmail = txtEmail.getText().toString();
				mPassword = txtPassword.getText().toString();

				// Perform the Post
				SessionHelper.Login(mEmail, mPassword,
						LoginNonFBActivity.this);
			}
		});

		nameField.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				nameField.setText("");
				nameField.setOnTouchListener(null);
				return false;
			}
		});
		
		passField.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				passField.setText("");
				passField.setOnTouchListener(null);
				return false;
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
		if (babbler != null) {
			DrypersResources.setBabbler(babbler);
			DrypersResources.Freeze(this);
			
			//
			Toast.makeText(context, "Fantastic! You're all logged in.",
					Toast.LENGTH_LONG).show();

			startActivity(new Intent(LoginNonFBActivity.this, BabbleActivity.class));	
			
			finish();
		}
	}

	@Override
	public void onLogout(Babbler babbler) {
	}

	@Override
	public void onProfile(Babbler babbler) {
	}

	@Override
	public void onUpdate(Babbler babbler) {
	}

	@Override
	public void onRegister(Babbler babbler) {
	}

	@Override
	public void onLookup(Babbler babbler) {
	}
}
