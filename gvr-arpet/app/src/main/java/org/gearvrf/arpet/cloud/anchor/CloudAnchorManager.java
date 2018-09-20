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
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.mixedreality.ICloudAnchorListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class CloudAnchorManager {
    private static final String TAG = CloudAnchorManager.class.getSimpleName();

    @IntDef({ResponseType.FAILURE, ResponseType.SUCCESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResponseType {
        int FAILURE = 0;
        int SUCCESS = 1;
    }

    private ArrayList<CloudAnchor> mCloudAnchors;
    private int mCountSuccess;
    private int mCountFailure;
    private boolean mIsReady;
    private final PetContext mPetContext;
    private OnCloudAnchorManagerListener mListener;

    public CloudAnchorManager(PetContext petContext, OnCloudAnchorManagerListener listener) {
        mPetContext = petContext;
        mCloudAnchors = new ArrayList<>();
        mCountSuccess = 0;
        mCountFailure = 0;
        mIsReady = false;
        mListener = listener;
    }

    public void hostAnchor(AnchoredObject object) {
        if (!isCloudAnchorApiKeySet()) {
            Log.e(TAG, "Cloud Anchor API key is not set!");
            return;
        }

        CloudAnchor cloudAnchor = new CloudAnchor(object.getObjectType());
        mCloudAnchors.add(cloudAnchor);
        Log.d(TAG, "hosting anchor...");
        mPetContext.getMixedReality().hostAnchor(
                object.getAnchor(),
                (anchor) -> {
                    String id = anchor.getCloudAnchorId();
                    if (!id.isEmpty()) {
                        cloudAnchor.setCloudAnchorId(id);
                        onHostResult(ResponseType.SUCCESS);
                    } else {
                        Log.d(TAG, "cloud anchor ID is empty");
                        onHostResult(ResponseType.FAILURE);
                    }
                });
    }

    public void resolveAnchor(String anchorId, ICloudAnchorListener listener) {
        if (!isCloudAnchorApiKeySet()) {
            Log.e(TAG, "Cloud Anchor API key is not set!");
            return;
        }
        Log.d(TAG, "resolving anchor ID...");
        mPetContext.getMixedReality().resolveCloudAnchor(anchorId, listener);
    }

    public void clearAnchors() {
        mCloudAnchors.clear();
        mIsReady = false;
        mCountSuccess = 0;
        mCountFailure = 0;
    }

    public ArrayList<CloudAnchor> getCloudAnchors() {
        return mCloudAnchors;
    }

    public boolean isReady() {
        return mIsReady;
    }

    private boolean isCloudAnchorApiKeySet() {
        return ContextUtils.isMetaDataSet(mPetContext.getGVRContext().getContext(),
                ApiConstants.GOOGLE_CLOUD_ANCHOR_KEY_NAME);
    }

    private void onHostResult(int type) {
        if (type == ResponseType.SUCCESS) {
            Log.d(TAG, "anchor was hosted successfully!");
            mCountSuccess++;
        } else if (type == ResponseType.FAILURE) {
            Log.d(TAG, "anchor was not hosted");
            mCountFailure++;
        }

        if ((mCountSuccess + mCountFailure) == mCloudAnchors.size()) {
            if (mCountFailure == 0) {
                mIsReady = true;
                Log.d(TAG, "the manager is ready");
                mListener.onHostReady();
            } else {
                // TODO: handle this exception in UI
                Log.d(TAG, "the manager is ready but some anchor was not hosted correctly");
            }
            mCountSuccess = 0;
            mCountFailure = 0;
        }
    }
}
