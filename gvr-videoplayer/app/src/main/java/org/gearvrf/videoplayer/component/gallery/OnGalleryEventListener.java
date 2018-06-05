package org.gearvrf.videoplayer.component.gallery;

import org.gearvrf.videoplayer.model.Video;

import java.util.List;

public interface OnGalleryEventListener {

    void onVideosSelected(List<Video> videoList);
}
