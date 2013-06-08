package com.suterastudio.drypers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.autotalent.Autotalent;

import org.customsoft.stateless4j.StateMachine;
import org.customsoft.stateless4j.delegates.Action;

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
import com.suterastudio.drypers.data.LanguagePreference;
import com.suterastudio.drypers.data.LearningState;
import com.suterastudio.drypers.network.SessionHelper;
import com.suterastudio.drypers.network.SessionHelper.Gender;
import com.suterastudio.drypers.network.SessionHelper.ScoreType;
import com.suterastudio.dsp.sampled.AudioFileFormat;
import com.suterastudio.dsp.sampled.AudioInputStream;
import com.suterastudio.dsp.sampled.AudioSystem;
import com.suterastudio.dsp.sampled.UnsupportedAudioFileException;
import com.suterastudio.media.autotune.AutotuneException;
import com.suterastudio.media.autotune.AutotuneHandler;
import com.suterastudio.media.autotune.AutotuneParameter;
import com.suterastudio.media.autotune.Autotuner;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LearningActivity extends GenericActivity implements
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

	// Learning state goodies
	LearningState previousLearningState;
	private MediaPlayer mVoiceMeadiaPlayer;

	// View goodies
	private ImageView mTemplateSelect = null;
	private TextView mTemplateText = null;
	private Button mBackButton = null;
	private Button mMenuButton = null;
	// new View goodies
	private Button mLeftButton;
	private Button mRightButton;
	private Button mSaveButton;
	private ImageView mObjectImageView;
	private ImageView mLetterImageView;
	private ImageView mTextImageView;
	private ImageView mPopUpImageView;
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
				new AlertDialog.Builder(LearningActivity.this)
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
												LearningActivity.this,
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
				startActivity(new Intent(LearningActivity.this,
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

	private OnClickListener mBackgroundClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)) {
				playLearningVoice();
			}

		}
	};
	private OnClickListener mSaveButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMachine.CanFire(Trigger.CONFIRM)) {
				try {
					mMachine.Fire(Trigger.CONFIRM);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	private OnClickListener mLeftClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)
					&& !LearningState.isFirstState()) {
				try {
					LearningState.goToPreviousState();
					stopLearningVoice();
					updateViews();
					mCountdownTimer = new CountDownTimer(800, 800) {
						public void onTick(long millisUntilFinished) {
						}

						public void onFinish() {
							playLearningVoice();
						}
					}.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	};
	private OnClickListener mRightClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMachine.IsInState(State.IDLING)
					&& !LearningState.isLastState()) {
				try {
					LearningState.goToNextState();
					stopLearningVoice();
					updateViews();
					mCountdownTimer = new CountDownTimer(800, 800) {
						public void onTick(long millisUntilFinished) {
						}

						public void onFinish() {
							playLearningVoice();
						}
					}.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	};

	private OnCompletionListener learningVoiceCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			stopLearningVoice();
			if (mMachine.IsInState(State.IDLING)
					&& mMachine.CanFire(Trigger.RECORD)) {
				try {
					mMachine.Fire(Trigger.RECORD);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			stopRecording();
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
			stopPlaying();
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
	private LanguagePreference chosenLanguage;

	// //
	// Constructor(s)
	// //

	public LearningActivity() {
		try {
			chosenLanguage = ContextHelper.thawChosenLanguage();

			mVoiceMeadiaPlayer = new MediaPlayer();
			mMachine.Configure(State.IDLING).OnEntry(mIdler)
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
		setContentView(R.layout.learning);

		// Initialize states
		LearningState.initializeState(chosenLanguage);

		// Create views
		createViews();

		// Update views
		updateViews();

		// Manage (and potentially create) and Songs
		checkSongs();

		//Set song from previous activity
		String template=ContextHelper.getSelectedSong();
		if (template!=null &&!template.isEmpty())
			mTemplateText.setText(template);
		// Set song
		setSong();
		
		// say the first letter
		mCountdownTimer = new CountDownTimer(1000, 1000) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				playLearningVoice();
			}
		}.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		// TODO: CHANGE HEAD ACCORDING TO GENDER
		ImageView babyGender = (ImageView) findViewById(R.id.learning_baby);
		// get gender data from previous activity and change the baby's gender
		// appropriately
		mGender = DrypersResources.ThawGender(this);
		if (mGender == Gender.BOY) {
			babyGender.setImageResource(R.drawable.__learning_baby1);
		} else {
			babyGender.setImageResource(R.drawable.babygirl2);
		}
	}

	/**
*
*/
	@Override
	public void onPause() {
		super.onPause();
		mTimerElapesd = 0;
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
		if (mVoiceMeadiaPlayer != null) {
			if (mVoiceMeadiaPlayer.isPlaying()) {
				mVoiceMeadiaPlayer.stop();
			}
			mVoiceMeadiaPlayer.reset();
		}

		if (mCountdownTimer != null) {
			mCountdownTimer.cancel();
		}
		if (mObjectImageView!=null)
			mObjectImageView.clearAnimation();
		if (mLetterImageView!=null)
		mLetterImageView.clearAnimation();

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
		if (mVoiceMeadiaPlayer!=null)
			mVoiceMeadiaPlayer.release();
		mVoiceMeadiaPlayer=null;
		mPlayerVocal=null;
		mPlayerSong=null;
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

		if (!DrypersResources.Learning_A_Bahasa.exists()
				|| !DrypersResources.Learning_B_Bahasa.exists()
				|| !DrypersResources.Learning_C_Bahasa.exists()
				|| !DrypersResources.Learning_D_Bahasa.exists()
				|| !DrypersResources.Learning_E_Bahasa.exists()) {
			ContextHelper.createAsset(this, DrypersResources.Learning_A_Bahasa,
					"a_bah.mp3", "a_bah.mp3");
			ContextHelper.createAsset(this, DrypersResources.Learning_B_Bahasa,
					"b_bah.mp3", "b_bah.mp3");
			ContextHelper.createAsset(this, DrypersResources.Learning_C_Bahasa,
					"c_bah.mp3", "c_bah.mp3");
			ContextHelper.createAsset(this, DrypersResources.Learning_D_Bahasa,
					"d_bah.mp3", "d_bah.mp3");
			ContextHelper.createAsset(this, DrypersResources.Learning_E_Bahasa,
					"e_bah.mp3", "e_bah.mp3");
		}
		if (!DrypersResources.Learning_A_English.exists()
				|| !DrypersResources.Learning_B_English.exists()
				|| !DrypersResources.Learning_C_English.exists()
				|| !DrypersResources.Learning_D_English.exists()
				|| !DrypersResources.Learning_E_English.exists()) {
			ContextHelper.createAsset(this,
					DrypersResources.Learning_A_English, "a_eng.mp3",
					"a_eng.mp3");
			ContextHelper.createAsset(this,
					DrypersResources.Learning_B_English, "b_eng.mp3",
					"b_eng.mp3");
			ContextHelper.createAsset(this,
					DrypersResources.Learning_C_English, "c_eng.mp3",
					"c_eng.mp3");
			ContextHelper.createAsset(this,
					DrypersResources.Learning_D_English, "d_eng.mp3",
					"d_eng.mp3");
			ContextHelper.createAsset(this,
					DrypersResources.Learning_E_English, "e_eng.mp3",
					"e_eng.mp3");
		}

		// Create files
		// DrypersResources.AutotunePass1.createNewFile();
		// DrypersResources.AutotunePass2.createNewFile();
		// DrypersResources.AutotunePass3.createNewFile();
		// DrypersResources.AutotunePass4.createNewFile();
		// DrypersResources.AutotunePass5.createNewFile();
	}

	private void createViews() {

		// Element Bindings
		mBackButton = (Button) findViewById(R.id.back_recording);
		mMenuButton = (Button) findViewById(R.id.menu_recording);
		ImageView babyHead = (ImageView) findViewById(R.id.learning_image_babyhead);
		ImageView babyGender = (ImageView) findViewById(R.id.learning_baby);
		mTemplateSelect = (ImageView) findViewById(R.id.active_template);
		mTemplateText = (TextView) findViewById(R.id.template_name);

		// get gender data from previous activity and change the baby's gender
		// appropriately
		mGender = DrypersResources.ThawGender(this);
		if (mGender == Gender.BOY) {
			babyGender.setImageResource(R.drawable.__learning_baby1);
		} else {
			babyGender.setImageResource(R.drawable.babygirl2);
		}

		if (DrypersResources.BabyHead.exists()) {
			Bitmap savedHead = (Bitmap) BitmapFactory
					.decodeFile(DrypersResources.BabyHead.getAbsolutePath());
			Drawable headDrawable = new BitmapDrawable(getResources(),
					savedHead);
			babyHead.setImageDrawable(headDrawable);
		}
		// Button Click Listeners
		mBackButton.setOnClickListener(mBackClickListener);
		mMenuButton.setOnClickListener(mMenuClickListener);

		// Create context pointer
		mTemplateSelect.setOnClickListener(mTemplateClickListener);

		mLeftButton = (Button) findViewById(R.id.learning_btn_left);
		mRightButton = (Button) findViewById(R.id.learning_btn_right);
		mSaveButton = (Button) findViewById(R.id.learning_button_save);
		mObjectImageView = (ImageView) findViewById(R.id.learning_image_object);
		mLetterImageView = (ImageView) findViewById(R.id.learning_image_letter);
		mTextImageView = (ImageView) findViewById(R.id.learning_image_text);
		mPopUpImageView = (ImageView) findViewById(R.id.learning_notification);
		RelativeLayout mLearningArea = (RelativeLayout) findViewById(R.id.learning_area_repeat);
		// listener binding
		mLeftButton.setOnClickListener(mLeftClickListener);
		mRightButton.setOnClickListener(mRightClickListener);
		mSaveButton.setOnClickListener(mSaveButtonClickListener);
		mLearningArea.setOnClickListener(mBackgroundClickListener);
		int count = mLearningArea.getChildCount();
		for (int i = 0; i < count; i++) {
			mLearningArea.getChildAt(i).setOnClickListener(
					mBackgroundClickListener);
		}
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

	protected int mTimerElapesd = 0;

	private void doCountDown(final MediaPlayer mediaPlayer) {
		mCountdownTimer = new CountDownTimer(4000, 250) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				mTimerElapesd = 0;
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

		Log.i(LearningActivity.class.getName(), "finished muxing");
		if (mMachine.CanFire(Trigger.IDLE)) {
			try {
				Log.i(LearningActivity.class.getName(), "trying to fire IDLE)");
				mMachine.Fire(Trigger.IDLE);
				startPlaying();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setSong() {
		// Set song title
		mSongTitle = mTemplateText.getText().toString();
		ContextHelper.setSelectedSong(mSongTitle);
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
		textEntryView.setText(LearningState.getCurrentstate().getBabbleName());
		builder.setView(textEntryView);
		// Add the buttons
		builder.setPositiveButton("Save!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
							mBabbleTitle = textEntryView.getText().toString();
							try {
								mMachine.Fire(Trigger.UPLOAD);
							} catch (Exception e) {
								e.printStackTrace();
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
		startAnimatingObjects();
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
			Log.e(LearningActivity.class.toString(), "prepareAsnyc() failed");
		}
	}

	private void stopPlaying() {

		stopAnimatingObjects();
		mPlayerVocal.release();
		mPlayerVocal = null;
		mPlayerSong.release();
		mPlayerSong = null;
		//
		// if (mMachine.CanFire(Trigger.IDLE)) {
		// try {
		// mMachine.Fire(Trigger.IDLE);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
	}

	private void startRecording() {
		if (mTimerElapesd == 0) {
			mSimpleRecorder = new SimpleRecorder(this,
					DrypersResources.getLibraryDirectory(),
					DrypersResources.SourceFile.getName(), this, false);

			mSimpleRecorder.start();
		} else if (mSimpleRecorder != null)
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

	private void startUploading() {
		Log.i("SessionMsg", "Root URL: " + SessionHelper.hostUrl);
		if (SessionHelper.hostUrl == null) {
			SessionHelper.hostUrl = SessionHelper.GetBaseURL();
		}
		if (SessionHelper.hostUrl != null) {
			String userBabbleTitle = mBabbleTitle;
			SessionHelper.UploadBabble(userBabbleTitle, mGender,
					DrypersResources.BabyHead, DrypersResources.MuxedAudio,
					LearningActivity.this);

			// award score
			connectionDialog(ScoreType.CREATE);
			// save recording and goes to babblebox
			connectionDialog(BabbleBoxActivity.class);
		} else {
			Toast.makeText(LearningActivity.this, "Connection Failure",
					Toast.LENGTH_LONG).show();
			startActivity(new Intent(LearningActivity.this,
					LoginMasterActivity.class));
			finish();
		}
	}

	private void animateViewPrevious(final ImageView v, final int nextPicture) {
		TranslateAnimation trans = new TranslateAnimation(0, 100, 0, 0);
		trans.setDuration(400);
		AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
		alpha.setDuration(400);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(trans);
		set.addAnimation(alpha);

		TranslateAnimation trans1 = new TranslateAnimation(-100, 0, 0, 0);
		trans1.setDuration(400);
		AlphaAnimation alpha1 = new AlphaAnimation(0f, 1f);
		alpha1.setDuration(400);
		final AnimationSet set1 = new AnimationSet(true);
		set1.addAnimation(trans1);
		set1.addAnimation(alpha1);

		set.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.INVISIBLE);
				v.setImageResource(nextPicture);
				v.startAnimation(set1);
				v.setVisibility(View.VISIBLE);
			}
		});
		v.startAnimation(set);

	}

	private void animateViewNext(final ImageView v, final int nextPicture) {
		TranslateAnimation trans = new TranslateAnimation(0, -100, 0, 0);
		trans.setDuration(400);
		AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
		alpha.setDuration(400);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(trans);
		set.addAnimation(alpha);

		TranslateAnimation trans1 = new TranslateAnimation(100, 0, 0, 0);
		trans1.setDuration(400);
		AlphaAnimation alpha1 = new AlphaAnimation(0f, 1f);
		alpha1.setDuration(400);
		final AnimationSet set1 = new AnimationSet(true);
		set1.addAnimation(trans1);
		set1.addAnimation(alpha1);

		set.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.INVISIBLE);
				v.setImageResource(nextPicture);
				v.startAnimation(set1);
				v.setVisibility(View.VISIBLE);
			}
		});
		v.startAnimation(set);

	}

	private void updateViews() {
		final ImageView backgroundImage = (ImageView) findViewById(R.id.learning_image_background);
		backgroundImage.setImageResource(R.drawable.learning_bg);
		mPopUpImageView.setVisibility(View.INVISIBLE);
		// Melody animation

		switch (mMachine.getState()) {
		case IDLING:
			// Show/Hide views

			// mBackButton.setVisibility(View.VISIBLE);
			// mMenuButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mBackButton, true);
			viewStateFlipper(mMenuButton, true);
			viewStateFlipper(mTemplateSelect, true);
			RelativeLayout mLearningArea = (RelativeLayout) findViewById(R.id.learning_area_repeat);

			ImageView learningBG = (ImageView) findViewById(R.id.learning_image_background);
			if (!mReadyToUpload) {
				mSaveButton.setVisibility(View.INVISIBLE);
				learningBG.setImageResource(R.drawable.learning_bg);
				findViewById(R.id.relativeLayout1).setVisibility(View.VISIBLE);
				mBackButton.setOnClickListener(mBackClickListener);
				mLearningArea.setOnClickListener(mBackgroundClickListener);
				int count = mLearningArea.getChildCount();
				for (int i = 0; i < count; i++) {
					mLearningArea.getChildAt(i).setOnClickListener(
							mBackgroundClickListener);
					viewStateFlipper(mTemplateSelect, true);
				}

			} else {
				mSaveButton.setVisibility(View.VISIBLE);
				viewStateFlipper(mSaveButton, true);
				learningBG.setImageResource(R.drawable.babble_bg3);
				findViewById(R.id.relativeLayout1)
						.setVisibility(View.INVISIBLE);
				View.OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(View v) {
						stopPlaying();
						updateViews();
					}
				};
				mBackButton.setOnClickListener(listener);

				mReadyToUpload = false;
				View.OnClickListener listener1 = new OnClickListener() {

					@Override
					public void onClick(View v) {
						stopPlaying();
						startPlaying();
					}
				};
				mLearningArea.setOnClickListener(listener1);
				int count = mLearningArea.getChildCount();
				for (int i = 0; i < count; i++) {
					mLearningArea.getChildAt(i).setOnClickListener(listener1);
				}
				viewStateFlipper(mTemplateSelect, false);
			}
			mPopUpImageView.setVisibility(View.INVISIBLE);
			LearningState state = LearningState.getCurrentstate();
			if (LearningState.isFirstState()) {
				mLeftButton.setEnabled(false);
				viewStateFlipper(mLeftButton, false);
			} else {
				mLeftButton.setEnabled(true);
				viewStateFlipper(mLeftButton, true);
			}
			if (LearningState.isLastState()) {
				mRightButton.setEnabled(false);
				viewStateFlipper(mRightButton, false);
			} else {
				mRightButton.setEnabled(true);
				viewStateFlipper(mRightButton, true);
			}
			if (previousLearningState != null) {
				if (previousLearningState.getIndex() > state.getIndex()) {
					animateViewPrevious(mObjectImageView,
							state.getObjectResource());
					animateViewPrevious(mLetterImageView,
							state.getLetterResource());
					animateViewPrevious(mTextImageView,
							state.getWritingResource());
					animateViewPrevious((ImageView)findViewById(R.id.learning_shadow),
							R.drawable.__learning_shadow);
				} else if (previousLearningState.getIndex() < state.getIndex()) {
					animateViewNext(mObjectImageView, state.getObjectResource());
					animateViewNext(mLetterImageView, state.getLetterResource());
					animateViewNext(mTextImageView, state.getWritingResource());
					animateViewNext((ImageView)findViewById(R.id.learning_shadow),
							R.drawable.__learning_shadow);
				}
				
			}
			else
			{
				mObjectImageView.setImageResource(state.getObjectResource());
				mLetterImageView.setImageResource(state.getLetterResource());
				mTextImageView.setImageResource(state.getWritingResource());
			}
			previousLearningState = state;
			break;

		case RECORDING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);

			mPopUpImageView.setVisibility(View.VISIBLE);
			// Disco Time!
			break;

		case CANCELING:
			break;

		case AUTOTUNING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);
			backgroundImage.setImageResource(R.drawable.babble_bg3);
			findViewById(R.id.relativeLayout1).setVisibility(View.INVISIBLE);
			mSaveButton.setVisibility(View.VISIBLE);
			viewStateFlipper(mSaveButton, false);

			break;

		case CONFIRMING:
			// Show/Hide views
			viewStateFlipper(mBackButton, false);
			viewStateFlipper(mMenuButton, false);
			viewStateFlipper(mTemplateSelect, false);

			backgroundImage.setImageResource(R.drawable.babble_bg3);
			findViewById(R.id.relativeLayout1).setVisibility(View.INVISIBLE);
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
			// mSaveButton.setVisibility(View.INVISIBLE);

			break;

		case STOPPING:
			// Tenderly caress views
			viewStateFlipper(mTemplateSelect, true);

			break;

		case UPLOADING:
			backgroundImage.setImageResource(R.drawable.babble_bg3);
			findViewById(R.id.relativeLayout1).setVisibility(View.INVISIBLE);
			viewStateFlipper(mTemplateSelect, false);
			// mRecordButton.setVisibility(View.INVISIBLE);
			// mSaveButton.setVisibility(View.INVISIBLE);
			// mPlayButton.setVisibility(View.INVISIBLE);
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
			for (int i = 0; i < biter.length; i++) {
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
		stopAnimatingObjects();
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
		if (mMachine.IsInState(State.RECORDING) && mTimerElapesd == 0) {
			try {
				mMachine.Fire(Trigger.AUTOTUNE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//Playback
	public void playLearningVoice() {
		try {
			stopLearningVoice();
			mVoiceMeadiaPlayer = new MediaPlayer();
			mVoiceMeadiaPlayer.setLooping(false);
			mVoiceMeadiaPlayer
					.setOnCompletionListener(learningVoiceCompletionListener);
			mVoiceMeadiaPlayer.setDataSource(LearningState.getCurrentstate()
					.getFile().getAbsolutePath());
			mVoiceMeadiaPlayer.setOnPreparedListener(this);
			mVoiceMeadiaPlayer.prepareAsync();
			mVoiceMeadiaPlayer.setVolume(0.5f, 0.5f);
		} catch (Exception e) {
			Log.e(LearningActivity.class.getName(), e.getMessage());
		}
	}
	private void startAnimatingObjects()
	{
		RotateAnimation animation=new RotateAnimation(-20,20,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		animation.setDuration(400);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		TranslateAnimation anim1=new TranslateAnimation(0, 0, 0, -60);
		anim1.setDuration(400);
		anim1.setRepeatCount(Animation.INFINITE);
		anim1.setRepeatMode(Animation.REVERSE);
		mLetterImageView.startAnimation(animation);
		mObjectImageView.startAnimation(anim1);
	}
	private void stopAnimatingObjects()
	{
		mObjectImageView.clearAnimation();
		mLetterImageView.clearAnimation();
	}
	private void stopLearningVoice() {
		if (mVoiceMeadiaPlayer == null)
			return;
		mVoiceMeadiaPlayer.release();
		mVoiceMeadiaPlayer = null;
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
