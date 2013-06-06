package com.suterastudio.drypers;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.customsoft.stateless4j.StateMachine;
import org.customsoft.stateless4j.delegates.Action;
import org.joda.time.DateTime;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.suterastudio.android.helpers.BabbleComparator;
import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper;

public class BabbleBoxActivity extends GenericActivity implements BabbleHandler {

	private final MediaPlayer mp = new MediaPlayer();
	private CountDownTimer ctd;
	private Integer localIndex;
	private int[] coords;

	private View inflatedLayout;
	private LinearLayout.LayoutParams lp;

	// pointers
	private ProgressBar progressBarPointer;
	private Button playButtonPointer;

	private int renders = 2;
	private LinearLayout babbleViewer;
	private LinearLayout babbleViewerFriends;
	private LinearLayout babbleViewerPopular;

	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private DateFormat df2 = new SimpleDateFormat("dd MMM yyyy");

	private StateMachine<State, Trigger> machine;

	private enum State {
		START, LOADING, DISPLAYING, PLAYING
	}

	private enum Trigger {
		LOAD, DISPLAY, PLAY, STOP, LIKE
	}

	public BabbleBoxActivity() {
		machine = new StateMachine<State, Trigger>(State.START);

		try {
			Action loader = new Action() {
				@Override
				public void doIt() {
					// alert loading msg
					triggerProgressDialog("Connecting",
							"Retrieving lists of babbles...");

					SessionHelper.GetMyBabbles(BabbleBoxActivity.this);
					SessionHelper.GetAllBabbles(BabbleBoxActivity.this);

					Session fbActiveSession = null;
					Long FB_EXPIRES = null;
					try {
						fbActiveSession = Session.getActiveSession();
						FB_EXPIRES = Session.getActiveSession()
								.getExpirationDate().getTime();
					} catch (Exception e) {
						e.printStackTrace();
						// Toast.makeText(BabbleBoxActivity.this,
						// e.getMessage(),
						// Toast.LENGTH_LONG).show();
					}
					if (fbActiveSession != null
							&& (fbActiveSession.getAccessToken() != null)
							&& (FB_EXPIRES != null)) {
						renders++;
						SessionHelper.GetFriendsBabbles(BabbleBoxActivity.this);
					}
				}
			};

			Action player = new Action() {
				@Override
				public void doIt() {

				}
			};

			machine.Configure(State.START).Permit(Trigger.LOAD, State.LOADING);

			machine.Configure(State.LOADING).OnEntry(loader)
					.Permit(Trigger.DISPLAY, State.DISPLAYING);

			machine.Configure(State.DISPLAYING).Permit(Trigger.PLAY,
					State.PLAYING);

			machine.Configure(State.PLAYING).Permit(Trigger.STOP,
					State.DISPLAYING);
		} catch (Exception e) {
			Log.e(getCallingPackage(), e.getMessage());
		}
	}

	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		setContentView(R.layout.babblebox);

		createViews();

		try {
			machine.Fire(Trigger.LOAD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
			}
			mp.reset();
		}

		if (ctd != null) {
			ctd.cancel();
		}

		if (progressBarPointer != null) {
			progressBarPointer.setProgress(0);
			progressBarPointer.setVisibility(View.INVISIBLE);
			progressBarPointer = null;
		}

