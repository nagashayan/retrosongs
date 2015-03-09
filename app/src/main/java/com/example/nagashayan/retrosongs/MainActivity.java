package com.example.nagashayan.retrosongs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import com.example.nagashayan.retrosongs.MusicService.MusicBinder;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MediaController.MediaPlayerControl;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * This is demo code to accompany the Mobiletuts+ series:
 * Android SDK: Creating a Music Player
 * 
 * Sue Smith - February 2014
 */

public class MainActivity extends ActionBarActivity implements MediaPlayerControl{

    //song list variables
    private ArrayList<Song> songList;
    private ListView songView;

    //service
    private MusicService musicSrv;
    private Intent playIntent;
    //binding
    private static boolean musicBound=false;
    //for music controllers
    private MusicController controller;
    //for handling pause and play
    private static boolean paused=false, playbackPaused=false;

    // few variables for accessibility for user
    private static String CURRENT_SONG_PLAYING = null;
    private static String LAST_SONG_PLAYED = null;
    private static int DEFAULT_FIRST_SONG = 0;
    private static String CURRENT_PLAYER_STATE = null;
    private static String DEFAULT_PLAYER_STATE = "pause";
    private static String CURRENT_SONG_DURATION = null;
    private static String CURRENT_SONG_TITLE = null;
    private static String SERVER_URL = "http://10.0.2.2/songlist.php";

    // Instance Variables
    private MainActivity mainActivity = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("on create","activity");
        setContentView(R.layout.activity_main);

        //retrieve list view
        songView = (ListView)findViewById(R.id.song_list);
        //instantiate list
        songList = new ArrayList<Song>();
        //get songs from device
       // getSongList();
           /*
     * Spawn a GetListTask thread. This thread will get the data from the
     * server in the background, so as not to block our main (UI) thread.
     */
        (new GetListTask()).execute((Object)null);
        //sort alphabetically by title
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        //create and set adapter
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        //set controller
        setController();
        Log.v("on create", "after set controller");

        //create fragment for handling orientation changes
       /* FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);*/

    }
    //init first song
    private void loadFirstSong(){
        //set default song for first time use - as of now first song we are just loading song to enable controller
        musicSrv.setSong(0);
        //musicSrv.playSong();
        controller.show(0);
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
            Log.v("after service connected","musicsrv initialized");
            //loadFirstSong();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            Log.v("activity","onStart binding service");
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);


        }
    }

    //user song select
    public void songPicked(View view){
        Log.v("songpicked", view.getTag().toString());

        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onDestroy() {
        Log.e("activity","destroyed");
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();

    }
    private void setController(){
        //set the controller up
        if (controller == null) {
            Log.v("MainActivity","First time controller init");
            controller = new MusicController(this);
        }
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
        Log.v("set controller","After set controller");
    }
    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
    @Override
    public boolean canPause() {
        return true;
    }
    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }



    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        Log.v("seeking pos",Integer.toString(pos));
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        Log.v("inside","start");
        musicSrv.go();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.v("inside","on pause");
        paused=true;

    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.v("activity","on resume"+paused);
        if(paused){
            //as this was resetting controller, like pause was changed to start on resume
            //setController();
            paused=false;
        }
        // Set up receiver for media player onPrepared broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
    }
    @Override
    protected void onStop() {
        Log.v("inside","on stop"+CURRENT_SONG_TITLE);
        //controller.hide();
        super.onStop();

    }
    // Broadcast receiver to determine when music player has been prepared
    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            Log.v("receiver","received player prepared"+i.getAction());
            switch (i.getAction()){
                case "MEDIA_PLAYER_PREPARED":
                    // When music player has been prepared, show controller
                    controller.show(0);
                    //change player state
                    CURRENT_PLAYER_STATE = "play";
                    CURRENT_SONG_TITLE = i.getStringExtra("SONG_TITLE");
                    break;
                case "CURRENT_SONG_DURATION":

                    break;
                case "MESSAGES":
                    Toast.makeText(getApplicationContext(),i.getStringExtra("MESSAGE"),Toast.LENGTH_LONG).show();
                    break;

            }

        }
    };

    //method to retrieve song info from device
    public String getSongList(){
        Log.v("inside","getslong list");

        /*
                 * Let's construct the query string. It should be a key/value pair. In
                 * this case, we just need to specify the command, so no additional
                 * arguments are needed.
                 */
        String data = null;
        try {
            data = "command=" + URLEncoder.encode("getsongs", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return executeHttpRequest(data);



       /* //query external audio
        ContentResolver musicResolver = getContentResolver();

        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        } */
    }
    private class GetListTask extends AsyncTask {

        /**
         * Let's make the http request and return the result as a String.
         */
        protected String doInBackground(Object... args) {
            Log.v("inside","background");
            return getSongList();
        }

        /**
         * Parse the String result, and create a new array adapter for the list
         * view.
         */
        protected void onPostExecute(Object objResult) {
            // check to make sure we're dealing with a string
            if(objResult != null) {

                String result =   (String) objResult;
                Log.v("got result",result);
                JSONParser parser=new JSONParser();
                Object obj = null;
                try {

                    try {
                        JSONArray jsonarr = new JSONArray(result);
                        //Log.v("jsonarr length","="+jsonarr.length());

                        for(int i = 0; i < jsonarr.length(); i++){

                            org.json.JSONObject jsonobj = jsonarr.getJSONObject(i);
                            //get song id
                            int id = jsonobj.getInt("id");
                            Log.v("id","="+id);
                            // get song title
                            String title=jsonobj.getString("title");
                            Log.v("title",title);
                            // get song url
                            String url=jsonobj.getString("url");
                            Log.v("url",url);
                            songList.add(new Song(id, title,"myself", url));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // now we'll supply the data structure needed by this ListActivity
                    //  ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(mainActivity, R.layout.song_list, responseList);
                    //mainActivity.setListAdapter(newAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
    private static String executeHttpRequest(String data) {
        Log.v("inside","execute http");
        String result = "";
        try {
            URL url = new URL(SERVER_URL);
            URLConnection connection = url.openConnection();

            // We need to make sure we specify that we want to provide input and
            // get output from this connection. We also want to disable caching,
            // so that we get the most up-to-date result. And, we need to
            // specify the correct content type for our data.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send the POST data
            DataOutputStream dataOut = new DataOutputStream(connection.getOutputStream());
            dataOut.writeBytes(data);
            dataOut.flush();
            dataOut.close();

            // get the response from the server and store it in result
            DataInputStream dataIn = new DataInputStream(connection.getInputStream());
            String inputLine;
            while ((inputLine = dataIn.readLine()) != null) {
                result += inputLine;
            }
            dataIn.close();
        } catch (IOException e) {
        /*
         * In case of an error, we're going to return a null String. This
         * can be changed to a specific error message format if the client
         * wants to do some error handling. For our simple app, we're just
         * going to use the null to communicate a general error in
         * retrieving the data.
         */
            e.printStackTrace();
            result = null;
            Log.v("inside","executehttp"+e);
        }

        return result;
    }


}
