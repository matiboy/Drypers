package com.suterastudio.drypers;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public abstract class FacebookActivity extends GenericActivity {	
	private boolean checkedPublishPermissions = false;

	private class FacebookSessionStatusCallback implements
		Session.StatusCallback {
		
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

			// Check if session is null
			if (session == null) {
				Log.i(this.getClass().toString(),
						"Session error...");
				dismissProgressDialog();
				showAlertMessage("Facebook unreachable", "Check Facebook App or internet connection..");
			} 
			
			// Check if session is opened
			if (state == SessionState.OPENED ) {
				Log.i(this.getClass().toString(),
						"Facebook opened session callback");
	
				// Set permissions
				if(checkedPublishPermissions || checkPermissions(session)) {
					// make request to the /me API
					getProfile(session);
				}
			} 
			else if(state == SessionState.OPENED_TOKEN_UPDATED) {	
				Log.i(this.getClass().toString(),
						"Facebook token updated session callback");
	
				// Set permissions
				if(checkedPublishPermissions || checkPermissions(session)) {
					// make request to the /me API
					getProfile(session);
				}
			}
			else if (state == SessionState.CLOSED) {
				dismissProgressDialog();
				FacebookActivity.this.finish();

//				showAlertMessage("Facebook unreachable", "Check Facebook App or internet connection..");
//				Log.i(this.getClass().toString(),
//						"Facebook closed session callback");
			}
			else if(state == SessionState.CLOSED_LOGIN_FAILED) {
				dismissProgressDialog();
				FacebookActivity.this.finish();
			}
	
			if (exception != null) {
//				showAlertMessage("Facebook Error", exception.getMessage());
				exception.printStackTrace();
			}				
		}
	}

	protected FacebookSessionStatusCallback mStatusCallback = new FacebookSessionStatusCallback();
	protected List<String> mReadPermissions = Arrays.asList("email", "user_location", "user_birthday", "user_hometown");
	protected List<String> mPublishPermissions = Arrays.asList("publish_stream");	
	protected UiLifecycleHelper mLifecycleHelper;
	protected GraphUser mUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(this.getClass().toString(), "On create FB activity");

		// Call super
		super.onCreate(savedInstanceState);
		// Create FB lifecycle helper
		mLifecycleHelper = new UiLifecycleHelper(this, mStatusCallback);
		
		// Call FB onCreate
		mLifecycleHelper.onCreate(savedInstanceState);		
	}

	// Request permissions from FB
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(this.getClass().toString(), "On activity result FB activity");

		super.onActivityResult(requestCode, resultCode, data);
		
		try {
			mLifecycleHelper.onActivityResult(requestCode, resultCode, data);
		} catch(FacebookOperationCanceledException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		Log.i(this.getClass().toString(), "On pause FB activity");

		super.onPause();		
		mLifecycleHelper.onPause();
	}

	@Override
	protected void onResume() {
		Log.i(this.getClass().toString(), "On resume FB activity");

		super.onResume();
		mLifecycleHelper.onResume();	
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(this.getClass().toString(), "On save instance state FB activity");

		super.onSaveInstanceState(outState);		
		mLifecycleHelper.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onDestroy() {
		Log.i(this.getClass().toString(), "On destroy FB activity");

		super.onDestroy();
		mLifecycleHelper.onDestroy();		
	}
	
	private boolean checkPermissions(Session session) {
		Log.i(this.getClass().toString(), "Checking FB permissions");

		try {
			
			List<String> readPermissions = mReadPermissions;

			if (!session.getPermissions().containsAll(readPermissions)) {
				Session.NewPermissionsRequest newReadPermissionsRequest = new Session.NewPermissionsRequest(
						this, readPermissions);
				session.requestNewReadPermissions(newReadPermissionsRequest);
				return false;
			}

			List<String> publishPermissions = mPublishPermissions;
			if (!session.getPermissions().containsAll(publishPermissions)) {
				Session.NewPermissionsRequest newPublishPermissionsRequest = new Session.NewPermissionsRequest(
						this, publishPermissions);
				session.requestNewPublishPermissions(newPublishPermissionsRequest);
				checkedPublishPermissions = true;
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		
		return true;
	}	
	
	private void doFacebook() {
		triggerMessageDialog("Connecting", "\n\n\nContacting Facebook...");
		
	    Session session = Session.getActiveSession();
	    if(!session.isOpened() && !session.isClosed()) {
			Log.i(this.getClass().toString(), "No Facebook session");

			session.openForRead(new Session.OpenRequest(this)
	            .setPermissions(mReadPermissions)
	            .setCallback(mStatusCallback));
	    } else {
			Log.i(this.getClass().toString(), "Yes Facebook session");

			Session.openActiveSession(this, true, mStatusCallback);
	    }		
	}
	
	protected void doFacebookLogin() {
		Log.i(this.getClass().toString(), "Doing Facebook login");		
		
		doFacebook();
	}
	
	// function to check existing / create new accounts
	protected void doFacebookRegistration() {
		Log.i(this.getClass().toString(), "Doing Facebook registration");

		doFacebook();	
	}
	
	protected void getProfile(Session session) {
		Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
			// callback after Graph API response with
			// user
			// object
			@Override
			public void onCompleted(GraphUser user, Response response) {
				// Set Profile Fields
				if (user != null) {
					mUser = user;
					//Log.i(getPackageName(),(String) mUser.asMap().get("email")); // gets email if you wanna use it
					onUser();
				} else if (user == null) {
					showAlertMessage("Check Connection",
							"Whoops! Please check your internet connection and try again.");
				}
			}
		});
	}
	
	protected abstract void onUser();
}
