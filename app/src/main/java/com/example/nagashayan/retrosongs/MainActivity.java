package com.example.nagashayan.retrosongs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.nagashayan.retrosongs.MusicService.MusicBinder;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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
		getSongList();
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
            loadFirstSong();
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

	//method to retrieve song info from device
	public void getSongList(){
		//query external audio
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
		}
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

            }

        }
    };

}
