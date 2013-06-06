package com.suterastudio.drypers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.autotalent.Autotalent;

import org.customsoft.stateless4j.StateMachine;
import org.customsoft.stateless4j.delegates.Action;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.suterastudio.android.helpers.ContextHelper;
import com.suterastudio.android.media.AudioHelper;
import com.suterastudio.android.media.RecordException;
import com.suterastudio.android.media.RecordListener;
import com.suterastudio.android.media.SimpleRecorder;
import com.suterastudio.android.media.WaveLooper;
import com.suterastudio.drypers.data.Babble;
import com.suterastudio.drypers.data.BabbleHandler;
import com.suterastudio.drypers.data.DrypersResources;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.Gender;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;
import com.suterastudio.dsp.sampled.AudioFileFormat;
import com.suterastudio.dsp.sampled.AudioInputStream;
import com.suterastudio.dsp.sampled.AudioSystem;
import com.suterastudio.dsp.sampled.Clip;
import com.suterastudio.dsp.sampled.DataLine;
import com.suterastudio.dsp.sampled.LineUnavailableException;
import com.suterastudio.dsp.sampled.Mixer.Info;
import com.suterastudio.dsp.sampled.UnsupportedAudioFileException;
import com.suterastudio.media.autotune.AutotuneException;
import com.suterastudio.media.autotune.AutotuneHandler;
import com.suterastudio.media.autotune.AutotuneParameter;
import com.suterastudio.media.autotune.Autotuner;

