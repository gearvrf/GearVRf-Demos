package org.gearvrf.videoplayer.model;

import android.support.annotation.IntDef;

public abstract class GalleryItem {

    @IntDef({Type.TYPE_HOME, Type.TYPE_ALBUM, Type.TYPE_VIDEO})
    public @interface Type {
        int TYPE_HOME = 0;
        int TYPE_ALBUM = 1;
        int TYPE_VIDEO = 2;
    }

    @Type
    int type;

    GalleryItem(@Type int type) {
        this.type = type;
    }

    @Type
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "GalleryItem{" +
                "type=" + type +
                '}';
    }
}
