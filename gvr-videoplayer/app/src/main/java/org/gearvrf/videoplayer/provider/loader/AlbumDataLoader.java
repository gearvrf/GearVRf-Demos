package org.gearvrf.videoplayer.provider.loader;

import android.content.Context;

import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.provider.asyntask.AlbumAsyncTask;

import java.util.List;

public class AlbumDataLoader extends DataLoader<List<Album>> {

    public AlbumDataLoader(Context context) {
        super(context);
    }

    @Override
    public List<Album> loadInBackground() {
        return new AlbumAsyncTask().load();
    }
}
