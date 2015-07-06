package com.example.android.spotify_streamer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    class TrackAdapterItem {
        private Track _track;
        public TrackAdapterItem(Track track) {
            _track=track;
        }
        public String getImage(int pixels) {
            String imageUrl="";
            for (int i=0;i<_track.album.images.size();i++){
                Image image=_track.album.images.get(i);
                if (imageUrl=="" || (image.height>=pixels && image.width>pixels)) {
                    imageUrl=image.url;
                }
            }
            return imageUrl;
        }
        public String getTrackName() {
            return _track.name;
        }

        public String getTrackNameAndAlbumName() {
            return _track.name+"\r\n"+_track.album.name;
        }
    }

    public class TrackAdapter extends ArrayAdapter<TrackAdapterItem>
    {
        public TrackAdapter(Context context) {
            super(context, 0, new ArrayList<TrackAdapterItem>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrackAdapterItem trackAdapterItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.individual_track, parent, false);
            }
            TextView tvTrackName = (TextView) convertView.findViewById(R.id.trackName);
            tvTrackName.setText(trackAdapterItem.getTrackNameAndAlbumName());

            //get ImageView in pixels. No need to download bigger images
            int valueInPixels = (int) getResources().getDimension(R.dimen.spotify_small_image);

            String imageUrl=trackAdapterItem.getImage(valueInPixels);
            if (imageUrl!="") {
                ImageView tvAlbumImage= (ImageView) convertView.findViewById(R.id.albumImage);
                Picasso.with(getContext()).load(imageUrl).into(tvAlbumImage);
            }
            return convertView;
        }
    }
    TrackAdapter trackAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        trackAdapter=new TrackAdapter(getActivity());

        Intent intent=getActivity().getIntent();
        View rootView =  inflater.inflate(R.layout.fragment_top_tracks, container, false);
        if (intent!=null && intent.hasExtra(Intent.EXTRA_REFERRER)){
            String artisName=intent.getStringExtra(Intent.EXTRA_TEXT);
            String artistId=intent.getStringExtra(Intent.EXTRA_REFERRER);

            ActionBarActivity activity=(ActionBarActivity) getActivity();
            android.support.v7.app.ActionBar mActionBar = activity.getSupportActionBar();
            mActionBar.setTitle("Top 10 Tracks");
            mActionBar.setSubtitle(artisName);
            new SearchTopTracks().execute(artistId);
        }

        ListView listView=(ListView) rootView.findViewById(R.id.tracks);
        listView.setAdapter(trackAdapter);
        return rootView;
    }

    public class SearchTopTracks extends AsyncTask<String,Integer,ArrayList<TrackAdapterItem>>{
        @Override
        protected ArrayList<TrackAdapterItem> doInBackground(String... params) {
            final String artistId = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            HashMap<String, Object> query=new HashMap<String,Object>();
            query.put("country","NO");

            Tracks tracks = spotify.getArtistTopTrack(artistId,query);

            ArrayList<TrackAdapterItem> trackAdapterItems = new ArrayList<TrackAdapterItem>();
            for (Track track : tracks.tracks) {
                trackAdapterItems.add(new TrackAdapterItem(track));
                //Log.v("Track found: ", track.name);
            }
            return trackAdapterItems;
        }

        @Override
        protected void onPostExecute(ArrayList<TrackAdapterItem> trackAdapterItems) {
            trackAdapter.clear();
            trackAdapter.addAll(trackAdapterItems);

            if (trackAdapterItems.size()==0){
                Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
