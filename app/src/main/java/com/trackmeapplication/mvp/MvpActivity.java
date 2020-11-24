package com.trackmeapplication.mvp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class MvpActivity extends AppCompatActivity implements MvpView {
    protected abstract MvpPresenter getPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenter().onAttach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().onDetach();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
