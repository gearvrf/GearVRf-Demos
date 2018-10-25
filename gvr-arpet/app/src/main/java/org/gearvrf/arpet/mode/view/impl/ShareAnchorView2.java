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

package org.gearvrf.arpet.mode.view.impl;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.mode.BasePetView;
import org.gearvrf.arpet.mode.view.IAnchorSharedView;
import org.gearvrf.arpet.mode.view.IConnectionFoundView;
import org.gearvrf.arpet.mode.view.ILetsStartView;
import org.gearvrf.arpet.mode.view.INoConnectionFoundView;
import org.gearvrf.arpet.mode.view.ISharingAnchorView;
import org.gearvrf.arpet.mode.view.ISharingErrorView;
import org.gearvrf.arpet.mode.view.IWaitingForGuestView;
import org.gearvrf.arpet.mode.view.IWaitingForHostView;
import org.gearvrf.arpet.mode.view.IWaitingMessageView;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ShareAnchorView2 extends BasePetView {

    private static final Map<Class<? extends ISharingAnchorView>, ViewInfo> sViewInfo;
    private ViewGroup mMainView;
    private ISharingAnchorView mCurrentView;

    static {
        sViewInfo = new HashMap<>();
        sViewInfo.put(ILetsStartView.class, new ViewInfo(R.layout.view_lets_start, LetsStartView.class));
        sViewInfo.put(IWaitingForHostView.class, new ViewInfo(R.layout.view_waiting_for_host, WaitingForHostView.class));
        sViewInfo.put(IWaitingForGuestView.class, new ViewInfo(R.layout.view_waiting_for_guests, WaitingForGuestView.class));
        sViewInfo.put(IConnectionFoundView.class, new ViewInfo(R.layout.view_connection_found, ConnectionFoundView.class));
        sViewInfo.put(INoConnectionFoundView.class, new ViewInfo(R.layout.view_no_connection_found, NoConnectionFoundView.class));
        sViewInfo.put(IWaitingMessageView.class, new ViewInfo(R.layout.view_waiting_message, WaitingMessageView.class));
        sViewInfo.put(IAnchorSharedView.class, new ViewInfo(R.layout.view_anchor_shared, AnchorSharedView.class));
        sViewInfo.put(ISharingErrorView.class, new ViewInfo(R.layout.view_sharing_error, SharingErrorView.class));
    }

    public ShareAnchorView2(PetContext petContext) {
        super(petContext);

        mMainView = (ViewGroup) View.inflate(petContext.getGVRContext().getContext(), R.layout.view_sharing_anchor_main, null);
        GVRViewSceneObject viewObject = new GVRViewSceneObject(petContext.getGVRContext(), mMainView);

        viewObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        viewObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);

        getTransform().setPosition(0.0f, 0.0f, -0.9f);
        addChildObject(viewObject);
    }

    public <T extends ISharingAnchorView> void showView(@NonNull Class<T> type, @NonNull OnShowViewListener<T> listener) {
        mPetContext.getActivity().runOnUiThread(() -> {
            mMainView.removeAllViews();
            ViewInfo viewInfo = sViewInfo.get(type);
            View view = View.inflate(mPetContext.getGVRContext().getContext(), viewInfo.layoutId, mMainView);
            T viewModel = null;
            try {
                Constructor constructor = viewInfo.viewType.getConstructor(View.class);
                viewModel = (T) constructor.newInstance(view);
            } catch (NoSuchMethodException | IllegalAccessException
                    | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
            mCurrentView = viewModel;
            listener.onShown(viewModel);
        });
    }

    public ISharingAnchorView getCurrentView() {
        return mCurrentView;
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    private static class ViewInfo {

        @LayoutRes
        int layoutId;

        Class<? extends ISharingAnchorView> viewType;

        ViewInfo(@LayoutRes int layoutId, Class<? extends ISharingAnchorView> viewType) {
            this.layoutId = layoutId;
            this.viewType = viewType;
        }
    }

    @FunctionalInterface
    public interface OnShowViewListener<T extends ISharingAnchorView> {
        void onShown(T view);
    }
}
