package org.gearvrf.videoplayer.component.gallery;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;

public class Gallery extends GVRSceneObject {
    RecyclerView mRecyclerView;
    GVRViewSceneObject mObjectViewGallery;
    View mMainViewGallery;

    public Gallery(GVRContext gvrContext) {
        super(gvrContext);

        mMainViewGallery = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.gallery_layout, null);
        mRecyclerView = mMainViewGallery.findViewById(R.id.recycler_view);

        AdapterGallery adapterGallery = new AdapterGallery();
        mRecyclerView.setAdapter(adapterGallery);

        mRecyclerView.setLayoutManager(new GridLayoutManager(gvrContext.getContext(), 3));

        mObjectViewGallery = new GVRViewSceneObject(gvrContext, mMainViewGallery ,1f,1f);
        addChildObject(mObjectViewGallery);

    }

}

