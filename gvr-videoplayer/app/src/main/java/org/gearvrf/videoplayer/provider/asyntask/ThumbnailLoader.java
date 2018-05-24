package org.gearvrf.videoplayer.provider.asyntask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

class ThumbnailLoader extends AsyncTask<Long, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;

    ThumbnailLoader(ImageView imageView) {
        this.imageViewReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(Long... params) {
        return MediaStore.Video.Thumbnails.getThumbnail(
                imageViewReference.get().getContext().getContentResolver(),
                params[0],
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
        );
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
