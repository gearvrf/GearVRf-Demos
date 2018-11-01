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
import android.util.DisplayMetrics;
import android.util.Log;
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
import org.gearvrf.arpet.mode.view.IGuestLookingAtTargetView;
import org.gearvrf.arpet.mode.view.IHostLookingAtTargetView;
import org.gearvrf.arpet.mode.view.ILetsStartView;
import org.gearvrf.arpet.mode.view.INoConnectionFoundView;
import org.gearvrf.arpet.mode.view.ISharingAnchorView;
import org.gearvrf.arpet.mode.view.ISharingErrorView;
import org.gearvrf.arpet.mode.view.ISharingFinishedView;
import org.gearvrf.arpet.mode.view.IWaitingDialogView;
import org.gearvrf.arpet.mode.view.IWaitingForGuestView;
import org.gearvrf.arpet.mode.view.IWaitingForHostView;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ShareAnchorView extends BasePetView {

    private static final String TAG = ShareAnchorView.class.getSimpleName();
    private static final Map<Class<? extends ISharingAnchorView>, ViewInfo> sViewInfo;

    private ViewGroup mContentView;
    private BaseSharingAnchorView mViewModel;
    private DisplayMetrics mDisplayMetrics;

    static {
        sViewInfo = new HashMap<>();
        sViewInfo.put(ILetsStartView.class, new ViewInfo(R.layout.view_lets_start, LetsStartView.class));
        sViewInfo.put(IWaitingForHostView.class, new ViewInfo(R.layout.view_waiting_for_host, WaitingForHostView.class));
        sViewInfo.put(IWaitingForGuestView.class, new ViewInfo(R.layout.view_waiting_for_guests, WaitingForGuestView.class));
        sViewInfo.put(IConnectionFoundView.class, new ViewInfo(R.layout.view_connection_found, ConnectionFoundView.class));
        sViewInfo.put(INoConnectionFoundView.class, new ViewInfo(R.layout.view_no_connection_found, NoConnectionFoundView.class));
        sViewInfo.put(IGuestLookingAtTargetView.class, new ViewInfo(R.layout.view_guest_looking_at_target, GuestLookingAtTargetView.class));
        sViewInfo.put(IHostLookingAtTargetView.class, new ViewInfo(R.layout.view_host_looking_at_target, HostLookingAtTargetView.class));
        sViewInfo.put(IWaitingDialogView.class, new ViewInfo(R.layout.view_waiting_dialog, WaitingDialogView.class));
        sViewInfo.put(ISharingFinishedView.class, new ViewInfo(R.layout.view_sharing_finished, SharingFinishedView.class));
        sViewInfo.put(IAnchorSharedView.class, new ViewInfo(R.layout.view_anchor_shared, AnchorSharedView.class));
        sViewInfo.put(ISharingErrorView.class, new ViewInfo(R.layout.view_sharing_error, SharingErrorView.class));
    }

    public ShareAnchorView(PetContext petContext) {
        super(petContext);

        mDisplayMetrics = new DisplayMetrics();
        petContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(mDisplayMetrics);

        mContentView = (ViewGroup) View.inflate(petContext.getGVRContext().getContext(), R.layout.view_sharing_anchor_main, null);
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));

        GVRViewSceneObject viewObject = new GVRViewSceneObject(petContext.getGVRContext(), mContentView);
        viewObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        viewObject.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);

        getTransform().setPosition(0.0f, 0.0f, -0.74f);
        addChildObject(viewObject);
    }

    @SuppressWarnings("unchecked")
    public <T extends ISharingAnchorView> T makeView(@NonNull Class<T> type) {

        ViewInfo viewInfo = sViewInfo.get(type);
        View view = View.inflate(mPetContext.getGVRContext().getContext(), viewInfo.layoutId, null);
        T viewModel = null;

        try {
            Constructor constructor = viewInfo.viewType.getConstructor(View.class, ShareAnchorView.class);
            viewModel = (T) constructor.newInstance(view, this);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            Log.e(TAG, "Error showing view of type " + type.getName(), e);
        }

        return viewModel;
    }

    <T extends ISharingAnchorView> void showView(T viewModel) {

        if (!viewModel.getClass().isInstance(mViewModel)) {
            mPetContext.getActivity().runOnUiThread(() -> {

                BaseSharingAnchorView vm = (BaseSharingAnchorView) viewModel;

                View view = vm.getView();
                view.setLayoutParams(new ViewGroup.LayoutParams(
                        mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));

                clearContentView();
                mContentView.addView(view);
                mViewModel = vm;

                Log.d(TAG, "showView: " + viewModel.getClass().getSimpleName());
            });
        }
    }

    public ISharingAnchorView getCurrentView() {
        return mViewModel;
    }

    private void clearContentView() {
        mPetContext.getActivity().runOnUiThread(() -> {
            mContentView.removeAllViews();
            mViewModel = null;
        });
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        clearContentView();
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
}
