package com.suterastudio.android.media;

import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;

import com.suterastudio.dsp.sampled.AudioFileFormat;
import com.suterastudio.dsp.sampled.AudioFormat;
import com.suterastudio.dsp.sampled.AudioInputStream;
import com.suterastudio.dsp.sampled.AudioSystem;
import com.suterastudio.dsp.sampled.UnsupportedAudioFileException;

public class WaveLooper {
	private File mInput;
	private File mInput2;
	private File mOutput;
	private double wavLength;
	private int desiredLength;
	private double wavRatio;

	// Class constructor
	public WaveLooper(File inTake, File inTake2, File outPut, int seconds) {
		mInput = inTake;
		mInput2 = inTake2;
		mOutput = outPut;
		desiredLength = seconds;
	}

	public void wavExtender() throws UnsupportedAudioFileException, IOException {
		if (mInput.exists()) {
			// TODO: Finish implementing indeterminate length
			wavLength = wavLength(mInput);
			if (wavLength < desiredLength) {
				wavRatio = Math.ceil(desiredLength / wavLength);
				AudioInputStream inputFileStream = AudioSystem
						.getAudioInputStream(mInput);
				AudioInputStream inputFileStream2 = AudioSystem
						.getAudioInputStream(mInput2);

				AudioInputStream appendedFiles = new AudioInputStream(
						new SequenceInputStream(inputFileStream,
								inputFileStream2), inputFileStream.getFormat(),
						inputFileStream.getFrameLength()
								+ inputFileStream2.getFrameLength());

				AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE,
						mOutput);
			}
		}
	}

	public static double wavLength(File file) {
		AudioInputStream stream = null;

		try {
			stream = AudioSystem.getAudioInputStream(file);

			AudioFormat format = stream.getFormat();

			return file.length() / format.getSampleRate()
					/ (format.getSampleSizeInBits() / 8.0)
					/ format.getChannels();
		} catch (Exception e) {
			// log an error
			return -1;
		} finally {
			try {
				stream.close();
			} catch (Exception ex) {
			}
		}
	}
}
