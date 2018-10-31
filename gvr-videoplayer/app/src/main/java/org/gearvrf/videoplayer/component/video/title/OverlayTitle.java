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

package org.gearvrf.videoplayer.component.video.title;

import android.support.annotation.NonNull;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.focus.Focusable;

public class OverlayTitle extends FadeableObject implements Focusable, IViewEvents {

    private GVRViewSceneObject mTitleObject;

    public OverlayTitle(GVRContext gvrContext) {
        super(gvrContext);
        mTitleObject = new GVRViewSceneObject(gvrContext, R.layout.layout_title_image, this);
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mTitleObject;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(gvrViewSceneObject);
    }

    @Override
    public void gainFocus() {
        mTitleObject.getRenderData().getMaterial().setOpacity(2.f);
    }

    @Override
    public void loseFocus() {
        mTitleObject.getRenderData().getMaterial().setOpacity(1f);
    }
}
