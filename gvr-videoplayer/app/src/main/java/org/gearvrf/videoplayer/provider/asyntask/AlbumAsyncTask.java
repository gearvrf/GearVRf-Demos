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
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.videoplayer.VideoPlayerApp;
import org.gearvrf.videoplayer.model.Album;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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
                VideoColumns.HEIGHT,
                "is_360_video"
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
                    isRatio21,
                    getInt(getColumnIndexOrThrow("is_360_video")) == 1,
                    Pattern.matches(".*360.*", getString(getColumnIndexOrThrow(VideoColumns.TITLE))),
                    org.gearvrf.videoplayer.model.Video.VideoType.LOCAL
            );
        }
    }
}
