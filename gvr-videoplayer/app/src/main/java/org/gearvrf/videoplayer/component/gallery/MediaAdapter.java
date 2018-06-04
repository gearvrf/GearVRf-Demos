package org.gearvrf.videoplayer.component.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
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

public class MediaAdapter<T extends Media> extends RecyclerView.Adapter<ViewHolder> {

    private List<T> mMedias;
    private OnMediaSelectionListener mOnMediaSelectionListener;

    MediaAdapter(@NonNull List<T> mMedias) {
        this.mMedias = mMedias;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @Media.Type int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        if (viewType == Media.Type.TYPE_VIDEO) {
            return new VideoViewHolder(inflater.inflate(R.layout.layout_item_video, viewGroup, false));
        } else {
            return new AlbumViewHolder(inflater.inflate(R.layout.layout_item_album, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Media media = mMedias.get(position);

        if (media.getType() == Media.Type.TYPE_VIDEO) {
            Video video = (Video) media;
            VideoViewHolder viewHolder = (VideoViewHolder) holder;
            viewHolder.title.setText(video.getTitle());
            viewHolder.duration.setText(TimeUtils.formatDurationFull(video.getDuration()));
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

    private void notifyMediaSelected(List<? extends Media> mediaList) {
        if (mOnMediaSelectionListener != null) {
            mOnMediaSelectionListener.onMediaSelected(mediaList);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMedias.get(position).getType();
    }

    public void setOnMediaSelectionListener(OnMediaSelectionListener listener) {
        this.mOnMediaSelectionListener = listener;
    }

    class VideoViewHolder extends ViewHolder implements View.OnClickListener {

        ImageView thumbnail;
        TextView title;
        TextView duration;

        VideoViewHolder(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.title);
            duration = itemView.findViewById(R.id.duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyMediaSelected(mMedias.subList(getAdapterPosition(), mMedias.size()));
        }
    }

    class AlbumViewHolder extends ViewHolder implements View.OnClickListener {

        ImageView mThumbnail;
        TextView mTextView;

        AlbumViewHolder(View itemView) {
            super(itemView);
            mThumbnail = itemView.findViewById(R.id.thumbnail);
            mTextView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyMediaSelected(mMedias.subList(getAdapterPosition(), mMedias.size()));
        }
    }
}
