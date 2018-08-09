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

package org.gearvrf.videoplayer.model;

import android.support.annotation.IntDef;

public abstract class GalleryItem {

    @IntDef({Type.TYPE_HOME, Type.TYPE_ALBUM, Type.TYPE_VIDEO})
    public @interface Type {
        int TYPE_HOME = 0;
        int TYPE_ALBUM = 1;
        int TYPE_VIDEO = 2;
    }

    @Type
    int type;

    GalleryItem(@Type int type) {
        this.type = type;
    }

    @Type
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "GalleryItem{" +
                "type=" + type +
                '}';
    }
}