public class RecordingActivity extends GenericActivity implements
		AutotuneHandler, OnPreparedListener, OnCompletionListener,
		BabbleHandler, RecordListener {
	// Context goodies
	private Gender mGender;
	private String mSongTitle = null;
	private String mBabbleTitle = null;
	private File mSongTrack = null;
	private List<AutotuneParameter> mAutotuneParams = null;
	private SimpleRecorder mSimpleRecorder;
	private CountDownTimer mCountdownTimer;
	private int mAutotunePasses = 0;
	private boolean mReadyToUpload = false;
	private MediaPlayer mPlayerVocal = null;
	private MediaPlayer mPlayerSong = null;

	// View goodies
	private ImageView mTemplateSelect = null;
	private TextView mTemplateText = null;
	private ProgressBar mSingingProgress = null;
	private LinearLayout mSingingBar = null;
	private Button mBackButton = null;
	private Button mMenuButton = null;
	private Button mPlayButton = null;
	private Button mRecordButton = null;
	private Button mSaveButton = null;

	// Listeners
	private OnClickListener mSaveClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mReadyToUpload && mMachine.CanFire(Trigger.CONFIRM)) {
				try {
					mMachine.Fire(Trigger.CONFIRM);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	private OnClickListener mPlayClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.PLAYING)) {
				try {
					mMachine.Fire(Trigger.STOP);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (mMachine.IsInState(State.IDLING)) {
				try {
					mMachine.Fire(Trigger.PLAY);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	private OnClickListener mRecordClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMachine.CanFire(Trigger.RECORD)) {
				try {
					mMachine.Fire(Trigger.RECORD);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (mMachine.IsInState(State.RECORDING)) {
				try {
					mMachine.Fire(Trigger.CANCEL);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	private OnClickListener mTemplateClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)) {
				// API call for templates here
				final CharSequence[] items = { "Loving Your Baby Groove",
						"Baby, You're My Sunshine", "Shake it N Love it",
						"Dancing Baby", "Hip Hop Baby" };
				new AlertDialog.Builder(RecordingActivity.this)
						.setSingleChoiceItems(items, 0, null)
						.setPositiveButton(R.string.ok_button_label,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
										int selectedPosition = ((AlertDialog) dialog)
												.getListView()
												.getCheckedItemPosition();
										CharSequence text = items[selectedPosition];
										mTemplateText.setText(text);
										Toast.makeText(
												RecordingActivity.this,
												"Template changes will take effect when next you record a song.",
												Toast.LENGTH_LONG).show();
										setSong();
									}
								}).show();
			}
		}
	};
	private OnClickListener mMenuClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)) {
				startActivity(new Intent(RecordingActivity.this,
						MenuCreatorActivity.class));
			}
		}
	};
	private OnClickListener mBackClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)) {
				finish();
			}
		}
	};

	// FSM FTW!
	private StateMachine<State, Trigger> mMachine = new StateMachine<State, Trigger>(
			State.IDLING);

	private enum State {
		IDLING, RECORDING, CANCELING, AUTOTUNING, PLAYING, STOPPING, CONFIRMING, UPLOADING
	}

	private enum Trigger {
		IDLE, RECORD, CANCEL, AUTOTUNE, PLAY, STOP, CONFIRM, UPLOAD, PAUSE
	}

	private Action mIdler = new Action() {
		@Override
		public void doIt() {
			updateViews();
		}
	};
	private Action mRecorder = new Action() {
		@Override
		public void doIt() {
			updateViews();
			startRecording();
		}
	};
	private Action mCanceler = new Action() {
		@Override
		public void doIt() {
			updateViews();
			pauseRecording();
		}
	};
	private Action mAutotuner = new Action() {
		@Override
		public void doIt() {
			updateViews();
			startAutotuning();
		}
	};
	private Action mConfirmer = new Action() {
		@Override
		public void doIt() {
			updateViews();
			startConfirming();
		}
	};
	private Action mPlayer = new Action() {
		@Override
		public void doIt() {
			updateViews();
			startPlaying();
		}
	};
	private Action mStopper = new Action() {
		@Override
		public void doIt() {
			updateViews();
			stopPlaying();
		}
	};
	private Action mUploader = new Action() {
		@Override
		public void doIt() {
			updateViews();
			startUploading();
		}
	};

	// //
	// Constructor(s)
	// //

	public RecordingActivity() {
		try {
			mMachine.Configure(State.IDLING)
					.OnEntry(mIdler)
					.Permit(Trigger.RECORD, State.RECORDING)
					.Permit(Trigger.PLAY, State.PLAYING)
					.Permit(Trigger.CONFIRM, State.CONFIRMING);

			mMachine.Configure(State.RECORDING).OnEntry(mRecorder)
					.Permit(Trigger.CANCEL, State.CANCELING)
					.Permit(Trigger.AUTOTUNE, State.AUTOTUNING)
					.Permit(Trigger.PAUSE, State.IDLING);

			mMachine.Configure(State.CANCELING).OnEntry(mCanceler)
					.Permit(Trigger.IDLE, State.IDLING);

			mMachine.Configure(State.PLAYING).OnEntry(mPlayer)
					.Permit(Trigger.STOP, State.STOPPING);

			mMachine.Configure(State.STOPPING).OnEntry(mStopper)
					.Permit(Trigger.IDLE, State.IDLING);

			mMachine.Configure(State.CONFIRMING).OnEntry(mConfirmer)
					.Permit(Trigger.IDLE, State.IDLING)
					.Permit(Trigger.UPLOAD, State.UPLOADING);

			mMachine.Configure(State.UPLOADING).OnEntry(mUploader)
					.Permit(Trigger.IDLE, State.IDLING);

			mMachine.Configure(State.AUTOTUNING).OnEntry(mAutotuner)
					.Permit(Trigger.IDLE, State.IDLING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// //
	// Android Lifecycle Events
	// //

	/**
     *
     */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Prevent screen blackout on this view
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// XML Layout
		setContentView(R.layout.recording);

		// Create views
		createViews();

		// Update views
		updateViews();

		// Manage (and potentially create) and Songs
		checkSongs();

		// Set song
		setSong();
	}

	@Override
	public void onResume() {
		super.onResume();
		ImageView babyGender = (ImageView) findViewById(R.id.head_final);
		// get gender data from previous activity and change the baby's gender
		// appropriately
		mGender = DrypersResources.ThawGender(this);
		if (mGender == Gender.BOY) {
			babyGender.setBackgroundResource(R.drawable.baby_whead);
		} else {
			babyGender.setBackgroundResource(R.drawable.babygirl2);
		}
	}

	/**
     *
     */
	@Override
	public void onPause() {
		super.onPause();
		mTimerElapesd=0;
		if (mSimpleRecorder != null) {
			if (mSimpleRecorder.isRunning()) {
				stopRecording();
			}
			mSimpleRecorder.cleanup();
		}
		if (mPlayerVocal != null) {
			if (mPlayerVocal.isPlaying()) {
				mPlayerVocal.stop();
			}
			mPlayerVocal.reset();
		}
		if (mPlayerSong != null) {
			if (mPlayerSong.isPlaying()) {
				mPlayerSong.stop();
			}
			mPlayerSong.reset();
		}

		if (mCountdownTimer != null) {
			mCountdownTimer.cancel();
		}

		if (mSingingProgress != null) {
			mSingingProgress.setProgress(0);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		onPause();

		if (mPlayerVocal != null) {
			mPlayerVocal.release();
		}
		if (mPlayerSong != null) {
			mPlayerSong.release();
		}
	}

	private void checkSongs() {
		// Create Assets
		if (!DrypersResources.KPopA.exists()
				|| !DrypersResources.KPopB.exists()
				|| !DrypersResources.KidsA.exists()
				|| !DrypersResources.KidsB.exists()
				|| !DrypersResources.Gangster.exists()
				|| !DrypersResources.HipHop.exists()) {
			ContextHelper.createAsset(this, DrypersResources.KPopA,
					"kpopa.wav", "kpopa.wav");
			ContextHelper.createAsset(this, DrypersResources.KPopB,
					"kpopb.wav", "kpopb.wav");
			ContextHelper.createAsset(this, DrypersResources.KidsA,
					"kidsa.wav", "kidsa.wav");
			ContextHelper.createAsset(this, DrypersResources.KidsB,
					"kidsb.wav", "kidsb.wav");
			ContextHelper.createAsset(this, DrypersResources.Gangster,
					"gangster.wav", "gangster.wav");
			ContextHelper.createAsset(this, DrypersResources.HipHop,
					"hiphop.wav", "hiphop.wav");
		}

		// Create files
		// DrypersResources.AutotunePass1.createNewFile();
		// DrypersResources.AutotunePass2.createNewFile();
		// DrypersResources.AutotunePass3.createNewFile();
		// DrypersResources.AutotunePass4.createNewFile();
		// DrypersResources.AutotunePass5.createNewFile();
	}

	private void createViews() {
		// Programmatic layout
		LinearLayout ll = (LinearLayout) findViewById(R.id.recording_buttons_1st_row);
		LinearLayout ll2 = (LinearLayout) findViewById(R.id.recording_buttons_2nd_row);
		mRecordButton = new Button(this);
		mRecordButton.setOnClickListener(mRecordClickListener);
		ll.addView(mRecordButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		mPlayButton = new Button(this);
		mPlayButton.setOnClickListener(mPlayClickListener);
		ll2.addView(mPlayButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		mSaveButton = new Button(this);
		mSaveButton.setOnClickListener(mSaveClickListener);
		ll2.addView(mSaveButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		// Set drawables
		mRecordButton.setBackgroundResource(R.drawable.abtn_start_singing);
		mPlayButton.setBackgroundResource(R.drawable.abtn_play_babble);
		mSaveButton.setBackgroundResource(R.drawable.abtn_save);

		// Element Bindings
		mBackButton = (Button) findViewById(R.id.back_recording);
		mMenuButton = (Button) findViewById(R.id.menu_recording);
		ImageView babyHead = (ImageView) findViewById(R.id.male_baby);
		ImageView babyGender = (ImageView) findViewById(R.id.head_final);
		mTemplateSelect = (ImageView) findViewById(R.id.active_template);
		mTemplateText = (TextView) findViewById(R.id.template_name);
		mSingingBar = (LinearLayout) findViewById(R.id.singing_progress);
		mSingingProgress = (ProgressBar) findViewById(R.id.singing_progress_prompt);
		TextView singingLabel = (TextView) findViewById(R.id.singing_progress_label);

		// Babble font
		singingLabel.setTypeface(font);

		// get gender data from previous activity and change the baby's gender
		// appropriately
		mGender = DrypersResources.ThawGender(this);
		if (mGender == Gender.BOY) {
			babyGender.setBackgroundResource(R.drawable.baby_whead);
		} else {
			babyGender.setBackgroundResource(R.drawable.babygirl2);
		}

		if (DrypersResources.BabyHead.exists()) {
			Bitmap savedHead = (Bitmap) BitmapFactory
					.decodeFile(DrypersResources.BabyHead.getAbsolutePath());
			Drawable headDrawable = new BitmapDrawable(getResources(),
					savedHead);
			babyHead.setBackgroundDrawable(headDrawable);
			babyHead.bringToFront();
		}
		// Button Click Listeners
		mBackButton.setOnClickListener(mBackClickListener);
		mMenuButton.setOnClickListener(mMenuClickListener);

		// Create context pointer
		mTemplateSelect.setOnClickListener(mTemplateClickListener);
	}

	private void doAutotuning() {
		if (mAutotunePasses == 5) {
			Autotalent.destroyAutotalent();

			// Cleanup
			Log.i(this.getClass().toString(), "Autotuned");

			mAutotunePasses = 0;

			doMuxing();
		} else {
			Autotalent.destroyAutotalent();
			File autotuneFile = DrypersResources.AutotunePasses
					.get(mAutotunePasses);
			AutotuneParameter autotuneParam = mAutotuneParams
					.get(mAutotunePasses);
			new Autotuner(DrypersResources.getLibraryDirectory(),
					DrypersResources.SourceFile.getName(),
					autotuneFile.getName(), 22050, this,
					autotuneParam.getKey(), autotuneParam.getPitch(), 0.0f);
			mAutotunePasses++;
		}
	}
	protected int mTimerElapesd=0;
	private void doCountDown(final MediaPlayer mediaPlayer) {
		mSingingProgress.setMax(4800);
		mSingingProgress.setProgress(0);
		mCountdownTimer = new CountDownTimer(4800-mTimerElapesd, 250) {
			public void onTick(long millisUntilFinished) {
				mTimerElapesd=mTimerElapesd+ 250;
				mSingingProgress
						.setProgress(mTimerElapesd);
			}

			public void onFinish() {
				mSingingProgress.setProgress(4800);
				mTimerElapesd=0;
				stopRecording();
				this.cancel();
			}
		}.start();
	}

	private void doLoop(File left, File right, File result) {
		WaveLooper mWavLooper = new WaveLooper(left, right, result, 30);
		try {
			mWavLooper.wavExtender();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doMuxing() {
		doLoop(DrypersResources.AutotunePass1, DrypersResources.AutotunePass2,
				DrypersResources.FinalFile);
		doLoop(DrypersResources.FinalFile, DrypersResources.AutotunePass3,
				DrypersResources.LoopedAudio);
		doLoop(DrypersResources.LoopedAudio, DrypersResources.AutotunePass4,
				DrypersResources.FinalFile);
		doLoop(DrypersResources.FinalFile, DrypersResources.AutotunePass5,
				DrypersResources.LoopedAudio);

		// Mux the files
		try {
			AudioInputStream voiceStream = AudioSystem
					.getAudioInputStream(DrypersResources.LoopedAudio);
			AudioInputStream musicStream = AudioSystem
					.getAudioInputStream(mSongTrack);

			ArrayList<AudioInputStream> streamList = new ArrayList<AudioInputStream>();
			streamList.add(voiceStream);
			streamList.add(musicStream);
			AudioInputStream resultStream = AudioHelper.mixAudio(streamList);
			AudioSystem.write(resultStream, AudioFileFormat.Type.WAVE,
					DrypersResources.MuxedAudio);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ready to Upload!
		mReadyToUpload = true;

		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				mMachine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setSong() {
		// Set song title
		mSongTitle = mTemplateText.getText().toString();

		Log.i(this.getClass().toString(), "Template Selected: " + mSongTitle);

		if (mSongTitle.equals("pop A")) {
			mSongTrack = DrypersResources.KPopA;
			mAutotuneParams = DrypersResources.KPopAParams;
		} else if (mSongTitle.equals("Loving Your Baby Groove")) {
			mSongTrack = DrypersResources.KPopB;
			mAutotuneParams = DrypersResources.KPopBParams;
		} else if (mSongTitle.equals("Baby, You're My Sunshine")) {
			mSongTrack = DrypersResources.KidsA;
			mAutotuneParams = DrypersResources.KidsAParams;
		} else if (mSongTitle.equals("Shake it N Love it")) {
			mSongTrack = DrypersResources.KidsB;
			mAutotuneParams = DrypersResources.KidsBParams;
		} else if (mSongTitle.equals("Dancing Baby")) {
			mSongTrack = DrypersResources.Gangster;
			mAutotuneParams = DrypersResources.GangsterParams;
		} else if (mSongTitle.equals("Hip Hop Baby")) {
			mSongTrack = DrypersResources.HipHop;
			mAutotuneParams = DrypersResources.HipHopParams;
		}
	}

	private void startAutotuning() {
		doAutotuning();
	}

	private void startConfirming() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add text entry field for Babble title
		LayoutInflater factory = LayoutInflater.from(this);
		final TextView textEntryView = (TextView) factory.inflate(
				R.layout.babble_name, null);
		builder.setView(textEntryView);
		// Add the buttons
		builder.setPositiveButton("Save!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mMachine.IsInState(State.CONFIRMING)) {
							mBabbleTitle = textEntryView.getText().toString();
							try {
								mMachine.Fire(Trigger.UPLOAD);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
		builder.setNegativeButton("Go Back",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mMachine.IsInState(State.CONFIRMING)) {
							try {
								mMachine.Fire(Trigger.IDLE);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
		builder.setTitle("Saving Babble");
		builder.setMessage("Please name this babble..");

		// Create the AlertDialog
		builder.show();
	}

	private void startPlaying() {
		mPlayerVocal = new MediaPlayer();
		mPlayerSong = new MediaPlayer();

		// Set volumes
		mPlayerSong.setVolume(0.5f, 0.5f);
		try {
			if (DrypersResources.LoopedAudio.exists() && mSongTrack.exists()) {
				mPlayerVocal.setDataSource(DrypersResources.LoopedAudio
						.getAbsolutePath());
				mPlayerVocal.prepareAsync();
				mPlayerVocal.setOnPreparedListener(this);
				mPlayerSong.setDataSource(mSongTrack.getAbsolutePath());
				mPlayerSong.setOnCompletionListener(this);
				mPlayerSong.prepareAsync();
				mPlayerSong.setOnPreparedListener(this);
			}
		} catch (IOException e) {
			Log.e(RecordingActivity.class.toString(), "prepareAsnyc() failed");
		}
	}

	private void stopPlaying() {
		mPlayerVocal.release();
		mPlayerVocal = null;
		mPlayerSong.release();
		mPlayerSong = null;

		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				mMachine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void startRecording() {
		if (mTimerElapesd==0)
		{
			mSimpleRecorder = new SimpleRecorder(this,
				DrypersResources.getLibraryDirectory(),
				DrypersResources.SourceFile.getName(), this, false);
		
			mSimpleRecorder.start();
		}
		else if (mSimpleRecorder!=null)
			mSimpleRecorder.resume();
			

		// Singing Progress Indicator
		doCountDown(mPlayerSong);

		// GA Template Tracking
		EasyTracker.getTracker().sendEvent("Templates", "Template Selected",
				mTemplateText.getText().toString(), (long) 1);
	}

	private void stopRecording() {
		mSimpleRecorder.stop();
		mCountdownTimer.cancel();

		// Show button after recording process
		// mRecordButton.setBackgroundResource(R.drawable.abtn_rerecord);

		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				mMachine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void pauseRecording() {
		mSimpleRecorder.pause();
		mCountdownTimer.cancel();

		// Show button after recording process
		// mRecordButton.setBackgroundResource(R.drawable.abtn_rerecord);

		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				mMachine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void startUploading() {
		Log.i("SessionMsg", "Root URL: " + SessionHelper.hostUrl);
		if (SessionHelper.hostUrl == null) {
			SessionHelper.hostUrl = SessionHelper.GetBaseURL();
		}
		if (SessionHelper.hostUrl != null) {
			String userBabbleTitle = mBabbleTitle;
			SessionHelper.UploadBabble(userBabbleTitle, mGender,
					DrypersResources.BabyHead, DrypersResources.MuxedAudio,
					RecordingActivity.this);

			// award score
			connectionDialog(ScoreType.CREATE);
			// save recording and goes to babblebox
			connectionDialog(BabbleBoxActivity.class);
		} else {
			Toast.makeText(RecordingActivity.this, "Connection Failure",
					Toast.LENGTH_LONG).show();
			startActivity(new Intent(RecordingActivity.this,
					LoginMasterActivity.class));
			finish();
		}
	}

	private void updateViews() {
		final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.main_layout);
		mainLayout.setBackgroundResource(R.drawable.babble_bg1);

		// Melody animation
		final LinearLayout recordAnimation = (LinearLayout) findViewById(R.id.recording_anim_layer);

		switch (mMachine.getState()) {
		case IDLING:
			// Show/Hide views

			// mBackButton.setVisibility(View.VISIBLE);
			// mMenuButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mBackButton, true);
			viewStateFlipper(mMenuButton, true);
			viewStateFlipper(mTemplateSelect, true);

			mSingingBar.setVisibility(View.INVISIBLE);
			mRecordButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mRecordButton, true);

			if (mReadyToUpload && mTimerElapesd==0) {
				mPlayButton.setVisibility(View.VISIBLE);
				mSaveButton.setVisibility(View.VISIBLE);
				viewStateFlipper(mPlayButton, true);
				viewStateFlipper(mSaveButton, true);

				// Reset record button
				mRecordButton.setBackgroundResource(R.drawable.abtn_rerecord);
				// Reset background
				mainLayout.setBackgroundResource(R.drawable.babble_bg3);
			} else {
				mPlayButton.setVisibility(View.INVISIBLE);
				mSaveButton.setVisibility(View.INVISIBLE);

				// Reset record button
				mRecordButton.setBackgroundResource(R.drawable.record);
				// Reset background
				mainLayout.setBackgroundResource(R.drawable.babble_bg1);
				if (mTimerElapesd!=0)
				{
					mSingingBar.setVisibility(View.VISIBLE);
					viewStateFlipper(mSingingBar, false);
					((TextView)findViewById(R.id.singing_progress_label)).setText(R.string.recording_paused);
				}
				else
				{
					mSingingBar.setVisibility(View.INVISIBLE);
				}
			}

			// Reset background animation
			recordAnimation.setBackgroundResource(0);
			break;

		case RECORDING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);

			mSingingBar.setVisibility(View.VISIBLE);
			viewStateFlipper(mSingingBar, true);
			((TextView)findViewById(R.id.singing_progress_label)).setText(R.string.keep_singing_);
			// mRecordButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mRecordButton, true);

			mPlayButton.setVisibility(View.INVISIBLE);
			mSaveButton.setVisibility(View.INVISIBLE);

			// Set record button to stop
			// Set background
			mainLayout.setBackgroundResource(R.drawable.babble_bg1);
			mRecordButton.setBackgroundResource(R.drawable.recordstop);

			// Disco Time!
			recordAnimation.setBackgroundResource(R.drawable.anim_record);
			AnimationDrawable mFrameAnimation = (AnimationDrawable) recordAnimation
					.getBackground();
			mFrameAnimation.start();
			break;

		case CANCELING:
			break;

		case AUTOTUNING:
			// Show/Hide views
			mSingingBar.setVisibility(View.INVISIBLE);
			mRecordButton.setVisibility(View.INVISIBLE);
			mPlayButton.setVisibility(View.INVISIBLE);
			mSaveButton.setVisibility(View.INVISIBLE);
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);

			break;

		case CONFIRMING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mRecordButton, false);
			viewStateFlipper(mPlayButton, false);
			viewStateFlipper(mSaveButton, false);
			viewStateFlipper(mTemplateSelect, false);

			// mBackButton.setVisibility(View.INVISIBLE);
			// mMenuButton.setVisibility(View.INVISIBLE);
			// mSingingBar.setVisibility(View.INVISIBLE);
			// mRecordButton.setVisibility(View.INVISIBLE);
			// mPlayButton.setVisibility(View.INVISIBLE);
			// mSaveButton.setVisibility(View.INVISIBLE);
			break;

		case PLAYING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);
			// mBackButton.setVisibility(View.INVISIBLE);
			// mMenuButton.setVisibility(View.INVISIBLE);
			mSingingBar.setVisibility(View.INVISIBLE);
			// mRecordButton.setVisibility(View.INVISIBLE);
			// mPlayButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mRecordButton, false);
			viewStateFlipper(mPlayButton, true);
			viewStateFlipper(mSaveButton, false);
			// mSaveButton.setVisibility(View.INVISIBLE);

			// Toggle main background
			mainLayout.setBackgroundResource(R.drawable.babble_bg3);

			// Set play button to stop
			mPlayButton.setBackgroundResource(R.drawable.stopplaybtn);
			break;

		case STOPPING:
			// Tenderly caress views
			viewStateFlipper(mSaveButton, true);
			viewStateFlipper(mTemplateSelect, true);

			// Toggle main background
			mainLayout.setBackgroundResource(R.drawable.babble_bg1);

			// Set play button to play
			mPlayButton.setBackgroundResource(R.drawable.playbackbtn);
			break;

		case UPLOADING:
			// Show/Hide views
			viewStateFlipper(mRecordButton, false);
			viewStateFlipper(mPlayButton, false);
			viewStateFlipper(mSaveButton, false);
			viewStateFlipper(mTemplateSelect, false);
			// mRecordButton.setVisibility(View.INVISIBLE);
			// mSaveButton.setVisibility(View.INVISIBLE);
			// mPlayButton.setVisibility(View.INVISIBLE);
			mSingingBar.setVisibility(View.INVISIBLE);
			break;
		}

	}

	@SuppressLint("NewApi")
	private void viewStateFlipper(View view, Boolean state) {
		if (state) {
			view.setEnabled(true);
			view.setAlpha(1f);
		} else {
			view.setEnabled(false);
			view.setAlpha(0.3f);
		}
	}
	
	
	@SuppressLint("NewApi")
	private void verifyAutotuneIntegrity() {
		File autotuneFile = DrypersResources.AutotunePasses
				.get(mAutotunePasses - 1);
		try {
			AudioInputStream tunedStream = AudioSystem
					.getAudioInputStream(autotuneFile);
			byte[] biter = new byte[tunedStream.getFormat().getFrameSize()
					* (int) (tunedStream.getFormat().getFrameRate())];
			tunedStream.read(biter, 0, biter.length);
			int total = 0;
			for(int i = 0; i < biter.length; i++) {
				total += biter[i];
			}
			if (total == 0) {
				Log.i("Autotune", "Failed autotune. Retrying...");
				mAutotunePasses -= 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAutotune() {
		verifyAutotuneIntegrity();
		doAutotuning();
	}

	@Override
	public void onBabbleUploaded(Babble babble) {
		if (babble != null) {
			// award points with connection check
			connectionDialog(ScoreType.CREATE);
			Log.i(this.getClass().toString(), "Uploaded babble" + babble.id);
		} else {
			Log.i(this.getClass().toString(),
					"Uploaded babble but no babble returned??");
		}

		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				mMachine.Fire(Trigger.IDLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPrepared(MediaPlayer play) {
		play.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mMachine.CanFire(Trigger.STOP)) {
			try {
				mMachine.Fire(Trigger.STOP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onException(AutotuneException exception) {
		Log.e(this.getClass().toString(), exception.toString());
	}

	@Override
	public void onRecord() {
		// Check if still singing
		// Check if can fire a
		if (mMachine.IsInState(State.RECORDING) && mTimerElapesd==0) {
			try {
				mMachine.Fire(Trigger.AUTOTUNE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onException(DrypersException exception) {
		Log.e(this.getClass().toString(), exception.toString());
	}

	@Override
	public void onProgress(String message) {
	}

	@Override
	public void onBabbleLiked(Babble babble) {
	}

	@Override
	public void onBabbles(List<Babble> babbles) {
	}

	@Override
	public void onMyBabbles(List<Babble> babbles) {
	}

	@Override
	public void onFriendsBabbles(List<Babble> babbles) {
	}

	@Override
	public void onException(RecordException exception) {
	}

	@Override
	public void onBabble(Babble babble) {
	}
}
