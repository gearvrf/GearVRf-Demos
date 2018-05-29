package org.gearvrf.videoplayer.component.gallery;

import android.annotation.SuppressLint;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.model.Media;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.AlbumAsyncTask;
import org.gearvrf.videoplayer.provider.asyntask.GetDataCallback;
import org.gearvrf.videoplayer.provider.asyntask.VideoAsyncTask;

import java.util.LinkedList;
import java.util.List;

public class Gallery extends GVRSceneObject implements OnMediaSelectionListener {

    private static final String TAG = Gallery.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private GVRViewSceneObject mObjectViewGallery;
    private View mMainViewGallery;
    private List<Media> mMediaList = new LinkedList<>();
    private Breadcrumb mBreadcrumb;

    @SuppressLint("InflateParams")
    public Gallery(GVRContext gvrContext) {
        super(gvrContext);

        mMainViewGallery = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.gallery_layout, null);
        mRecyclerView = mMainViewGallery.findViewById(R.id.recycler_view);

        MediaAdapter adapterGallery = new MediaAdapter<>(mMediaList);
        adapterGallery.setOnMediaSelectionListener(this);
        mRecyclerView.setAdapter(adapterGallery);

        mRecyclerView.setLayoutManager(new GridLayoutManager(gvrContext.getContext(), 3));

        mObjectViewGallery = new GVRViewSceneObject(gvrContext, mMainViewGallery, 1, 1);
        mObjectViewGallery.getTransform().setScale(6, 6, 1);
        addChildObject(mObjectViewGallery);

        mBreadcrumb = new Breadcrumb(mMainViewGallery);
        mBreadcrumb.showHome();
        mBreadcrumb.setOnBreadcrumbListener(new OnBreadcrumbListener() {
            @Override
            public void onHomeClicked() {
                loadAlbums();
            }
        });
        loadAlbums();
    }

    private void loadAlbums() {
        new AlbumAsyncTask(new GetDataCallback<List<Album>>() {
            @Override
            public void onResult(List<Album> data) {
                mMediaList.clear();
                mMediaList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    private void loadVideos(String albumTitle) {
        new VideoAsyncTask(albumTitle, new GetDataCallback<List<Video>>() {
            @Override
            public void onResult(List<Video> data) {
                mMediaList.clear();
                mMediaList.addAll(data);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).execute();
    }

    @Override
    public void onMediaSelected(Media media) {

        switch (media.getType()) {
            case Media.Type.TYPE_ALBUM:
                loadVideos(((Album) media).getTitle());
                mBreadcrumb.showAlbum(((Album) media).getTitle());
                break;
            case Media.Type.TYPE_VIDEO:
                break;
            default:
                Log.d(TAG, "Unknown type: " + media.getType());
        }
    }
}

