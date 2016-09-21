package com.demo.panguso.mvp_mode.mvp.ui.fragment.base;


import android.support.v4.app.Fragment;

import com.demo.panguso.mvp_mode.app.App;
import com.demo.panguso.mvp_mode.mvp.presenter.base.BasePresenter;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by ${yangfang} on 2016/9/9.
 */
public class BaseFragment<T extends BasePresenter> extends Fragment {

    protected T mPresenter;

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getWatcher(getActivity());
        refWatcher.watch(this);
        if (mPresenter != null) {
            mPresenter.onDestory();
        }
    }
}
