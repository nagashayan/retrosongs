package com.example.nagashayan.retrosongs;

/**
 * Created by root on 10/3/15.
 */
public class Grid {

    private long id;
    private String title;
    private String artist;
    private String url;


    public Grid(long songID, String songTitle, String songArtist, String songUrl){
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
