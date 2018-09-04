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

import android.util.Log;

import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.ICloudAnchorListener;

public class CloudAnchor {
    private final String TAG = CloudAnchor.class.getSimpleName();

    private AnchoredObject mAnchoredObject;

    private String mCloudAnchorId;
    private CloudAnchorListener mCloudListener;
    private OnCloudAnchorResponseListener mResponseListener;

    public CloudAnchor(AnchoredObject anchoredObject) {
        mAnchoredObject = anchoredObject;
        mCloudListener = new CloudAnchorListener();
    }

    public void setResponseListener(OnCloudAnchorResponseListener listener) {
        mResponseListener = listener;
    }

    public CloudAnchorListener getCloudListener() {
        return mCloudListener;
    }

    public String getCloudAnchorId() {
        return mCloudAnchorId;
    }

    private class CloudAnchorListener implements ICloudAnchorListener {
        @Override
        public void onTaskComplete(GVRAnchor gvrAnchor) {
            String id = gvrAnchor.getCloudAnchorId();
            if (!id.isEmpty()) {
                mCloudAnchorId = id;
                mResponseListener.onResult(CloudAnchorManager.ResponseType.SUCCESS);
            } else {
                Log.d(TAG, "cloud anchor ID is empty");
                mResponseListener.onResult(CloudAnchorManager.ResponseType.FAILURE);
            }
        }
    }
}
