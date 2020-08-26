package com.example.music.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.music.SongData;
import com.example.music.controllers.LayoutController;
import com.example.music.controllers.PortLayoutController;
import com.example.music.controllers.LandLayoutController;
import com.example.music.R;
import com.example.music.Song;
import com.example.music.services.MediaPlaybackService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.LinkedList;

public class ActivityMusic extends AppCompatActivity {

    public static final String TAG = "ActivityMusic";
    public static final int REQUEST_CODE = 1;
    public static LinkedList<Song> mSongList = new LinkedList<>();
    private boolean isPermission = false;
    private MediaPlaybackService mediaPlaybackService;
    private Intent playIntent;
    private boolean isConnected = false;
    private LayoutController mLayoutController;
    private int mSongLastPossition = -1;
    private long mSongLastDuration = -1;
    private Boolean mSongLastIsPlaying = false;
    private ServiceConnection mServiceConnection;
    private boolean isFirst = true;
    private boolean isPortrait;
    private Bundle savedInstanceState;
    private boolean mSongLastIsRepeat = false;
    private boolean mSongLastIsShuffle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_music);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isPortrait = getResources().getBoolean(R.bool.isPortrait);

        permission();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
                mediaPlaybackService = binder.getMediaPlaybackService();
                isConnected = true;
                if (isPermission){
                    mediaPlaybackService.setSongData(new SongData(getApplicationContext()));
                    mLayoutController.setMediaPlaybackService(mediaPlaybackService);
                    mLayoutController.setConnected(true);
                    mLayoutController.onConnection();
                }
                Log.d(TAG, "onServiceConnected() ");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
            }
        };

        if (savedInstanceState != null){
            mSongLastPossition = savedInstanceState.getInt(LayoutController.LAST_SONG_POS_EXTRA);
            mSongLastDuration = savedInstanceState.getLong(LayoutController.LAST_SONG_DURATION_EXTRA);
            mSongLastIsPlaying = savedInstanceState.getBoolean(LayoutController.LAST_SONG_ISPLAYING_EXTRA);
            mSongLastIsRepeat = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_REPEAT_EXTRA);
            mSongLastIsShuffle = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_SHUFFLE_EXTRA);
        }

        if (isPermission){
            isFirst = false;
            mLayoutController = isPortrait ? new PortLayoutController(this)
                    : new LandLayoutController(this);
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition , mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
        }

    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            isPermission = true;
//            mSongList = getAllSongs(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(ActivityMusic.this, MediaPlaybackService.class);
        Log.d(TAG,"onStart: ");
        playIntent.setAction("");
        startService(playIntent);
        bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//        ContextCompat.startForegroundService(this.getApplicationContext(),playIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPermission && isConnected && isFirst){
            isFirst = false;
            mediaPlaybackService.setSongData(new SongData(getApplicationContext()));
            mLayoutController = isPortrait ? new PortLayoutController(this)
                    : new LandLayoutController(this);
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition , mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
            mLayoutController.setMediaPlaybackService(mediaPlaybackService);
            mLayoutController.setConnected(true);
            mLayoutController.onConnection();
        }

        Log.d(TAG, "onResume: "+isConnected);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (isConnected){
            unbindService(mServiceConnection);
            isConnected = false;
        }
//        stopService(playIntent);
//        mLayoutController.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mLayoutController.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermission = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    public static LinkedList<Song> getAllSongs(Context context) {
        LinkedList<Song> songList = new LinkedList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int pos = 0;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int trackNumber = cursor.getInt(1);
                long duration = cursor.getInt(2);
                String title = cursor.getString(3);
                String artistName = cursor.getString(4);
                String composer = cursor.getString(5);
                String albumName = cursor.getString(6);
                String data = cursor.getString(7);

                Song song = new Song(pos,id,title,artistName,composer,albumName,data,trackNumber,duration);
                Log.d(TAG, "Data: "+ data + " Album: " + albumName);
                songList.add(song);
                pos++;
            }
            cursor.close();
        }
        return  songList;
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_search) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportActionBar().show();
    }
}
