package org.gearvrf.videoplayer.component.gallery;

import android.annotation.SuppressLint;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.model.Video;

import java.util.LinkedList;
import java.util.List;

@SuppressLint("InflateParams")
public class VideoGridSceneObject extends GVRSceneObject {

    private List<Video> mVideos = new LinkedList<>();
    private RecyclerView mRecyclerView;

    public VideoGridSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        createView();
    }

    private void createView() {
        View view = LayoutInflater.from(getGVRContext().getContext()).inflate(R.layout.layout_videos_container, null);
        mRecyclerView = view.findViewById(R.id.videosContainer);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getGVRContext().getContext(), 3));
        mRecyclerView.setAdapter(new VideoAdapter(mVideos));
        addChildObject(new GVRViewSceneObject(getGVRContext(), view, 11, 6));
    }

    public void updateVideos(List<Video> videos) {
        mVideos.clear();
        mVideos.addAll(videos);
        if (mRecyclerView != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
