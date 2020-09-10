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

import com.example.music.Helper;
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
import com.example.music.fragments.BaseSongsFragment;
import com.example.music.fragments.FavoriteSongsFragment;
import com.example.music.fragments.MediaPlaybackFragment;
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
import android.view.View;
import android.widget.Toast;

public class ActivityMusic extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "ActivityMusic";
    public static final String IS_FAVORITE_LAYOUT = "is_favorite_layout";
    public static final String SHARED_PREF_FILE = "com.example.music";
    SharedPreferences mPreferences;
    public static final int REQUEST_CODE = 1;
    private ServiceConnection mServiceConnection;
    private Bundle savedInstanceState;
    private MediaPlaybackService mediaPlaybackService;
    private LayoutController mLayoutController;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private boolean isPermission = false;
    private boolean isConnected = false;
    private int mSongLastPossition = -1;
    private long mSongLastDuration = -1;
    private Boolean isFavoriteLayout = false;
    private Boolean mSongLastIsPlaying = false;
    private boolean mSongLastIsRepeat = false;
    private boolean mSongLastIsShuffle = false;
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

        boolean isPortrait = getResources().getBoolean(R.bool.isPortrait);
        permission();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlaybackService.MediaPlaybackBinder binder = (MediaPlaybackService.MediaPlaybackBinder) service;
                mediaPlaybackService = binder.getMediaPlaybackService();
                Log.d(TAG, "onServiceConnected() " + mediaPlaybackService.isFirst());
                isConnected = true;
                if (isPermission) {
                    mLayoutController.setMediaPlaybackService(mediaPlaybackService);
                    if (mediaPlaybackService.isFirst()) {
                        Log.d(TAG, "onServiceConnected() " + mSongLastPossition);
                        mediaPlaybackService.setCurrentSongPosition(mSongLastPossition);
                        mediaPlaybackService.setCurrentSongId(mSongLastId);
//                        mSongLastPossition = mSongData.getSongId(mSongLastId) != null ? mSongData.getSongId(mSongLastId).getPos() : -1;
                        mSongLastIsPlaying = false;
                    }
                    Log.d(TAG, "onServiceConnected() mSongLastDuration " + mSongLastDuration);
                    mLayoutController.setConnected(true);
                    mLayoutController.onConnection();
                    if (isFavoriteLayout) {
                        getSupportActionBar().setTitle("Favorite Songs");
                        mLayoutController.onCreateFavorite();
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
            }
        };
        Log.d(TAG, "onCreate: " + mSongLastIsPlaying);
        if (savedInstanceState != null) {
            mSongLastPossition = savedInstanceState.getInt(LayoutController.LAST_SONG_POS_EXTRA);
            mSongLastDuration = savedInstanceState.getLong(LayoutController.LAST_SONG_DURATION_EXTRA);
            mSongLastIsPlaying = savedInstanceState.getBoolean(LayoutController.LAST_SONG_ISPLAYING_EXTRA);
            mSongLastIsRepeat = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_REPEAT_EXTRA);
            mSongLastIsShuffle = savedInstanceState.getBoolean(LayoutController.LAST_SONG_IS_SHUFFLE_EXTRA);
            isFavoriteLayout = savedInstanceState.getBoolean(IS_FAVORITE_LAYOUT);
        }
        mPreferences = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        mSongLastId = mPreferences.getInt(LayoutController.LAST_SONG_ID_EXTRA, -1);
        mLayoutController = isPortrait ? new PortLayoutController(this)
                : new LandLayoutController(this);
        Log.d(TAG, "onCreate: " + mSongLastId);
        if (isPermission) {
            mSongLastPossition = mSongData.getSongId(mSongLastId) != null ? mSongData.getSongId(mSongLastId).getPos() : -1;
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastId, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
        }

    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            isPermission = true;
            Helper.getAllSongs(this);
            mSongData = new SongData(getApplicationContext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent playIntent = new Intent(ActivityMusic.this, MediaPlaybackService.class);
        Log.d(TAG, "onStart: ");
        Log.d(TAG, "onStart: " + mNavigationView.getCheckedItem());
        playIntent.setAction("");
        startService(playIntent);
        bindService(playIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (isConnected && isPermission) {
            Log.d(TAG, "onPause: " + mediaPlaybackService.getCurrentStreamPosition());
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
        outState.putBoolean(IS_FAVORITE_LAYOUT, isFavoriteLayout);
        if (mLayoutController != null) mLayoutController.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermission = true;
                Helper.getAllSongs(this);
                mSongData = new SongData(getApplicationContext());
                if (isConnected) {
                    mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastId, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
                    mLayoutController.setMediaPlaybackService(mediaPlaybackService);
                    mLayoutController.setConnected(true);
                    mLayoutController.onConnection();
                }
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityMusic.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // case 4 User has denied permission but not permanently
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                } else {
                    // case 5. Permission denied permanently.
                    // You can open Permission setting's page from here now.
                    finish();
                }
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
        Handler handler = new Handler();

        switch (item.getItemId()) {
            case R.id.nav_listen_now:
                isFavoriteLayout = false;
                getSupportActionBar().setTitle("Music");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutController.onCreateAllSong();
                    }
                }, 370);
                mDrawerLayout.closeDrawers();
                break;
            case R.id.nav_favorite_songs:
                isFavoriteLayout = true;
                getSupportActionBar().setTitle("Favorite Songs");
                Toast.makeText(this, "Favorite songs" + mediaPlaybackService.getCurrentSongId(), Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutController.onCreateFavorite();
                    }
                }, 370);
                mDrawerLayout.closeDrawers();
                break;
            default:
                break;

        }
        return true;
    }

}
