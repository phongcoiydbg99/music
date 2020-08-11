package com.example.music;

public class Song {

    private int id;
    private int trackNumber;
    private long duration;
    private String title;
    private String artistName;
    private String composer;
    private String albumName;
    private String data;


    public static Song EMPTY() {
        return new Song(0,"","","","","",0,0);
    }


    public Song(int id, String title, String artistName,
                     String composer, String albumName, String data, int trackNumber,long duration) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.composer = composer;
        this.albumName = albumName;
        this.data = data;
        this.trackNumber = trackNumber;
        this.duration = duration;
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

    public String getComposer() {
        return composer;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getData() {
        return data;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public long getDuration() {
        return duration;
    }
}