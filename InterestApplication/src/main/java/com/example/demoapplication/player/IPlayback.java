package com.example.demoapplication.player;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/5/16
 * Time: 6:02 PM
 * Desc: IPlayer
 */
public interface IPlayback {


    boolean play();

    boolean pause();

    boolean isPlaying();

    int getProgress();

    boolean seekTo(int progress);

    void registerCallback(Callback callback);

    void unregisterCallback(Callback callback);

    void removeCallbacks();

    void releasePlayer();

    interface Callback {
        void onPlayStatusChanged(boolean isPlaying);
    }
}
