package com.trackmeapplication.ui.home.tracks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.trackmeapplication.R;
import com.trackmeapplication.adapter.RecordListViewAdapter;
import com.trackmeapplication.database.DatabaseHandler;
import com.trackmeapplication.database.RouteRecord;
import com.trackmeapplication.ui.base.BaseFragment;
import com.trackmeapplication.mvpFragment.MvpFragmentPresenter;

import java.util.ArrayList;
import java.util.List;

public class TracksFragment extends BaseFragment implements ITracksView {
    private ArrayList<RouteRecord> mData;
    ListView listView;
    private ITracksPresenter presenter = new TracksPresenterImpl();

    public TracksFragment() {
    }

    @Override
    protected MvpFragmentPresenter getPresenter() {
        return presenter;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate();
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);

        loadData();
        if (mData.size() <= 0)
            return root;

        listView = (ListView)root.findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem=(String) listView.getItemAtPosition(position);
                Toast.makeText(getActivity(),clickedItem,Toast.LENGTH_LONG).show();
            }
        });
        RecordListViewAdapter recordListViewAdapter = new RecordListViewAdapter(mData, getActivity());
        listView.setAdapter(recordListViewAdapter);
        return root;
    }

    public void loadData() {
        DatabaseHandler handler = new DatabaseHandler(getActivity(), null, null, 1);
        ArrayList<RouteRecord> data = handler.loadData();
        mData = data;
    }

    @Override
    public void updateDatabase() {
        super.updateDatabase();
        loadData();
    }
}