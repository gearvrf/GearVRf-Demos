package org.gearvrf.videoplayer.model;

import android.content.Context;
import android.content.res.Resources;

import org.gearvrf.videoplayer.R;

public class LocalHomeItem extends HomeItem {
    public LocalHomeItem(Context context) {
        super(context.getString(R.string.gallery_source_type_local), R.drawable.ic_folder_local_videos);
    }
}
