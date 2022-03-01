# android

ExoPlayer 2.16.1
ExoPlayer is an application level media player for Android. It provides an alternative to Android’s MediaPlayer API for playing audio and video both locally and over the Internet. ExoPlayer supports features not currently supported by Android’s MediaPlayer API, including DASH and SmoothStreaming adaptive playbacks. Unlike the MediaPlayer API, ExoPlayer is easy to customize and extend, and can be updated through Play Store application updates

This project has the following feature: 

- When you click on Article/Podcast, you'll be able to add a bottom track player

- When you click on the bottom track player, the entire player screen will appear

- Play, pause, next, prev, and progressbar are all available on the bottom track player

- Play, pause, next, previous, progressbar, speed change, reset, fast forward, and backward are all available in the full screen player

- ExoPlayer features extension modules that rely on CustomView for bottom player functionality, while PlayerActivity is in charge of full screen player functionality

- MyCustomReactViewManager has a functionality to get data from React native. It is added as a requireNativeComponent on React Native platform 

- PlayerManager has functionalities for Attaching the player and player states to a view

- ViewDialog is a custom module to change the speed on full screen player

- NewsItem class is used to stored the current track data which is received from customview(Reactnative) and passes to the PlayerManager class 

- NewsItemStorage class is used to store the duration history of the track player which is played before

- SharedPrefHelper is used to store the necessary data(track id, duration, track speed choosen by user) at locally (like AsyncStorage of react native)

- Seekbar is used to make a custom speed progress bar for the full screen of the player

The following is a list of features that must be implemented:

- When you click next/previous, the playercontext should shift to the current track, and the playlist should be marked with the changes

- Background services and notification bar(while phone is lock)

- Reordering to the playlist
