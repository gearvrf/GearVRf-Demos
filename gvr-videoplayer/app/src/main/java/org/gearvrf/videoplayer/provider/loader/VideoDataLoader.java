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

import android.content.Context;

import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.provider.asyntask.LocalVideoAsyncTask;

import java.util.List;

public class VideoDataLoader extends DataLoader<List<Video>> {

    private String mAlbumTitleFilter;

    public VideoDataLoader(Context context, String albumTitleFilter) {
        super(context);
        this.mAlbumTitleFilter = albumTitleFilter;
    }

    @Override
    public List<Video> loadInBackground() {
        return new LocalVideoAsyncTask(mAlbumTitleFilter).loadVideos();
    }
}
