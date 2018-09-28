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

package org.gearvrf.videoplayer.component.video.backbutton;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.Focusable;

@SuppressLint("InflateParams")
public class BackButton extends FadeableObject implements Focusable, IViewEvents {

    private GVRViewSceneObject mBackButtonObject;
    private ImageView mBackButton;
    private FocusListener mFocusListener = null;
    private View.OnClickListener mClickListener = null;

    public BackButton(final GVRContext gvrContext, int intViewId) {
        super(gvrContext);
        mBackButtonObject = new GVRViewSceneObject(gvrContext, intViewId, this);
        setName(getClass().getSimpleName());
    }

    public void setFocusListener(@NonNull FocusListener listener) {
        mFocusListener = listener;
    }

    public void setOnClickListener(@NonNull final View.OnClickListener listener) {
        mClickListener = listener;

        if (mBackButton != null) {
            mBackButton.setOnClickListener(listener);
        }
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mBackButtonObject;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mBackButton = view.findViewById(R.id.backButtonImage);
        mBackButton.setOnClickListener(mClickListener);
        mBackButton.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    mBackButtonObject.getRenderData().getMaterial().setOpacity(2.f);
                    if (mFocusListener != null) {
                        mFocusListener.onFocusGained(BackButton.this);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mBackButtonObject.getRenderData().getMaterial().setOpacity(.5f);
                    if (mFocusListener != null) {
                        mFocusListener.onFocusLost(BackButton.this);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(gvrViewSceneObject);
    }

    public void performClick() {
        mBackButton.post(new Runnable() {
            @Override
            public void run() {
                mBackButton.performClick();
            }
        });
    }

    @Override
    public void gainFocus() {

    }

    @Override
    public void loseFocus() {
    }
}
