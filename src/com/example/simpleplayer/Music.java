package com.example.simpleplayer;

import java.io.File;

import android.media.MediaMetadataRetriever;
import android.util.Log;

public class Music {
	private final String UNKNOWN = "Unknown";
	private File file;
	private String name = UNKNOWN;
	private String artist = UNKNOWN;
	private String album = UNKNOWN;
	private int timesPlayed = 0;
	private String duration;
	private File albumCover;
	private int time;
	
	public Music(String filePath) {
		File file = new File(filePath);
		if (file.exists()) populateMusicData(file);
		else Log.w("FileDoesntExist", "Music File at " + filePath + " does not exist.");
	}
	
	public Music(Music music) {
		file = music.file;
		name = music.name;
		artist = music.artist;
		album = music.album;
		timesPlayed = music.timesPlayed;
		duration = music.duration;
		albumCover = music.albumCover;
		
		Log.d("Music", "Done making the music object with following data: " + toString());
	}
	
	private void populateMusicData(File file) {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(file.getAbsolutePath());
		
		this.file = file;
		
		String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
		String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		
		if (name != null && !name.equals("")) this.name = name;
		if (artist != null && !artist.equals("")) this.artist= artist;
		if (album != null && !album.equals("")) this.album = album;
		if (duration != null && !duration.equals("")){
			this.duration = duration;
			this.time = Integer.parseInt(this.duration)/1000;
		}
	}
	
	public Music(File file, String name, String artist, String album, String duration) {
		this.file = file;
		this.name = name;
		this.artist = artist;
		this.album = album;
		this.duration = duration;
	}
	
	public String getMusicLocation() {
		return file.getAbsolutePath();
	}
	
	@Override
	public String toString() {
		return "Music [file=" + file + ", name=" + name + ", artist=" + artist + ", album=" + album + ", timesPlayed=" + timesPlayed + "]";
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public int getTimesPlayed() {
		return timesPlayed;
	}

	public File getAlbumCover() {
		return albumCover;
	}

	public String getPlayableFilePath() {
		return file.getAbsolutePath();
	}
	
	public String getDuration() {
		return duration;
	}
	
	public int getTime() {
		return time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Music other = (Music) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (artist == null) {
			if (other.artist != null)
				return false;
		} else if (!artist.equals(other.artist))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
