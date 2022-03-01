package com.guichaguri.trackplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.exoplayer2.MediaItem;
import com.guichaguri.trackplayer.R;
import com.guichaguri.trackplayer.module.NewsItem;
import com.guichaguri.trackplayer.module.NewsItemStorage;
import com.guichaguri.trackplayer.preferences.SharedPrefHelper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class CustomView extends LinearLayout implements View.OnClickListener{

    Context context;
    ReadableArray readableNativeArray;
    private TextView txtTitle, txtArtist;
    ImageView imgPlayer, imgSkipToPrev, imgSkipToNext;
    private RelativeLayout bottomMediaControllerLayout;
    private ImageView playImage, pauseImage;
    private ProgressBar seekBar;
    public Handler seekHandler = new Handler();
    private NewsItem newsItem;
   public static PlayerManager playerManager;

    public CustomView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public void init() {
        inflate(this.context, R.layout.activity_main, this);
        playerManager =
                new PlayerManager(this.context);
        bottomMediaControllerLayout = findViewById(R.id.bottom_panel);
        txtTitle = findViewById(R.id.media_title_text_view);
        txtArtist =  findViewById(R.id.media_artist_text_view);
        imgPlayer =  findViewById(R.id.image_media);
        playImage = findViewById(R.id.play_image_view);
        pauseImage = findViewById(R.id.pause_image_view);
        imgSkipToNext = findViewById(R.id.bottom_panel_skip_to_next);
        imgSkipToPrev = findViewById(R.id.bottom_panel_skip_to_prev);
        seekBar = findViewById(R.id.horizontal_progress_bar);

        playImage.setOnClickListener(this);
        pauseImage.setOnClickListener(this);
        imgSkipToNext.setOnClickListener(this);
        imgSkipToPrev.setOnClickListener(this);
        bottomMediaControllerLayout.setOnClickListener(this);

    }

    public void setPlaylist(ReadableArray readableNativeArray) {
        this.readableNativeArray = readableNativeArray;
    }

    public void setTrackInfo(ReadableMap readableMap) {
        newsItem = new NewsItem(
                readableMap.getString("id"),
                readableMap.getString("title"),
                readableMap.getString("artist"),
                readableMap.getString("artwork"),
                readableMap.getDouble("duration"),
                readableMap.getString("description"),
                readableMap.getString("url"),
                readableMap.getString("genre"));
        if(SharedPrefHelper.getSharedOBJECT(this.context,newsItem.id) != null) {
            NewsItemStorage data = (NewsItemStorage) SharedPrefHelper.getSharedOBJECT(this.context, newsItem.id);
            setPlayer(newsItem, data.getLastPosition());
        }else{
            setPlayer(newsItem, 0);
        }

        playerManager.setNewsData(newsItem);
        playerManager.getNewsData().observeForever(new Observer<NewsItem>() {
            @Override
            public void onChanged(NewsItem newsItem) {
                updateUiForPlayingMediaItem(newsItem);
            }
        });
        playerManager.getIsPlaying().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    playImage.setVisibility(View.INVISIBLE);
                    pauseImage.setVisibility(View.VISIBLE);
                }else{
                    playImage.setVisibility(View.VISIBLE);
                    pauseImage.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private void setPlayer(NewsItem newsItem, long startPlayerPos){
        try {
            URL url = new URL(newsItem.url);
            Uri uri = Uri.parse(url.toURI().toString());
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(newsItem.id)
                    .setTag(newsItem)
                    .build();
            playerManager.addItem(mediaItem, startPlayerPos);
        } catch (MalformedURLException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
    private void initializeSeekbar(){
        seekBar.setProgress(0);
        seekBar.setMax(100);
        updateProgressBar();
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_image_view) {
            playerManager.play();

            return;
        }
        else if (id == R.id.pause_image_view) {
            playerManager.pause();

            return;
        }
        else if (id == R.id.bottom_panel_skip_to_next) {
            playerManager.skipToNext();
            return;
        }
        else if (id == R.id.bottom_panel_skip_to_prev) {
            playerManager.skipToPrev();
            return;
        }
        else if(id == R.id.bottom_panel){
            Intent intent = new Intent(this.context, PlayerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("news", newsItem);
            intent.putExtras(bundle);
            this.context.startActivity(intent);
        }
    }

    private void updateUiForPlayingMediaItem(NewsItem metadata) {
        txtTitle.setText(metadata.title);
        txtArtist.setText(metadata.artist);
        Glide.with(context.getApplicationContext())
                .load(metadata.artwork)
                .into(imgPlayer);
        initializeSeekbar();
    }

    public void storeToPref(long duration, long position){
    NewsItemStorage newsItemStorage = new NewsItemStorage(duration, position);
    SharedPrefHelper.setSharedOBJECT(this.context,newsItem.id,newsItemStorage);

    }

    public void updateProgressBar() {
        seekHandler.postDelayed(mUpdateTimeTask, 100);
    }

// Background Runnable thread
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            // Updating progress bar
            int progress = playerManager.getProgressPercentage();
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);
            // Running this thread after 100 milliseconds
            seekHandler.postDelayed(this, 100);
            storeToPref(playerManager.getDuration(), playerManager.getCurrentDuration());
        }
    };


}
