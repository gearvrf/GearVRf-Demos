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

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;

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

import java.sql.Struct;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_TRIANGLES;

public class WidgetActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new WidgetMain());
    }

    private static class WidgetPickHandler implements IPickEvents, ITouchEvents
    {
        static class Selection
        {
            GVRPicker.GVRPickedObject Hit;
            Widget  FocusWidget;

            public Selection(GVRPicker.GVRPickedObject hit, Widget widget)
            {
                Hit = hit;
                FocusWidget = widget;
            }
        }

        private final List<Selection> mSelected = new ArrayList<Selection>();
        private final List<Widget> mTouched = new ArrayList<Widget>();

        public WidgetPickHandler()
        {
        }

        private Selection findSelected(GVRSceneObject hitObject)
        {
            for (Selection sel : mSelected)
            {
                if (sel.Hit.hitObject == hitObject)
                {
                    return sel;
                }
            }
            return null;
        }

        private Selection removeSelected(GVRSceneObject hitObject)
        {
            Iterator<Selection> iter = mSelected.iterator();
            while (iter.hasNext())
            {
                Selection sel = iter.next();
                if (sel.Hit.hitObject == hitObject)
                {
                    iter.remove();
                    return sel;
                }
            }
            return null;
        }

        public void onPick(GVRPicker picker)
        {
            if (!picker.HasPickListChanged())
            {
                return;
            }
            GVRPicker.GVRPickedObject[] picked = picker.getPicked();

            for (GVRPicker.GVRPickedObject hit : picked)
            {
                GVRSceneObject hitObj = hit.hitObject;

                Selection sel = findSelected(hitObj);
                if (sel == null)
                {
                    continue;
                }
                Widget widget = sel.FocusWidget;

                if (widget.isFocused() || widget.doOnFocus(true))
                {
                    Log.e(TAG, "%s widget focused", widget.getName());
                    break;
                }
            }
         }

        public void onNoPick(GVRPicker picker)
        {
            if (picker.HasPickListChanged())
            {
                Log.e(TAG, "%s", "selection cleared");
                mSelected.clear();
            }
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            Widget widget = (Widget) sceneObj.getComponent(Widget.getComponentType());

            while (widget != null)
            {
                if (widget.isFocusEnabled())
                {
                    Selection sel = findSelected(sceneObj);
                    if (sel == null)
                    {
                        Log.e(TAG, "%s select widget %s", sceneObj.getName(), widget.getName());
                        mSelected.add(new Selection(collision, widget));
                    }
                    return;
                }
                widget = widget.getParent();
            }
        }

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            Widget widget = (Widget) sceneObj.getComponent(Widget.getComponentType());

            while (widget != null)
            {
                if (widget.isFocused() && widget.isTouchable() && !mTouched.contains(widget))
                {
                    Log.e(TAG, "%s start touch widget %s", sceneObj.getName(), widget.getName());
                    mTouched.add(widget);
                    return;
                }
                widget = widget.getParent();
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            Widget widget = (Widget) sceneObj.getComponent(Widget.getComponentType());

            while (widget != null)
            {
                if (widget.isFocused() &&
                    widget.isTouchable() &&
                    mTouched.contains(widget) &&
                    widget.doOnTouch(collision.hitLocation))
                {
                    Log.e(TAG, "%s end touch widget %s", sceneObj.getName(), widget.getName());
                    mTouched.remove(widget);
                    return;
                }
                widget = widget.getParent();
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision)
        {
            Selection sel = removeSelected(sceneObj);
            if (sel != null)
            {
                sel.FocusWidget.doOnFocus(false);
                Log.e(TAG, "%s deselect", sceneObj.getName());
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onMotionOutside(GVRPicker picker, MotionEvent event) {}
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
                    GVRPicker picker = newController.getPicker();
                    picker.setPickClosest(false);
                    newController.addPickEventListener(mPickHandler);
                }
            });
            makeObjects(scene);
        }

        private void makeObjects(GVRScene scene)
        {
            GVRContext context = scene.getGVRContext();
            GVRMaterial backmtl = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);

            backmtl.setDiffuseColor(0.7f, 0.6f, 0.3f, 1);

            GroupWidget group = new GroupWidget(context, 4.5f, 2.5f);
            GVRSceneObject background = group.getOwnerObject();
            background.getRenderData().setMaterial(backmtl);
            background.getTransform().setPositionZ(-3.0f);

            RadioButton redbutton = new RadioButton(context, 2.0f, 2.0f);
            GVRSceneObject redthing = redbutton.getOwnerObject();
            redthing.getTransform().setPosition(1, 0, 0.01f);

            RadioButton bluebutton = new RadioButton(context, 2.0f, 2.0f);
            GVRSceneObject bluething = bluebutton.getOwnerObject();
            bluething.getTransform().setPosition(-1, 0, 0.01f);
            group.addChild(redbutton);
            group.addChild(bluebutton);
            redbutton.setName("RedButton");
            bluebutton.setName("BlueButton");
            background.setName("Background");
            redbutton.setGraphicColor(1, 0, 0);
            redbutton.setFocusEnabled(true);
            bluebutton.setGraphicColor(0, 0, 1);
            bluebutton.setFocusEnabled(true);
            scene.addSceneObject(background);
        }
    }
}
