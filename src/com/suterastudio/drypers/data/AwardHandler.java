package com.suterastudio.drypers.data;

import java.util.List;

import com.suterastudio.drypers.network.SessionHandler;

public interface AwardHandler extends SessionHandler {
    public abstract void onAwards(List<Award> awards);
}
