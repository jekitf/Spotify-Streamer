package com.example.android.spotify_streamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by PC on 05.09.2015.
 */
public class PlayerService extends Service {

    public static final String NOTIFICATION = "com.example.android.spotify_streamer.PlayerService.receiver";
    public static final String DURATION="DURATION";
    public static final String ERROR="ERROR";
    private final IBinder mBinder = new MyBinder();
    private WifiManager.WifiLock mWifiLock;
    private boolean mMediaPlayerIsPlaying =false;
    private boolean mMediaPlayerInit =false;
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "lock_PlayerUIActivityFragmentViewHolder");
    }

    @Override
    public void onDestroy() {
        mMediaPlayerIsPlaying=false;
        if (mMediaPlayer !=null) {
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
        if (mWifiLock.isHeld())
            mWifiLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public class MyBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void StartPlaying(String url) {
        if (mMediaPlayerInit && mMediaPlayer !=null)
            mMediaPlayer.release();
        mMediaPlayerIsPlaying =true;
        mMediaPlayerInit =false;
        mMediaPlayer =new MediaPlayer();
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Play();
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new PrepareMediaPlayer().execute(mMediaPlayer);
    }

    public void Play() {
        mMediaPlayerIsPlaying =true;
        if (mMediaPlayerInit && mMediaPlayer !=null) {
            mMediaPlayer.start();
            mWifiLock.acquire();
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Pause();
                if (mMediaPlayerInit && mMediaPlayer != null)
                    mMediaPlayer.seekTo(0);
            }
        });
    }

    public void Pause() {
        mMediaPlayerIsPlaying =false;
        if (mMediaPlayerInit && mMediaPlayer !=null) {
            mMediaPlayer.pause();
            mWifiLock.release();
        }
    }

    public boolean IsPlaying() {
        return mMediaPlayerIsPlaying;
    }

    public void SeekPlay(int miliseconds) {
        if (mMediaPlayerInit && mMediaPlayer !=null)
            mMediaPlayer.seekTo(miliseconds);
    }

    public int getCurrentPosition()
    {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration()
    {
        return mMediaPlayer.getDuration();
    }


    private class PrepareMediaPlayer extends AsyncTask<MediaPlayer,Void,MediaPlayer> {

        private Exception exception=null;

        @Override
        protected MediaPlayer doInBackground(MediaPlayer... params) {
            MediaPlayer mediaPlayer=params[0];
            try {
                mediaPlayer.prepare();
            } catch (Exception e) {
                exception=e;
                e.printStackTrace();
            }
            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(MediaPlayer mediaPlayer) {
            super.onPostExecute(mediaPlayer);

            if (exception!=null){

                //send broadcast
                Intent intent = new Intent(NOTIFICATION);
                intent.putExtra(ERROR, exception.getMessage());
                sendBroadcast(intent);

                mMediaPlayerIsPlaying=false;
                mediaPlayer.release();
                if (mMediaPlayer !=null && mMediaPlayer.equals(mediaPlayer)) {
                    mMediaPlayer = null;
                }
            } else {
                if (mMediaPlayer !=null && mMediaPlayer.equals(mediaPlayer)) {

                    //send broadcast
                    Intent intent = new Intent(NOTIFICATION);
                    intent.putExtra(DURATION, mMediaPlayer.getDuration());
                    sendBroadcast(intent);

                    mMediaPlayerInit =true;
                } else if (mMediaPlayer !=null ) {
                    mediaPlayer.release();
                }
            }
        }
    }
}
