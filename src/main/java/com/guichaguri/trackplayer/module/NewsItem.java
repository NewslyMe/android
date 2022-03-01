package com.guichaguri.trackplayer.module;

import java.io.Serializable;

public class NewsItem implements Serializable {
    String mediaID;
    public String id;
    public String title;
    public String artist;
    public String artwork;
    double duration;
    String description;
    public String url;
    String genre;

    public NewsItem(String id, String title, String artist, String artwork, double duration, String description, String url, String genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.artwork = artwork;
        this.duration = duration;
        this.description = description;
        this.url = url;
        this.genre = genre;
    }

    public NewsItem() {
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtwork() {
        return artwork;
    }

    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "title=" + title;
    }
}