		if (playButtonPointer != null) {
			playButtonPointer.setBackgroundResource(R.drawable.play_selector);
		}
	}

	@Override
	public void onDestroy() {
		onPause();
		if (mp != null) {
			mp.release();
		}
	}

	/**
	 * 
	 */
	private void createViews() {
		lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		// ScrollViews
		final ScrollView babbleScroller = (ScrollView) findViewById(R.id.babble_scroller);
		final ScrollView babbleScrollerFriends = (ScrollView) findViewById(R.id.babble_scroller_friends);
		final ScrollView babbleScrollerPopular = (ScrollView) findViewById(R.id.babble_scroller_popular);

		// Babble Containers
		babbleViewer = (LinearLayout) findViewById(R.id.babble_viewer);
		babbleViewerFriends = (LinearLayout) findViewById(R.id.babble_viewer_friends);
		babbleViewerPopular = (LinearLayout) findViewById(R.id.babble_viewer_popular);

		// Buttons
		final Button backBtn = (Button) findViewById(R.id.back_babblebox);
		final Button menuButton = (Button) findViewById(R.id.menu_babblebox);
		final Button tmSongsBtn = (Button) findViewById(R.id.my_songs);
		final Button tmFriendsBtn = (Button) findViewById(R.id.my_friends_songs);
		final Button tmPopularBtn = (Button) findViewById(R.id.popular_songs);

		final TextView title = (TextView) findViewById(R.id.babblebox_title);

		// babble font
		title.setTypeface(font);

		// pointer initialization
		progressBarPointer = null;
		playButtonPointer = null;

		// set 1st button focused
		tmSongsBtn.setBackgroundResource(R.drawable.mysong_a);

		// Click Listeners for Menu and Tabs
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		menuButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(BabbleBoxActivity.this,
						MenuCreatorActivity.class));
			}
		});

		// my Babbles
		tmSongsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// set mysong button focused
				tmSongsBtn.setBackgroundResource(R.drawable.mysong_a);
				tmFriendsBtn.setBackgroundResource(R.drawable.myfriend);
				tmPopularBtn.setBackgroundResource(R.drawable.popular);

				// Turn off inactive views
				babbleScrollerFriends.setVisibility(View.GONE);
				babbleScrollerPopular.setVisibility(View.GONE);

				// Show the desired view
				babbleScroller.setVisibility(View.VISIBLE);

				if (babbleViewer.getChildCount() == 0) {
					// tell users no babble
					Toast.makeText(context, "You have no babbles yet..",
							Toast.LENGTH_SHORT).show();
				}

				onPause(); // resets the goodies
			}
		});

		// Friends' Babbles
		tmFriendsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// set friends button focused
				tmSongsBtn.setBackgroundResource(R.drawable.mysong);
				tmFriendsBtn.setBackgroundResource(R.drawable.myfriend_a);
				tmPopularBtn.setBackgroundResource(R.drawable.popular);

				// Turn off inactive views
				babbleScroller.setVisibility(View.GONE);
				babbleScrollerPopular.setVisibility(View.GONE);
				// Show the desired view
				babbleScrollerFriends.setVisibility(View.VISIBLE);

				if (babbleViewerFriends.getChildCount() == 0) {
					// tell users score is uploaded
					Toast.makeText(context,
							"No babble from your friends yet..",
							Toast.LENGTH_SHORT).show();
				}

				onPause(); // resets the goodies
			}
		});

		// Popular Babbles
		tmPopularBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// set popular button focused
				tmSongsBtn.setBackgroundResource(R.drawable.mysong);
				tmFriendsBtn.setBackgroundResource(R.drawable.myfriend);
				tmPopularBtn.setBackgroundResource(R.drawable.popular_a);

				// Turn off inactive views
				babbleScroller.setVisibility(View.GONE);
				babbleScrollerFriends.setVisibility(View.GONE);
				// Show the desired view
				babbleScrollerPopular.setVisibility(View.VISIBLE);

				if (babbleViewerPopular.getChildCount() == 0) {
					// tell users score is uploaded
					Toast.makeText(context, "No top babbles yet..",
							Toast.LENGTH_SHORT).show();
				}

				onPause(); // resets the goodies
			}
		});
	}

	/**
	 * 
	 * @param index
	 * @param targetContainer
	 * @param babble
	 */
	private void createBabble(Integer index,
			final LinearLayout targetContainer, final Babble babble) {
		inflatedLayout = LayoutInflater.from(this).inflate(R.layout.one_babble,
				null);
		lp.setMargins(0, 0, 0, 10);
		inflatedLayout.setLayoutParams(lp);

		// Element bindings
		// final LinearLayout babbleItem = (LinearLayout) inflatedLayout
		// .findViewById(R.id.babble_item);
		final Button playBabbleItem = (Button) inflatedLayout
				.findViewById(R.id.play_babble_item);
		final Button babbleRateBtn = (Button) inflatedLayout
				.findViewById(R.id.babble_rating);
		final ProgressBar babbleItemProgress = (ProgressBar) inflatedLayout
				.findViewById(R.id.babble_item_progress);
		final TextView babbleSongName = (TextView) inflatedLayout
				.findViewById(R.id.babble_song_name);
		final TextView babbleBabblerName = (TextView) inflatedLayout
				.findViewById(R.id.babble_babbler_name);
		final TextView babbleDate = (TextView) inflatedLayout
				.findViewById(R.id.babble_date);
		final TextView babbleYourRating = (TextView) inflatedLayout
				.findViewById(R.id.babble_your_rating);
		final LinearLayout this_babble_box = (LinearLayout) inflatedLayout
				.findViewById(R.id.this_babble_box);

		babbleSongName.setTypeface(font);
		babbleBabblerName.setTypeface(font);
		babbleDate.setTypeface(font);
		babbleYourRating.setTypeface(font);

		babbleSongName.setText(babble.title);
		babbleBabblerName.setText(babble.babbler_name.split("@")[0]);

		// date format
		String date = "";
		try {
			date = df2.format(df1.parse(babble.created_at));
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		// babbleDate.setText(babble.created_at.split("T")[0]);
		babbleDate.setText(date);

		babbleYourRating.setText(babble.likes);
		babbleItemProgress.setVisibility(View.INVISIBLE);
		playBabbleItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<View> progressList = getViewsByContentDescription(
						targetContainer, "progress");
				for (int i = 0; i < progressList.size(); i++) {
					progressList.get(i).setVisibility(View.INVISIBLE);
				}
				ArrayList<View> playList = getViewsByContentDescription(
						targetContainer, "play");
				for (int i = 0; i < playList.size(); i++) {
					playList.get(i).setBackgroundResource(
							R.drawable.play_selector);
				}
				try {
					if (!mp.isPlaying()) {
						progressBarPointer = babbleItemProgress;
						playButtonPointer = playBabbleItem;
						babbleItemProgress.setVisibility(View.VISIBLE);
						mp.setDataSource(SessionHelper.hostUrl
								+ babble.track_url);
						mp.prepare();
						mp.start();
						playBabbleItem.setBackgroundResource(R.drawable.stop);
						babbleItemProgress.setMax(mp.getDuration());
						mCountDown(babbleItemProgress);
					} else {
						babbleItemProgress.setProgress(0);
						try {
							ctd.onFinish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		babbleRateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create an integer array and fill its slots with the absolute
				// coordinates of the launching like button
				coords = new int[2];
				babbleRateBtn.getLocationOnScreen(coords);
				display_share_menu(babble);
			}
		});

		runOnUiThread(new Runnable() {
			public void run() {
				targetContainer.addView(inflatedLayout);
			}
		});

		this_babble_box.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// Create an integer array and fill its slots with the absolute
				// coordinates of the launching like button
				if (babble.babbler_id.equals(DrypersResources.getBabbler().id)) {
					coords = new int[2];
					this_babble_box.getLocationOnScreen(coords);
					// display_myBabble_menu(babble);
				}
				return false;
			}
		});
	}

	/**
	 * 
	 * @param index
	 * @param targetContainer
	 * @param songTitle
	 * @param babblerName
	 * @param createdDate
	 * @param yourRating
	 * @param avgRating
	 * @param playbackLocation
	 * @param type
	 */

	private void createPopular(Integer index,
			final LinearLayout targetContainer, final Babble babble) {
		DateTime createdDateTime = new DateTime(babble.created_at);
		DateTime currentDateTime = new DateTime(System.currentTimeMillis());
		long differenceDate = currentDateTime.getMillis()
				- createdDateTime.getMillis();
		if (differenceDate > 604800000 || babble.babbler_id.equals("35")
				|| babble.babbler_id.equals("31")) {
			return;
		}
		inflatedLayout = LayoutInflater.from(this).inflate(
				R.layout.one_babble_popular, null);
		lp.setMargins(0, 0, 0, -10);
		inflatedLayout.setLayoutParams(lp);

		// Element bindings
		// final LinearLayout babbleItem = (LinearLayout) inflatedLayout
		// .findViewById(R.id.babble_item_pop);
		final Button playBabbleItem = (Button) inflatedLayout
				.findViewById(R.id.play_babble_item_pop);
		final Button babbleRateBtn = (Button) inflatedLayout
				.findViewById(R.id.babble_rating_pop);
		final ProgressBar babbleItemProgress = (ProgressBar) inflatedLayout
				.findViewById(R.id.babble_item_progress_pop);
		final TextView babbleSongName = (TextView) inflatedLayout
				.findViewById(R.id.babble_song_name_pop);
		final TextView babbleBabblerName = (TextView) inflatedLayout
				.findViewById(R.id.babble_babbler_name_pop);
		final TextView babbleDate = (TextView) inflatedLayout
				.findViewById(R.id.babble_date_pop);
		final TextView babbleYourRating = (TextView) inflatedLayout
				.findViewById(R.id.babble_your_rating_pop);
		final TextView babbleSongRanking = (TextView) inflatedLayout
				.findViewById(R.id.song_ranking_pop);
		final ImageView babblePic = (ImageView) inflatedLayout
				.findViewById(R.id.babble_pic);

		babbleSongName.setTypeface(font);
		babbleBabblerName.setTypeface(font);
		babbleDate.setTypeface(font);
		babbleYourRating.setTypeface(font);
		babbleSongRanking.setTypeface(font);

		// check for null images and not assign null images!
		if (!babble.image_url.equals("null")) {
			// download the image in the background
			new DownloadImageTask((ImageView) babblePic)
					.execute(SessionHelper.hostUrl + babble.image_url);
		}

		// Create Pointers to Tagged Views
		// Song Title
		babbleSongName.setText(babble.title);

		// Babbler Name
		babbleBabblerName.setText(babble.babbler_name.split("@")[0]);

		// Created Date
		// date format
		String date = "";
		try {
			date = df2.format(df1.parse(babble.created_at));
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		// babbleDate.setText(babble.created_at.split("T")[0]);
		babbleDate.setText(date);
		// babbleDate.setText(babble.created_at.split("T")[0]);

		// Rating
		babbleYourRating.setText(babble.likes);

		// ProgressBar
		babbleItemProgress.setVisibility(View.INVISIBLE);
		// Play Button

		playBabbleItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<View> progressList = getViewsByContentDescription(
						targetContainer, "progress");
				for (int i = 0; i < progressList.size(); i++) {
					progressList.get(i).setVisibility(View.INVISIBLE);
				}
				ArrayList<View> playList = getViewsByContentDescription(
						targetContainer, "play");
				for (int i = 0; i < playList.size(); i++) {
					playList.get(i).setBackgroundResource(
							R.drawable.play_selector);
				}
				try {
					if (!mp.isPlaying()) {
						progressBarPointer = babbleItemProgress;
						playButtonPointer = playBabbleItem;
						babbleItemProgress.setVisibility(View.VISIBLE);
						mp.setDataSource(SessionHelper.hostUrl
								+ babble.track_url);
						mp.prepare();
						mp.start();
						playBabbleItem.setBackgroundResource(R.drawable.stop);
						babbleItemProgress.setMax(mp.getDuration());
						mCountDown(babbleItemProgress);
					} else {
						babbleItemProgress.setProgress(0);
						try {
							ctd.onFinish();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// Pass in the Index to give menu offset
		localIndex = index;
		babbleRateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create an integer array and fill its slots with the absolute
				// coordinates of the launching like button
				coords = new int[2];
				babbleRateBtn.getLocationOnScreen(coords);
				display_share_menu(babble);
			}
		});

		runOnUiThread(new Runnable() {
			public void run() {
				targetContainer.addView(inflatedLayout);
			}
		});
		// Song Ranking
		Integer indexPlusOne = 1 + targetContainer.indexOfChild(inflatedLayout);
		babbleSongRanking.setText(indexPlusOne.toString());
	}

	/**
	 * 
	 * @param cdt
	 * @param progress
	 * @param mediaPlayer
	 */
	private void mCountDown(final ProgressBar progress) {
		progress.setProgress(0);
		progress.setMax(mp.getDuration());
		ctd = new CountDownTimer(mp.getDuration(), 250) {
			public void onTick(long millisUntilFinished) {
				if (mp.isPlaying()) {
					progress.setProgress(progress.getProgress() + 250);
				} else {
					this.cancel();
				}
			}

			public void onFinish() {
				progressBarPointer.setProgress(0);
				progressBarPointer.setVisibility(View.INVISIBLE);
				mp.stop();
				mp.reset();
				playButtonPointer
						.setBackgroundResource(R.drawable.play_selector);
				this.cancel();
			}
		}.start();
	}

	private void markRendered() {
		renders--;

		if (renders == 0) {
			try {
				machine.Fire(Trigger.DISPLAY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void display_share_menu(Babble babble) {
		startActivity(new Intent(BabbleBoxActivity.this,
				SharingMenuActivity.class).putExtra("Index", localIndex)
				.putExtra("Coords", coords).putExtra("babbleObject", babble));
	}

	// for future use if needed...
	private void display_myBabble_menu(Babble babble) {
		startActivity(new Intent(BabbleBoxActivity.this, EditMenuActivity.class)
				.putExtra("Index", localIndex).putExtra("Coords", coords)
				.putExtra("babbleObject", babble));
	}

	@Override
	public void onException(DrypersException exception) {
		exception.printStackTrace();
		Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProgress(String message) {
	}

	@Override
	public void onBabbleLiked(Babble babble) {

	}

	@Override
	public void onBabbleUploaded(Babble babble) {
	}

	@Override
	public void onBabbles(List<Babble> babbles) {
		if (babbles != null) {
			int i = 0;
			for (Babble babble : babbles) {
				createPopular(i++, babbleViewerPopular, babble);
			}

			markRendered();
			dismissProgressDialog();
			// TODO not graceful as it only
			// monitors 1
			// situation (hopefully the longest)

			// display initial msg if no babbles
			if (babbleViewer.getChildCount() == 0) {
				// tell users no babble
				Toast.makeText(context, "You have no babbles yet..",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	@Override
	public void onMyBabbles(List<Babble> babbles) {
		if (babbles != null) {
			Collections.sort(babbles, new BabbleComparator());

			int i = 0;
			for (Babble babble : babbles) {
				if (i == 30) {
					break;
				} else {
					createBabble(i++, babbleViewer, babble);
				}
			}
			markRendered();
		}
	}

	@Override
	public void onFriendsBabbles(List<Babble> babbles) {
		if (babbles != null) {
			Collections.sort(babbles, new BabbleComparator());
			int i = 0;
			for (Babble babble : babbles) {
				if (i == 30) {
					break;
				} else {
					createBabble(i++, babbleViewerFriends, babble);
				}
			}
		}
		markRendered();
	}

	private static ArrayList<View> getViewsByContentDescription(ViewGroup root,
			String cd) {
		ArrayList<View> views = new ArrayList<View>();
		final int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = root.getChildAt(i);
			if (child instanceof ViewGroup) {
				views.addAll(getViewsByContentDescription((ViewGroup) child, cd));
			}

			final Object cdObj = child.getContentDescription();
			if (cdObj != null && cdObj.equals(cd)) {
				views.add(child);
			}

		}
		return views;
	}

	@Override
	public void onBabble(Babble babble) {
		// TODO Auto-generated method stub

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
				mIcon11 = Bitmap.createScaledBitmap(mIcon11, 50, 50, true);
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
