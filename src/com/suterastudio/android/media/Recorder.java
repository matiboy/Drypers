package com.suterastudio.android.media;


public interface Recorder {

    public void start();
    
    public void pause();
    
    public void stop();
    
    public void cleanup();
    
    public boolean isRunning(); 
}
