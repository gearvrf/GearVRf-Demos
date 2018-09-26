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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.widgetlib.content_scene.ContentSceneController;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.TransformCache;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlibviewer.shaders.NoTextureShader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.gearvrf.utility.Log.tag;

public class Model extends GroupWidget {

    ArrayList<GVRMaterial> mOriginalMaterial;
    List<GVRAnimation> mAnimation;
    private float mCurrentZoom = 0;
    private boolean mLightEnabled = true;

    private static final String TAG = tag(Model.class);
    private static final float MIN_RADIUS = 1.5f;
    private static final float MAX_RADIUS = 2.5f;
    private static final float MAX_ZOOM = 4f;

    private static ModelViewer mModelViewer;
    private final String mLocation;

    public Model(GVRContext context, String name, String location) throws IOException {
        super(context, loadModel(context, location));
        mLocation = location;
        setName(location);
        GVRSceneObject model = getSceneObject();
        model.attachComponent(new GVRMeshCollider(context, true));

        // Adding Pointee to Model
        GVRSceneObject.BoundingVolume bv = model.getBoundingVolume();
        float originalRadius = bv.radius;
        Log.i(TAG, "Radius" + Float.toString(originalRadius));

        if (originalRadius > MAX_RADIUS || originalRadius < MIN_RADIUS) {
            float scaleFactor = MAX_RADIUS / originalRadius;
            setScale(scaleFactor, scaleFactor, scaleFactor);
        }

        // Make Copy of Original Render Data

        Widget.OnTouchListener homeButtonTouchListener = new Widget.OnTouchListener() {
            @Override
            public boolean onTouch(Widget widget, final float[] coords) {
                Log.d(TAG, "Home touched!");
                WidgetLib.getContentSceneController().goBack();
                return true;
            }
        };

        enableDisableLight(false);

        if (mModelViewer == null) {
            mModelViewer = new ModelViewer(context, homeButtonTouchListener);
        }
        mCache.save(this);

        setViewPortWidth(MAX_RADIUS);
        setViewPortHeight(MAX_RADIUS);
        setViewPortDepth(MAX_RADIUS);
        enableClipRegion();
        saveRenderData();
    }

    String getLocation() {
        return mLocation;
    }

    @Override
    protected void onAttached() {
        setChildrenFollowFocus(true);
        setChildrenFollowInput(true);
    }

    public void startModelViewer() {
        final ContentSceneController contentSceneController = WidgetLib.getContentSceneController();

        try {
            mModelViewer.setModel(new Model(getGVRContext(), getName(), mLocation));
            contentSceneController.goTo(mModelViewer);
        } catch (IOException e) {
            Log.w(TAG, "Model loading issue for %s", mLocation);
        }
    }

    TransformCache mCache = new TransformCache();

    private void saveRenderData() {
        mOriginalMaterial = new ArrayList<>();
        for (GVRRenderData r : getRenderDatas()) {
            mOriginalMaterial.add(r.getMaterial());
        }
    }

    private static GVRSceneObject loadModel(GVRContext context, String location) throws IOException {
        EnumSet<GVRImportSettings> additionalSettings = EnumSet.of(GVRImportSettings.CALCULATE_SMOOTH_NORMALS,  GVRImportSettings.NO_ANIMATION);
        EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettingsWith(additionalSettings);

        GVRMesh mesh = context.getAssetLoader().loadMesh(new GVRAndroidResource(context,
                "models/" + location), settings);
        GVRSceneObject model = new GVRSceneObject(context, mesh);

//        model = context.getAssetLoader().loadModel("models/" + location);
        return model;
    }

    public List<GVRAnimation> getAnimationsList() {
        return mAnimation;
    }

    float getCurrentZoom() {
        return mCurrentZoom;
    }

    public void onZoomOverModel(float zoomBy) {
        float zTransform = (int) ((zoomBy));
        if (zTransform > MAX_ZOOM) {
            zTransform = 0;
            setScaleX(mCache.getScaleX());
            setScaleY(mCache.getScaleY());
            setScaleZ(mCache.getScaleZ());
        } else {
            android.util.Log.d(TAG, "Zoom by" + Float.toString(zTransform) + "  " + Float.toString(zoomBy));
            float units = mCurrentZoom;
            float scaleFactor = units < zTransform ? zTransform - units : units - zTransform;
            float sf = units < zTransform ? 1.2f : 0.8f;

            for (int i = 0; i < scaleFactor; i++) {
                float x = getScaleX();
                float y = getScaleY();
                float z = getScaleZ();
                setScale(sf * x, sf * y, sf * z);
            }
        }
        mCurrentZoom = zTransform;
    }


    public void enableDisableLight(final boolean flag) {
        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (flag != isLightEnabled()) {
                    Log.d(TAG, "enableDisableLight flag is %b isLightEnabled() = %b", flag, isLightEnabled());
                    mLightEnabled = flag;
                    for (GVRRenderData r : getRenderDatas()) {
                        if (flag) {
                            r.enableLight();
                        } else {
                            r.disableLight();
                        }
                    }
                }
            }
        });
    }

    public boolean isLightEnabled() {
        return mLightEnabled;
    }

    private List<GVRRenderData> getRenderDatas() {
        return getSceneObject().getAllComponents(GVRRenderData.getComponentType());
    }

    public void applyCustomShader(ModelViewer.SHADER index) {
        List<GVRRenderData> renderDatas = getRenderDatas();
        switch (index) {
            case Original:
                for (int i = 0; i < renderDatas.size() && i < mOriginalMaterial.size(); i++) {
                    renderDatas.get(i).setMaterial(mOriginalMaterial.get(i));
                    renderDatas.get(i).setCullFace(GVRRenderPass.GVRCullFaceEnum.Back);
                    renderDatas.get(i).setDrawMode(4);
                }
                enableDisableLight(true);
                break;
            case NoTexture:
                for (GVRRenderData rdata : renderDatas) {
                    GVRMaterial noMaterial = new GVRMaterial(getGVRContext(),
                            new GVRShaderId(NoTextureShader.class));
                    rdata.setMaterial(noMaterial);
                }
                enableDisableLight(false);
                break;

            case Lines:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setDrawMode(1);
                }

                break;
            case LinesLoop:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setDrawMode(3);
                }

                break;
            case Points:
                for (GVRRenderData rdata : getRenderDatas()) {
                    rdata.setDrawMode(0);
                }

                break;
        }
    }
}
