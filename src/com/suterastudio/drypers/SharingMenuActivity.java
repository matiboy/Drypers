package com.suterastudio.drypers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.Session;
import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.data.ScoreHandler;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;
import com.suterastudio.social.FacebookHandler;
import com.suterastudio.social.FacebookHelper;

public class SharingMenuActivity extends GenericActivity implements
		OnTouchListener, ScoreHandler, BabbleHandler, FacebookHandler {
	Rect rectf = new Rect();
	Integer offset;
	int[] coords;
	Babble listedBabble;
	String songTitle;
	String playBackLocation;
	String babblerName;
	String babblerID;
	String photo;
	View dialog;
	View container;
	private ProgressDialog progress;
	private Activity thisActivity = this;
	private Context thisContext = this;
	private File ringtoneF = DrypersResources.Ringtone;
	private static String FB_ACCESS_TOKEN = null;
	private static Long FB_EXPIRES = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sharing_menu);

		createViews();
	}

	private boolean rideOrDie() {
		try {
			FB_ACCESS_TOKEN = Session.getActiveSession().getAccessToken();
			FB_EXPIRES = Session.getActiveSession().getExpirationDate()
					.getTime();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(
					context,
					"You're either not logged in via FB, or you haven't given us permission to post on your wall.",
					Toast.LENGTH_LONG).show();
		}

		if (FB_ACCESS_TOKEN != null && FB_EXPIRES != null) {
			return true;
		} else {
			return false;
		}
	}

	private void createViews() {
		// Get Extra Intent Bundled Params
		offset = getIntent().getExtras().getInt("Index");
		coords = getIntent().getExtras().getIntArray("Coords");

		// set the string values
		listedBabble = (Babble) getIntent().getExtras().getSerializable(
				"babbleObject");
		songTitle = listedBabble.title;
		playBackLocation = SessionHelper.hostUrl + "/babbles/"
				+ listedBabble.id + "/share";
		babblerName = listedBabble.babbler_name;
		babblerID = listedBabble.id;
		photo = SessionHelper.hostUrl + listedBabble.image_url;
		if (listedBabble.image_url.equals("null")) {
			photo = SessionHelper.hostUrl
					+ "/system/images/BAhbBlsHOgZmSSIrMjAxMy8wMy8yMi8wN18zNV81OV85MzZfYmFieW5vZ2dpbi5wbmcGOgZFVA/babynoggin.png";
		}

		// SessionHelper.getBabble(babblerID, SharingMenuActivity.this);

		// Bindings
		final Button fbBtn = (Button) findViewById(R.id.sharing_fb);
		final Button emailBtn = (Button) findViewById(R.id.sharing_email);
		final Button twitterBtn = (Button) findViewById(R.id.sharing_twitter);
		final Button likeBtn = (Button) findViewById(R.id.babblelike_btn);
		final Button ringtoneBtn = (Button) findViewById(R.id.set_ringtone_btn);

		// view listener
		View fullScreen = (View) findViewById(R.id.sharing_menu);
		fullScreen.setOnTouchListener(this);

		// hide if own babble...
		if (listedBabble.babbler_id.equals(DrypersResources.getBabbler().id)) {
			likeBtn.setVisibility(View.GONE);
		}

		// User Menu Buttons
		likeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SessionHelper
						.LikeBabble(listedBabble, SharingMenuActivity.this);
			}
		});

		ringtoneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// alert downloading... msg
				progress = new ProgressDialog(SharingMenuActivity.this);
				SessionHelper
				.LikeBabble(listedBabble, SharingMenuActivity.this);
				runOnUiThread(new Runnable() {
					public void run() {
						progress.setTitle("Downloading...");
						progress.setMessage("Retrieving track from web...");
						progress.show();
					}
				});

				// download the track..
				new DownloadTrackTask(ringtoneF).execute(SessionHelper.hostUrl
						+ listedBabble.track_url);
			}

		});

		fbBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SessionHelper
				.LikeBabble(listedBabble, SharingMenuActivity.this);
				boolean toking = rideOrDie();
				if (toking) {
					FacebookHelper mFB = new FacebookHelper(
							SharingMenuActivity.this, "280038485452137",
							SharingMenuActivity.this);
					mFB.publishFeedDialog(thisActivity, playBackLocation, photo);
				} else {
					return;
				}
			}

		});
		emailBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SessionHelper
				.LikeBabble(listedBabble, SharingMenuActivity.this);
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType("text/html");
				emailIntent.putExtra(Intent.EXTRA_EMAIL,
						"emailaddress@emailaddress.com");
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, babblerName
						+ " has created a Baby Babble Moment");
				emailIntent
						.putExtra(
								Intent.EXTRA_TEXT,
								"Check out this fantastic Baby Babble Moment called "
										+ songTitle
										+ " : "
										+ playBackLocation
										+ "\n Think YOUR baby's got soul? Make your own!"
										+ "\n Download it here: http://bit.ly/10ziN85");

				startActivity(Intent.createChooser(emailIntent, "Send Email"));

				// award points with connection check
				connectionDialog(ScoreType.SHARE);
				// TODO find a way to check if user really shared??
				finish();
			}

		});
		twitterBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SessionHelper
				.LikeBabble(listedBabble, SharingMenuActivity.this);
				Intent twitIntent = null;
				try {
					twitIntent = new Intent(findTwitterClient());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (twitIntent != null) {
					twitIntent
							.putExtra(
									Intent.EXTRA_TEXT,
									"Check out this fantastic Baby Babble Moment called "
											+ songTitle
											+ " : "
											+ playBackLocation
											+ ". Make your own at: http://bit.ly/10ziN85");
					startActivity(Intent.createChooser(twitIntent, null));
					// award points with connection check
					connectionDialog(ScoreType.SHARE);
					// TODO find a way to check if user really shared??
				} else {
					Toast.makeText(
							context,
							"It looks like you don't have any twitter clients installed.",
							Toast.LENGTH_LONG).show();
				}
				finish();
			}

		});
	}

	private Intent findTwitterClient() {
		final String[] twitterApps = {
				// package // name - nb installs (thousands)
				"com.twitter.android", // official - 10 000
				"com.twidroid", // twidroyd - 5 000
				"com.handmark.tweetcaster", // Tweecaster - 5 000
				"com.thedeck.android" }; // TweetDeck - 5 000
		Intent tweetIntent = new Intent();
		tweetIntent.setType("text/plain");
		final PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(
				tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

		for (int i = 0; i < twitterApps.length; i++) {
			for (ResolveInfo resolveInfo : list) {
				String p = resolveInfo.activityInfo.packageName;
				if (p != null && p.startsWith(twitterApps[i])) {
					tweetIntent.setPackage(p);
					return tweetIntent;
				}
			}
		}
		return null;
	}

	@Override
	public boolean onTouch(View view, MotionEvent mev) {
		float xCoord = mev.getX();
		float yCoord = mev.getY();
		if (xCoord > rectf.left && xCoord < rectf.right && yCoord > rectf.top
				&& yCoord < rectf.bottom) {
			// NOOP
		} else {
			finish();
		}

		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		offset = getIntent().getExtras().getInt("Index");
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		// move the window code below...

		// bindings...
		View container = (View) findViewById(R.id.sharing_menu_container);
		final ImageView triangleTop = (ImageView) findViewById(R.id.tri_indic_sharing_Top);
		final ImageView triangleBottom = (ImageView) findViewById(R.id.tri_indic_sharing_Bottom);

		// set the location offset
		container.setX((float) (coords[0] - (container.getWidth() * 0.72)));
		if (coords[1] > getMidDisplayLocation()) {
			triangleTop.setVisibility(View.INVISIBLE);
			triangleBottom.setVisibility(View.VISIBLE);
			container.setY((float) (coords[1] - container.getHeight() + 20));
		} else {
			triangleTop.setVisibility(View.VISIBLE);
			triangleBottom.setVisibility(View.INVISIBLE);
			container.setY((float) coords[1] + 40);

		}

		// set the boundary of the menu
		rectf.set((int) container.getX(),
				(int) (container.getY() + (container.getHeight() * 0.15)),
				(int) (container.getX() + (container.getWidth() * 0.9)),
				(int) container.getY() + container.getHeight());
	}

	public Integer getMidDisplayLocation() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		Integer midPoint = size.y / 2;
		return midPoint;
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
	public void onBabbleLiked(Babble babble) {
		// tell users score is uploaded
		Toast.makeText(context, "Babble liked!", Toast.LENGTH_SHORT).show();

		// award points with connection check
		connectionDialog(ScoreType.LIKE);

		// kill this activity when done...
		finish();
	}

	@Override
	public void onBabbleUploaded(Babble babble) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBabbles(List<Babble> babbles) {

	}

	@Override
	public void onMyBabbles(List<Babble> babbles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFriendsBabbles(List<Babble> babbles) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBabble(Babble babble) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPublish(String id) {
		connectionDialog(ScoreType.SHARE);
		finish();
	}

	// class to download track and save it async-ly
	private class DownloadTrackTask extends AsyncTask<String, Void, File> {
		private File babbleFile;

		public DownloadTrackTask(File file) {
			this.babbleFile = file;
		}

		protected File doInBackground(String... urls) {
			String urldisplay = urls[0];
			try {
				babbleFile.createNewFile();

				URL url = new URL(urldisplay);
				InputStream input = url.openStream();

				try {
					OutputStream output = new FileOutputStream(babbleFile);
					try {
						byte[] buffer = new byte[1024];
						int bytesRead = 0;
						while ((bytesRead = input
								.read(buffer, 0, buffer.length)) >= 0) {
							output.write(buffer, 0, bytesRead);
						}
					} finally {
						Log.i(ContextHelper.class.toString(), "Wrote "
								+ babbleFile);
						output.close();
					}
				} finally {
					input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return babbleFile;
		}

		protected void onPostExecute(File result) {
			ContentResolver mCr = context.getContentResolver();

			// set it to ringtone once file has been set
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, result.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, "BabyBabble");
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
			values.put(MediaStore.MediaColumns.SIZE, result.length());
			values.put(MediaStore.Audio.Media.ARTIST, listedBabble.babbler_name);
			values.put(MediaStore.Audio.Media.ALBUM, "Baby Babble Ringtones");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
			values.put(MediaStore.Audio.Media.IS_ALARM, false);
			values.put(MediaStore.Audio.Media.IS_MUSIC, false);

			Uri uri = MediaStore.Audio.Media.getContentUriForPath(result
					.getAbsolutePath());
			Uri newUri = mCr.insert(uri, values);

			try {
				RingtoneManager.setActualDefaultRingtoneUri(context,
						RingtoneManager.TYPE_RINGTONE, newUri);
			} catch (Throwable t) {
				t.printStackTrace();
			}

			progress.dismiss();

			// tell users ringtone is set...
			CharSequence text = "Track saved & set!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(thisContext, text, duration);
			toast.show();

			// kill this activity when done...
			finish();
		}
	}
}
