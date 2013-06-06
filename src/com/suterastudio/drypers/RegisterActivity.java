package com.suterastudio.drypers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.customsoft.stateless4j.StateMachine;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.BabblerHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper;

public class RegisterActivity extends FacebookActivity implements
		BabblerHandler {
	private String mEmail;
	private String mPassword;
	private CheckBox agree;
	private BroadcastReceiver recieveeStart;
	
	private StateMachine<State, Trigger> machine;

	private enum State {
		START, SNIFFING, LOOKUPING, FACEBOOKING, REGISTERING, REDIRECTING, CONTINUING
	}

	private enum Trigger {
		SNIFF, LOOKUP, FACEBOOK, REGISTER, REDIRECT, CONTINUE
	}

	public RegisterActivity() {
//		machine = new StateMachine<State, Trigger>(State.START);
//
//		try {
//			Action lookuper = new Action() {
//				@Override
//				public void doIt() {
//					// alert loading msg
//				}
//			};
//
//			machine.Configure(State.START).Permit(Trigger.LOOKUP, State.LOOKUPING);
//
//			machine.Configure(State.LOOKUPING).OnEntry(lookuper)
//					.Permit(Trigger.FACEBOOK, State.FACEBOOKING)
//					.Permit(Trigger.REGISTER, State.REGISTERING);
//
//			machine.Configure(State.FACEBOOKING)
//				.Permit(Trigger.REGISTER, State.REGISTERING)
//				.Permit(Trigger.REDIRECT, State.REDIRECTING);
//		} catch (Exception e) {
//
//		}		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Call super
		super.onCreate(savedInstanceState);
		// Set layout
		setContentView(R.layout.registration);
		
		// Create views
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
		Log.i(this.getClass().toString(), "Creating registration views");

		// Bindings
		final Button registerBtn = (Button) findViewById(R.id.btn_submit);
		final Button fbBtn = (Button) findViewById(R.id.btn_fb);
		final Button backBtn = (Button) findViewById(R.id.back_babblebox);
		final EditText nameField = (EditText) findViewById(R.id.txt_email);
		final EditText passField = (EditText) findViewById(R.id.txt_password);
		final TextView description = (TextView) findViewById(R.id.lbl_desc);
		final TextView tnc = (TextView) findViewById(R.id.txt_tnc);
		agree = (CheckBox) findViewById(R.id.checkBox_tnc);
		
		description.setTypeface(font);

		// User Menu Buttons
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissProgressDialog();
				
				startActivity(new Intent(RegisterActivity.this,
						LoginMasterActivity.class));
				
				finish();
			}
		});

		// Startup Buttons
		registerBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					
				if(passField.getText().length() < 4){
					Toast.makeText(	RegisterActivity.this,
							"Mininum password length is 4!",
							Toast.LENGTH_SHORT).show();
					
				}
				
				else if(agree.isChecked()){
					// Bindings
					final EditText txtEmail = (EditText) findViewById(R.id.txt_email);
	
					// Live Attributes
					mEmail = txtEmail.getText().toString();
					
					doNormalRegistration();
				} else{
					Toast.makeText(	RegisterActivity.this,
							"Please agree to the Terms and Conditions.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		fbBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(agree.isChecked()){
					doFacebookRegistration();
				}
				else{
					Toast.makeText(	RegisterActivity.this,
							"Please agree to the Terms and Conditions.",
							Toast.LENGTH_SHORT).show();
				}
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
		
		tnc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// web browser intent
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.drypers.com.my/Footer/Privacy-Policy/"));
				
				startActivity(browserIntent);
			}
		});
	}
	
	private void doNormalRegistration() {
		Log.i(this.getClass().toString(), "Doing normal registration");

		triggerProgressDialog("Registering", mEmail);
		
		try {
			SessionHelper.LookupBabbler(mEmail, this);
		} catch (DrypersException e) {
			e.printStackTrace();
			Toast.makeText(RegisterActivity.this, e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}
	
	private void makeAccount(String name, String email, String password,
			String facebook) {
		Log.i(this.getClass().toString(), "Making new account for " + email);

		// Try to register and log in in the user
		SessionHelper.Register(name, email, password, facebook, this);
	}

	@Override
	protected void onUser() {
		fbRegisterDialog();
	}
	
	private void fbRegisterDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
 
			// set title
			alertDialogBuilder.setTitle("Register with Facebook?");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("You seem to be a Facebook User. Do you want to register with Facebook?")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						doNormalRegistration();
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						mUser = null;
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	@Override
	public void onLookup(Babbler babbler) {
		// Check for FB user
		if (mUser != null) {
			// No existing Drypers user
			if (babbler == null) {
				Log.i(this.getClass().toString(), "Making FB account for "
						+ mEmail);

				// Create password from MD5 hash of username
				final String name = mUser.getName();
				final String username = mUser.getUsername();
				
				// Email and Password attributes
				//mEmail = (String) mUser.asMap().get("email"); un-used coz it will fuck shyt up
				if(mEmail == null){
					mEmail = username + "@facebook.com";
				}
				try {
					mPassword = MessageDigest.getInstance("MD5")
							.digest(username.getBytes()).toString();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					mPassword = "fbpassword";
				}

				makeAccount(name, mEmail, mPassword, username);
			// Existing Drypers user
			} else if (babbler != null && DrypersResources.getBabbler() == null) {
				dismissProgressDialog();
				Toast.makeText(
						RegisterActivity.this,
						"Sorry, perhaps you've already registered via Facebook.  Please log in",
						Toast.LENGTH_LONG).show();

				startActivity(new Intent(RegisterActivity.this,
						LoginActivity.class));
			}
		} else {
			if (babbler == null) {
				final EditText txtPassword = (EditText) findViewById(R.id.txt_password);
				mPassword = txtPassword.getText().toString();
				
				Log.i(this.getClass().toString(), "No existing account");
				makeAccount(mEmail, mEmail, mPassword, null);
			} else {
				dismissProgressDialog();
				Toast.makeText(
						RegisterActivity.this,
						"Hrmm... have you registered before? This email address already exists. " +
						"Please try another email address for registration.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onUpdate(Babbler babbler) {
	}

	@Override
	public void onRegister(Babbler babbler) {
		dismissProgressDialog();
		if(babbler != null){
			DrypersResources.setBabbler(babbler);
			DrypersResources.Freeze(this);
			
			// Head on over to babble land
			startActivity(new Intent(RegisterActivity.this, BabbleActivity.class));
			
			finish();
		} else {
			Toast.makeText(
					RegisterActivity.this,
					"Registration failed. Check if details are valid and try again.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onException(DrypersException exception) {
		//Toast.makeText(
				//loginActivity.this,
				//exception.getMessage(),
				//Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProgress(String message) {
	}

	@Override
	public void onLogin(Babbler babbler) {
	}

	@Override
	public void onLogout(Babbler babbler) {
	}

	@Override
	public void onProfile(Babbler babbler) {
	}
}