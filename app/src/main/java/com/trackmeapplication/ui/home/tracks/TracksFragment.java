package com.trackmeapplication.ui.home.tracks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.trackmeapplication.R;
import com.trackmeapplication.adapter.RecordListViewAdapter;
import com.trackmeapplication.database.DatabaseHandler;
import com.trackmeapplication.database.RouteRecord;
import com.trackmeapplication.ui.sharedData.SharedViewModel;

import java.util.ArrayList;

public class TracksFragment extends Fragment {
    private SharedViewModel sharedViewModel;
    private ListView listView;
    private RecordListViewAdapter recordListViewAdapter;

    public TracksFragment() {}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        loadData();

        recordListViewAdapter = new RecordListViewAdapter(new ArrayList<>(), getActivity());

        sharedViewModel.getRecords().observe(getViewLifecycleOwner(), new Observer<ArrayList<RouteRecord>>() {
            @Override
            public void onChanged(ArrayList<RouteRecord> records) {
                recordListViewAdapter.setRecords(records);
                listView.setAdapter(recordListViewAdapter);
            }
        });

        listView = (ListView)root.findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem=(String) listView.getItemAtPosition(position);
            }
        });
        return root;
    }

    public void loadData() {
        DatabaseHandler handler = new DatabaseHandler(getActivity(), null, null, 1);
        ArrayList<RouteRecord> data = handler.getAll();
        if (data.isEmpty())
            return;
        sharedViewModel.setRecords(data);
    }
}