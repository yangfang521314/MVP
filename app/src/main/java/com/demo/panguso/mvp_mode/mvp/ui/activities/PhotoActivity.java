package com.demo.panguso.mvp_mode.mvp.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.demo.panguso.mvp_mode.R;
import com.demo.panguso.mvp_mode.common.Constants;
import com.demo.panguso.mvp_mode.common.LoadNewsType;
import com.demo.panguso.mvp_mode.listener.OnItemClickListener;
import com.demo.panguso.mvp_mode.mvp.bean.PhotoGirl;
import com.demo.panguso.mvp_mode.mvp.presenter.impl.PhotoPresenterImpl;
import com.demo.panguso.mvp_mode.mvp.ui.activities.base.BaseActivity;
import com.demo.panguso.mvp_mode.mvp.ui.adapter.PhotoListAdapter;
import com.demo.panguso.mvp_mode.mvp.view.PhotoView;
import com.demo.panguso.mvp_mode.utils.NetUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class PhotoActivity extends BaseActivity implements PhotoView, SwipeRefreshLayout.OnRefreshListener {
    @Inject
    PhotoPresenterImpl mPhotoPresenter;

    @Inject
    PhotoListAdapter mPhotoListAdapter;
    @Inject
    Activity mActivity;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.photo_rv)
    RecyclerView mPhotoRV;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.empty_view)
    TextView mEmptyView;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    private boolean mIsAllLoad;

    @Override
    protected void initViews() {
        mToolbar.setTitle(R.string.news_photo);
//        mIsHasNavigationView = true;
        initSiwpRefresh();
        initRecyclerView();
        //图片的点击事件
        setAdapterItemClickEvent();
        initPresenter();
    }

    private void setAdapterItemClickEvent() {
        mPhotoListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int layoutPosition) {
                Intent intent = new Intent(PhotoActivity.this, PhotoDetailActivity.class);
                intent.putExtra(Constants.PHOTO_DETAIL, mPhotoListAdapter.getList().get(layoutPosition).getUrl());
                //整个打开图片的动画
                startActivity(itemView, intent);
            }
        });

    }

    private void startActivity(View itemView, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    mActivity, itemView, getResources().getString(R.string.transition_photos));
            startActivity(intent, options.toBundle());
        } else {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                    .makeScaleUpAnimation(itemView, itemView.getWidth() / 2, itemView.getHeight() / 2, 0, 0);
            ActivityCompat.startActivity(mActivity, intent, optionsCompat.toBundle());
        }
    }

    private void initPresenter() {
        mPresenter = mPhotoPresenter;
        mPresenter.attachView(this);
        mPresenter.onCreate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initRecyclerView() {
        mPhotoRV.setHasFixedSize(true);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mPhotoRV.setLayoutManager(layoutManager);
        mPhotoRV.setItemAnimator(new DefaultItemAnimator());
        mPhotoRV.setAdapter(mPhotoListAdapter);
        mPhotoRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int[] lastVisibleItemPosition = ((StaggeredGridLayoutManager) layoutManager)
                        .findLastVisibleItemPositions(null);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItmeCount = layoutManager.getItemCount();
                //这是在一下的几种情况下
                //(1) mIsAllLoad== false 在加载更多的时候
                //Reccyclerw没有滑动的时候
                //在显示最后一个显示的位置的时候所要存在的条件 > 总的-1
                if (!mIsAllLoad && visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE
                        && ((lastVisibleItemPosition[0]) >= totalItmeCount - 1)
                        || (lastVisibleItemPosition[1] >= totalItmeCount - 1)) {
                    mPhotoPresenter.loadMore();
                    mPhotoListAdapter.showFooter();
                    //当加载的时候，recyclerView显示的位置，当加载更多的时候
                    mPhotoRV.scrollToPosition(mPhotoListAdapter.getItemCount() - 1);
                }
            }
        });


    }

    private void initSiwpRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.gplus_colors));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo;
    }

    @Override
    protected void initInjector() {
        mActivityComponent.inject(this);
    }

    /**
     * 得到图片数据的方法
     *
     * @param photoGirls
     * @param loadType
     */
    @Override
    public void initPhotoList(List<PhotoGirl> photoGirls, @LoadNewsType.checker int loadType) {
        switch (loadType) {
            case LoadNewsType.TYPE_REFRESH_SUCCESS:
                mSwipeRefreshLayout.setRefreshing(false);
                mPhotoListAdapter.setList(photoGirls);
                mPhotoListAdapter.notifyDataSetChanged();
                checkIsEmpty(photoGirls);
                mIsAllLoad = false;
                break;
            case LoadNewsType.TYPE_REFRESH_ERROR:
                mSwipeRefreshLayout.setRefreshing(false);
                checkIsEmpty(photoGirls);
                break;
            case LoadNewsType.TYPE_LOAD_MORE_SUCCESS:
                mPhotoListAdapter.hideFooter();
                if (photoGirls == null || photoGirls.size() == 0) {
                    mIsAllLoad = true;
                    Snackbar.make(mPhotoRV, getString(R.string.no_more), Snackbar.LENGTH_LONG).show();
                } else {
                    mPhotoListAdapter.addMore(photoGirls);
                }
                break;
            case LoadNewsType.TYPE_LOAD_MORE_ERROR:
                mPhotoListAdapter.hideFooter();
                break;
        }
    }

    /**
     * 这是载数据为空的时候显示的view
     *
     * @param photoGirls
     */
    private void checkIsEmpty(List<PhotoGirl> photoGirls) {
        if (photoGirls == null && mPhotoListAdapter.getList() == null) {
            mPhotoRV.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mPhotoRV.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showErrorMsg(String message) {
        mProgressBar.setVisibility(View.GONE);
        if (NetUtil.isNetworkAvailable()) {
            Snackbar.make(mPhotoRV, message, Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick({R.id.fab, R.id.empty_view})
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            mPhotoRV.getLayoutManager().scrollToPosition(0);
        }
        if (view.getId() == R.id.empty_view) {
            mSwipeRefreshLayout.setRefreshing(true);
            mPhotoPresenter.refreshData();
        }
    }

    @Override
    public void onRefresh() {
        mPhotoPresenter.refreshData();
    }
}
