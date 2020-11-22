package com.trackmeapplication.ui.tracks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.trackmeapplication.R;

public class TracksFragment extends Fragment {
    private TracksViewModel tracksViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tracksViewModel =
                new ViewModelProvider(this).get(TracksViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        tracksViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}