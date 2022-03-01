package com.guichaguri.trackplayer.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.lifecycle.Observer;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.guichaguri.trackplayer.R;
import com.guichaguri.trackplayer.module.NewsItem;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgMore,imgReset,imgSpeed, skipToNext, skipToPrev, imgPause, imgPlay, skipToPrev10, skipToNext10;
    ShapeableImageView imgTrack;
    private TextView txtTitle, txtArtist, txtEpisode;
    private NewsItem newsItem;
    private Chronometer trackingChronometer;
    private Chronometer fullChronometer;
    private AppCompatSeekBar fullPanelSeekBar;
    public Handler seekHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_screen);
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        newsItem = (NewsItem) bundle.getSerializable("news");
        init();
    }

    public void init() {
        imgMore = findViewById(R.id.expand_more_image_view);
        imgReset = findViewById(R.id.reset_image_view);
        imgSpeed = findViewById(R.id.speed_image_view);
        skipToNext = findViewById(R.id.full_panel_skip_to_next);
        skipToPrev = findViewById(R.id.full_panel_skip_to_previous);
        skipToPrev10 = findViewById(R.id.skip_10_to_previous_image);
        skipToNext10 = findViewById(R.id.skip_10_to_next_image);
        imgPause = findViewById(R.id.pause_full_panel);
        imgPlay = findViewById(R.id.play_full_panel);
        imgTrack = findViewById(R.id.image_view);
        txtTitle = findViewById(R.id.full_panel_text_title);
        txtArtist= findViewById(R.id.full_panel_text_artist);
        txtEpisode = findViewById(R.id.txtEpisode);
        fullChronometer = findViewById(R.id.chronometer_full_duration);
        trackingChronometer = findViewById(R.id.chronometer_current_position);
        fullPanelSeekBar = findViewById(R.id.full_panel_seek_bar);

        imgMore.setOnClickListener(this);
        skipToNext.setOnClickListener(this);
        skipToPrev.setOnClickListener(this);
        skipToNext10.setOnClickListener(this);
        skipToPrev10.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgPause.setOnClickListener(this);
        imgReset.setOnClickListener(this);
        imgSpeed.setOnClickListener(this);

        updateUiForPlayingMediaItem(this.newsItem);

        CustomView.playerManager.getNewsData().observeForever(new Observer<NewsItem>() {
            @Override
            public void onChanged(NewsItem newsItem) {
                updateUiForPlayingMediaItem(newsItem);
            }
        });
        CustomView.playerManager.getIsPlaying().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    imgPause.setVisibility(View.VISIBLE);
                    imgPlay.setVisibility(View.INVISIBLE);
                } else {
                    imgPause.setVisibility(View.INVISIBLE);
                    imgPlay.setVisibility(View.VISIBLE);
                }
            }
        });

        fullPanelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    CustomView.playerManager.seekTo(progress);
                    seekBar.setProgress(progress);
                }
                trackingChronometer.setBase(SystemClock.elapsedRealtime() - CustomView.playerManager.getCurrentDuration());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.removeCallbacks(mUpdateTimeTask);
                int totalDuration = (int) CustomView.playerManager.getDuration();
                int currentPosition = CustomView.playerManager.progressToTimer(seekBar.getProgress(), totalDuration);
                // forward or backward to certain seconds
                CustomView.playerManager.seekTo(currentPosition);
                // update timer progress again
                updateProgressBar();
            }
        });

    }

    private void updateUiForPlayingMediaItem(NewsItem newsItem) {
        txtArtist.setText(newsItem.artist);
        txtTitle.setText(newsItem.title);
        txtEpisode.setText(newsItem.id);
        Glide.with(getApplicationContext())
                .load(newsItem.artwork)
                .into(imgTrack);
        fullChronometer.setBase((long) newsItem.getDuration());
        updateProgressBar();

    }

    public void updateProgressBar() {
        seekHandler.postDelayed(mUpdateTimeTask, 100);
    }



    // Background Runnable thread
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long currentDuration = CustomView.playerManager.getCurrentDuration();
            // Displaying Total Duration time
            fullChronometer.setBase(SystemClock.elapsedRealtime() - CustomView.playerManager.getDuration());
            trackingChronometer.setBase(SystemClock.elapsedRealtime() - currentDuration);
            // Updating progress bar
            int progress = CustomView.playerManager.getProgressPercentage();
            //Log.d("Progress", ""+progress);
            fullPanelSeekBar.setProgress(progress);
            // Running this thread after 100 milliseconds
            seekHandler.postDelayed(this, 100);
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.expand_more_image_view){
            finish();
        }
        if(id == R.id.full_panel_skip_to_next){
            CustomView.playerManager.skipToNext();
            return;
        }
        if(id == R.id.full_panel_skip_to_previous){
            CustomView.playerManager.skipToPrev();
            return;
        }
        if(id == R.id.skip_10_to_next_image){
            seekHandler.removeCallbacks(mUpdateTimeTask);
            CustomView.playerManager.increase10Seconds();
            updateProgressBar();
            return;
        }
        if(id == R.id.skip_10_to_previous_image){
            CustomView.playerManager.decrease10Seconds();
            return;
        }
        if(id == R.id.play_full_panel){
            CustomView.playerManager.play();
            return;
        }
        if(id == R.id.pause_full_panel){
            CustomView.playerManager.pause();
            return;
        }
        if(id== R.id.reset_image_view){
            showResetDialog();
            return;
        }
        if(id == R.id.speed_image_view){
            ViewDialog alert = new ViewDialog();
            alert.showSpeedChangeDialog(PlayerActivity.this);
        }
    }

    public void showResetDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
//set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                .setTitle("Are you sure you want to restart your track?")
//set message
                .setMessage("All progress in this session will be restarted.")
//set positive button
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        CustomView.playerManager.seekTo(0);
                    }
                })
//set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                       dialogInterface.dismiss();
                    }
                })
                .show();
    }



}
