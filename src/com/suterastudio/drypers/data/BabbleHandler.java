package com.suterastudio.drypers.data;

import java.util.List;

import com.suterastudio.drypers.network.SessionHandler;

public interface BabbleHandler extends SessionHandler {
    public abstract void onBabbleLiked(Babble babble);
    public abstract void onBabbleUploaded(Babble babble);
    public abstract void onBabbles(List<Babble> babbles);
    public abstract void onMyBabbles(List<Babble> babbles);
    public abstract void onFriendsBabbles(List<Babble> babbles);
    public abstract void onBabble(Babble babble);
}
