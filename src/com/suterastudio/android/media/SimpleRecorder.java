package com.suterastudio.android.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.autotalent.Autotalent;
import net.sourceforge.resample.Resample;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;
import com.intervigil.wave.exception.InvalidWaveException;
import com.suterastudio.drypers.data.DrypersResources;

public class SimpleRecorder implements Recorder {

    private static final int SIMPLE_RECORDER_BUFFER_SIZE = 8192;
    
    private static final int RECORDER_MESSAGE_INVALID_INSTRUMENTAL = 8675309;
    private static final int RECORDER_MESSAGE_IO_ERROR = 8675308;
    private static final int RECORDER_MESSAGE_RECORD_ERROR = 8675310;
    private static final int RECORDER_MESSAGE_FINISHED = 8675307;

    private final Context context;
    private MicWriter writerThread;
    private final boolean isLiveMode;
    private final int sampleRate;
    private static RecordListener postRecordTask;
    private final String mPath;
    private final String mName;
    
    public SimpleRecorder(Context context, String path, String name, RecordListener postRecordTask, boolean isLiveMode) {
        this.context = context;
        this.mPath = path;
        this.mName = name;
        this.sampleRate = 22050;
        SimpleRecorder.setPostRecordTask(postRecordTask);
        this.isLiveMode = isLiveMode;
    }

    @Override
    public void start() {
        try {
            writerThread = new MicWriter(mPath, mName);
            writerThread.start();
            Log.i(this.getClass().toString(), "Started recording");
        } catch (IllegalArgumentException e) {
            Log.e(this.getClass().toString(), e.getMessage());
            getPostRecordTask().onException(new RecordException(e.getMessage()));
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            writerThread.close();
            try {
                writerThread.join();
            } catch (InterruptedException e) {}
            writerThread = null;
        }
    }

    @Override
    public void cleanup() {
        stop();
    }

    @Override
    public boolean isRunning() {
        return (writerThread != null
                && writerThread.getState() != Thread.State.NEW && writerThread
                .getState() != Thread.State.TERMINATED);
    }

    public static RecordListener getPostRecordTask() {
		return postRecordTask;
	}

	public static void setPostRecordTask(RecordListener postRecordTask) {
		SimpleRecorder.postRecordTask = postRecordTask;
	}

	private static Handler recorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORDER_MESSAGE_RECORD_ERROR:
                    getPostRecordTask().onException(new RecordException(msg.toString()));
                    break;
                case RECORDER_MESSAGE_INVALID_INSTRUMENTAL:
                    getPostRecordTask().onException(new RecordException(msg.toString()));
                    break;
                case RECORDER_MESSAGE_IO_ERROR:
                    getPostRecordTask().onException(new RecordException(msg.toString()));
                    break;
                case RECORDER_MESSAGE_FINISHED:
                    getPostRecordTask().onRecord();
                    break;
            }
        }
    };
 
    private class MicWriter extends Thread {
    	private final AudioRecord audioRecord;
        private AudioTrack audioTrack;
        private final WaveWriter writer;
        private WaveReader instrumentalReader;
        private boolean running;

        public MicWriter(String path, String name) throws IllegalArgumentException {
        	this.running = false;
            this.audioRecord = AudioHelper.getRecorder(context);
            this.writer = new WaveWriter(path,
                    name, sampleRate,
                    AudioHelper.getChannelConfig(AudioConstants.DEFAULT_CHANNEL_CONFIG),
                    AudioHelper.getPcmEncoding(AudioConstants.DEFAULT_PCM_FORMAT));

            if (isLiveMode) {
                this.audioTrack = AudioHelper.getPlayer(context);
            }

            String trackName = DrypersResources.getInstrumentalTrack(context);
            if (!trackName.equals(AudioConstants.EMPTY_STRING)) {
                // start reading from instrumental track
                this.instrumentalReader = new WaveReader(new File(trackName));
            }          
        }

        public synchronized void close() {
            running = false;
        }

        public void initialize() throws FileNotFoundException, InvalidWaveException, IOException {
            if (instrumentalReader != null) {
                instrumentalReader.openWave();
                Resample.initialize(instrumentalReader.getSampleRate(), sampleRate, Resample.DEFAULT_BUFFER_SIZE, instrumentalReader.getChannels());
            }
            writer.createWaveFile();
            if (isLiveMode) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setMode(AudioManager.MODE_NORMAL);
            }
        }

        public void cleanup() {
            // stop things
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            if (isLiveMode) {
                audioTrack.stop();
                audioTrack.release();
            }
            if (instrumentalReader != null) {
                try {
                    Resample.close();
                    instrumentalReader.closeWaveFile();
                } catch (IOException e) {
                    // no recovery possible here
                    e.printStackTrace();
                }
            }
            // close file
            try {
                writer.closeWaveFile();
            } catch (IOException e) {
                // no recovery possible here
                e.printStackTrace();
            }
        }

        public void run() {
            Message msg;
            int numSamples;
            short[] buf = new short[SIMPLE_RECORDER_BUFFER_SIZE];

            try {
                initialize();
                running = true;
                audioRecord.startRecording();
                if (isLiveMode) {
                    audioTrack.play();
                }
                while (running) {
                    numSamples = audioRecord.read(buf, 0, SIMPLE_RECORDER_BUFFER_SIZE);
                    if (isLiveMode) {
                        processLiveAudio(buf, numSamples);
                        audioTrack.write(buf, 0, numSamples);
                    }
                    writer.write(buf, 0, numSamples);
                }
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_FINISHED);
            } catch (IllegalStateException e) {
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_RECORD_ERROR);
            } catch (FileNotFoundException e) {
                // couldn't find instrumental file
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_INVALID_INSTRUMENTAL);
            } catch (InvalidWaveException e) {
                // not a wave file
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_INVALID_INSTRUMENTAL);
            } catch (IOException e) {
                // file IO error, no recovery possible?
                e.printStackTrace();
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_IO_ERROR);
            }
            cleanup();
            recorderHandler.sendMessage(msg);
        }

        private void processLiveAudio(short[] samples, int numSamples) throws IOException {
            if (instrumentalReader != null) {
                int read, resampled;
                int bufferSize = (int) (numSamples / Resample.getFactor());
                short[] instrumental = new short[bufferSize];

                if (instrumentalReader.getChannels() == 1) {
                    read = instrumentalReader.read(instrumental, bufferSize);
                } else {
                    short[] instrRight = new short[bufferSize];
                    read = instrumentalReader.read(instrumental, instrRight, bufferSize);
                    Resample.downmix(instrumental, instrumental, instrRight, read);
                }
                resampled = Resample.process(instrumental, instrumental, Resample.CHANNEL_MONO, read != bufferSize);
                Autotalent.processSamples(samples, instrumental, numSamples);
            } else {
                Autotalent.processSamples(samples, numSamples);
            }
        }        
    }
}
