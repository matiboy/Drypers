package com.suterastudio.media.autotune;

public class AutotuneParameter {
	public float mPitch = 0;
	public char mKey = 'C';
	
	public AutotuneParameter(float pitch, char key) {
		mPitch = pitch;
		key = mKey;
	}

	public char getKey() {
		return mKey;
	}
	
	public float getPitch() {
		return mPitch;
	}
}
