package com.example.simpleplayer;

import android.content.Context;
import android.os.Binder;

public class MusicPlayerServiceBinder extends Binder{
	MusicPlayerService mMusicPlayerService;
	Context mApplication;
	SeekBarTextCallback mSeekBarTextCallback;

	public MusicPlayerServiceBinder(MusicPlayerService musicPlayerService, Context application) {
		mMusicPlayerService = musicPlayerService;
		mApplication = application;
	}
	
	public MusicPlayerService getService(SeekBarTextCallback seekBarTextCallback) {
		mSeekBarTextCallback = seekBarTextCallback;
		return mMusicPlayerService;
	}

	public synchronized void setCurrentTime(String time) {
		if (mApplication != null && mSeekBarTextCallback != null)
			mSeekBarTextCallback.setCurrentTime(time);
	}

	public void setTotalTime(String time) {
		if (mApplication != null && mSeekBarTextCallback != null)
			mSeekBarTextCallback.setTotalTime(time);
	}
}
