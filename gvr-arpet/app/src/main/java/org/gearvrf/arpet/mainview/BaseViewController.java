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

package org.gearvrf.arpet.mainview;

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
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BaseViewController extends BasePetView implements IViewController {

    private final String TAG = getClass().getSimpleName();
    private Map<Class<? extends IView>, ViewInfo> mViewInfo = new HashMap<>();

    private ViewGroup mContentView;
    private BaseView mViewModel;
    private DisplayMetrics mDisplayMetrics;

    public BaseViewController(PetContext petContext) {
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

    @Override
    public void registerView(Class<? extends IView> viewInterface, int layoutId, Class<? extends BaseView> viewImplementation) {
        mViewInfo.put(viewInterface, new ViewInfo(layoutId, viewImplementation));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IView> T makeView(@NonNull Class<T> type) {

        ViewInfo viewInfo = mViewInfo.get(type);
        if (viewInfo == null) {
            throw new RuntimeException("View type not registered: " + type.getClass().getSimpleName());
        }

        View view = View.inflate(mPetContext.getGVRContext().getContext(), viewInfo.layoutId, null);
        T viewModel = null;

        try {
            Constructor constructor = viewInfo.viewType.getConstructor(View.class, IViewController.class);
            viewModel = (T) constructor.newInstance(view, this);
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            Log.e(TAG, "Error showing view of type " + type.getName(), e);
        }

        return viewModel;
    }

    @Override
    public void showView(IView viewModel) {

        if (!viewModel.getClass().isInstance(mViewModel)) {
            mPetContext.getActivity().runOnUiThread(() -> {

                BaseView vm = (BaseView) viewModel;

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

    public IView getCurrentView() {
        return mViewModel;
    }

    private void clearContentView() {
        mPetContext.getActivity().runOnUiThread(() -> {
            mContentView.removeAllViews();
            mViewModel = null;
        });
    }

    @Override
    public void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    public void onHide(GVRScene mainScene) {
        clearContentView();
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    private static class ViewInfo {

        @LayoutRes
        int layoutId;

        Class<? extends IView> viewType;

        ViewInfo(@LayoutRes int layoutId, Class<? extends IView> viewType) {
            this.layoutId = layoutId;
            this.viewType = viewType;
        }
    }
}
