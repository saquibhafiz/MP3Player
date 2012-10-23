package com.example.simpleplayer;

import java.util.List;

public interface MusicPlayerServiceInterface {
	public void addMusicToQueue(Music music);
	public void addMusicToQueue(List<Music> music);
	public void removeMusicFromQueue(Music music);
	public void skipToPoint(int time);
	public void play();
	public void play(int position);
	public void pause();
}
