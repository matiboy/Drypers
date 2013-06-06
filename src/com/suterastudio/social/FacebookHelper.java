package com.suterastudio.social;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.android.Facebook;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.suterastudio.drypers.DrypersException;
import com.suterastudio.drypers.data.Score;
import com.suterastudio.drypers.data.ScoreHandler;

public class FacebookHelper implements ScoreHandler {
	private Facebook mFacebookApi;
	private FacebookHandler mHandler;
	private static final String FB_ACCESS_TOKEN = Session.getActiveSession()
			.getAccessToken();
	private static final Long FB_EXPIRES = Session.getActiveSession()
			.getExpirationDate().getTime();
	private static final String ACCESS_KEY = "fb_access_token";
	private static final String EXPIRY_KEY = "fb_expires";

	private Activity mActivity;

	private Runnable successRunnable = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(mActivity, "Success", Toast.LENGTH_LONG).show();
		}
	};

	public FacebookHelper(Activity activity, String appID,
			FacebookHandler handler) {
		mActivity = activity;
		mFacebookApi = new Facebook("280038485452137");
		mFacebookApi.setAccessToken(restoreAccessToken());
		mHandler = handler;
	}

	public void postOnWall(final String text, final String link) {
		Log.i(this.getClass().toString(), "FB ACCESS TOKEN: " + FB_ACCESS_TOKEN);
		new Thread() {
			@Override
			public void run() {
				try {
					Bundle parameters = new Bundle();
					parameters.putString("access_token", FB_ACCESS_TOKEN);
					parameters.putString("message", text);
					if (link != null) {
						parameters.putString("link", link);
					}
					String response = mFacebookApi.request("me/feed",
							parameters, "POST");
					if (!response.equals("")) {
						if (!response.contains("error")) {
							mActivity.runOnUiThread(successRunnable);
						} else {
							Log.e("Facebook error:", response);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@SuppressWarnings("unused")
	public void publishFeedDialog(final Context context, final String link,
			final String photo) {
		Bundle params = new Bundle();
		params.putString("name", "Drypers Baby Babble Moments Mobile App");
		params.putString("caption", "I've just created a Baby Babble Moment.");
		params.putString("description",
				"Tune in and take a listen. Think YOUR baby's got soul? Make your own!");
		params.putString("link", link);
		params.putString("picture", photo);

		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(context,
				Session.getActiveSession(), params)).setOnCompleteListener(
				new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(context,
										"Posted story, id: " + postId,
										Toast.LENGTH_SHORT).show();

								// award points with connection check
								mHandler.onPublish(postId);

							} else {
								// User clicked the Cancel button
								Toast.makeText(context.getApplicationContext(),
										"Publish cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Toast.makeText(context.getApplicationContext(),
									"Publish cancelled", Toast.LENGTH_SHORT)
									.show();
						} else {
							// Generic, ex: network error
							Toast.makeText(context.getApplicationContext(),
									"Error posting story", Toast.LENGTH_SHORT)
									.show();
						}
					}
				}).build();
		feedDialog.show();
	}

	public void save(String access_token, long expires) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mActivity);
		Editor editor = prefs.edit();
		// editor.putString(Facebook.TOKEN, FB_ACCESS_TOKEN);
		editor.putString(ACCESS_KEY, access_token);
		editor.putLong(EXPIRY_KEY, expires);
		editor.commit();
	}

	public String restoreAccessToken() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mActivity);
		return prefs.getString(ACCESS_KEY, null);
	}

	public long restoreExpires() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mActivity);
		return prefs.getLong(EXPIRY_KEY, 0);
	}

	// Image Manip Helper Functions
	public static URL getPicURL(String userID) throws MalformedURLException {
		String imageURL;
		imageURL = "http://graph.facebook.com/" + userID
				+ "/picture?type=small";
		URL url = new URL(imageURL);
		return url;
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
	public void onScoreUploaded(Score score) {
		// tell users score is uploaded
		CharSequence text = "Score updated.";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(mActivity, text, duration);
		toast.show();
	}

	@Override
	public void onMyScores(List<Score> scores) {
		// TODO Auto-generated method stub

	}
}
