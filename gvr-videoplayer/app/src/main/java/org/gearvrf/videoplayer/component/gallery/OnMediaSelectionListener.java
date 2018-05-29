package org.gearvrf.videoplayer.component.gallery;

import org.gearvrf.videoplayer.model.Media;

import java.util.List;

public interface OnMediaSelectionListener {

    void onMediaSelected(List<? extends Media> mediaList);
}
