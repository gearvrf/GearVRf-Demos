/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import java.util.regex.Pattern;

import static android.provider.MediaStore.Video.Media;
import static android.provider.MediaStore.Video.VideoColumns;

public class LocalVideoAsyncTask extends AsyncTask<Void, Void, List<Video>> {

    private static final String TAG = LocalVideoAsyncTask.class.getSimpleName();

    private GetDataCallback<List<Video>> mGetDataCallback;
    private String mAlbumTitleFilter;

    public LocalVideoAsyncTask(String albumTitleFilter, @NonNull GetDataCallback<List<Video>> mGetDataCallback) {
        this.mAlbumTitleFilter = albumTitleFilter;
        this.mGetDataCallback = mGetDataCallback;
    }

    public LocalVideoAsyncTask(String albumTitle) {
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
                VideoColumns.DURATION,
                VideoColumns.WIDTH,
                VideoColumns.HEIGHT,
                "is_360_video"
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

                    videos.add(new Video(videoId, videoTitle, cursorWrapper.getPath(),
                            cursorWrapper.getDuration(), cursorWrapper.getIsRatio21(),
                            cursorWrapper.getIs360Video(), cursorWrapper.has360onTitle(),
                            Video.VideoType.LOCAL));

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

        String getWidth() {
            return getString(getColumnIndexOrThrow(VideoColumns.WIDTH));
        }

        String getHeight() {
            return getString(getColumnIndexOrThrow(VideoColumns.HEIGHT));
        }

        boolean getIsRatio21() {
            String width = getWidth();
            String height = getHeight();
            if (width == null || height == null)
                return false;
            return Float.parseFloat(getWidth()) / Float.parseFloat(getHeight()) == 2;
        }

        boolean getIs360Video() {
            return getInt(getColumnIndexOrThrow("is_360_video")) == 1;
        }

        boolean has360onTitle() {
            return Pattern.matches(".*360.*", getTitle());
        }
    }
}
