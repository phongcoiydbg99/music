package com.example.music.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.music.SongData;
import com.example.music.controllers.LayoutController;
import com.example.music.controllers.PortLayoutController;
import com.example.music.controllers.LandLayoutController;
import com.example.music.R;
import com.example.music.fragments.AllSongsFragment;
import com.example.music.fragments.FavoriteSongsFragment;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ActivityMusic extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
    private ActionBarDrawerToggle mActionBarDrawerToggle;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.open,R.string.close);
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
                        Log.d(TAG, "onCreate: " + mSongLastId);
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
            mSongData = new SongData(getApplicationContext());
            isPermission = true;
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
            mediaPlaybackService.setSongData(new SongData(getApplicationContext()));
            mLayoutController = isPortrait ? new PortLayoutController(this)
                    : new LandLayoutController(this);
            mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
            mLayoutController.setMediaPlaybackService(mediaPlaybackService);
            mLayoutController.setConnected(true);
            mLayoutController.onConnection();
        }

        Log.d(TAG, "onResume: " + isConnected);
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
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutController.onCreate(savedInstanceState, mSongLastPossition, mSongLastDuration, mSongLastIsPlaying, mSongLastIsRepeat, mSongLastIsShuffle);
                        mLayoutController.setMediaPlaybackService(mediaPlaybackService);
                        mLayoutController.setConnected(true);
                        mLayoutController.onConnection();
                    }
                }, 350);

                break;
            case  R.id.nav_favorite_songs:
                Toast.makeText(this, "Favorite songs", Toast.LENGTH_SHORT).show();
                isAllSongFrag = false;
                mFavoriteSongsFragment = new FavoriteSongsFragment();
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_all_songs, mFavoriteSongsFragment).addToBackStack(null).commit();
                break;
            default:
                break;

        }
        return true;
    }
}
