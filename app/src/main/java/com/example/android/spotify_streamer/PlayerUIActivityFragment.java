package com.example.android.spotify_streamer;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.spotify_streamer.models.TrackAdapterItems;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerUIActivityFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInctances--;
        mTimerHandler.removeCallbacksAndMessages(null);
        mViewHolder.onDestroy();
    }

    private Handler mTimerHandler;
    private TrackAdapterItems mTrackAdapterItems =null;
    private PlayerUIActivityFragmentViewHolder mViewHolder =null;

    static private int sInctances =0;
    static public boolean IsInstanceRunning()
    {
        return sInctances >0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sInctances++;
        View rootView=inflater.inflate(R.layout.fragment_player_ui, container, false);
        Intent intent = getActivity().getIntent();
        mTrackAdapterItems =intent.getParcelableExtra("TrackAdapterItems");
        if (mTrackAdapterItems ==null)
            mTrackAdapterItems =getArguments().getParcelable("TrackAdapterItems");
        mViewHolder =new PlayerUIActivityFragmentViewHolder(rootView.getContext(), mTrackAdapterItems);
        mViewHolder.tvArtistName=(TextView) rootView.findViewById(R.id.artistName);
        mViewHolder.tvAlbumName=(TextView) rootView.findViewById(R.id.albumName);
        mViewHolder.tvTrackName=(TextView) rootView.findViewById(R.id.trackName);
        mViewHolder.tvCurrentPosition=(TextView) rootView.findViewById(R.id.currentPossion);
        mViewHolder.tvDuration =(TextView) rootView.findViewById(R.id.duration);
        mViewHolder.ivAlbumImage=(ImageView) rootView.findViewById(R.id.albumImage);
        mViewHolder.ivPlayPause=(ImageView) rootView.findViewById(R.id.media_play_pause);
        mViewHolder.sbSlider=(SeekBar) rootView.findViewById(R.id.Seekbar);
        rootView.setTag(mViewHolder);

        final ImageView ivPrevious=(ImageView) rootView.findViewById(R.id.media_previous);
        final ImageView ivNext=(ImageView) rootView.findViewById(R.id.media_next);
        ivPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrackAdapterItems.PreviousTrack())
                    mViewHolder.StartPlaying();
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrackAdapterItems.NextTrack())
                    mViewHolder.StartPlaying();
            }
        });
        mViewHolder.ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewHolder.IsPlaying())
                    mViewHolder.Pause();
                else
                    mViewHolder.Play();
                };
            });

        mViewHolder.sbSlider.setOnSeekBarChangeListener(this);
        mViewHolder.Update();
        mViewHolder.StartPlaying();

        mTimerHandler = new Handler();
        new Runnable() {

            @Override
            public void run() {
                mTimerHandler.postDelayed(this, 1000 / 60);
                if (!mIsTrackingTouch) {
                    mViewHolder.UpdateSlider();
                }
            }
        }.run();
        return rootView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mViewHolder.tvCurrentPosition.setText(PlayerUIActivityFragmentViewHolder.ConvertMsToString(progress));
    }

    private boolean mIsTrackingTouch =false;
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch =true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch =false;
        mViewHolder.SeekPlay(mViewHolder.sbSlider.getProgress());
    }
}
