package com.suterastudio.drypers;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.drypers.data.Babbler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.Score;
import com.suterastudio.drypers.data.ScoreHandler;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;

public abstract class GenericActivity extends Activity implements ScoreHandler {
	private ProgressDialog mProgress;
	private AlertDialog mMessage;
	private AlertDialog mAlert;
	//protected Activity activity = this;
	protected Context context = this;
	protected BroadcastReceiver recievee;
	// font face
	protected Typeface font;
	protected boolean safeToFinish = false;
	// Flurry api key
	protected static final String FLURRY_API_KEY = "79NTRQDZFSSFVCNVY3MJ";

	@Override
	protected void onPause() {
		super.onPause();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Add flurry support
		FlurryAgent.onStartSession(this, FLURRY_API_KEY);
		// Easytracker (GA) support
		EasyTracker.getInstance().activityStart(this);
		/** snip **/
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.package.ACTION_LOGOUT");
		recievee = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("onReceive", "Logout in progress");
				// At this point you should start the login activity and finish
				// this one
				finish();
			}
		};
		registerReceiver(recievee, intentFilter);
		// ** snip **//
	}

	@Override
	protected void onStop() {
		super.onStop();
		// add flurry support
		FlurryAgent.onEndSession(this);
		// Easytracker (GA) support
		EasyTracker.getInstance().activityStop(this);
		try {
			unregisterReceiver(recievee);
		} catch (RuntimeException er) {
			Log.e(getCallingPackage(), er.getMessage());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		font = DrypersResources.getTypeFace(context);
		// Kill the title bar
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Kill the notification bar
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.layout.main_menu, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.settings:
//			startActivity(new Intent(this, ProfileActivity.class));
//			break;
//		}
//		return true;
//	}

	protected void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing()) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Dismiss no matter what. Dismissing an unshown dialog
					// should
					// fail silently. This is to fix issues where the dialog
					// would
					// show up again, causing context leak and silently failing.
					mProgress.dismiss();
				}
			});
		}
		dismissMessageDialog();
	}
	
	protected void triggerProgressDialog(final String title,
			final String message) {
		if (mProgress == null) {
			runOnUiThread(new Runnable() {
				public void run() {
					mProgress = new ProgressDialog(context);

					runOnUiThread(new Runnable() {
						public void run() {
							mProgress.setTitle(title);
							mProgress.setMessage(message);
							mProgress.show();
						}
					});
				}
			});
		}
	}
	
	protected void dismissMessageDialog() {
		if (mMessage != null && mMessage.isShowing()) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Dismiss no matter what. Dismissing an unshown dialog
					// should
					// fail silently. This is to fix issues where the dialog
					// would
					// show up again, causing context leak and silently failing.
					mMessage.dismiss();
				}
			});
		}
	}
	
	protected void triggerMessageDialog(final String title,
			final String message) {
		if (mMessage == null) {
			runOnUiThread(new Runnable() {
				public void run() {
					mMessage = new AlertDialog.Builder(context).create();

					runOnUiThread(new Runnable() {
						public void run() {
							mMessage.setTitle(title);
							mMessage.setMessage(message);
							mMessage.show();
						}
					});
				}
			});
		}
	}

	protected void showAlertMessage(final String title, final String message) {

			runOnUiThread(new Runnable() {
				public void run() {
					dismissProgressDialog();
					mAlert = new AlertDialog.Builder(context).create();
					mAlert.setTitle(title);
					mAlert.setMessage(message);
					mAlert.show();
				}
			});
	}

	protected void awardPoints(ScoreType type) {
		SessionHelper.UploadScore(type, GenericActivity.this);
	}

	// the repeated code sequence in connectionDialog made generic
	private boolean connectionDialogBasics() {
		// Check for Active internet connection
		Boolean isOnline = ContextHelper.isOnline(context);

		safeToFinish = isOnline;
		
		Babbler babbler = null;
		if (isOnline) {
			babbler = DrypersResources.getBabbler();
		}

		if (!isOnline || babbler == null) {
			AlertDialog dialogName = new AlertDialog.Builder(context).create();
			// final needed for the hide() call
			final AlertDialog dialogPointer = dialogName;
			dialogName.setTitle("Check Connection");
			dialogName
					.setMessage("Whoops! Please check that you are connected to the internet and logged in to Baby Babble Moments.");
			dialogName.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialogPointer.hide();
				}
			});
			dialogName.show();
		}
		return isOnline;
	}
	
	// the repeated code sequence in connectionDialog re-tailored for points/awards only
		private boolean connectionDialogBasicsToaster() {
			// Check for Active internet connection
			Boolean isOnline = ContextHelper.isOnline(context);

			Babbler babbler = null;
			if (isOnline) {
				babbler = DrypersResources.getBabbler();
			}

			if (!isOnline || babbler == null) {
				Toast.makeText(context, "Awards function failed, Check connection!", 
						Toast.LENGTH_SHORT).show();
			}
			return isOnline;
		}

	protected void connectionDialog( Intent intent) {
		Boolean isOnline = connectionDialogBasics();
		if (isOnline == true) {
			startActivity(intent);
		}
	}

	protected void connectionDialog( Class className) {
		Boolean isOnline = connectionDialogBasics();
		if (isOnline == true) {
			Intent intent = new Intent(context, className);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
	}

	protected void connectionDialog( ScoreType type) {
		Boolean isOnline = connectionDialogBasicsToaster();
		if (isOnline == true) {
			SessionHelper.UploadScore(type, GenericActivity.this);
		}
		// TODO if user not online, score not awarded... too bad??? or make loop
		// to queue the award?
	}

	protected void connectionDialog( String title, String value) {
		Boolean isOnline = connectionDialogBasicsToaster();
		if (isOnline == true) {
			SessionHelper.UploadScoreRedemption(ScoreType.REDEEM, title, value,
					GenericActivity.this);
		}
		// TODO if user not online, score not awarded... too bad??? or make loop
		// to queue the award?
	}

	@Override
	public void onScoreUploaded(Score score) {
		Toast.makeText(context, "Points Awarded.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onMyScores(List<Score> scores) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onException(DrypersException exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgress(String message) {
		// TODO Auto-generated method stub

	}
}
