package com.suterastudio.android.media;

public class RecordException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8275078278099277657L;

	public RecordException() {
		super();
	}

	public RecordException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RecordException(String detailMessage) {
		super(detailMessage);
	}

	public RecordException(Throwable throwable) {
		super(throwable);
	}

}
