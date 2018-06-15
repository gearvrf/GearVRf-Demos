package org.gearvrf.videoplayer.component.gallery;

import org.gearvrf.videoplayer.model.GalleryItem;

import java.util.List;

public interface OnItemsSelectionListener {

    void onItemSelected(List<? extends GalleryItem> itemList);
}
