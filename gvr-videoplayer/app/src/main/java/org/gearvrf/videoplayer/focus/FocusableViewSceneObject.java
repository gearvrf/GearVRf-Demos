/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.videoplayer.focus;

import android.support.annotation.NonNull;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.component.FadeableViewObject;

public class FocusableViewSceneObject extends FadeableViewObject implements Focusable {

    private FocusListener<FocusableViewSceneObject> mFocusListener;

    public FocusableViewSceneObject(GVRContext gvrContext, View view, float width, float height) {
        super(gvrContext, view, width, height);
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return this;
    }

    @Override
    public void gainFocus() {
        if (this.mFocusListener != null) {
            this.mFocusListener.onFocusGained(this);
        }
    }

    @Override
    public void loseFocus() {
        if (this.mFocusListener != null) {
            mFocusListener.onFocusLost(this);
        }
    }

    public void setFocusListener(@NonNull FocusListener<FocusableViewSceneObject> mFocusListener) {
        this.mFocusListener = mFocusListener;
    }
}
