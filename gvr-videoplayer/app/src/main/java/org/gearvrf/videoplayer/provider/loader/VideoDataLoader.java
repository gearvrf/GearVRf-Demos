package org.gearvrf.videoplayer.provider.loader;

import android.content.Context;

import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.LocalVideoAsyncTask;

import java.util.List;

public class VideoDataLoader extends DataLoader<List<Video>> {

    private String mAlbumTitleFilter;

    public VideoDataLoader(Context context, String albumTitleFilter) {
        super(context);
        this.mAlbumTitleFilter = albumTitleFilter;
    }

    @Override
    public List<Video> loadInBackground() {
        return new LocalVideoAsyncTask(mAlbumTitleFilter).loadVideos();
    }
}
