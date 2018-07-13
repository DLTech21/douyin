package io.github.dltech21.dy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.github.dltech21.dy.model.NextEvent;
import io.github.dltech21.dy.model.RecommandBean;


public class RecommandAdapter extends RecyclerView.Adapter<RecommandAdapter.ViewHolder> {
    private Context mContext;
    private List<RecommandBean.AwemeListBean> datas;


    public RecommandAdapter(final Context mContext, List<RecommandBean.AwemeListBean> datas) {
        this.mContext = mContext;
        this.datas = datas;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.view_room_item, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String video = datas.get(position).getVideo().getPlay_addr().getUrl_list().get(datas.get(position).getVideo().getPlay_addr().getUrl_list().size() - 1);
        holder.gsyVideoPlayer.setLooping(false);
        holder.gsyVideoPlayer.setUpLazy(video, true, null, null, "这是title");
        holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
        holder.gsyVideoPlayer.getFullscreenButton().setVisibility(View.GONE);
        holder.gsyVideoPlayer.setPlayTag(position + "");
        holder.gsyVideoPlayer.setPlayPosition(position);
        holder.gsyVideoPlayer.setAutoFullWithSize(true);
        holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
        holder.gsyVideoPlayer.setShowFullAnimation(true);
        holder.gsyVideoPlayer.setIsTouchWiget(false);
        holder.gsyVideoPlayer.setNeedShowWifiTip(false);
        holder.gsyVideoPlayer.setKeepScreenOn(true);
        holder.gsyVideoPlayer.loadCoverImage(datas.get(position).getVideo().getOrigin_cover().getUrl_list().get(0), R.drawable.bukeyong_hong);
        holder.gsyVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                EventBus.getDefault().post(new NextEvent());
            }
        });
        holder.title.setText(datas.get(position).getDesc());
    }


    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public SampleCoverVideo gsyVideoPlayer;
        private TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            gsyVideoPlayer = itemView.findViewById(R.id.detail_player);
            title = itemView.findViewById(R.id.titletv);
        }

    }

}
