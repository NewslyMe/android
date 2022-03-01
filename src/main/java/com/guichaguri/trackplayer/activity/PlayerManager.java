/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guichaguri.trackplayer.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.Player.TimelineChangeReason;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.guichaguri.trackplayer.client.PlaybackInfoListener;
import com.guichaguri.trackplayer.client.PlayerAdapter;
import com.guichaguri.trackplayer.client.PlayerController;
import com.guichaguri.trackplayer.module.NewsItem;
import com.guichaguri.trackplayer.module.NewsItemStorage;
import com.guichaguri.trackplayer.preferences.SharedPrefHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** Manages players and an internal media queue for the demo app. */
/* package */ public class PlayerManager extends PlayerAdapter implements Player.Listener, PlayerController {

  private final Context context;
  private ExoPlayer localPlayer;
  private final ArrayList<MediaItem> mediaQueue;
  private TracksInfo lastSeenTrackGroupInfo;
  private int currentItemIndex;
  private final MutableLiveData<NewsItem> newsData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isPlayingMedia = new MutableLiveData<>();
  private static final String TAG = "PlayerManager";
  private PlaybackInfoListener playbackInfoListener;
  private MediaMetadataCompat currentMedia;
  private boolean mCurrentMediaPlayedToCompletion;
  private int currentState;
  private String mediaId;

  public PlayerManager(
      Context context, PlaybackInfoListener listener) {
    super(context);
    this.context = context;
    mediaQueue = new ArrayList<>();
    currentItemIndex = C.INDEX_UNSET;
    localPlayer = new ExoPlayer.Builder(context).build();
    localPlayer.addListener(this);
    playbackInfoListener = listener;
  }

  public PlayerManager(
          Context context) {
    super(context);
    this.context = context;
    mediaQueue = new ArrayList<>();
    currentItemIndex = C.INDEX_UNSET;
    localPlayer = new ExoPlayer.Builder(context).build();
    localPlayer.addListener(this);
  }

  /**
   * Plays a specified queue item in the current player.
   */
  public void selectQueueItem(int itemIndex) {
    setCurrentItem(itemIndex);
  }

  /** Returns the index of the currently played item. */
  public int getCurrentItemIndex() {
    return currentItemIndex;
  }

  /**
   * Appends {@code item} to the media queue.
   *
   * @param item The {@link MediaItem} to append.
   * @param startPlayerPos
   */
  public void addItem(MediaItem item, long startPlayerPos) {
    mediaId = item.mediaId;
    int itemIndex = mediaQueue.indexOf(item);
    if (itemIndex == -1) {
      mediaQueue.add(item);
      localPlayer.addMediaItem(item);
      setCurrentPlayer(localPlayer, startPlayerPos);
    }else {
      localPlayer.removeMediaItem(itemIndex);
      mediaQueue.remove(itemIndex);
      mediaQueue.add(item);
      localPlayer.addMediaItem(item);
      setCurrentPlayer(localPlayer, startPlayerPos);
    }
  }

  /** Returns the size of the media queue. */
  public int getMediaQueueSize() {
    return mediaQueue.size();
  }

  /**
   * Returns the item at the given index in the media queue.
   *
   * @param position The index of the item.
   * @return The item at the given index in the media queue.
   */
  public MediaItem getItem(int position) {
    return mediaQueue.get(position);
  }

  /**
   * Removes the item at the given index from the media queue.
   *
   * @param item The item to remove.
   * @return Whether the removal was successful.
   */
  public boolean removeItem(MediaItem item) {
    int itemIndex = mediaQueue.indexOf(item);
    if (itemIndex == -1) {
      return false;
    }
    localPlayer.removeMediaItem(itemIndex);
    mediaQueue.remove(itemIndex);
    if (itemIndex == currentItemIndex && itemIndex == mediaQueue.size()) {
      maybeSetCurrentItemAndNotify(C.INDEX_UNSET);
    } else if (itemIndex < currentItemIndex) {
      maybeSetCurrentItemAndNotify(currentItemIndex - 1);
    }
    return true;
  }
  /**
   * Moves an item within the queue.
   *
   * @param item The item to move.
   * @param newIndex The target index of the item in the queue.
   * @return Whether the item move was successful.
   */
  public boolean moveItem(MediaItem item, int newIndex) {
    int fromIndex = mediaQueue.indexOf(item);
    if (fromIndex == -1) {
      return false;
    }

    // Player update.
    localPlayer.moveMediaItem(fromIndex, newIndex);
    mediaQueue.add(newIndex, mediaQueue.remove(fromIndex));

    // Index update.
    if (fromIndex == currentItemIndex) {
      maybeSetCurrentItemAndNotify(newIndex);
    } else if (fromIndex < currentItemIndex && newIndex >= currentItemIndex) {
      maybeSetCurrentItemAndNotify(currentItemIndex - 1);
    } else if (fromIndex > currentItemIndex && newIndex <= currentItemIndex) {
      maybeSetCurrentItemAndNotify(currentItemIndex + 1);
    }

    return true;
  }

  /**
   * Dispatches a given {@link KeyEvent} to the corresponding view of the current player.
   *
   * @param event The {@link KeyEvent}.
   * @return Whether the event was handled by the target view.
   */


  /** Releases the manager and the players that it holds. */
  public void release() {
    currentItemIndex = C.INDEX_UNSET;
    mediaQueue.clear();
    localPlayer.release();
  }

  // Player.Listener implementation.

  @Override
  public void onPlaybackStateChanged(@Player.State int playbackState) {
    updateCurrentItemIndex();
  }

  @Override
  public void onMediaItemTransition(
          @Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
    @Nullable NewsItem metadata = null;

    if (mediaItem != null && mediaItem.localConfiguration != null) {
      mediaId = mediaItem.mediaId;
      metadata = (NewsItem) mediaItem.localConfiguration.tag;
      setNewsData(metadata);
    }
  }

  public void setNewsData(NewsItem newsItem) {
    newsData.setValue(newsItem);
  }

  public LiveData<NewsItem> getNewsData() {
    return newsData;
  }

  public LiveData<Boolean> getIsPlaying() {
    return isPlayingMedia;
  }

  public String getMediaItemID() {
    return mediaId;
  }

  /**
   * Function to get Progress percentage
   * */
  public int getProgressPercentage(){
    long totalDuration = localPlayer.getDuration();
    long currentDuration = localPlayer.getCurrentPosition();

    Double percentage = (double) 0;

    long currentSeconds = (int) (currentDuration / 1000);
    long totalSeconds = (int) (totalDuration / 1000);

    // calculating percentage
    percentage =(((double)currentSeconds)/totalSeconds)*100;

    // return percentage
    return percentage.intValue();
  }

  public int progressToTimer(int progress, int totalDuration) {
    int currentDuration = 0;
    totalDuration = (int) (totalDuration / 1000);
    currentDuration = (int) ((((double)progress) / 100) * totalDuration);

    // return current duration in milliseconds
    return currentDuration * 1000;
  }

  @Override
  public void onPositionDiscontinuity(
      Player.PositionInfo oldPosition,
      Player.PositionInfo newPosition,
      @DiscontinuityReason int reason) {
      updateCurrentItemIndex();
  }

  @Override
  public void onTimelineChanged(Timeline timeline, @TimelineChangeReason int reason) {
    updateCurrentItemIndex();
  }

  @Override
  public void onTracksInfoChanged(TracksInfo tracksInfo) {
    if (tracksInfo == lastSeenTrackGroupInfo) {
      return;
    }
    lastSeenTrackGroupInfo = tracksInfo;
  }

  // Internal methods.

  private void updateCurrentItemIndex() {
    int playbackState = localPlayer.getPlaybackState();
    maybeSetCurrentItemAndNotify(
        playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED
            ? localPlayer.getCurrentMediaItemIndex()
            : C.INDEX_UNSET);
  }

  private void setCurrentPlayer(ExoPlayer currentPlayer, long startPlayerPos) {
    this.localPlayer = currentPlayer;
    // Media queue management.
    currentPlayer.setMediaItems(mediaQueue, mediaQueue.size() - 1, startPlayerPos);
    currentPlayer.setPlayWhenReady(true);
    isPlayingMedia.setValue(true);
    currentPlayer.prepare();
    currentPlayer.play();
  }

  /**
   * Starts playback of the item at the given index.
   *
   * @param itemIndex The index of the item to play.
   */
  private void setCurrentItem(int itemIndex) {
    maybeSetCurrentItemAndNotify(itemIndex);
    if (localPlayer.getCurrentTimeline().getWindowCount() != mediaQueue.size()) {
      // This only happens with the cast player. The receiver app in the cast device clears the
      // timeline when the last item of the timeline has been played to end.
      localPlayer.setMediaItems(mediaQueue, itemIndex, C.TIME_UNSET);
    } else {
      localPlayer.seekTo(itemIndex, C.TIME_UNSET);
    }
    localPlayer.setPlayWhenReady(true);
  }

  private void maybeSetCurrentItemAndNotify(int currentItemIndex) {
    if (this.currentItemIndex != currentItemIndex) {
      int oldIndex = this.currentItemIndex;
      this.currentItemIndex = currentItemIndex;
//      listener.onQueuePositionChanged(oldIndex, currentItemIndex);
    }
  }

  public void skipToNext(){
    localPlayer.seekToNextMediaItem();
  }

  public void skipToPrev(){
    localPlayer.seekToPreviousMediaItem();
  }

  @Override
  protected void onPlay() {
    if (localPlayer != null) {
      isPlayingMedia.setValue(true);
      localPlayer.setPlayWhenReady(true);
      localPlayer.play();
    }
  }

  @Override
  protected void onPause() {
    if (localPlayer != null) {
      isPlayingMedia.setValue(false);
      localPlayer.setPlayWhenReady(false);
      localPlayer.pause();
    }
  }

  @Override
  public void playFromMedia(MediaMetadataCompat metadata) {
    playFile(metadata);
    startTrackingPlayback();
    Log.d(TAG, "playFromMedia: called");
  }


  @Override
  public MediaMetadataCompat getCurrentMedia() {
    return currentMedia;
  }

  @Override
  public boolean isPlaying() {
    return localPlayer != null && localPlayer.getPlayWhenReady();
  }

  @Override
  protected void onStop() {
    Log.d(TAG, "onStop: called");
    setNewState(PlaybackStateCompat.STATE_STOPPED);
    release();
  }

  @Override
  public void seekTo(long position) {
    if (localPlayer != null) {
      localPlayer.seekTo(position);
    }
  }

  @Override
  public void setVolume(float volume) {
    if (localPlayer != null) {
      localPlayer.setVolume(volume);
    }
  }

  @Override
  public void setSpeed(float speed) {
    PlaybackParameters parameters = new PlaybackParameters(speed);
    localPlayer.setPlaybackParameters(parameters);
  }

  @Override
  public void increase10Seconds() {
    localPlayer.seekTo(localPlayer.getContentPosition()+10000);
  }

  @Override
  public void decrease10Seconds() {
    if(localPlayer.getContentPosition() <= 10000){
      localPlayer.seekTo(0);
    }else{
      localPlayer.seekTo(localPlayer.getContentPosition() - 10000);
    }
  }

  @Override
  public long getDuration() {
    return localPlayer.getDuration();
  }

  public long getCurrentDuration() {
    return localPlayer.getCurrentPosition();
  }

  @Override
  public void speedUp(float speed) {

  }
  private void startTrackingPlayback() {
    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (isPlaying()) {
          playbackInfoListener.onSeekTo(localPlayer.getContentPosition(), localPlayer.getDuration());
          handler.postDelayed(this, 100);
        }
        //the getDuration method returns a negative one when the audio is stopped
        if (localPlayer.getContentPosition() >= localPlayer.getDuration()
                && localPlayer.getDuration() > 0) {
          playbackInfoListener.onPlaybackComplete();
        }
      }
    };
    handler.postDelayed(runnable, 100);
  }

  private void playFile(MediaMetadataCompat metaData) {
    String mediaId = metaData.getDescription().getMediaId();
    boolean mediaChanged = (currentMedia == null) || !mediaId.equals(currentMedia.getDescription().getMediaId());
    if (mCurrentMediaPlayedToCompletion) {
      mediaChanged = true;
      mCurrentMediaPlayedToCompletion = false;
    }
    if (!mediaChanged) {
      if (!isPlaying()) {
        play();

      }
      return;
    } else {
      release();
    }
    currentMedia = metaData;
  //setCurrentPlayer(localPlayer);
    play();
    Log.d(TAG, "playFile: play ya nigm");

  }
  // This is the main reducer for the player state machine.
  private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
    Log.d(TAG, "setNewState: Called");
    currentState = newPlayerState;

    if (currentState == PlaybackStateCompat.STATE_STOPPED) {
      mCurrentMediaPlayedToCompletion = true;
    }

    long reportTime = localPlayer == null ? 0 : localPlayer.getCurrentPosition();
    publicStateBuilder(reportTime);
  }

  //this is the method that sends the state to the media session
  private void publicStateBuilder(long reportTime) {
    Log.d(TAG, "publicStateBuilder: Called");
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
    stateBuilder.setActions(getAvailableActions())
            .setState(currentState, reportTime, 1.0f, SystemClock.elapsedRealtime());
//    setSpeedCustomAction(stateBuilder);
    playbackInfoListener.onPlaybackStateChanged(stateBuilder.build());
    playbackInfoListener.updateUI(currentMedia.getDescription().getMediaId());
  }

  @PlaybackStateCompat.Actions
  private long getAvailableActions() {
    long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
            | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
    switch (currentState) {
      case PlaybackStateCompat.STATE_STOPPED:
        actions |= PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PAUSE;
        break;
      case PlaybackStateCompat.STATE_PLAYING:
        actions |= PlaybackStateCompat.ACTION_STOP
                | PlaybackStateCompat.ACTION_PAUSE
                | PlaybackStateCompat.ACTION_SEEK_TO;
        break;
      case PlaybackStateCompat.STATE_PAUSED:
        actions |= PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_STOP;
        break;
      default:
        actions |= PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PLAY_PAUSE
                | PlaybackStateCompat.ACTION_STOP
                | PlaybackStateCompat.ACTION_PAUSE;
    }
    return actions;
  }
}
