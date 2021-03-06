package com.sina.weibo.sdk.simple.weibo.ui.fragment;


import android.app.ActivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.orhanobut.logger.Logger;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;
import com.sina.weibo.sdk.simple.weibo.R;
import com.sina.weibo.sdk.simple.weibo.adapter.UserFriendsAdapter;
import com.sina.weibo.sdk.simple.weibo.model.CommonFriendsInfo;
import com.sina.weibo.sdk.simple.weibo.presenter.UserFriendsPresenter;
import com.sina.weibo.sdk.simple.weibo.ui.activity.PublicTimeLineActivity;
import com.sina.weibo.sdk.simple.weibo.ui.view.LoadMoreFooterView;
import com.sina.weibo.sdk.simple.weibo.ui.view.RefreshHeaderView;
import com.sina.weibo.sdk.simple.weibo.util.Tools;
import com.sina.weibo.sdk.simple.weibo.view.UserFriendsInfoView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFansFragment extends Fragment {

    @BindView(R.id.title_bar_title)
    TextView mTitleBarTitle;
    @BindView(R.id.title_bar_write_image_view)
    ImageView mTitleBarWriteImageView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swipe_refresh_header)
    RefreshHeaderView mSwipeRefreshHeader;
    @BindView(R.id.swipe_target)
    RecyclerView mSwipeTarget;
    @BindView(R.id.swipe_load_more_footer)
    LoadMoreFooterView mSwipeLoadMoreFooter;
    @BindView(R.id.swipeToLoadLayout)
    SwipeToLoadLayout mSwipeToLoadLayout;
    @BindView(R.id.iv_empty)
    ImageView mIvEmpty;
    @BindView(R.id.empty_view)
    RelativeLayout mEmptyView;
    Unbinder unbinder;

    private Oauth2AccessToken mAccessToken;
    List<CommonFriendsInfo.UsersBean> mUsersBeanList;
    private UserFriendsAdapter mUserFriendsAdapter;
    private UserFriendsPresenter mUserFriendsPresenter;

    public UserFansFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_fans, container, false);
        unbinder = ButterKnife.bind(this, view);
        //加载数据
        initData();
        //加载监听
        initListener();
        return view;
    }


    /**
     * 初始化监听
     */
    private void initListener() {

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSwipeToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });

        mSwipeToLoadLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });


    }


    /**
     * 初始化数据
     */
    private void initData() {
        mTitleBarWriteImageView.setVisibility(View.GONE);
        mTitleBarTitle.setText(getResources().getString(R.string.user_fans_list));

        mSwipeTarget.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersBeanList = new ArrayList<>();
        mUserFriendsAdapter = new UserFriendsAdapter(getActivity(), mUsersBeanList);
        mSwipeTarget.setAdapter(mUserFriendsAdapter);

        mUserFriendsPresenter = new UserFriendsPresenter(getActivity());
        mUserFriendsPresenter.onCreate();

        mAccessToken = AccessTokenKeeper.readAccessToken(getActivity());
        if (mAccessToken.isSessionValid()) {
            mSwipeToLoadLayout.setRefreshing(true);
        }
    }


    /**
     * 加载更多数据
     */
    private void loadMoreData() {
        mUserFriendsPresenter.getUserFansInfo(mAccessToken, Long.valueOf(mAccessToken.getUid()), 0);
        mUserFriendsPresenter.onAttachView(new UserFriendsInfoView() {
            @Override
            public void onSuccess(CommonFriendsInfo commonFriendsInfo) {
                List<CommonFriendsInfo.UsersBean> usersBeanList = commonFriendsInfo.getUsers();
                loadData(usersBeanList);
                mSwipeToLoadLayout.setLoadingMore(false);
            }

            @Override
            public void onFailure(String errorMsg) {
                Logger.d(errorMsg);
            }
        });
    }


    /**
     * 刷新数据
     */
    private void refreshData() {
        mUserFriendsPresenter.getUserFansInfo(mAccessToken, Long.valueOf(mAccessToken.getUid()), 0);
        mUserFriendsPresenter.onAttachView(new UserFriendsInfoView() {
            @Override
            public void onSuccess(CommonFriendsInfo commonFriendsInfo) {
                List<CommonFriendsInfo.UsersBean> usersBeanList = commonFriendsInfo.getUsers();
                loadData(usersBeanList);
                Tools.showEmptyView(mEmptyView, mSwipeToLoadLayout, mSwipeTarget);
                mSwipeToLoadLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(String errorMsg) {
                Logger.d(errorMsg);
            }
        });
    }


    //加载数据
    private void loadData(final List<CommonFriendsInfo.UsersBean> usersBeanList) {
        Tools.decideData(getActivity(), usersBeanList, new Tools.LoadMoreCallback() {
            @Override
            public void loadMoreData() {
                mUsersBeanList.clear();
                mUsersBeanList.addAll(usersBeanList);
                mUserFriendsAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mUserFriendsPresenter.onStop();
    }
}
