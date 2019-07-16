package com.example.demoapplication.player;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/5/16
 * Time: 5:57 PM
 * Desc: Player
 */
public class Player implements IPlayback, MediaPlayer.OnCompletionListener {

    private static final String TAG = "Player";

    private static volatile Player sInstance;

    private MediaPlayer mPlayer;

    private String dataSource;
    // Default size 2: for service and UI
    private List<Callback> mCallbacks = new ArrayList<>(2);

    // Player status
    private boolean isPaused;

    private Player() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
    }

    public static Player getInstance() {
        if (sInstance == null) {
            synchronized (Player.class) {
                if (sInstance == null) {
                    sInstance = new Player();
                }
            }
        }
        return sInstance;
    }

    @Override
    public boolean play() {
        if (isPaused) {
            mPlayer.start();
            notifyPlayStatusChanged(true);
            return true;
        }
        try {
            mPlayer.reset();
            mPlayer.setDataSource(dataSource);
            mPlayer.prepare();
            mPlayer.start();
            notifyPlayStatusChanged(true);
        } catch (IOException e) {
            Log.e(TAG, "play: ", e);
            notifyPlayStatusChanged(false);
            return false;
        }
        return true;
    }


    @Override
    public boolean pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPaused = true;
            notifyPlayStatusChanged(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public int getProgress() {
        return mPlayer.getCurrentPosition();
    }


    @Override
    public boolean seekTo(int progress) {
//        if (currentSong != null) {
//            if (currentSong.getDuration() <= progress) {
//                onCompletion(mPlayer);
//            } else {
//                mPlayer.seekTo(progress);
//            }
//            return true;
//        }
        return false;
    }

    // Listeners

    @Override
    public void onCompletion(MediaPlayer mp) {
        play();
    }

    @Override
    public void releasePlayer() {
        dataSource = null;
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        sInstance = null;
    }

    // Callbacks

    @Override
    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void removeCallbacks() {
        mCallbacks.clear();
    }

    private void notifyPlayStatusChanged(boolean isPlaying) {
        for (Callback callback : mCallbacks) {
            callback.onPlayStatusChanged(isPlaying);
        }
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

}
