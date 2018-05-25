package org.gearvrf.videoplayer.component.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.ThumbnailLoader;
import org.gearvrf.videoplayer.util.TimeUtils;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> mVideos;

    VideoAdapter(@NonNull List<Video> mVideos) {
        this.mVideos = mVideos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new VideoViewHolder(inflater.inflate(R.layout.layout_item_video, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = mVideos.get(position);
        holder.title.setText(video.getTitle());
        holder.duration.setText(TimeUtils.formatDuration(video.getDuration()));
        new ThumbnailLoader(holder.background).execute(video.getId());
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {

        ImageView background;
        TextView title;
        TextView duration;

        VideoViewHolder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.background);
            title = itemView.findViewById(R.id.title);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}
