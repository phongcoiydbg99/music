package com.example.music;

import javax.xml.datatype.Duration;

public class Song {
    private int pos;
    private int id;
    private long duration;
    private String title;
    private String artistName;
    private String data;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying;


    public static Song EMPTY() {
        return new Song(-1,0,"","","",0);
    }
    public Song(){

    }

    public Song(int pos, int id, String title, String artistName,
                    String data,long duration) {
        this.pos = pos;
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.data = data;
        this.duration = duration;
        isPlaying = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getData() {
        return data;
    }

    public long getDuration() {
        return duration;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String formattedTime() {
        String seconds = String.valueOf((duration/1000) % 60);
        String minutes = String.valueOf((duration/1000) / 60);
        if (minutes.length() <= 1) minutes = "0" + minutes;
        if (seconds.length() <= 1) seconds = "0" + seconds;
        return minutes +":"+seconds;
    }

}