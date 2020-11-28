package com.trackmeapplication.mvpFragment;

import android.content.Context;

import androidx.fragment.app.Fragment;

public abstract class MvpFragment extends Fragment implements MvpFragmentView {
    protected abstract MvpFragmentPresenter getPresenter();

    protected void onCreate() {
        getPresenter().onAttach(this);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
