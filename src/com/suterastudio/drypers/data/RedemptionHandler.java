package com.suterastudio.drypers.data;

import java.util.List;

import com.suterastudio.drypers.network.SessionHandler;

public interface RedemptionHandler extends SessionHandler {
    public abstract void onRedemptionUploaded(Redemption redemption);
    public abstract void onMyRedemptions(List<Redemption> redemption);
}
