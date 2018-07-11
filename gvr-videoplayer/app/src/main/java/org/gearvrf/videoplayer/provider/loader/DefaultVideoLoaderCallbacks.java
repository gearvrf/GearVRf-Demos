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

package org.gearvrf.videoplayer.provider.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import org.gearvrf.videoplayer.model.Video;

import java.util.List;

public class DefaultVideoLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Video>> {

    private Context mContext;
    private String mAlbumTitleFilter;

    public DefaultVideoLoaderCallbacks(Context mContext, String albumTitleFilter) {
        this.mContext = mContext;
        this.mAlbumTitleFilter = albumTitleFilter;
    }

    @Override
    public Loader<List<Video>> onCreateLoader(int id, Bundle args) {
        return new VideoDataLoader(mContext, mAlbumTitleFilter);
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> data) {
    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) {
    }

}
