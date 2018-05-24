package org.gearvrf.videoplayer.provider.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import org.gearvrf.videoplayer.model.Album;

import java.util.List;

public class DefaultAlbumLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Album>> {

    private Context mContext;

    public DefaultAlbumLoaderCallbacks(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {
        return new AlbumDataLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {
    }
}
