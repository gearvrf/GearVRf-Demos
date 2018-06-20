package org.gearvrf.videoplayer.component.gallery;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.model.GalleryItem;
import org.gearvrf.videoplayer.model.HomeItem;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.ThumbnailLoader;
import org.gearvrf.videoplayer.util.TimeUtils;

import java.util.List;

public class GalleryItemAdapter<T extends GalleryItem> extends RecyclerView.Adapter<ViewHolder> {

    private List<T> mItemList;
    private OnItemsSelectionListener mOnItemsSelectionListener;

    GalleryItemAdapter(@NonNull List<T> mItemList) {
        this.mItemList = mItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @GalleryItem.Type int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        if (viewType == GalleryItem.Type.TYPE_HOME) {
            return new HomeViewHolder(inflater.inflate(R.layout.layout_item_home, viewGroup, false));
        } else if (viewType == GalleryItem.Type.TYPE_VIDEO) {
            return new VideoViewHolder(inflater.inflate(R.layout.layout_item_video, viewGroup, false));
        } else {
            return new AlbumViewHolder(inflater.inflate(R.layout.layout_item_album, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GalleryItem item = mItemList.get(position);

        if (item.getType() == GalleryItem.Type.TYPE_HOME) {

            HomeItem homeItem = (HomeItem) item;
            HomeViewHolder viewHolder = (HomeViewHolder) holder;
            viewHolder.mImage.setImageResource(homeItem.getImageResourceId());
            viewHolder.mLabel.setText(homeItem.getLabel());

        } else if (item.getType() == GalleryItem.Type.TYPE_ALBUM) {

            Album album = (Album) item;
            AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
            viewHolder.mTextView.setText(album.getTitle());
            new ThumbnailLoader(viewHolder.mThumbnail).execute(album.getVideoForThumbnail().getId());

        } else if (item.getType() == GalleryItem.Type.TYPE_VIDEO) {

            Video video = (Video) item;
            VideoViewHolder viewHolder = (VideoViewHolder) holder;
            viewHolder.title.setText(video.getTitle());
            viewHolder.duration.setText(TimeUtils.formatDurationFull(video.getDuration()));
            new ThumbnailLoader(viewHolder.thumbnail).execute(video.getId());

        } else {
            Log.d(getClass().getSimpleName(), "Unknown type:" + item.getType());
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private void notifyItemSelected(List<? extends GalleryItem> items) {
        if (mOnItemsSelectionListener != null) {
            mOnItemsSelectionListener.onItemSelected(items);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position).getType();
    }

    public void setOnItemSelectionListener(OnItemsSelectionListener listener) {
        this.mOnItemsSelectionListener = listener;
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
            itemView.findViewById(R.id.overlay_video).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyItemSelected(mItemList.subList(getAdapterPosition(), mItemList.size()));
        }
    }

    class AlbumViewHolder extends ViewHolder implements View.OnClickListener {

        ImageView mThumbnail;
        TextView mTextView;

        AlbumViewHolder(View itemView) {
            super(itemView);
            mThumbnail = itemView.findViewById(R.id.thumbnail);
            mTextView = itemView.findViewById(R.id.textView);
            itemView.findViewById(R.id.overlay).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyItemSelected(mItemList.subList(getAdapterPosition(), mItemList.size()));
        }
    }

    class HomeViewHolder extends ViewHolder implements View.OnClickListener {

        ImageView mImage;
        TextView mLabel;

        HomeViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.image);
            mLabel = itemView.findViewById(R.id.label);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            notifyItemSelected(mItemList.subList(getAdapterPosition(), mItemList.size()));
        }
    }
}
