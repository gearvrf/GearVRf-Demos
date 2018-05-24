package org.gearvrf.videoplayer.provider.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.gearvrf.videoplayer.model.Video;

import java.util.List;

public class DefaultVideoLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Video>> {

    private Context mContext;
    private String mAlbumTitleFilter;

    public DefaultVideoLoaderCallbacks(Context mContext, String albumTitleFilter) {
        this.mContext = mContext;
        this.mAlbumTitleFilter = albumTitleFilter;
    }

    @Override
    public Loader<List<Video>> onCreateLoader(int id, Bundle args) {
        return new VideoDataLoader(mContext, mAlbumTitleFilter);
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) {
    }

}
