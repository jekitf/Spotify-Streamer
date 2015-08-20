package com.example.android.spotify_streamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotify_streamer.models.TrackAdapterItem;
import com.example.android.spotify_streamer.models.TrackAdapterItems;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by PC on 03.08.2015.
 */
public class PlayerUIActivityFragmentViewHolder {
    TextView tvArtistName;
    TextView tvAlbumName;
    TextView tvTrackName;
    TextView tvCurrentPosition;
    TextView tvDuration;
    ImageView ivAlbumImage;
    ImageView ivPlayPause;
    SeekBar sbSlider;

    final private WifiManager.WifiLock mWifiLock;
    final private Context mContext;
    final private TrackAdapterItems mTrackAdapterItems;
    public PlayerUIActivityFragmentViewHolder(Context context, TrackAdapterItems trackAdapterItems) {
        mContext =context;
        mTrackAdapterItems =trackAdapterItems;
        mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "lock_PlayerUIActivityFragmentViewHolder");
    }

    void Update()
    {
        TrackAdapterItem item= mTrackAdapterItems.getTrack();
        tvArtistName.setText(item.getArtistName());
        tvAlbumName.setText(item.getAlbumName());
        tvTrackName.setText(item.getTrackName());
        if (!item.getImage(0).equals("")) {
            Picasso.with(mContext).load(item.getImage(0)).into(ivAlbumImage);
        }
    }

    private boolean mMediaPlayerIsPlaying =false;
    private boolean mMediaPlayerInit =false;
    private MediaPlayer mMediaPlayer;
    public void StartPlaying() {
        Update();
        if (mMediaPlayerInit && mMediaPlayer !=null)
            mMediaPlayer.release();
        mMediaPlayerIsPlaying =true;
        mMediaPlayerInit =false;
        tvCurrentPosition.setText("0:00");
        tvDuration.setText("");
        mMediaPlayer =new MediaPlayer();
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Play();
        try {
            mMediaPlayer.setDataSource(mTrackAdapterItems.getTrack().getPreviewUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new PrepareMediaPlayer().execute(mMediaPlayer);
    }

    private Toast toast;

    public void onDestroy() {
        if (mWifiLock.isHeld())
            mWifiLock.release();
        mMediaPlayer.release();
        mMediaPlayer =null;
    }

    public void Pause() {
        mMediaPlayerIsPlaying =false;
        ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
        if (mMediaPlayerInit && mMediaPlayer !=null) {
            mMediaPlayer.pause();
            mWifiLock.release();
        }

    }

    public void Play() {
        mMediaPlayerIsPlaying =true;
        ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
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

    public boolean IsPlaying() {
        return mMediaPlayerIsPlaying;
    }

    static String ConvertMsToString(Integer milliseconds)
    {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public void SeekPlay(int miliseconds) {
        if (mMediaPlayerInit && mMediaPlayer !=null)
            mMediaPlayer.seekTo(miliseconds);
    }

    public void UpdateSlider() {
        if (IsPlaying()) {
            sbSlider.setProgress(mMediaPlayer.getCurrentPosition());
        }
    }

    private class PrepareMediaPlayer extends AsyncTask<MediaPlayer,Void,MediaPlayer> {
        @Override
        protected MediaPlayer doInBackground(MediaPlayer... params) {
            MediaPlayer mediaPlayer=params[0];
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                cancel(false);
            }
            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(MediaPlayer mediaPlayer) {
            super.onPostExecute(mediaPlayer);
            if (mMediaPlayer !=null && mMediaPlayer.equals(mediaPlayer)) {
                tvDuration.setText(ConvertMsToString(mMediaPlayer.getDuration()));
                sbSlider.setMax(mMediaPlayer.getDuration());
                mMediaPlayerInit =true;
                mMediaPlayer.start();
                mWifiLock.acquire();
            } else if (mMediaPlayer !=null ) {
                mediaPlayer.release();
            }
        }

        @Override
        protected void onCancelled(MediaPlayer mediaPlayer) {
            super.onCancelled(mediaPlayer);
            mediaPlayer.release();
            if (mMediaPlayer !=null && mMediaPlayer.equals(mediaPlayer)) {
                mMediaPlayer =null;
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
