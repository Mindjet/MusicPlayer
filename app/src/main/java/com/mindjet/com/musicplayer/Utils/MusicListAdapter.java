package com.mindjet.com.musicplayer.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindjet.com.musicplayer.ItemBean.Mp3Info;
import com.mindjet.com.musicplayer.R;

import java.util.List;

/**
 * @author Mindjet
 * @date 2016/5/26
 */
public class MusicListAdapter extends BaseAdapter{

    private List<Mp3Info> mp3InfoList;
    private LayoutInflater inflater;

    public MusicListAdapter(List<Mp3Info> mp3InfoList, Context context) {
        this.mp3InfoList = mp3InfoList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mp3InfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3InfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView==null){

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item,parent,false);
            holder.title  = (TextView) convertView.findViewById(R.id.title);
            holder.artist = (TextView) convertView.findViewById(R.id.artist);
            holder.imageView = (ImageView) convertView.findViewById(R.id.album);
            holder.duration = (TextView) convertView.findViewById(R.id.duration);
            convertView.setTag(holder);

        }else {

            holder = (ViewHolder) convertView.getTag();

        }

        holder.title.setText(mp3InfoList.get(position).title);
        holder.artist.setText(mp3InfoList.get(position).artist);
        holder.duration.setText(MediaUtil.formatTime(mp3InfoList.get(position).duration));
        holder.imageView.setImageResource(R.mipmap.playing);

        return convertView;
    }

    class ViewHolder{

        public ImageView imageView;
        public TextView title;
        public TextView artist;
        public TextView duration;

    }

}
