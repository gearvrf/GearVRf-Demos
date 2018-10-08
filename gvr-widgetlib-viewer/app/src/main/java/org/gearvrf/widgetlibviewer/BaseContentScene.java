package org.gearvrf.widgetlibviewer;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.content_scene.ScrollableContentScene;
import org.gearvrf.widgetlib.main.MainScene;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.custom.ControlBar;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;
import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import static org.gearvrf.utility.Log.tag;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optJSONObject;

public abstract class BaseContentScene extends ScrollableContentScene {
    public BaseContentScene(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float padding = optFloat(properties, Properties.padding, CONTROL_BAR_PADDING);

        mMainWidget = new GroupWidget(gvrContext, 0, 0);
        mMainWidget.setName("MainWidget < " + TAG + " >");

        LinearLayout mainLayout = new LinearLayout();
        mainLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        mainLayout.setDividerPadding(padding, Layout.Axis.Y);
        mMainWidget.applyLayout(mainLayout);
        mMainScene = WidgetLib.getMainScene();

        JSONObject controlBarProperties = optJSONObject(properties, Properties.control_bar);
        mControlBar = controlBarProperties != null ?
                new ControlBar(gvrContext, controlBarProperties):
                new ControlBar(gvrContext);
    }

    abstract protected Widget createContent();

    protected void setContentWidget(Widget content) {
        if (mContent != null) {
            mMainWidget.removeChild(mContent);
        }

        mContent = content;
        if (mContent != null) {
            mMainWidget.addChild(mContent, 0);
        }
    }

    protected enum Properties {padding, control_bar}

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        if (mFirstShow) {
            setContentWidget(createContent());
            mFirstShow = false;
        }
        mMainWidget.addChild(mControlBar);
        mMainScene.addSceneObject(mMainWidget);
    }

    @Override
    public void hide() {
        mMainWidget.removeChild(mControlBar);
        mMainScene.removeSceneObject(mMainWidget);
    }

    @Override
    public void onSystemDialogRemoved() {

    }

    @Override
    public void onSystemDialogPosted() {

    }

    private static float CONTROL_BAR_PADDING = 1.5f;

    protected final GVRContext mGvrContext;
    private Widget mContent;
    private GroupWidget mMainWidget;
    protected ControlBar mControlBar;
    protected MainScene mMainScene;
    protected boolean mFirstShow = true;

    private static final String TAG = tag(BaseContentScene.class);
}

