package com.example.music.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.music.MusicDB;
import com.example.music.MusicProvider;
import com.example.music.Song;
import com.example.music.SongData;
import com.example.music.adapters.SongListAdapter;
import com.example.music.controllers.LayoutController;
import com.example.music.controllers.PortLayoutController;
import com.example.music.controllers.LandLayoutController;
import com.example.music.R;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.FavoriteSongsFragment;
import com.example.music.interfaces.SongItemClickListener;
import com.example.music.services.MediaPlaybackService;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.LinkedList;

public class ActivityMusic extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SongListAdapter.SongItemClickIdListener {

    public static final String TAG = "ActivityMusic";
    private String sharedPrefFile =
            "com.example.music";
    SharedPreferences mPreferences;
    public static final int REQUEST_CODE = 1;
    private boolean isPermission = false;
    private MediaPlaybackService mediaPlaybackService;
    private boolean isConnected = false;
    private LayoutController mLayoutController;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FavoriteSongsFragment mFavoriteSongsFragment;
    private int mSongLastPossition = -1;
    private long mSongLastDuration = -1;
    private Boolean mSongLastIsPlaying = false;
    private ServiceConnection mServiceConnection;
    private boolean isFirst = true;
    private boolean isPortrait;
    private Bundle savedInstanceState;
    private boolean mSongLastIsRepeat = false;
    private boolean mSongLastIsShuffle = false;
    private boolean isAllSongFrag = true;
    private int mSongLastId = -1;
    private SongData mSongData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_music);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mActionBarDrawerToggle.syncState();

        isPortrait = getResources().getBoolean(R.bool.isPortrait);
        permission();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
                mediaPlaybackService = binder.getMediaPlaybackService();
                Log.d(TAG, "onServiceConnected() " + mediaPlaybackService.isFirst());
                isConnected = true;
                if (isPermission) {
                    mediaPlaybackService.setSongData(mSongData);
                    mLayoutController.setMediaPlaybackService(mediaPlaybackService);
                    if (mediaPlaybackService.isFirst()) {
                        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
                        mSongLastId = mPreferences.getInt(LayoutController.LAST_SONG_ID_EXTRA, -1);
                        Log.d(TAG, "onServiceConnected() " + mSongLastPossition);
                        mediaPlaybackService.setCurrentSongPosition(mSongLastPossition);
                        mediaPlaybackService.setCurrentSongId(mSongLastId);
                        mSongLastPossition = mSongData.getSongId(mSongLastId) != null ? mSongData.getSongId(mSongLastId).getPos() : -1;
                        mSongLastIsPlaying = false;
//                        mediaPlaybackService.setFirst(false);
                    }
                    Log.d(TAG, "onServiceConnected() mSongLastDuration " + mSongLastDuration);
                    mLayoutController.setConnected(true);
                    mLayoutController.onConnection();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
            }
        };
        Log.d(TAG, "onCreate: "+mSongLastIsPlaying);
        if (savedInstanceState != null) {
            mSongLastPossition = savedInstanceState.getInt(LayoutController.LAST_SONG_POS_EXTRA);
            mSongLastDuration = savedInstanceState.getLong(LayoutController.LAST_SONG_DURATION_EXTRA);
            mSongLastIsPlaying = savedInstanceState.getBoolean(LayoutController.LAST_SONG_ISPLAYING_EXTRA);
            mSongLastIsRepeat = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_REPEAT_EXTRA);
            mSongLastIsShuffle = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_SHUFFLE_EXTRA);
        }
        if (mSongLastDuration >= 0) isFirst = false;
        mPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);
        mSongLastId = mPreferences.getInt(LayoutController.LAST_SONG_ID_EXTRA, -1);
        if (isPermission) {
            mSongLastPossition = mSongData.getSongId(mSongLastId) != null ? mSongData.getSongId(mSongLastId).getPos() : -1;
            isFirst = false;
            mLayoutController = isPortrait ? new PortLayoutController(this)
                    : new LandLayoutController(this);
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
        }

    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            isPermission = true;
            getAllSongs(this);
            mSongData = new SongData(getApplicationContext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent playIntent = new Intent(ActivityMusic.this, MediaPlaybackService.class);
        Log.d(TAG, "onStart: ");
        playIntent.setAction("");
        startService(playIntent);
        bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPermission && isConnected && isFirst) {
            isFirst = false;
            mediaPlaybackService.setSongData(mSongData);
            mLayoutController = isPortrait ? new PortLayoutController(this)
                    : new LandLayoutController(this);
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
            mLayoutController.setMediaPlaybackService(mediaPlaybackService);
            mLayoutController.setConnected(true);
            mLayoutController.onConnection();
        }

        Log.d(TAG, "onResume: " + isFirst);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (isConnected && isPermission) {
            Log.d(TAG, "onPause: "+mediaPlaybackService.getDuration() );
            if (mediaPlaybackService.getDuration() > 0){
                mediaPlaybackService.setFirst(false);
            }
            int id = mediaPlaybackService.getCurrentSongId();
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putInt(LayoutController.LAST_SONG_ID_EXTRA, id);
            preferencesEditor.apply();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (isConnected) {
            mediaPlaybackService.cancelNotification();
            unbindService(mServiceConnection);
            isConnected = false;
        }
//        stopService(playIntent);
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
                getAllSongs(this);
                mSongData = new SongData(getApplicationContext());
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportActionBar().show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        getSupportActionBar().show();
        switch (item.getItemId()){
            case R.id.nav_listen_now:
                getSupportActionBar().setTitle("Music");
                if (!isAllSongFrag) {
                    isAllSongFrag = true;
                    getSupportFragmentManager().popBackStack();
                }
                break;
            case  R.id.nav_favorite_songs:
                getSupportActionBar().setTitle("Favorite Songs");
                Toast.makeText(this, "Favorite songs"+mediaPlaybackService.getCurrentSongId(), Toast.LENGTH_SHORT).show();
                isAllSongFrag = false;
                mFavoriteSongsFragment = new FavoriteSongsFragment();
                mFavoriteSongsFragment.setOnSongItemClickIdListener(this);
                mFavoriteSongsFragment.setMediaPlaybackService(mediaPlaybackService);
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_all_songs, mFavoriteSongsFragment).addToBackStack(null).commit();
                break;
            default:
                break;

        }
        return true;
    }
    public static void getAllSongs(Context context) {
        int pos = 0;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
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
        String[] projectionDB = {
                MusicDB.ID,
                MusicDB.ID_PROVIDER,
                MusicDB.TITLE,
                MusicDB.ARTIST,
                MusicDB.DURATION,
                MusicDB.DATA,
                MusicDB.IS_FAVORITE,
                MusicDB.COUNT_OF_PLAY
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

                Song song = new Song(pos,id,title,artistName,data,duration);
                Cursor cursorDB = context.getContentResolver().query(MusicProvider.CONTENT_URI, projectionDB, null, null, null);
                Log.d(TAG, "getAllSongs: ----------------"+pos);
                if (cursorDB.moveToPosition(pos)){
                    Log.d(TAG, "getAllSongs: "+  String.valueOf(cursorDB.getString(cursorDB.getColumnIndexOrThrow(MusicDB.IS_FAVORITE)))+ " " + pos);
                }
                else  {
                    ContentValues values = new ContentValues();
                    values.put(MusicDB.ID_PROVIDER, id);
                    values.put(MusicDB.TITLE, title);
                    values.put(MusicDB.ARTIST, artistName);
                    values.put(MusicDB.DURATION, duration);
                    values.put(MusicDB.DATA, data);
                    values.put(MusicDB.IS_FAVORITE, 0);
                    values.put(MusicDB.COUNT_OF_PLAY, 0);
                    // insert a record
                    Log.d(TAG, "getAllSongs: "+id+" "+data);
                    context.getContentResolver().insert(MusicProvider.CONTENT_URI,values);
                }
                pos++;
            }
            cursor.close();
        }
    }


    @Override
    public void onSongItemClickId(SongListAdapter.SongViewHolder holder, int id) {
        Toast.makeText(this,"Text: "+id+" "+holder.getLayoutPosition(),Toast.LENGTH_SHORT).show();
        Song song = mSongData.getSongId(id);
        mediaPlaybackService.play(song.getPos());
        mediaPlaybackService.startForegroundService(song.getPos(),true);
//        getSupportFragmentManager().popBackStack();
        mFavoriteSongsFragment.updateUi(holder.getLayoutPosition(),true);
    }
}
