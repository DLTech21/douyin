package io.github.dltech21.dy;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.dltech21.dlapp.network.RequestCallBack;
import io.github.dltech21.dlapp.network.ResponseContent;
import io.github.dltech21.dy.model.NextEvent;
import io.github.dltech21.dy.model.RecommandBean;


public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.view_pager)
    RecyclerView viewPager;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;

    private RecommandAdapter recommandAdapter;
    private List<RecommandBean.AwemeListBean> datas;

    private int currentPosition = 0;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ButterKnife.bind(this);
        swipeLayout.setColorSchemeResources(R.color.white_button_text_normal_color);
        swipeLayout.setOnRefreshListener(this);
        datas = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        viewPager.setLayoutManager(linearLayoutManager);
        recommandAdapter = new RecommandAdapter(this, datas);
        viewPager.setAdapter(recommandAdapter);
        new GravityPagerSnapHelper(Gravity.BOTTOM, true, new GravitySnapHelper.SnapListener() {
            @Override
            public void onSnap(final int position) {
                if (currentPosition == position) {
                    return;
                }
                currentPosition = position;

                viewPager.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startPlay(position);
                    }
                }, 500);

                if (currentPosition == datas.size() - 1) {
                    fetch(-1);
                }

            }
        }).attachToRecyclerView(viewPager);

        AndPermission.with(this)
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        onRefresh();
                    }
                })
                .start();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(NextEvent event) {
        viewPager.smoothScrollToPosition(currentPosition + 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void fetch(final int cursor) {
        RecommandAPI.fetchDy(this, cursor, new RequestCallBack() {
            @Override
            public void onFail(String msg) {
                if (swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onSuccess(ResponseContent content) {
                if (content.getCode() == 0) {
                    RecommandBean data = JSON.parseObject(content.getContent(), RecommandBean.class);
                    if (cursor == 0) {
                        datas.clear();
                    }
                    datas.addAll(data.getAweme_list());
                    recommandAdapter.notifyDataSetChanged();
                    if (cursor == 0) {
                        viewPager.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startPlay(0);
                            }
                        }, 500);
                    }
                }
                else {
                    if (swipeLayout.isRefreshing()) {
                        swipeLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onAfter() {
                super.onAfter();
                if (swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        fetch(0);
    }

    private void startPlay(int position) {
        if (null != viewPager.findViewHolderForAdapterPosition(position)) {
            RecommandAdapter.ViewHolder viewHolder = (RecommandAdapter.ViewHolder) viewPager.findViewHolderForAdapterPosition(position);
            viewHolder.gsyVideoPlayer.startPlayLogic();
        }
    }

}
