package com.trackmeapplication.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.trackmeapplication.R;
import com.trackmeapplication.database.RouteRecord;

import java.util.ArrayList;

public class RecordListViewAdapter implements ListAdapter {
    private ArrayList<RouteRecord> records;
    private Context context;

    public RecordListViewAdapter(ArrayList<RouteRecord> records, Context context) {
        this.records = records;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RouteRecord record = records.get(position);
        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_item,null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            TextView txtViewSpeed = (TextView)convertView.findViewById(R.id.txt_record_avg_speed);
            TextView txtViewDuration = (TextView)convertView.findViewById(R.id.txt_record_duration);
            TextView txtViewDistance = (TextView)convertView.findViewById(R.id.txt_record_distance);
            txtViewSpeed.setText(String.valueOf(record.getAvgSpeed()));
            txtViewDuration.setText(String.valueOf(record.getDuration()));
            txtViewDistance.setText(String.valueOf(record.getDistance()));
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return records.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
