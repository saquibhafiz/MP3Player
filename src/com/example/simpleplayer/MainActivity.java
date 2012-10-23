package com.example.simpleplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
	ListView listOfFiles;
	ArrayList<Music> list;
	ArrayList<Music> nowPlaying;
	ArrayList<Music> library;
	MusicViewAdapter mMusicViewAdapter;
	ImageView PlayPauseButton;
	SeekBar mSeekBar;
	TextView mTotalTime;
	TextView mCurrentTime;
	Spinner mSpinner;
	RelativeLayout mActions;
	
	MusicPlayerService mService;
	MusicPlayerServiceBinder mBinder;
	ServiceConnection mConnection;
	OnSeekBarChangeListener mOnSeekBarChangeListener;
	boolean mBound = false;
	
	List<String> filePaths = null;
	String typeOfOrders[] = {"Title","Artist"};
	
	int state;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        library = new ArrayList<Music>();
        
        final FileFlattener ff = new FileFlattener();

        listOfFiles = (ListView) findViewById(R.id.listoffiles);
        mMusicViewAdapter = new MusicViewAdapter(this, R.layout.musicview, library);
        listOfFiles.setAdapter(mMusicViewAdapter);
        listOfFiles.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mService.play(position);
			}
		});
        
        mSpinner = (Spinner) findViewById(R.id.order_selection);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,typeOfOrders);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				Collections.sort(library, getComparitor(position));
				if(mBound)
					mService.changeQueue(library);
				mMusicViewAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
        
        mActions = (RelativeLayout) findViewById(R.id.actions);
        final int origin[] = {-1,-1};
        mActions.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					origin[0] = (int) event.getX();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					int distance = ((int)event.getX()) - origin[0];
					if (distance > view.getWidth() / 5 * 2)
						mService.playNext();
					else if (-distance > view.getWidth() / 5 * 2)
						mService.playLast();
					
					origin[0] = -1;
				}
				return true;
			}
		});
        
        PlayPauseButton = (ImageView) findViewById(R.id.playpausebutton);
        PlayPauseButton.setClickable(false);
        
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setBackgroundColor(0x00000000);
        setOnSeekBarChangeListener();
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        
        mTotalTime = (TextView) findViewById(R.id.total_time);
        mTotalTime.setText("/ 0:00");
        mCurrentTime = (TextView) findViewById(R.id.current_time);
        mCurrentTime.setText("0:00 ");
        
        defineServiceConnection();	// we define our service connection mConnection
        bindService(new Intent(this, MusicPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);

        createLibrary(ff);	// create the library by grabbing all the files
    }

	private void setOnSeekBarChangeListener() {
		mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (mService == null)
					return;
				
				if (mBound)
					state = mService.changeState();
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mService == null)
					return;
				
				if (mBound)
					state = mService.changeState();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mService == null || !fromUser)
					return;
				
				mService.skipToPoint(progress);
			}
		};
	}

	private void defineServiceConnection() {
		mConnection = new ServiceConnection() {
        	
        	@Override
        	public void onServiceConnected(ComponentName name, IBinder service) {
        		startService(new Intent(MainActivity.this, MusicPlayerService.class));
        		mBinder = (MusicPlayerServiceBinder) service;
        		mService = mBinder.getService(new SeekBarTextCallback() {
					
					@Override
					public void setTotalTime(String time) {
						if (mTotalTime != null)
							mTotalTime.setText(time);
					}
					
					@Override
					public void setCurrentTime(String time) {
						if (mCurrentTime != null)
							mCurrentTime.setText(time);
					}
				});
        		state = mService.getState();
        		setPlayPauseOnClickListener();
        		mService.registerSeekBar(mSeekBar);
        		mBound = true;
        		
        		if (filePaths != null) 
        			initQueue();
        		
        		Log.d(getApplicationContext().toString(),"Service is connected and good to go");
        	}
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mBound = false;
				Log.d(getApplicationContext().toString(),"Service is disconnected and good to go");
			}
		};
	}

	private synchronized void initQueue() {
		mService.addMusicToQueue(library);
		mService.playNext();
		PlayPauseButton.setClickable(true);
	}

	private void createLibrary(final FileFlattener ff) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ff.flattenFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music", 6);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void params) {
				filePaths = ff.getFlattenedFiles();
				Log.d(this.toString(),"Done getting the files from the file flatenner");
				
				for (String filePath : filePaths) {
					library.add(new Music(filePath));
				}
				
				mMusicViewAdapter.notifyDataSetChanged();
				
				if (mBound)
					initQueue();
			}
        	
        }.execute();
	}

    @Override
    public void onDestroy() {

//    	if (mBound && mService.getState() == MusicPlayerService.PAUSED) {
    		super.onDestroy();
    		stopService(new Intent(MainActivity.this, MusicPlayerService.class));
    		unbindService(mConnection);
    		mBound = false;
//    	} else {
//    		super.onPause();
//    	}
    	
    }
    
    private void setPlayPauseOnClickListener(){
    	PlayPauseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mBound) {
					state = mService.changeState();
					
					switch (state){
					case MusicPlayerService.PLAYING:
						PlayPauseButton.setImageResource(R.drawable.pause);
						break;
					case MusicPlayerService.PAUSED:
						PlayPauseButton.setImageResource(R.drawable.play);
						break;
					}
							
				}
			}
		});
    }
    
    public void setCurrentTime(String time) {
    	if (mCurrentTime != null)
    		mCurrentTime.setText(time);
    }
    
    public void setTotalTime(String time) {
    	if (mTotalTime != null)
    		mTotalTime.setText(time);
    }
    
    private Comparator<Music> getComparitor(final int position) {
    	return new Comparator<Music>() {

			@Override
			public int compare(Music lhs, Music rhs) {
				if (position == 0)
					return (lhs.getName()).compareTo(rhs.getName());
				else if (position == 1)
					return (lhs.getArtist()).compareTo(rhs.getArtist());

				return 0;
			}
		};
    }
}
