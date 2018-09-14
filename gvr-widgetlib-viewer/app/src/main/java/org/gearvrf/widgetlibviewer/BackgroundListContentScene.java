package org.gearvrf.widgetlibviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.MainScene;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.ListWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.custom.PickerWidget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.LayoutScroller;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;
import org.gearvrf.widgetlib.widget.layout.basic.ArchLayout;
import org.gearvrf.widgetlib.widget.layout.basic.GridLayout;
import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.gearvrf.utility.Log.tag;
import static org.gearvrf.widgetlib.main.Utility.getId;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optInt;

public class BackgroundListContentScene extends BaseContentScene {
    BackgroundListContentScene(GVRContext gvrContext,
                               ViewerMain.BackgroundWidget bgWidget, Widget.OnTouchListener homeListener) {
        super(gvrContext);
        mBgWidget = bgWidget;
        mControlBar.addControlListener("Home", homeListener);
    }

    @Override
    protected Widget createContent() {
        mHorizontalPicker = setupHorizontalPicker();
        return mHorizontalPicker;
    }

    private enum Properties {arch_radius, rows_num, columns_num}

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void show() {
        super.show();
        mHorizontalPicker.show();
    }

    @Override
    public void hide() {
        super.hide();
        mHorizontalPicker.hide();
    }

    @Override
    public void onSystemDialogRemoved() {

    }

    @Override
    public void onSystemDialogPosted() {

    }

    @Override
    public void onProximityChange(boolean onProximity) {
        if (onProximity) {
            mHorizontalPicker.hide();
            mHorizontalPicker.show();
        }
    }

    class Background implements BitmapAdapter.BitmapGetter {
        private final int mThumbnailId;
        Background(int thumbnailId) {
            Log.d(TAG, "create bitmap with thumbnailId = %d", thumbnailId);
            mThumbnailId = thumbnailId;
        }

        @Override
        public Bitmap get() {
            return BitmapFactory.decodeResource(mGvrContext.getContext()
                    .getResources(), mThumbnailId);
        }
    }

    public final List<? extends BitmapAdapter.BitmapGetter> getBackgroundList() {
        final List<Background> backgrounds = new ArrayList<>();

        for (final String thumb : mBgWidget.getThumbnailsList()) {
            int thumbResId = getId(mGvrContext.getActivity(), thumb, "raw");
            backgrounds.add(new Background(thumbResId));
        }

        return backgrounds;
    }

    private PickerWidget setupHorizontalPicker() {
        Adapter adapter = new BitmapAdapter(mGvrContext, getBackgroundList()) {
            @Override
            public float getUniformWidth() {
                return widthQuad;
            }

            public float getUniformHeight() {
                return heightQuad;
            }

            @Override
            public boolean hasUniformViewSize() {
                return true;
            }
        };

        JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        final float panelPadding = optFloat(properties, BaseContentScene.Properties.padding, PANEL_PADDING);
        final float radius = optFloat(properties, Properties.arch_radius, ARCH_RADIUS);
        final int rows = optInt(properties, Properties.rows_num, ROWS_NUM);
        final int columns = optInt(properties, Properties.columns_num, COLUMNS_NUM);

        GridLayout pickerLayout = new GridLayout(rows, columns);

        pickerLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        pickerLayout.setDividerPadding(panelPadding, Layout.Axis.X);
        pickerLayout.setDividerPadding(panelPadding, Layout.Axis.Y);
        pickerLayout.setVerticalGravity(LinearLayout.Gravity.TOP);
        pickerLayout.setHorizontalGravity(LinearLayout.Gravity.LEFT);
        pickerLayout.enableClipping(true);

        ArchLayout archLayout = new ArchLayout(radius);

        PickerWidget horizontalPicker = new PickerWidget(mGvrContext, adapter,0, 0);

        horizontalPicker.enableFocusAnimation(true);
        horizontalPicker.enableTransitionAnimation(true);

        horizontalPicker.setViewPortWidth(columns * (adapter.getUniformWidth() + panelPadding));
        horizontalPicker.setViewPortHeight(rows * (adapter.getUniformHeight() + panelPadding));

        horizontalPicker.applyLayout(pickerLayout);
        horizontalPicker.applyLayout(archLayout);

        ListWidget.OnItemTouchListener touchListener = new ListWidget.OnItemTouchListener() {
            public boolean onTouch(ListWidget list, int dataIndex) {
                mBgWidget.setLevel(dataIndex);
                return true;
            }
        };
        horizontalPicker.addOnItemTouchListener(touchListener);
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


    // default properties
    private static final float PANEL_PADDING = 0.8f;
    private static final float ARCH_RADIUS = 7;
    private static final int ROWS_NUM = 3;
    private static final int COLUMNS_NUM = 3;

    private LayoutScroller mLayoutScroller;
    private PickerWidget mHorizontalPicker;
    private ViewerMain.BackgroundWidget mBgWidget;

    private static final String TAG = tag(BackgroundListContentScene.class);
}
