package org.gearvrf.videoplayer.model;

import android.support.annotation.IntDef;

public abstract class Media {

    @IntDef({Type.TYPE_ALBUM, Type.TYPE_VIDEO})
    public @interface Type {
        int TYPE_ALBUM = 0;
        int TYPE_VIDEO = 1;
    }

    @Type
    int type;

    Media(@Type int type) {
        this.type = type;
    }

    @Type
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Media{" +
                "type=" + type +
                '}';
    }
}
