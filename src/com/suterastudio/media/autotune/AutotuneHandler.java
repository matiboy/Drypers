package com.suterastudio.media.autotune;

public interface AutotuneHandler {

    public void onAutotune();

    public void onException(AutotuneException exception);
}