package org.gearvrf.widgetlibviewer;

import android.graphics.Bitmap;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.adapter.BaseAdapter;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.GVRBitmapTexture;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

import java.util.ArrayList;
import java.util.List;

import static org.gearvrf.utility.Threads.spawn;

public class BitmapAdapter extends BaseAdapter {
    protected float widthQuad = 4;
    protected float heightQuad = widthQuad / 2.f;

    public BitmapAdapter(GVRContext gvrContext,
                         final List<? extends BitmapGetter> items) {
        Log.d(TAG, "CTOR(): items: %d", items.size());
        mBitmaps = new ArrayList<Bitmap>();
        mGvrContext = gvrContext;
        final Runnable onBackgroundThread = new Runnable() {
            @Override
            public void run() {
                getBitmaps(items);
                notifyDataSetChanged();
            }
        };
        spawn(onBackgroundThread);
    }

    @Override
    public int getCount() {
        final int size = mBitmaps.size();
        Log.d(TAG, "getCount(): %d", size);
        return size;
    }

    @Override
    public GVRBitmapTexture getItem(int position) {
        GVRBitmapTexture texture = mTextures.get(position);
        if (texture == null) {
            final Bitmap bitmap = mBitmaps.get(position);
            texture = new GVRBitmapTexture(mGvrContext, bitmap);
            mTextures.set(position, texture);

            mBitmaps.set(position, null);
        }
        return texture;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void getBitmaps(
            final List<? extends BitmapGetter> backgroundsInfo) {
        for (BitmapGetter item : backgroundsInfo) {
            final Bitmap bitmap = item.get();
            if (null == bitmap) {
                Log.e(TAG,
                        "no bitmap object retrieved from the bitmap getter; item"
                                + item);
            }
            mBitmaps.add(bitmap);
            mTextures.add(null); // Prep list for on-demand texture
            // generation
        }
    }

    private final List<Bitmap> mBitmaps;
    private final List<GVRBitmapTexture> mTextures = new ArrayList<GVRBitmapTexture>();
    private final GVRContext mGvrContext;
    private final String TAG = BitmapAdapter.class.getSimpleName();

    @Override
    public Widget getView(int position,
                          Widget convertView,
                          GroupWidget parent) {

        Log.d(TAG, "getView(): %05.2f, %05.2f at %d", widthQuad,
                heightQuad, position);
        final GVRBitmapTexture texture = getItem(position);
        if (convertView != null) {
            convertView.setTexture(texture);
            return convertView;
        }
        Widget quadWidget = new Widget(mGvrContext, widthQuad, heightQuad);
        quadWidget.setTexture(texture);
        return quadWidget;
    }

    public interface BitmapGetter {
        Bitmap get();
    }
}
