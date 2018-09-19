package org.gearvrf.widgetlibviewer;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.gearvrf.utility.Log.tag;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public class ModelViewer extends BaseContentScene {
    ModelViewer(GVRContext gvrContext, Widget.OnTouchListener homeListener) {
        super(gvrContext);

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float zoom_step = optFloat(properties, Properties.zoom_step, ZOOM_STEP);
        mPadding = optFloat(properties, Properties.padding, PADDING);

        Widget.OnTouchListener zoomListener = new Widget.OnTouchListener() {
            @Override
            public boolean onTouch(Widget widget, float[] floats) {
                if (mModel != null) {
                    mModel.onZoomOverModel(mModel.getCurrentZoom() + zoom_step);
                }
                return true;
            }
        };

        mControlBar.addControlListener("Home", homeListener);
        mControlBar.addControlListener("Zoom", zoomListener);

        JSONObject listProperties = WidgetLib.getPropertyManager().getInstanceProperties(CheckList.class, TAG);
        final JSONObject labelProperties = optJSONObject(listProperties, CheckList.Properties.label);

        List<CheckList.Item> shaders = new ArrayList<>();
        shaders.add(new CheckList.Item(gvrContext, "Original", labelProperties, new ShaderAction(0)));
        shaders.add(new CheckList.Item(gvrContext, "No Texture", labelProperties, new ShaderAction(1)));
        shaders.add(new CheckList.Item(gvrContext, "Outline", labelProperties, new ShaderAction(2)));
        shaders.add(new CheckList.Item(gvrContext, "Lines", labelProperties, new ShaderAction(3)));
        shaders.add(new CheckList.Item(gvrContext, "Lines_Loop", labelProperties, new ShaderAction(4)));
        shaders.add(new CheckList.Item(gvrContext, "Points", labelProperties, new ShaderAction(5)));

        mShaderList = new CheckList(gvrContext, "ShaderList", shaders);
    }

    private final static int DEFAULT_SHADER_ID = 0;
    private class ShaderAction implements CheckList.Action {
        private final int mShaderId;
        ShaderAction(int shaderId) {
            mShaderId = shaderId;
        }

        @Override
        public void enable() {
            if (mModel != null) {
                mModel.applyCustomShader(mShaderId);
            }
        }

        @Override
        public void disable() {
            if (mModel != null) {
                mModel.applyCustomShader(DEFAULT_SHADER_ID);
            }
        }
    };

    private enum Properties {zoom_step, padding}

    void setModel(Model model) {
        mModel = model;
        mFirstShow = true;
    }

    class ModelBox extends GroupWidget {
        ModelBox(GVRContext gvrContext, Model model) {
            super(gvrContext);
            OrientedLayout layout = new org.gearvrf.widgetlib.widget.layout.basic.LinearLayout();
            getDefaultLayout().setDividerPadding(mPadding, Layout.Axis.X);
            applyLayout(layout);

            addChild(mShaderList);
            addChild(model);
        }
    }

    @Override
    protected Widget createContent() {
        return new ModelBox(mGvrContext, mModel);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        if (mModel != null) {
            super.show();
            mModel.enableDisableLight(true);
            mShaderList.enable();
        }
    }

    @Override
    public void hide() {
        if (mModel != null) {
            mModel.enableDisableLight(false);
            mShaderList.disable();
            super.hide();
        }
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
        }
    }

    private Model mModel;
    private static final float ZOOM_STEP = 1;
    private static final float PADDING = 3;
    private final float mPadding;
    private CheckList mShaderList;

    private static final String TAG = tag(ModelViewer.class);
}
