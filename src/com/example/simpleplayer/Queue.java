package com.example.simpleplayer;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
//import java.util.Random;


public class Queue {
	private List<Music> queue = new ArrayList<Music>();
	private int current = 0;
	private boolean random = false;
	private List<Music> random_queue = new ArrayList<Music>();
//	private Random rnd = new Random();
	
	public Music getCurrentlyPlaying() {
		return queue.get(current);
	}
	
	public void addMusicToQueue(Music music) {
		if (!queue.contains(music)){
			queue.add(music);
//			random_queue.add(rnd.nextInt(random_queue.size()), music);
			Log.d(this.toString(), "added " + music.toString());
		}
	}
	
	public void removeMusicFromQueue(Music music) {
		queue.remove(music);
		random_queue.remove(music);
	}
	
	public void addMusicToQueue(List<Music> list) {
		for (Music music : list){
			addMusicToQueue(music);
		}
	}
	
	public void addMusicToQueue(Queue queue) {
		addMusicToQueue(queue.queue);
	}
	
	public void addMusicToQueue(Music music, int index) {
		queue.add(index, music);
	}
	
	public int getSizeOfQueue() {
		return queue.size();
	}
	
	public Music next() {
		current = ++current % queue.size();
		if (queue.size() >= 1 && random) return random_queue.get(current%random_queue.size());
		else if (queue.size() >= 1) return queue.get(current%queue.size());
		else return null;
	}
	
	public Music last() {
		current = --current % queue.size();
		if (queue.size() >= 1 && random) return random_queue.get(current%random_queue.size());
		else if (queue.size() >= 1) return queue.get(current%queue.size());
		else return null;
	}
	
	public Music playGet(int position) {
		current = position;
		return queue.get(position);
	}
	
	public void clearQueue() {
		queue.clear();
		current = 0;
	}
}
