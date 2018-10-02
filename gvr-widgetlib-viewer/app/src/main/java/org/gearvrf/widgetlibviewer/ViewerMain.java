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

package org.gearvrf.widgetlibviewer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.widgetlib.content_scene.ContentSceneController;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.MainScene;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.properties.JSONHelpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.gearvrf.utility.Log.tag;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.getString;

public class ViewerMain extends GVRMain {
    private static final String TAG = tag(ViewerMain.class);

    private WidgetLib mWidgetLib;
    private final GVRWidgetLibViewer mActivity;
    private MainScene mMainScene;
    private GVRContext mGVRContext;
    private ContentSceneController mContentSceneController;
    private BackgroundWidget mBackgroundWidget;
    private Lights mLight;

    private ModelsListContentScene mModelsList;
    private BackgroundListContentScene mBackgroundList;
    private NotificationsContentScene mNotificationList;

    ViewerMain(GVRWidgetLibViewer activity) {
        mActivity = activity;
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    static class BackgroundWidget extends Widget {
        private enum Properties_level_ext {
            thumbnail
        }

        private List<String> mThumbnailsList = new ArrayList<>();

        BackgroundWidget(final GVRContext gvrContext) {
            super(gvrContext);
            setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND - 1);
            setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            JSONObject metadata = getObjectMetadata();
            JSONArray levels = JSONHelpers.optJSONArray(metadata, Properties.levels);
            for (int index = 0; index < levels.length(); ++index) {
                try {
                    String resIdStr = getString(levels.getJSONObject(index), Properties_level_ext.thumbnail);
                    mThumbnailsList.add(resIdStr);
                } catch (Exception e) {
                    Log.e(TAG, e, "Could not create background at %d", index);
                }
            }

            ArrayList<GVRRenderData> rdata = getSceneObject().getAllComponents(GVRRenderData.getComponentType());
            for (GVRRenderData r : rdata) {
                r.disableLight();
            }
            setTouchable(false);
            setFocusEnabled(false);
        }

        List<String> getThumbnailsList() {
            return mThumbnailsList;
        }
    }

    // WidgetLib members
    @Override
    public void onInit(final GVRContext gvrContext) {
        mGVRContext = gvrContext;
        try {
            mWidgetLib = WidgetLib.init(gvrContext, "app_metadata.json");
            mContentSceneController = WidgetLib.getContentSceneController();

            mMainScene = WidgetLib.getMainScene();
            mMainScene.adjustClippingDistanceForAllCameras();

            // initialize lights
            mLight = new Lights();
            mLight.loadLights(gvrContext);
            mLight.getLightScene().getTransform().setPosition(0, 10, 0);
            mLight.getLightScene().getTransform().rotateByAxis(-90, 1, 0, 0);
            mMainScene.addSceneObject(mLight.getLightScene());


        } catch (Exception e) {
            Log.e(TAG, "Could not initialize Widget library");
            e.printStackTrace();
            Log.e(TAG, e, "onCreate()");
            mActivity.finish();
            return;
        }

        GVRContext.addResetOnRestartHandler(WidgetLib.getSimpleAnimationTracker().clearTracker);

        mBackgroundWidget = new BackgroundWidget(mGVRContext);

        mModelsList = new ModelsListContentScene(gvrContext, mSettingsButtonTouchListener);

        mNotificationList = new NotificationsContentScene(mGVRContext, mHomeButtonTouchListener);

        WidgetLib.getMainThread().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mMainScene.addSceneObject(mBackgroundWidget);
                mContentSceneController.goTo(mModelsList);
//                mContentSceneController.goTo(mNotificationList);
                closeSplashScreen();
            }
        });

        mBackgroundList = new BackgroundListContentScene(mGVRContext,
                mBackgroundWidget, mHomeButtonTouchListener);
        WidgetLib.getTouchManager().setDefaultRightClickAction(defaultBackAction);

        Log.init(gvrContext.getContext(), true);
//        Log.enableSubsystem(Log.SUBSYSTEM.FOCUS, true);
//        Log.enableSubsystem(Log.SUBSYSTEM.WIDGET, true);
//        Log.enableSubsystem(Log.SUBSYSTEM.INPUT, true);
    }


    private Widget.OnTouchListener mSettingsButtonTouchListener = new Widget.OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            mContentSceneController.goTo(mBackgroundList);
            return true;
        }
    };

    private Widget.OnTouchListener mHomeButtonTouchListener = new Widget.OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            mContentSceneController.goBack();
            return true;
        }
    };


    @Override
    public void onStep() {
        FPSCounter.tick();
    }


    private final Runnable defaultBackAction = new Runnable() {
        @Override
        public void run() {
            mContentSceneController.goBack();
        }
    };
}