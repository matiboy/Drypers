package com.suterastudio.drypers.data;

import com.suterastudio.drypers.network.SessionHandler;

public interface BabblerHandler extends SessionHandler {
    public abstract void onLogin(Babbler babbler);
    public abstract void onLogout(Babbler babbler);
    public abstract void onProfile(Babbler babbler);
    public abstract void onLookup(Babbler babbler);    
    public abstract void onUpdate(Babbler babbler);
    public abstract void onRegister(Babbler babbler);
}
