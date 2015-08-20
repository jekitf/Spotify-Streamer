package com.example.android.spotify_streamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotify_streamer.models.TrackAdapterItem;
import com.example.android.spotify_streamer.models.TrackAdapterItems;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    static final String ARTIST_ID_URI = "ARTIST_ID_URI";
    static final String ARTIST_NAME_URI = "ARTIST_NAME_URI";

    public class TrackAdapter extends ArrayAdapter<TrackAdapterItem>
    {
        class ViewHolder {
            TextView tvTrackName;
            ImageView tvAlbumImage;
        }
        public TrackAdapter(Context context) {
            super(context, 0, new ArrayList<TrackAdapterItem>());

            // populate data from last trackAdapter
            if (sTrackAdapter!=null) {
                for (int i = 0; i < sTrackAdapter.getCount(); i++)
                    add(sTrackAdapter.getItem(i));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrackAdapterItem trackAdapterItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.individual_track, parent, false);
                ViewHolder holder=new ViewHolder();
                holder.tvTrackName=(TextView) convertView.findViewById(R.id.trackName);
                holder.tvAlbumImage=(ImageView) convertView.findViewById(R.id.albumImage);
                convertView.setTag(holder);
            }

            ViewHolder holder=(ViewHolder) convertView.getTag();
            holder.tvTrackName.setText(trackAdapterItem.getTrackNameAndAlbumName());

            //get ImageView in pixels. No need to download bigger images
            int valueInPixels = (int) getResources().getDimension(R.dimen.spotify_small_image);

            String imageUrl=trackAdapterItem.getImage(valueInPixels);
            if (!imageUrl.equals("")) {
                Picasso.with(getContext()).load(imageUrl).into(holder.tvAlbumImage);
            }
            return convertView;
        }
    }

    //to store/retrieve view data when going back
    private static Parcelable sParcelableState;

    //used static so when hitting back, old data will be cached
    private static TrackAdapter sTrackAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        sParcelableState =listView.onSaveInstanceState();
    }

    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sTrackAdapter=new TrackAdapter(getActivity());

        View rootView =  inflater.inflate(R.layout.fragment_top_tracks, container, false);
        Intent intent=getActivity().getIntent();
        Bundle args = getArguments();
        if (args != null) {
            String artistName=args.getString(ARTIST_NAME_URI);
            String artistId=args.getString(ARTIST_ID_URI);
            new SearchTopTracks().execute(artistId);
        }
        else if (intent!=null && intent.hasExtra(Intent.EXTRA_REFERRER)){
            String artistName=intent.getStringExtra(Intent.EXTRA_TEXT);
            String artistId=intent.getStringExtra(Intent.EXTRA_REFERRER);

            AppCompatActivity activity=(AppCompatActivity) getActivity();
            android.support.v7.app.ActionBar mActionBar = activity.getSupportActionBar();
            if (mActionBar!=null) {
                mActionBar.setTitle("Top 10 Tracks");
                mActionBar.setSubtitle(artistName);
            }
            new SearchTopTracks().execute(artistId);
        }

        listView=(ListView) rootView.findViewById(R.id.tracks);
        listView.setAdapter(sTrackAdapter);
        if (sParcelableState !=null) {
            listView.onRestoreInstanceState(sParcelableState);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!PlayerUIActivityFragment.IsInstanceRunning())
                {
                    if (true)//MainActivity.IsUseTwoPane())
                    {
                        // Create the fragment and show it as a dialog.
                        PlayerUIActivityFragment playerUIDialog = new PlayerUIActivityFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("TrackAdapterItems", new TrackAdapterItems(position, sTrackAdapter));
                        playerUIDialog.setArguments(args);
                        playerUIDialog.show(getFragmentManager(), "dialog");
                    } else
                    {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        PlayerUIActivityFragment playerUIDialog = new PlayerUIActivityFragment();
                        Bundle args = new Bundle();
                        args.putParcelable("TrackAdapterItems", new TrackAdapterItems(position, sTrackAdapter));
                        playerUIDialog.setArguments(args);
                        ft.add(R.id.individual_track, playerUIDialog);
                        ft.addToBackStack(null);
                        ft.commit();
                        /*Intent showPlayerUI = new Intent(getActivity(), PlayerUIActivity.class);
                        showPlayerUI.putExtra("TrackAdapterItems",new TrackAdapterItems(position,sTrackAdapter));
                        startActivity(showPlayerUI);*/
                    }
                }
            }
        });
        return rootView;
    }



    private class SearchTopTracks extends AsyncTask<String,Integer,ArrayList<TrackAdapterItem>>{
        @Override
        protected ArrayList<TrackAdapterItem> doInBackground(String... params) {
            final String artistId = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            HashMap<String, Object> query=new HashMap<>();
            query.put("country","NO");

            Tracks tracks = spotify.getArtistTopTrack(artistId,query);

            ArrayList<TrackAdapterItem> trackAdapterItems = new ArrayList<>();
            for (Track track : tracks.tracks) {
                trackAdapterItems.add(new TrackAdapterItem(track));
                //Log.v("Track found: ", track.name);
            }
            return trackAdapterItems;
        }

        @Override
        protected void onPostExecute(ArrayList<TrackAdapterItem> trackAdapterItems) {
            sTrackAdapter.clear();
            sTrackAdapter.addAll(trackAdapterItems);

            if (trackAdapterItems.size()==0){
                Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
