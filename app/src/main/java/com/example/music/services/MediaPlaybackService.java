package com.example.music.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.R;
import com.example.music.Song;
import com.example.music.SongData;

public class MediaPlaybackService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private static final String TAG = MediaPlaybackService.class.getSimpleName();
    public static final String SONG_PLAY_COMPLETE = "song_play_complete";
    public static final String MESSAGE_SONG_PLAY_COMPLETE = "message_song_play_complete";
    public static final String NOTIFICATION_CHANNEL = "notification_channel";
    public static final int NOTIFICATION_ID = 123;
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
    PlayerThread mPlayerThread;
    private int currentSongPosition;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    public MediaPlaybackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mSongData = new SongData(this);
        mPlayerThread = new PlayerThread();
        mPlayerThread.start();
        
        // init service
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        createNotificationChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_SERVICE_ACTION_PREV);
        filter.addAction(MUSIC_SERVICE_ACTION_NEXT);
        filter.addAction(MUSIC_SERVICE_ACTION_PAUSE);
        filter.addAction(MUSIC_SERVICE_ACTION_PLAY);
        registerReceiver(mBroadcastReceiver,filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public class MediaPlaybackBinder extends Binder {
        public MediaPlaybackService getMediaPlaybackService() {
            return MediaPlaybackService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: "+ intent.getAction());
        startForeground(NOTIFICATION_ID, showNotification());

//        switch (intent.getAction()){
//            case MUSIC_SERVICE_ACTION_PLAY:
//                start();
//                mNotifyManager.notify(NOTIFICATION_ID, showNotification());
//                break;
//            case MUSIC_SERVICE_ACTION_PAUSE:
//                pause();
//                mNotifyManager.notify(NOTIFICATION_ID, showNotification());
//                break;
//            case MUSIC_SERVICE_ACTION_NEXT:
//                playNext();
//                mNotifyManager.notify(NOTIFICATION_ID, showNotification());
//                break;
//            case MUSIC_SERVICE_ACTION_PREV:
//                playPrev();
//                mNotifyManager.notify(NOTIFICATION_ID, showNotification());
//                break;
//            default:
//                Log.d(TAG, "onStartCommand: default");
//                break;
//        }
        return START_STICKY;
    }

    private Notification showNotification() {
        Intent playIntent = new Intent(this,MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this,
                0, playIntent, 0);

        Intent pauseIntent = new Intent(this,MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this,
                0, pauseIntent, 0);

        Intent nextIntent = new Intent(this,MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this,
                0, nextIntent, 0);

        Intent prevIntent = new Intent(this,MediaPlaybackService.class).setAction(MUSIC_SERVICE_ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getService(this,
                0, prevIntent, 0);
        // Build the notification with all of the parameters using helper
        // method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        notifyBuilder.addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent);
        if (isPlaying()) notifyBuilder.addAction(R.drawable.ic_pause_circle,"Pause", pausePendingIntent);
        else notifyBuilder.addAction(R.drawable.ic_play_circle, "Play", playPendingIntent);
        notifyBuilder.addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent);
        // Deliver the notification.
        NotificationCompat.Builder notifyBuilderq =  new NotificationCompat
                .Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.mipmap.ic_launcher_music)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_ALL);;
        return notifyBuilderq.build();
    }
    private NotificationCompat.Builder getNotificationBuilder() {

        Intent notificationIntent = new Intent(this, MediaPlaybackService.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity
                (this, NOTIFICATION_ID, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat
                .Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.mipmap.ic_launcher_music)
                .setAutoCancel(true).setContentIntent(notificationPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }
    public void createNotificationChannel() {

        // Create a notification manager object.
        mNotifyManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    (PRIMARY_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManager.IMPORTANCE_HIGH);

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
        unregisterReceiver(mBroadcastReceiver);
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        currentSongPosition++;
        if (currentSongPosition == mSongData.getSongList().size()) currentSongPosition = 0;
        Intent intent = new Intent(SONG_PLAY_COMPLETE);
        intent.putExtra(MESSAGE_SONG_PLAY_COMPLETE,String.valueOf(currentSongPosition));
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

    public MediaPlayer getmPlayer() {
        return mPlayer;
    }

    public SongData getSongData() {
        return mSongData;
    }

    public void start() {
        mPlayer.start();
    }

    public void play(int songPos) {
        mPlayer.reset();
        Song playSong = mSongData.getSongAt(songPos);
        Log.d(TAG, playSong.getData());
        play(playSong);
    }

    public void play(Song song) {
        mPlayerThread.play(song);
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
//        play(mSongData.getNextSong(mCurrentSong));
    }

    public void playPrev() {
        currentSongPosition--;
        if (currentSongPosition < 0) currentSongPosition = mSongData.getSongList().size() - 1;
        play(currentSongPosition);
//        play(mSongData.getPreviousSong(mCurrentSong));
    }

    public class PlayerThread extends Thread {
        private Handler mHandler;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }

        public void play(final Song song) {
            mCurrentSong = song;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mPlayer != null) {
                        mPlayer.reset();
                        try {
                            mPlayer.setDataSource(song.getData());
                            mPlayer.prepareAsync();

                        } catch (Exception e) {
                            Log.e(TAG, "Error playing from data source", e);
                        }
                    }
                }
            });
        }

        public void prepareNext() {

        }

        public void exit() {
            mHandler.getLooper().quit();
        }
    }
}
