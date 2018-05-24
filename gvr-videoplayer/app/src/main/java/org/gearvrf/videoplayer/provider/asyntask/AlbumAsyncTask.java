package org.gearvrf.videoplayer.provider.asyntask;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.provider.MediaStore.Video;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.videoplayer.VideoPlayerApp;
import org.gearvrf.videoplayer.model.Album;

import java.util.LinkedList;
import java.util.List;

public class AlbumAsyncTask extends AsyncTask<Void, Void, List<Album>> {

    private static final String TAG = AlbumAsyncTask.class.getSimpleName();
    private GetDataCallback<List<Album>> mGetDataCallback;

    public AlbumAsyncTask() {
    }

    public AlbumAsyncTask(@NonNull GetDataCallback<List<Album>> mGetDataCallback) {
        this.mGetDataCallback = mGetDataCallback;
    }

    @Override
    protected List<Album> doInBackground(Void... voids) {
        return load();
    }

    public List<Album> load() {

        Context context = VideoPlayerApp.getInstance().getApplicationContext();
        List<Album> albums = new LinkedList<>();

        String[] projection = new String[]{
                Video.VideoColumns._ID,
                Video.VideoColumns.BUCKET_DISPLAY_NAME,
                Video.VideoColumns.TITLE
        };

        String sortOrder = Video.VideoColumns.ALBUM + " ASC, " + Video.VideoColumns.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                Video.Media.EXTERNAL_CONTENT_URI,
                projection, "0 == 0) GROUP BY (" + Video.VideoColumns.ALBUM, null, sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {

                Log.d(TAG, "Result count = " + cursor.getCount());

                AlbumCursorWrapper cursorWrapper = new AlbumCursorWrapper(cursor);

                do {

                    String albumTitle = cursorWrapper.getTitle();
                    albums.add(new Album(albumTitle, cursorWrapper.getVideo()));

                } while (cursor.moveToNext());
            }
        }

        return albums;
    }

    @Override
    protected void onPostExecute(List<Album> albums) {
        super.onPostExecute(albums);
        mGetDataCallback.onResult(albums);
    }

    private static class AlbumCursorWrapper extends CursorWrapper {

        AlbumCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        String getTitle() {
            return getString(getColumnIndexOrThrow(Video.VideoColumns.BUCKET_DISPLAY_NAME));
        }

        org.gearvrf.videoplayer.model.Video getVideo() {
            return new org.gearvrf.videoplayer.model.Video(
                    getLong(getColumnIndexOrThrow(Video.VideoColumns._ID)),
                    getString(getColumnIndexOrThrow(Video.VideoColumns.TITLE)),
                    null
            );
        }
    }
}
