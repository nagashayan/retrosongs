package com.example.nagashayan.retrosongs;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.widget.RemoteViews;
/*
 * This is demo code to accompany the Mobiletuts+ series:
 * Android SDK: Creating a Music Player
 * 
 * Sue Smith - February 2014
 */

public class MusicService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener,MediaPlayer.OnBufferingUpdateListener {

	//media player
	private MediaPlayer player;
	//song list
	private ArrayList<Song> songs;
	//current position
	private int songPosn;
	//binder
	private final IBinder musicBind = new MusicBinder();
    //for notification
    private String songTitle;
    private static final int NOTIFY_ID=1;
    //for shuffling
    private boolean shuffle=false;
    private Random rand;
    //for managing service in background
    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return START_NOT_STICKY;
    }*/
	public void onCreate(){
        Log.v("inside"," oncreate");
		//create the service
		super.onCreate();
		//initialize position
		songPosn=0;
		//create player
		player = new MediaPlayer();
		//initialize
		initMusicPlayer();
        //initiate random for shuffling
        rand=new Random();
	}

	public void initMusicPlayer(){
        Log.v("on init","music player service");
		//set player properties
		player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//set listeners
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	//pass song list
	public void setList(ArrayList<Song> theSongs){
		songs=theSongs;
	}

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.v("on buffering","="+percent);
    }

    //binder
	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	//activity will bind to service
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	//release resources when unbind
	@Override
	public boolean onUnbind(Intent intent){
		player.stop();
		player.release();
		return false;
	}

	//play a song
	public void playSong(){
        Log.v("inside","playsong");
		//play
		player.reset();

		//get song
		Song playSong = songs.get(songPosn);
        //get title
        songTitle=playSong.getTitle();
        Log.v("song",songTitle);
		//get id
		long currSong = playSong.getID();
        //get url of the song
        String url = playSong.getUrl();
        Log.v("url",url);
		//set uri
		/*Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				currSong);
		*///set the data source
        //to check if setting datasource was successfull
         boolean success = true;
		try{
			//player.setDataSource(getApplicationContext(), trackUri);
            //String url = "http://programmerguru.com/android-tutorial/wp-content/uploads/2013/04/hosannatelugu.mp3";
            player.setDataSource(url);
            Log.v("setted","datasource");
            player.setOnBufferingUpdateListener(this);
		}
		catch(Exception e){
			Log.e("MUSIC SERVICE", "Error setting data source", e);
            success = false;
		}
        if(success)
		player.prepareAsync();
        else{
            Log.e("unable to play","may be media file corrupted");
            //send broadcast to toast this msg to user
            // Broadcast intent to activity to let it know the media player has been prepared
            Intent onPreparedIntent = new Intent("MESSAGES");
            onPreparedIntent.putExtra("MESSAGE","Wrong media file selected,select another one");
            LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
        }

	}

	//set the song
	public void setSong(int songIndex){
        Log.v("inside", "set song");
		songPosn=songIndex;
	}

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

	@Override
	public void onPrepared(MediaPlayer mp) {
		//start playback
		mp.start();
        //show player start button

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_mediacontroller);
        notificationView.setTextViewText(R.id.textView, "Playing: "+songTitle);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //Notification.MediaStyle style = new Notification.MediaStyle();
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)
                .setContent(notificationView);

        Notification not = builder.build();


        startForeground(NOTIFY_ID, not);

        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        onPreparedIntent.putExtra("SONG_TITLE",songTitle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
	}
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        Log.v("inside", "go");
        if(player != null){
            player.start();
        }
    }
    public void playPrev(){
        songPosn--;
        if(songPosn < 0) songPosn=songs.size()-1;
        playSong();
    }
    //skip to next
    public void playNext(){

        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn >= songs.size()) songPosn=0;
        }
        playSong();
    }
    @Override
    public void onDestroy() {
        Log.e("service","destroyed");
        stopForeground(true);

    }
    //for setting shuffle
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }
    // check data connection enabled or not
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
