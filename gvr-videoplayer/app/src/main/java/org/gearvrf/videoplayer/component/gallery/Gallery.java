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
import org.gearvrf.videoplayer.model.Album;
import org.gearvrf.videoplayer.model.Media;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.AlbumAsyncTask;
import org.gearvrf.videoplayer.provider.asyntask.GetDataCallback;
import org.gearvrf.videoplayer.provider.asyntask.VideoAsyncTask;

import java.util.LinkedList;
import java.util.List;

public class Gallery extends GVRSceneObject {

    private RecyclerView mRecyclerView;
    private GVRViewSceneObject mObjectViewGallery;
    private View mMainViewGallery;
    private List<Media> mMediaList = new LinkedList<>();

    @SuppressLint("InflateParams")
    public Gallery(GVRContext gvrContext) {
        super(gvrContext);

        mMainViewGallery = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.gallery_layout, null);
        mRecyclerView = mMainViewGallery.findViewById(R.id.recycler_view);

        MediaAdapter adapterGallery = new MediaAdapter<>(mMediaList);
        mRecyclerView.setAdapter(adapterGallery);

        mRecyclerView.setLayoutManager(new GridLayoutManager(gvrContext.getContext(), 3));

        mObjectViewGallery = new GVRViewSceneObject(gvrContext, mMainViewGallery, 1f, 1f);
        addChildObject(mObjectViewGallery);

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

    class Breadcrumb {

        View mHome;
        View mAlbum;

        public Breadcrumb(View mainView) {
            mHome = mainView.findViewById(R.id.home);
            mAlbum = mainView.findViewById(R.id.album);
        }

        void showHome() {

        }

        void showAlbum(String albumTitle) {

        }
    }
}

