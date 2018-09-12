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

import android.support.annotation.IntDef;
import android.util.Log;

import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.ICloudAnchorListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class CloudAnchorManager {
    private static final String TAG = CloudAnchorManager.class.getName();

    @IntDef({ResponseType.FAILURE, ResponseType.SUCCESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResponseType {
        int FAILURE = 0;
        int SUCCESS = 1;
    }

    private List<CloudAnchor> mCloudAnchors;
    private int mCountSuccess;
    private boolean mIsReady;

    public CloudAnchorManager() {
        mCloudAnchors = new ArrayList<>();
        mCountSuccess = 0;
        mIsReady = false;
    }

    public void hostAnchor(PetContext petContext, AnchoredObject object) {
        CloudAnchor cloudAnchor = new CloudAnchor(object);
        cloudAnchor.setResponseListener(new CloudAnchorResponseListener());
        Log.d(TAG, "hosting anchor...");
        petContext.getMixedReality().hostAnchor(object.getAnchor(), cloudAnchor.getCloudListener());
        mCloudAnchors.add(cloudAnchor);
    }

    public void resolveAnchor(GVRMixedReality mixedReality, CloudAnchor cloudAnchor, ICloudAnchorListener listener) {
        mixedReality.resolveCloudAnchor(cloudAnchor.getCloudAnchorId(), listener);
        mCloudAnchors.remove(cloudAnchor);
    }

    public void clearAnchors() {
        mCloudAnchors.clear();
        mIsReady = false;
        mCountSuccess = 0;
    }

    public List<CloudAnchor> getCloudAnchors() {
        return mCloudAnchors;
    }

    public boolean isReady() {
        return mIsReady;
    }

    private class CloudAnchorResponseListener implements OnCloudAnchorResponseListener {
        @Override
        public void onResult(int type) {
            if (type == ResponseType.SUCCESS) {
                Log.d(TAG, "anchor was hosted successfully!");
                mCountSuccess++;
            }

            if (mCountSuccess == mCloudAnchors.size()) {
                // TODO: manager is ready to notify the clients
                mIsReady = true;
                Log.d(TAG, "the manager is ready");
            }
        }
    }
}
