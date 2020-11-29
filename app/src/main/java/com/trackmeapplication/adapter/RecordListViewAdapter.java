package com.trackmeapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trackmeapplication.R;
import com.trackmeapplication.ui.detail.RouteDetailActivity;
import com.trackmeapplication.database.RouteRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordListViewAdapter implements ListAdapter {
    private ArrayList<RouteRecord> records;
    private Context context;

    public RecordListViewAdapter(ArrayList<RouteRecord> records, Context context) {
        this.records = records;
        this.context = context;
    }

    public void setRecords(ArrayList<RouteRecord> records) {
        this.records = records;
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
                    Intent intent = new Intent(context.getApplicationContext(), RouteDetailActivity.class);
                    intent.putExtra("RouteRecord", new Gson().toJson(record));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
            });
            TextView txtDescriptioin = (TextView)convertView.findViewById(R.id.txt_record_time);
            TextView txtViewSpeed = (TextView)convertView.findViewById(R.id.txt_record_avg_speed);
            TextView txtViewDuration = (TextView)convertView.findViewById(R.id.txt_record_duration);
            TextView txtViewDistance = (TextView)convertView.findViewById(R.id.txt_record_distance);

            Date date = new java.util.Date(record.getID());
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(date);

            txtDescriptioin.setText(("Route: %1").replace("%1", formattedDate));
            txtViewSpeed.setText(("%1 m/s").replace("%1", String.format("%.02f", record.getAvgSpeed())));
            txtViewDuration.setText(("%1 s").replace("%1", String.valueOf(record.getDuration())));
            txtViewDistance.setText(("%1 m").replace("%1", String.format("%.02f", record.getDistance())));
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
