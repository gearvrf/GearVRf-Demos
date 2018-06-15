package org.gearvrf.videoplayer.component.gallery;

import android.support.annotation.IntDef;

@IntDef({SourceType.LOCAL, SourceType.EXTERNAL})
public @interface SourceType {
    int LOCAL = 0;
    int EXTERNAL = 1;
}
