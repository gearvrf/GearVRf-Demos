package org.gearvrf.widgetlibviewer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.widgetlib.adapter.BaseAdapter;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelAdapter extends BaseAdapter {
    protected float mSizeQuad = 4;

    public ModelAdapter(GVRContext gvrContext,
                         final List<String> modelsList) {
        Log.d(TAG, "CTOR(): items: %d", modelsList.size());
        mModelsPath = modelsList;
        mGvrContext = gvrContext;
    }

    @Override
    public int getCount() {
        final int size = mModelsPath.size();
        Log.d(TAG, "getCount(): %d", size);
        return size;
    }

    @Override
    public Model getItem(int position) {
        Model model = null;
        try {
            model = new Model(mGvrContext,
                    mModelsPath.get(position).replaceAll("\\..*", ""),
                    mModelsPath.get(position));
        } catch (IOException e) {
            Log.w(TAG, "No models loaded!");
        }
        return model;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private final List<String> mModelsPath;
    private final GVRContext mGvrContext;
    private final String TAG = ModelAdapter.class.getSimpleName();

    @Override
    public boolean hasUniformViewSize() {
        return true;
    }

    @Override
    public float getUniformWidth() {
        return mSizeQuad;
    }

    @Override
    public float getUniformHeight() {
        return mSizeQuad;
    }

    @Override
    public float getUniformDepth() {
        return mSizeQuad;
    }

    @Override
    public Widget getView(int position,
                          Widget convertView,
                          GroupWidget parent) {
        Widget modelBox = getItem(position);
        return modelBox;
    }
}
