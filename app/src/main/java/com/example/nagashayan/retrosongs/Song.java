package com.example.nagashayan.retrosongs;

/*
 * This is demo code to accompany the Mobiletuts+ series:
 * Android SDK: Creating a Music Player
 * 
 * Sue Smith - February 2014
 */

public class Song {
	
	private long id;
	private String title;
	private String artist;
    private String url;
	
	public Song(long songID, String songTitle, String songArtist, String songUrl){
		id=songID;
		title=songTitle;
		artist=songArtist;
        url=songUrl;
	}
	
	public long getID(){return id;}
	public String getTitle(){return title;}
	public String getArtist(){return artist;}
    public String getUrl(){return url;}

}
