package com.example.simpleplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HeadPhoneBroadcastReceiver extends BroadcastReceiver {

	private static final String IN = "IN";
	private static final String NOT_IN = "NOT IN";
	
	private MusicPlayerService mMusicPlayerService;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() != null && Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())){
			Bundle bundle = intent.getExtras();
			
			int state = bundle.getInt("state");
			
			if (state == 0){
				Log.d("HEADSET", NOT_IN);
				if (mMusicPlayerService != null && mMusicPlayerService.getState() == MusicPlayerService.PLAYING)
					mMusicPlayerService.pause();
			}
			else{
				Log.d("HEADSET", IN);
			}
		}
	}
	
	public void registerMusicPlayerService(MusicPlayerService musicPlayerService) {
		if (mMusicPlayerService != null)
			mMusicPlayerService = musicPlayerService;
	}
}
