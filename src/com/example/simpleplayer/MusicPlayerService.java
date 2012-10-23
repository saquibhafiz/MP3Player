package com.example.simpleplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.Toast;

public class MusicPlayerService extends Service implements MusicPlayerServiceInterface{
	public final static int PAUSED = 0;
	public final static int PLAYING = 1;
	
	private int state;
	
	private MusicPlayerServiceBinder mMusicPlayerServiceBinder;
	private Queue mNowPlaying;
	private MediaPlayer mMediaPlayer;
	private OnCompletionListener mCompletionListener;
	
	private HeadPhoneBroadcastReceiver mHeadPhoneBroadcastReceiver;
	private SeekBar mSeekBar;
	
	private AsyncTask<Void, Void, Void> seekBarChanger;

	@Override
	public IBinder onBind(Intent intent) {
		mMusicPlayerServiceBinder = new MusicPlayerServiceBinder(this, this);
		state = PLAYING;
		
		mNowPlaying = new Queue();			// setup the now playing queue
		mMediaPlayer = new MediaPlayer();	// setup the media player
		
		mCompletionListener = new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				playNext();
			}
		};
		mMediaPlayer.setOnCompletionListener(mCompletionListener);
		
		mHeadPhoneBroadcastReceiver = new HeadPhoneBroadcastReceiver();
		registerReceiver(mHeadPhoneBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		mHeadPhoneBroadcastReceiver.registerMusicPlayerService(this);
		
		return mMusicPlayerServiceBinder;
	}

	public void addMusicToQueue(Music music) {
		mNowPlaying.addMusicToQueue(music);
	}
	
	public void addMusicToQueue(List<Music> music) {
		mNowPlaying.addMusicToQueue(music);
	}
	
	public void changeQueue(ArrayList<Music> list) {
		mNowPlaying.clearQueue();
		mNowPlaying.addMusicToQueue(list);
	}
	
	public synchronized void play() {
		state = PLAYING;
		mMediaPlayer.start();
	}
	
	public synchronized void play(int position) {
		playFetched(mNowPlaying.playGet(position).getMusicLocation());
	}
	
	public synchronized void playNext() {
		playFetched(mNowPlaying.next().getMusicLocation());
	}
	
	private synchronized void playFetched(String path) {
		state = PLAYING;
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		try {
			mMediaPlayer.setDataSource(path);
			
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					int totalTime = mNowPlaying.getCurrentlyPlaying().getTime();
					mSeekBar.setMax(totalTime*1000);
					
					int minutes = totalTime/60, seconds = totalTime%60;
                	if (seconds >= 10) mMusicPlayerServiceBinder.setTotalTime("/ " + minutes + ":" + seconds);
                	else mMusicPlayerServiceBinder.setTotalTime("/ " + minutes + ":0" + seconds);
                	
					play();
					setSeekBarTracker();
				}
			});
			
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setSeekBarTracker() {
		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;
		seekBarChanger = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				while (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() < mMediaPlayer.getDuration()){
            		if (state == PLAYING){
            			int currentPosition = mMediaPlayer.getCurrentPosition();
            			mSeekBar.setProgress(currentPosition);
            			
//            			int minutes = currentPosition/60, seconds = currentPosition%60;
//                    	if (seconds >= 10) mMusicPlayerServiceBinder.setCurrentTime(minutes + ":" + seconds);
//                    	else mMusicPlayerServiceBinder.setCurrentTime(minutes + ":0" + seconds);
            		}

            		try { Thread.sleep(100); } catch (InterruptedException e) {}
            	}
				return null;
			}
		};
		seekBarChanger.execute();
	}

	public void pause() {
		state = PAUSED;
		mMediaPlayer.pause();
	}
	
	public int changeState() {
		switch(state){
		case PLAYING:
			pause(); break;
		case PAUSED:
			play(); break;
		}
		
		return state;									// return the value of the changed state as confirmation
	}
	
	public int getState() {
		return state;
	}
	
	@Override
	public void removeMusicFromQueue(Music music) {
		mNowPlaying.removeMusicFromQueue(music);
	}
	
	@Override
	public void skipToPoint(int point) {
		mMediaPlayer.seekTo(point);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		unregisterReceiver(mHeadPhoneBroadcastReceiver);

		
		if (seekBarChanger != null)
			seekBarChanger.cancel(false);
		seekBarChanger = null;
		
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		mMediaPlayer.release();
		Toast.makeText(this, "unBind with state: " + ((state == PLAYING) ? "PLAYING" : "PAUSED"), Toast.LENGTH_SHORT).show();
		return true;
	}

	public void registerSeekBar(SeekBar mSeekBar) {
		this.mSeekBar = mSeekBar;
	}

	public synchronized void playLast() {
		playFetched(mNowPlaying.last().getMusicLocation());
	}
}
