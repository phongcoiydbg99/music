package com.example.music.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;

import static android.support.v4.media.session.MediaSessionCompat.*;

public class MediaPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    public static final String SONG_PLAY_COMPLETE = "song_play_complete";
    public static final String SONG_PLAY_CHANGE = "song_play_change";
    public static final String MESSAGE_SONG_PLAY_COMPLETE = "message_song_play_complete";
    public static final String MESSAGE_SONG_PLAY_CHANGE = "message_song_play_change";
    public static final String SONG_STATE_PLAY = "song_state_play";
    public static final String SONG_STATE_PAUSE = "song_state_pause";
    public static final int NOTIFICATION_CHANNEL = 112;
    public static final int NOTIFICATION_ID = 111;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private static final String MUSIC_SERVICE_ACTION_PAUSE = "music_service_action_pause";
    private static final String MUSIC_SERVICE_ACTION_PLAY = "music_service_action_play";
    private static final String MUSIC_SERVICE_ACTION_NEXT = "music_service_action_next";
    private static final String MUSIC_SERVICE_ACTION_PREV = "music_service_action_prev";
    private static final String MUSIC_SERVICE_ACTION_STOP = "music_service_action_stop";
    private static final String MUSIC_SERVICE_ACTION_START = "music_service_action_start";


    private NotificationManager mNotifyManager;
    private final IBinder mBinder = new MediaPlaybackBinder();
    private MediaPlayer mPlayer;
    private Song mCurrentSong;
    private SongData mSongData;
    private boolean isRepeat = false;
    private boolean isShuffle = false;

    private int currentSongPosition;
    private int currentSongId = -1;

    public MediaPlaybackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
//        mSongData = new SongData(this);
        // init service
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        createNotificationChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MediaPlaybackBinder extends Binder {
        public MediaPlaybackService getMediaPlaybackService() {
            return MediaPlaybackService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case MUSIC_SERVICE_ACTION_PLAY:
                start();
                startForegroundService(currentSongPosition,true);
                sendMessageChangeState("song_state_play");
                break;
            case MUSIC_SERVICE_ACTION_PAUSE:
                pause();
                startForegroundService(currentSongPosition, false);
                sendMessageChangeState("song_state_pause");
                break;
            case MUSIC_SERVICE_ACTION_NEXT:
                playNext();
                startForegroundService(currentSongPosition, true);
                break;
            case MUSIC_SERVICE_ACTION_PREV:
                playPrev();
                startForegroundService(currentSongPosition,true);
                break;
            case MUSIC_SERVICE_ACTION_STOP:
                stop();
                stopForeground(true);
                stopSelf();
            default:
                Log.d(TAG, "onStartCommand: default");
                break;
        }

        return START_NOT_STICKY;
    }

    public void startForegroundService(int currentSongPosition, boolean isPlaying) {
//        startForeground(NOTIFICATION_CHANNEL, showNotification());
        Song song = mSongData.getSongAt(currentSongPosition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            showNotification(song, isPlaying);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showNotification(Song song, Boolean isPlaying) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat( this, "tag");
        Intent playIntent = new Intent(this, MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this,
                0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this,
                0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this,
                0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this,
                0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = null;
        try {
            byte[] albumArt = SongData.getAlbumArt(song.getData());
            bitmap = BitmapFactory.decodeByteArray(albumArt , 0, albumArt .length);
        } catch (Exception e) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.art_song_default);
        }
        Log.d(TAG, "showNotification: "+ bitmap);
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);

        notificationLayout.setImageViewBitmap(R.id.notify_song_art, bitmap);
        notificationLayoutExpanded.setImageViewBitmap(R.id.notify_song_art, bitmap);
        notificationLayoutExpanded.setTextViewText(R.id.notify_song_title, song.getTitle());
        notificationLayoutExpanded.setTextViewText(R.id.notify_song_artist, song.getArtistName());
        notificationLayout.setOnClickPendingIntent(R.id.notify_skip_next,nextPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.notify_skip_previous,prevPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notify_skip_next,nextPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notify_skip_previous,prevPendingIntent);
        if (isPlaying) {
            notificationLayout.setImageViewResource(R.id.notify_play_button, R.drawable.ic_pause_circle);
            notificationLayout.setOnClickPendingIntent(R.id.notify_play_button,pausePendingIntent);
            notificationLayoutExpanded.setImageViewResource(R.id.notify_play_button, R.drawable.ic_pause_circle);
            notificationLayoutExpanded.setOnClickPendingIntent(R.id.notify_play_button,pausePendingIntent);
        }
        else {
            notificationLayout.setImageViewResource(R.id.notify_play_button, R.drawable.ic_play_circle);
            notificationLayout.setOnClickPendingIntent(R.id.notify_play_button,playPendingIntent);
            notificationLayoutExpanded.setImageViewResource(R.id.notify_play_button, R.drawable.ic_play_circle);
            notificationLayoutExpanded.setOnClickPendingIntent(R.id.notify_play_button,playPendingIntent);
        }

        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_circle)
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded);

        notificationManagerCompat.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotifyManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    (getString(R.string.notification_channel_description));

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy Music Service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotifyManager.cancelAll();
        }
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isRepeat){

        } else
        if (isShuffle){
            currentSongPosition = mSongData.getRandomSongPos();
        }
        else {
            currentSongPosition++;
            if (currentSongPosition == mSongData.getSongList().size()) currentSongPosition = 0;
        }
        startForegroundService(currentSongPosition,true);
        Intent intent = new Intent(SONG_PLAY_COMPLETE);
        intent.putExtra(MESSAGE_SONG_PLAY_COMPLETE, String.valueOf(currentSongPosition));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public int getCurrentSongPosition() {
        return currentSongPosition;
    }

    public void setCurrentSongPosition(int currentSongPosition) {
        this.currentSongPosition = currentSongPosition;
    }

    public int getCurrentSongId() {
        return currentSongId;
    }

    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public SongData getSongData() {
        return mSongData;
    }

    public void setSongData(SongData mSongData) {
        this.mSongData = mSongData;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public void start() {
        mPlayer.start();
    }

    public void play(int songPos) {
        mPlayer.reset();
        currentSongPosition = songPos;
        Song playSong = mSongData.getSongAt(songPos);
        currentSongId = playSong.getId();
        Log.d(TAG, playSong.getData());
        play(playSong);
    }

    public void play(Song song) {
        if (mPlayer != null) {
            mPlayer.reset();
            Log.d(TAG, String.valueOf("play: "+ mPlayer == null));
            try {
                mPlayer.setDataSource(song.getData());
                mPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "Error playing from data source", e);
            }
        }
    }

    public void sendMessageChangePos() {
        Intent intent = new Intent(SONG_PLAY_CHANGE);
        intent.putExtra(MESSAGE_SONG_PLAY_CHANGE, String.valueOf(currentSongPosition));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    public void sendMessageChangeState(String state) {
        Intent intent = new Intent(SONG_PLAY_CHANGE);
        intent.putExtra(MESSAGE_SONG_PLAY_CHANGE,state);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentStreamPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public void seekTo(int position) {
        mPlayer.seekTo(position);
    }

    public void playNext() {
        currentSongPosition++;
        if (currentSongPosition == mSongData.getSongList().size()) currentSongPosition = 0;
        Log.d(TAG, "playNext: " + currentSongPosition);
        play(currentSongPosition);
        sendMessageChangePos();
    }

    public void playPrev() {
        currentSongPosition--;
        if (currentSongPosition < 0) currentSongPosition = mSongData.getSongList().size() - 1;
        play(currentSongPosition);
        sendMessageChangePos();
    }

}
