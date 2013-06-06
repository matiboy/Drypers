package com.suterastudio.drypers.network;

import com.suterastudio.drypers.DrypersException;

public interface SessionHandler {
	public abstract void onException(DrypersException exception);
	public abstract void onProgress(String message);
}
