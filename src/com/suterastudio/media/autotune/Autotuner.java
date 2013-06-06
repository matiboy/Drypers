package com.suterastudio.media.autotune;

import java.io.IOException;

import net.sourceforge.autotalent.Autotalent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

public class Autotuner {
	public static final int AUTOTALENT_CHUNK_SIZE = 8192;
	public static final int AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR = 48105;

	private final String mPath;
	private final String mSource;
	private final String mDestination;
	private final int mSampleRate;
	private static AutotuneHandler Listener;
	private char mKey = 'C';
	private float mPitchShift = 0.0f;
	private float mFixedPitch = 0.0f;

	public Autotuner(String path, String source, String destination,
			int sampleRate, AutotuneHandler listener, char key,
			float pitchShift, float fixedPitch) {
		this.mPath = path;
		this.mSource = source;
		this.mDestination = destination;
		this.mSampleRate = sampleRate;
		Autotuner.setListener(listener);
		// Set configuration values
		this.mKey = key;
		this.mPitchShift = pitchShift;
		this.mFixedPitch = fixedPitch;

		new AutotuneTask().execute(path, source, destination);
	}

	private static Handler autotalentTaskHandler = new Handler() {
		// use the handler to receive error messages from the recorder object
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR:
				// received error that the writer couldn't create the recording
				getListener()
						.onException(new AutotuneException(msg.toString()));
				break;
			}
		}
	};

	private void loadAutotalent(char key, float pitchShift, float fixedPitch) {
		Autotalent.instantiateAutotalent(mSampleRate);
		Autotalent.setKey(this.mKey);
		Autotalent.setConcertA(440.0f);
		Autotalent.setFixedPitch(this.mFixedPitch);
		Autotalent.setFixedPull(0.0f);
		Autotalent.setCorrectionStrength(1.0f);
		Autotalent.setCorrectionSmoothness(0.0f);
		Autotalent.setPitchShift(this.mPitchShift);
		Autotalent.setScaleRotate(0);
		Autotalent.setLfoDepth(0);
		Autotalent.setLfoRate(0);
		Autotalent.setLfoShape(0);
		Autotalent.setLfoSymmetric(0);
		Autotalent.setLfoQuantization(0);
		Autotalent.setFormantCorrection(0);
		Autotalent.setFormantWarp(-0.1f);
		Autotalent.setMix(1.0f);
	}

	private void unloadAutotalent() {
		Autotalent.destroyAutotalent();
	}

	public static AutotuneHandler getListener() {
		return Listener;
	}

	public static void setListener(AutotuneHandler listener) {
		Autotuner.Listener = listener;
	}

	private class AutotuneTask extends AsyncTask<String, Void, Void> {
		public AutotuneTask() {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(String... params) {
			// maybe ugly but we only pass one string in anyway
			String path = params[0];
			String source = params[1];
			String destination = params[2];
			Message msg = null;

			try {
				processPitchCorrection(path, source, destination);
			} catch (IOException e) {
				e.printStackTrace();
				msg = autotalentTaskHandler
						.obtainMessage(AUTOTALENT_TASK_MESSAGE_RECORDING_IO_ERROR);
			}

			if (msg != null) {
				autotalentTaskHandler.sendMessage(msg);
			}

			return null;
		}

		private void processPitchCorrection(String path, String source,
				String destination) throws IOException {
			WaveReader reader = null;
			WaveWriter writer = null;
			short[] buf = new short[AUTOTALENT_CHUNK_SIZE];
			try {
				loadAutotalent(mKey, mPitchShift, mFixedPitch);

				reader = new WaveReader(path, source);
				reader.openWave();
				writer = new WaveWriter(path, destination,
						reader.getSampleRate(), reader.getChannels(),
						reader.getPcmFormat());
				writer.createWaveFile();
				while (true) {
					int samplesRead = reader.read(buf, AUTOTALENT_CHUNK_SIZE);
					if (samplesRead > 0) {
						Log.i(this.getClass().toString(), "Samples Read: "
								+ samplesRead);
						Autotalent.processSamples(buf, samplesRead);
						writer.write(buf, 0, samplesRead);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				throw e;
			} finally {
				try {
					unloadAutotalent();
					if (reader != null) {
						reader.closeWaveFile();
					}
					if (writer != null) {
						writer.closeWaveFile();
					}
				} catch (IOException e) {
					// I hate you sometimes java
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPostExecute(Void unused) {
			getListener().onAutotune();
		}
	}

}