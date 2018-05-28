package org.gearvrf.videoplayer.component.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.model.Media;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.ThumbnailLoader;
import org.gearvrf.videoplayer.util.TimeUtils;

import java.util.List;

public class MediaAdapter<T extends Media> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mMedias;

    public MediaAdapter(@NonNull List<T> mMedias) {
        this.mMedias = mMedias;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @Media.Type int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        if (viewType == Media.Type.TYPE_VIDEO) {
            return new VideoViewHolder(inflater.inflate(R.layout.layout_item_video, viewGroup, false));
        } else {
            return new AlbumViewHolder(inflater.inflate(R.layout.item_layout, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Media media = mMedias.get(position);

        if (media.getType() == Media.Type.TYPE_VIDEO) {
            Video video = (Video) media;
            VideoViewHolder viewHolder = (VideoViewHolder) holder;
            viewHolder.title.setText(video.getTitle());
            viewHolder.duration.setText(TimeUtils.formatDuration(video.getDuration()));
            new ThumbnailLoader(viewHolder.thumbnail).execute(video.getId());
        } else {
            Album album = (Album) media;
            AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
            viewHolder.mTextView.setText(album.getTitle());
            new ThumbnailLoader(viewHolder.mThumbnail).execute(album.getVideoForThumbnail().getId());
        }
    }

    @Override
    public int getItemCount() {
        return mMedias.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView title;
        TextView duration;

        VideoViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView mThumbnail;
        TextView mTextView;

        AlbumViewHolder(View v) {
            super(v);
            mThumbnail = v.findViewById(R.id.thumbnail);
            mTextView = v.findViewById(R.id.textView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMedias.get(position).getType();
    }
}
