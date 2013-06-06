package com.suterastudio.android.media;

public interface RecordListener {

    public void onRecord();
    
    public void onException(RecordException exception);
}