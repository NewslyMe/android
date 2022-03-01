package com.guichaguri.trackplayer.module;

public class NewsItemStorage {
    long duration;
    long lastPosition;

    public NewsItemStorage(long duration, long lastPosition) {
        this.duration = duration;
        this.lastPosition = lastPosition;

    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(long lastPosition) {
        this.lastPosition = lastPosition;
    }
}
