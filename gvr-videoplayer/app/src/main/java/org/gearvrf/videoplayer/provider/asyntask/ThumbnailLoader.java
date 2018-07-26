/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.videoplayer.provider.asyntask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ThumbnailLoader extends AsyncTask<Long, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;

    public ThumbnailLoader(ImageView imageView) {
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
