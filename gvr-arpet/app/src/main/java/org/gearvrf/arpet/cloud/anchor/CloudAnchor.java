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
 */

package org.gearvrf.arpet.cloud.anchor;

import org.gearvrf.arpet.AnchoredObject;

import java.io.Serializable;

public class CloudAnchor implements Serializable {
    private final String TAG = CloudAnchor.class.getSimpleName();

    private String mCloudAnchorId;
    @AnchoredObject.ObjectType
    private int mObjectType;

    public CloudAnchor(@AnchoredObject.ObjectType int type) {
        mObjectType = type;
    }

    public String getCloudAnchorId() {
        return mCloudAnchorId;
    }

    public int getObjectType() {
        return mObjectType;
    }

    public void setCloudAnchorId(String id) {
        mCloudAnchorId = id;
    }
}
