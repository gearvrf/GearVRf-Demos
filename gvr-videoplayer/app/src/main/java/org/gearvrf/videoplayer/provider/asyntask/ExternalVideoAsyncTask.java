package org.gearvrf.videoplayer.provider.asyntask;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.videoplayer.VideoPlayerApp;
import org.gearvrf.videoplayer.model.Video;

import java.util.LinkedList;
import java.util.List;

import static android.provider.MediaStore.Video.Media;
import static android.provider.MediaStore.Video.VideoColumns;

public class ExternalVideoAsyncTask extends AsyncTask<Void, Void, List<Video>> {

    private static final String TAG = ExternalVideoAsyncTask.class.getSimpleName();

    private GetDataCallback<List<Video>> mGetDataCallback;
    private String mAlbumTitleFilter;

    public ExternalVideoAsyncTask(String albumTitleFilter, @NonNull GetDataCallback<List<Video>> mGetDataCallback) {
        this.mAlbumTitleFilter = albumTitleFilter;
        this.mGetDataCallback = mGetDataCallback;
    }

    public ExternalVideoAsyncTask(String albumTitle) {
        this.mAlbumTitleFilter = albumTitle;
    }

    @Override
    protected List<Video> doInBackground(Void... voids) {
        return loadVideos();
    }

    public List<Video> loadVideos() {

        Context context = VideoPlayerApp.getInstance().getApplicationContext();
        List<Video> videos = new LinkedList<>();

        String[] projection = new String[]{
                VideoColumns._ID,
                VideoColumns.TITLE,
                VideoColumns.DATA,
                VideoColumns.DURATION
        };

        String selection = mAlbumTitleFilter != null ? VideoColumns.BUCKET_DISPLAY_NAME + "=?" : null;
        String[] selectionArgs = mAlbumTitleFilter != null ? new String[]{mAlbumTitleFilter} : null;

        String sortOrder = VideoColumns.TITLE + " ASC";

        try (Cursor cursor = context.getContentResolver().query(
                Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {

                Log.d(TAG, "Result count = " + cursor.getCount());

                VideoCursorWrapper cursorWrapper = new VideoCursorWrapper(cursor);

                do {

                    String videoTitle = cursorWrapper.getTitle();
                    long videoId = cursorWrapper.getId();

                    videos.add(new Video(videoId, videoTitle, cursorWrapper.getPath(), cursorWrapper.getDuration()));

                } while (cursor.moveToNext());
            }
        }

        return videos;
    }

    @Override
    protected void onPostExecute(List<Video> videos) {
        super.onPostExecute(videos);
        if (mGetDataCallback != null) {
            mGetDataCallback.onResult(videos);
        }
    }

    private static class VideoCursorWrapper extends CursorWrapper {

        VideoCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        String getTitle() {
            return getString(getColumnIndexOrThrow(VideoColumns.TITLE));
        }

        String getPath() {
            return getString(getColumnIndexOrThrow(VideoColumns.DATA));
        }

        long getId() {
            return getLong(getColumnIndexOrThrow(VideoColumns._ID));
        }

        long getDuration() {
            return getLong(getColumnIndexOrThrow(VideoColumns.DURATION));
        }
    }
}
