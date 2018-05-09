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

package org.gearvrf.widgets;

import android.os.Bundle;

import com.samsung.smcl.vr.widgetlib.main.WidgetLib;
import com.samsung.smcl.vr.widgetlib.widget.GroupWidget;
import com.samsung.smcl.vr.widgetlib.widget.TouchManager;
import com.samsung.smcl.vr.widgetlib.widget.Widget;
import com.samsung.smcl.vr.widgetlib.widget.basic.RadioButton;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_TRIANGLES;

public class WidgetActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new WidgetMain());
    }

    private Widget.OnFocusListener mFocusHandler = new  Widget.OnFocusListener() {
        public boolean onFocus(Widget widget, boolean focused)
        {
            GVRSceneObject sceneObj = widget.getOwnerObject();
            GVRRenderData rdata = sceneObj.getRenderData();
            if (focused)
            {
                rdata.setDrawMode(GL_LINES);
            }
            else
            {
                rdata.setDrawMode(GL_TRIANGLES);
            }
            return true;
        }

        public boolean onLongFocus(Widget widget)
        {
           return false;
        }
    };

    private static class WidgetPickHandler implements IPickEvents
    {
        private final List<GVRSceneObject> mSelected = new ArrayList<GVRSceneObject>();

        public WidgetPickHandler()
        {
        }

        public void onPick(GVRPicker picker)
        {
            GVRPicker.GVRPickedObject[] picked = picker.getPicked();

            for (GVRPicker.GVRPickedObject hit : picked)
            {
                GVRSceneObject hitObj = hit.hitObject;
                Widget widget = (Widget) hitObj.getComponent(Widget.getComponentType());
                if (widget != null)
                {
                    if (mSelected.contains(widget))
                    {
                        if (widget.doOnFocus(true))
                        {
                            break;
                        }
                        if (widget.doOnTouch(hit.hitLocation))
                        {
                            break;
                        }
                    }
                }
                mSelected.clear();
            }
        }

        public void onNoPick(GVRPicker picker)
        {
            mSelected.clear();
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            Widget widget = (Widget) sceneObj.getComponent(Widget.getComponentType());

            if ((widget != null) && widget.isFocusEnabled())
            {
                mSelected.add(sceneObj);
            }
        }

        public void onExit(GVRSceneObject sceneObj)
        {
            Widget widget = (Widget) sceneObj.getComponent(Widget.getComponentType());

            if ((widget != null) && widget.isFocusEnabled())
            {
                widget.doOnFocus(false);
                mSelected.remove(sceneObj);
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {

        }

    }

    private static class WidgetMain extends GVRMain
    {
        private WidgetPickHandler mPickHandler = new WidgetPickHandler();

        @Override
        public void onInit(GVRContext gvrContext)
        {
            GVRScene scene = gvrContext.getMainScene();
            scene.setBackgroundColor(1, 1, 1, 1);

            try
            {
                WidgetLib.init(gvrContext, null);
            }
            catch (Exception ex)
            {
                Log.e(TAG, ex.getMessage());
            }
            gvrContext.getInputManager().selectController(new GVRInputManager.ICursorControllerSelectListener()
            {
                public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
                {
                    if (oldController != null)
                    {
                        oldController.removePickEventListener(mPickHandler);
                    }
                    newController.addPickEventListener(mPickHandler);
                }
            });
            makeObjects(scene);
        }

        private void makeObjects(GVRScene scene)
        {
            GVRContext context = scene.getGVRContext();
            GVRMaterial backmtl = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
            GVRMaterial redmtl = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
            GVRMaterial bluemtl = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
            GVRSceneObject background = new GVRSceneObject(context, 4.5f, 2.5f, "float3 a_position", backmtl);
            GVRSceneObject redthing = new GVRSceneObject(context, 2.0f, 2.0f, "float3 a_position", redmtl);
            GVRSceneObject bluething = new GVRSceneObject(context, 2.0f, 2.0f, "float3 a_position", bluemtl);

            backmtl.setDiffuseColor(0.7f, 0.6f, 0.3f, 1);
            redmtl.setDiffuseColor(1, 0.2f, 0, 1);
            bluemtl.setDiffuseColor(0.2f, 0, 1, 1.0f);
            background.getTransform().setPosition(0.0f, 0.0f, -3);
            redthing.getTransform().setPosition(1, 0, 0.01f);
            bluething.getTransform().setPosition(-1, 0, 0.01f);
            background.addChildObject(redthing);
            background.addChildObject(bluething);
            scene.addSceneObject(background);

            GroupWidget group = new GroupWidget(context, background);
            RadioButton redbutton = new RadioButton(context, redthing);
            RadioButton bluebutton = new RadioButton(context, bluething);
            group.addChild(redbutton);
            group.addChild(bluebutton);
        }
    }
}
