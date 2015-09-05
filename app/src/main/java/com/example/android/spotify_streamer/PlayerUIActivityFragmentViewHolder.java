package com.example.android.spotify_streamer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
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
public class PlayerUIActivityFragmentViewHolder extends BroadcastReceiver {
    TextView tvArtistName;
    TextView tvAlbumName;
    TextView tvTrackName;
    TextView tvCurrentPosition;
    TextView tvDuration;
    ImageView ivAlbumImage;
    ImageView ivPlayPause;
    SeekBar sbSlider;

    final private Context mContext;
    final private TrackAdapterItems mTrackAdapterItems;
    private PlayerService sPlayerService;
    private ServiceConnection mPlayerServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,IBinder binder) {
            PlayerService.MyBinder b = (PlayerService.MyBinder) binder;
            sPlayerService = b.getService();
            StartPlaying(mNewTrack);
        }

        public void onServiceDisconnected(ComponentName className) {
            sPlayerService = null;
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()==PlayerService.NOTIFICATION) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int duration = bundle.getInt(PlayerService.DURATION,-1);
                if (duration!=-1) {
                    tvDuration.setText(ConvertMsToString(duration));
                    sbSlider.setMax(duration);
                    Play();
                } else {
                    String error = bundle.getString(PlayerService.ERROR, "");
                    String errorMsg="Error";
                    if (error!="")
                        errorMsg+=": "+error;
                    if (toast!=null)
                        toast.cancel();
                    toast=Toast.makeText(context, errorMsg,Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    final private boolean mNewTrack;
    public PlayerUIActivityFragmentViewHolder(Context context, TrackAdapterItems trackAdapterItems,boolean newTrack) {
        mContext =context;
        mTrackAdapterItems =trackAdapterItems;
        mNewTrack=newTrack;

        //make sure PlayerService runs
        Intent intent= new Intent(context, PlayerService.class);
        mContext.startService(intent);
        context.bindService(intent, mPlayerServiceConnection, Context.BIND_AUTO_CREATE);
        context.registerReceiver(this, new IntentFilter(PlayerService.NOTIFICATION));
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

    public void StartPlaying(boolean newTrack) {
        Update();
        if (newTrack) {
            tvCurrentPosition.setText("0:00");
            tvDuration.setText("");
            if (sPlayerService != null)
                sPlayerService.StartPlaying(mTrackAdapterItems.getTrack().getPreviewUrl());
        } else {
            tvDuration.setText(ConvertMsToString(sPlayerService.getDuration()));
            sbSlider.setMax(sPlayerService.getDuration());
            if (IsPlaying())
                ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            else
                ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
            sbSlider.setProgress(sPlayerService.getCurrentPosition());
        }
    }

    private Toast toast;

    public void Pause() {
        ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
        if (sPlayerService!=null)
            sPlayerService.Pause();
    }

    public void Play() {
        ivPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        if (sPlayerService!=null)
            sPlayerService.Play();
    }

    public boolean IsPlaying() {
        if (sPlayerService!=null)
            return sPlayerService.IsPlaying();
        return false;
    }

    static String ConvertMsToString(Integer milliseconds)
    {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public void SeekPlay(int miliseconds) {
        if (sPlayerService!=null)
            sPlayerService.SeekPlay(miliseconds);
    }

    public void UpdateSlider() {
        if (sPlayerService!=null && IsPlaying()) {
            sbSlider.setProgress(sPlayerService.getCurrentPosition());
        }
    }

    public void onDestroy(boolean stopPayerService) {
        mContext.unbindService(mPlayerServiceConnection);
        mContext.unregisterReceiver(this);
        if (stopPayerService) {
            Intent intent= new Intent(mContext, PlayerService.class);
            mContext.stopService(intent);
        }
    }
}
