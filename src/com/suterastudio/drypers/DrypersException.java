package com.suterastudio.drypers;

public class DrypersException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8275078278099277657L;

	public DrypersException() {
		super();
	}

	public DrypersException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DrypersException(String detailMessage) {
		super(detailMessage);
	}

	public DrypersException(Throwable throwable) {
		super(throwable);
	}

}
