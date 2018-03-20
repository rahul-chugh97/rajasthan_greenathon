package com.specico.solarpesa.Utils;

import android.graphics.Color;
import android.view.View;

import com.specico.solarpesa.R;

import io.saeid.fabloading.LoadingView;

/**
 * Created by rajkumar on 03/03/18.
 */

public class LoaderUtility {

    public LoaderUtility(LoadingView loadingView) {
        this.loadingView = loadingView;
        initializeLoader();
    }

    LoadingView loadingView;
    boolean loadingPaused = false;


    public void startLoader() {
        if(loadingPaused) {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.resumeAnimation();
            loadingView.startAnimation();
        }
        else {
            loadingView.setVisibility(View.VISIBLE);
            loadingView.startAnimation();
        }

    }

    public void stopLoader() {
        loadingPaused = true;
        loadingView.setVisibility(View.GONE);
        loadingView.pauseAnimation();
    }

    public void initializeLoader() {
        loadingView.addAnimation(Color.rgb(212,122,59), R.mipmap.launcher, LoadingView.FROM_LEFT);
        loadingView.addAnimation(Color.rgb(94,105,174), R.mipmap.launcher, LoadingView.FROM_TOP);
        loadingView.addAnimation(Color.rgb(229,181,81), R.mipmap.launcher, LoadingView.FROM_RIGHT);
        loadingView.addAnimation(Color.rgb(155,192,85), R.mipmap.launcher, LoadingView.FROM_BOTTOM);

        loadingView.addListener(new LoadingView.LoadingListener() {
            @Override
            public void onAnimationStart(int currentItemPosition) {

            }

            @Override
            public void onAnimationRepeat(int nextItemPosition) {

            }

            @Override
            public void onAnimationEnd(int nextItemPosition) {
                loadingView.startAnimation();
            }
        });
    }
}
