package org.gearvrf.videoplayer.provider.asyntask;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
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
                VideoColumns._ID,
                VideoColumns.BUCKET_DISPLAY_NAME,
                VideoColumns.TITLE,
                VideoColumns.DATA,
                VideoColumns.DURATION,
                VideoColumns.WIDTH,
                VideoColumns.HEIGHT
        };

        String sortOrder = VideoColumns.BUCKET_DISPLAY_NAME + " ASC, " + VideoColumns.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                Video.Media.EXTERNAL_CONTENT_URI,
                projection, "0 == 0) GROUP BY (" + VideoColumns.BUCKET_DISPLAY_NAME, null, sortOrder)) {

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
            return getString(getColumnIndexOrThrow(VideoColumns.BUCKET_DISPLAY_NAME));
        }

        org.gearvrf.videoplayer.model.Video getVideo() {
            float width = Float.parseFloat(getString(getColumnIndexOrThrow(VideoColumns.WIDTH)));
            float height = Float.parseFloat(getString(getColumnIndexOrThrow(VideoColumns.HEIGHT)));
            boolean isRatio21 =  width / height == 2;

            return new org.gearvrf.videoplayer.model.Video(
                    getLong(getColumnIndexOrThrow(VideoColumns._ID)),
                    getString(getColumnIndexOrThrow(VideoColumns.TITLE)),
                    getString(getColumnIndexOrThrow(VideoColumns.DATA)),
                    getLong(getColumnIndexOrThrow(VideoColumns.DURATION)),
                    isRatio21
            );
        }
    }
}
