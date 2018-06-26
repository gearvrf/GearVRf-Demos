package org.gearvrf.videoplayer.provider.asyntask;

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
                Log.d(TAG, ""+jsonObject.getString("name"));
                videos.add(new Video(i, jsonObject.getString("name"), jsonObject.getString("uri"), 0, false, Video.VideoType.EXTERNAL));
            }
        } catch (JSONException e) {
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
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
