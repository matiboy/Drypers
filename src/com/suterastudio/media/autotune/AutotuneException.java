package com.suterastudio.media.autotune;

public class AutotuneException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8275078278099277657L;

    public AutotuneException() {
        super();
    }

    public AutotuneException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public AutotuneException(String detailMessage) {
        super(detailMessage);
    }

    public AutotuneException(Throwable throwable) {
        super(throwable);
    }

}