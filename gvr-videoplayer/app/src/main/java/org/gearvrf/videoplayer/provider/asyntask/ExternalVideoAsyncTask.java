package org.gearvrf.videoplayer.provider.asyntask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.videoplayer.VideoPlayerApp;
import org.gearvrf.videoplayer.model.Video;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class ExternalVideoAsyncTask extends AsyncTask<Void, Void, List<Video>> {

    private static final String TAG = ExternalVideoAsyncTask.class.getSimpleName();

    private GetDataCallback<List<Video>> mGetDataCallback;

    public ExternalVideoAsyncTask(@NonNull GetDataCallback<List<Video>> mGetDataCallback) {
        this.mGetDataCallback = mGetDataCallback;
    }

    @Override
    protected List<Video> doInBackground(Void... voids) {
        return loadVideos();
    }

    public List<Video> loadVideos() {
        List<Video> videos = new LinkedList<>();
        JSONObject videosJSON = loadJSONFromFile();
        try {
            JSONArray jsonArray = videosJSON.getJSONArray("videos");
            int arrayLength = jsonArray.length();
            for (int i = 0; i < arrayLength; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Video video = new Video(jsonObject.getInt("id"), jsonObject.getString("name"),
                                        jsonObject.getString("uri"), jsonObject.getInt("duration"),
                                        false, false, false, Video.VideoType.EXTERNAL);

                video.setThumbnail(loadBitmap(jsonObject.getString("thumbnail")));
                videos.add(video);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Could not load videos info from JSON file");
            e.printStackTrace();
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

    private JSONObject loadJSONFromFile() {
        String jsonFile;
        JSONObject json = null;
        try {
            InputStream is = VideoPlayerApp.getInstance().getAssets().open("external_videos.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonFile = new String(buffer, "UTF-8");
            json = new JSONObject(jsonFile);
        } catch (IOException e) {
            Log.e(TAG, "Could not load JSON file");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "Could not load JSON file");
            e.printStackTrace();
        }
        return json;
    }

    private Bitmap loadBitmap(String thumbnailPath) {
        Bitmap bitmap = null;
        try {
            InputStream is = VideoPlayerApp.getInstance().getAssets().open(thumbnailPath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "Could not load thumbnail");
            e.printStackTrace();
        }
        return bitmap;
    }
}
