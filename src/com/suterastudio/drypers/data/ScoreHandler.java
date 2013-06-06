package com.suterastudio.drypers.data;

import java.util.List;

import com.suterastudio.drypers.network.SessionHandler;

public interface ScoreHandler extends SessionHandler {
    public abstract void onScoreUploaded(Score score);
    public abstract void onMyScores(List<Score> scores);
}
