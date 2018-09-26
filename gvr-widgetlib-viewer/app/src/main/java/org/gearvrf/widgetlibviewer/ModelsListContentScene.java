package org.gearvrf.widgetlibviewer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.ListWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.custom.PickerWidget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.LayoutScroller;
import org.gearvrf.widgetlib.widget.layout.basic.ArchLayout;
import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.gearvrf.utility.Log.tag;
import static org.gearvrf.widgetlib.main.Utility.getId;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optFloat;

public class ModelsListContentScene extends BaseContentScene {
    ModelsListContentScene(GVRContext gvrContext, Widget.OnTouchListener settingsListener) {
        super(gvrContext);
        mControlBar.addControlListener("Settings", settingsListener);
    }

    @Override
    protected Widget createContent() {
        mHorizontalPicker = setupHorizontalPicker();
        return mHorizontalPicker;
    }
    private enum Properties {arch_radius}

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        Log.d(TAG, "model list content scene show!");
        super.show();
        mHorizontalPicker.show();
    }

    @Override
    public void hide() {
        super.hide();
        mHorizontalPicker.hide();
    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
            mHorizontalPicker.hide();
            mHorizontalPicker.show();
        }
    }

    class ModelThumbnail implements BitmapAdapter.BitmapGetter {
        private final int mThumbnailId;
        ModelThumbnail(int thumbnailId) {
            Log.d(TAG, "create model with thumbnailId = %d", thumbnailId);
            mThumbnailId = thumbnailId;
        }

        @Override
        public Bitmap get() {
            return BitmapFactory.decodeResource(mGvrContext.getContext()
                    .getResources(), mThumbnailId);
        }
    }

    public ArrayList<Model> loadModelsList(GVRContext context) {
        ArrayList<Model> models = new ArrayList<>();
        try {
            for (String model: context.getContext().getAssets().list("models")) {
                models.add(new Model(context, model.replaceAll("\\..*",""), model));
            }
        } catch (IOException e) {
            Log.w(TAG, "No models found!");
        }
        return models;
    }

    public ArrayList<String> getModelsList(Resources resources ) {
        ArrayList<String> models = new ArrayList<>();
        try {
            for (String model: resources.getAssets().list("models")) {
                models.add(model);
            }
        } catch (IOException e) {
            Log.w(TAG, "No models found!");
        }
        return models;
    }

    public final List<ModelThumbnail> getModelsThumbnailList() {
        Map<String, String> modelsMap = new LinkedHashMap<>();
        modelsMap.put("Skylight", "R.raw.skylight_sky_thumbnail");
        modelsMap.put("CubicDreams", "R.raw.cubic_dreams_sky_thumbnail");
        modelsMap.put("DesertSky", "R.raw.skybox_desert_thumbnail");
        modelsMap.put("HarborView", "R.raw.harbor_view_sky_thumbnail");

        final List<ModelThumbnail> models = new ArrayList<>();

        for (final Map.Entry<String, String> thumb : modelsMap.entrySet()) {
            int thumbResId = getId(mGvrContext.getActivity(), thumb.getValue(), "raw");
            models.add(new ModelThumbnail(thumbResId));
        }

        return models;
    }

    private PickerWidget setupHorizontalPicker() {
        Adapter modelAdapter = new ModelAdapter(mGvrContext, getModelsList(mGvrContext.getContext().getResources()));

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float panelPadding = optFloat(properties, BaseContentScene.Properties.padding, PANEL_PADDING);
        final float radius = optFloat(properties, Properties.arch_radius, ARCH_RADIUS);

        LinearLayout listLayout = new LinearLayout();
        listLayout.setDividerPadding(panelPadding, Layout.Axis.X);
        listLayout.enableClipping(true);

        ArchLayout archLayout = new ArchLayout(radius);

        PickerWidget horizontalPicker = new PickerWidget(mGvrContext, modelAdapter,0, 0);

        horizontalPicker.setViewPortWidth(Float.POSITIVE_INFINITY);

        horizontalPicker.enableFocusAnimation(true);
        horizontalPicker.enableTransitionAnimation(true);


        horizontalPicker.applyLayout(listLayout);
        horizontalPicker.applyLayout(archLayout);

        ListWidget.OnItemFocusListener focusListener = new ListWidget.OnItemFocusListener() {
            public void onFocus(ListWidget list, boolean focused, int dataIndex) {
                Model model = (Model)(list.getView(dataIndex));
                model.enableDisableLight(focused);
            }
            public void onLongFocus(ListWidget list, int dataIndex) {
                Model model = (Model)(list.getView(dataIndex));
                model.startModelViewer();
            }
        };

        horizontalPicker.addOnItemFocusListener(focusListener);

        ListWidget.OnItemTouchListener touchListener = new ListWidget.OnItemTouchListener() {
            @Override
            public boolean onTouch(ListWidget listWidget, int dataIndex) {
                Model model = (Model)(listWidget.getView(dataIndex));
                model.startModelViewer();
                return true;
            }
        };

        horizontalPicker.addOnItemTouchListener(touchListener);
        horizontalPicker.setViewPortHeight(2.5f);


        horizontalPicker.hide();
        return horizontalPicker;
    }

    private LayoutScroller getLayoutScroller() {
        if (mLayoutScroller == null) {
            mLayoutScroller = new LayoutScroller(mGvrContext.getContext(), mHorizontalPicker);
        }
        return mLayoutScroller;
    }

    protected void scrollLeft() {
        getLayoutScroller().scrollToPrevItem();
    }

    protected void scrollRight() {
        getLayoutScroller().scrollToNextItem();
    }

    private static final float PANEL_PADDING = 0.8f;
    private static final float ARCH_RADIUS = 7;

    private LayoutScroller mLayoutScroller;
    private PickerWidget mHorizontalPicker;

    private static final String TAG = tag(ModelsListContentScene.class);
}
