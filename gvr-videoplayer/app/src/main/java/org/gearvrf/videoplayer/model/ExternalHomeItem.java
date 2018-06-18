package org.gearvrf.videoplayer.model;

import android.content.Context;

import org.gearvrf.videoplayer.R;

public class ExternalHomeItem extends HomeItem {
    public ExternalHomeItem(Context context) {
        super(context.getString(R.string.gallery_source_type_external), R.drawable.ic_folder_external_videos);
    }
}
