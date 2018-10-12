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

package org.gearvrf.videoplayer.component.video.loading;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;

public class LoadingAsset extends GVRSceneObject implements IViewEvents {
    private static final String TAG = LoadingAsset.class.getSimpleName();
    private GVRViewSceneObject mLoadingObject;
    LinearLayout mLoading;
    GVRAnimation mAnimation;

    public LoadingAsset(GVRContext gvrContext) {
        super(gvrContext);
        mLoadingObject = new GVRViewSceneObject(gvrContext, R.layout.layout_loading, this);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mLoading = view.findViewById(R.id.loading);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(gvrViewSceneObject);
        mAnimation = new GVRRotationByAxisWithPivotAnimation(this, 2, -360f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f).start(getGVRContext().getAnimationEngine());
        mAnimation.setRepeatMode(1);
        mAnimation.setRepeatCount(-1);
        Log.d(TAG, "Animation Loading ");
    }
}
