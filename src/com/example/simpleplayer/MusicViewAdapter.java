package com.example.simpleplayer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MusicViewAdapter extends ArrayAdapter<Music>{
	private List<Music> list;
	private Context context;

	public MusicViewAdapter(Context context, int textViewResourceId, List<Music> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		list = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.musicview, null);
        }

        Music music = list.get(position);
        if (music != null) {
        	TextView title = (TextView)view.findViewById(R.id.title);
        	TextView artist = (TextView)view.findViewById(R.id.artist);
//        	TextView album = (TextView)view.findViewById(R.id.album);
//        	ImageView profile_picture =(ImageView)view.findViewById(R.id.user_pic);
        	TextView duration = (TextView)view.findViewById(R.id.duration);
        	
        	title.setText(music.getName());
        	artist.setText(music.getArtist());
//        	album.setText(music.getAlbum());

        	int minutes = music.getTime()/60, seconds = music.getTime()%60;
        	if (seconds >= 10) duration.setText(minutes + ":" + seconds);
        	else duration.setText(minutes + ":0" + seconds);
        }

        return view;
	}
}
