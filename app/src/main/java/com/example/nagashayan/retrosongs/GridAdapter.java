package com.example.nagashayan.retrosongs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 10/3/15.
 */

public class GridAdapter extends BaseAdapter {

    //Grid list and layout
    private ArrayList<Grid> Grids;
    private LayoutInflater GridInf;

    //constructor
    public GridAdapter(Context c, ArrayList<Grid> theGrids){
        Grids=theGrids;
        GridInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return Grids.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to Grid layout
        RelativeLayout GridLay = (RelativeLayout)GridInf.inflate
                (R.layout.list_text_view, parent, false);
        //get title and artist views
        TextView nameView = (TextView)GridLay.findViewById(R.id.name);
        //ImageView imageView = (ImageView)GridLay.findViewById(R.id.image);
        //get Grid using position
        Grid currGrid = Grids.get(position);
        //get title and artist strings
        nameView.setText(currGrid.getTitle());
        //imageView.setText(currGrid.getArtist());
        //set position as tag
        GridLay.setTag(position);
        return GridLay;
    }

}

