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

package org.gearvrf.arpet.manager.cloud.anchor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.arpet.BuildConfig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.manager.cloud.anchor.exception.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.exception.NetworkException;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.arpet.util.NetworkUtils;
import org.gearvrf.mixedreality.GVRAnchor;

public class CloudAnchorManager {

    private static final String TAG = CloudAnchorManager.class.getSimpleName();

    private final PetContext mPetContext;
    private OnCloudAnchorCallback mCallback;

    public CloudAnchorManager(PetContext petContext) {
        mPetContext = petContext;
    }

    private boolean isCloudAnchorApiKeySet() {
        boolean isKeySet = ContextUtils.isMetaDataSet(mPetContext.getGVRContext().getContext(),
                PetConstants.GOOGLE_CLOUD_ANCHOR_KEY_NAME);
        if (!isKeySet && BuildConfig.DEBUG) {
            Context context = mPetContext.getActivity().getApplicationContext();
            mPetContext.runOnPetThread(() ->
                    Toast.makeText(context, "Cloud anchor API is not set",
                            Toast.LENGTH_LONG).show());
        }
        return isKeySet;
    }

    public void hostAnchors(
            @NonNull ManagedAnchor<GVRAnchor> managedAnchor,
            @NonNull OnCloudAnchorCallback<GVRAnchor> callback) {

        mCallback = callback;

        if (!checkPreconditions()) {
            return;
        }

        try {
            mPetContext.getMixedReality().hostAnchor(
                    managedAnchor.getAnchor(),
                    (resultAnchor) -> onResultAnchor(managedAnchor, resultAnchor)
            );
        } catch (Throwable cause) {
            mCallback.onError(new CloudAnchorException(cause));
        }
    }

    public void resolveAnchors(
            @NonNull ManagedAnchor<CloudAnchor> managedAnchor,
            @NonNull OnCloudAnchorCallback<GVRAnchor> callback) {

        mCallback = callback;

        if (!checkPreconditions()) {
            return;
        }

        try {
            mPetContext.getMixedReality().resolveCloudAnchor(
                    managedAnchor.getAnchor().getCloudAnchorId(),
                    (resultAnchor) -> onResultAnchor(managedAnchor, resultAnchor)
            );
        } catch (Throwable cause) {
            mCallback.onError(new CloudAnchorException(cause));
        }
    }

    @SuppressWarnings("unchecked")
    private void onResultAnchor(ManagedAnchor managedAnchor, GVRAnchor resultAnchor) {
        if (resultAnchor != null && !resultAnchor.getCloudAnchorId().isEmpty()) {
            mCallback.onResult(new ManagedAnchor<>(managedAnchor.getObjectType(), resultAnchor));
        } else {
            mCallback.onError(new CloudAnchorException("Returned id is empty"));
        }
    }

    private boolean checkPreconditions() {

        if (!isCloudAnchorApiKeySet()) {
            String errorString = "Unable to resolver anchors. Cloud anchor API key is not set.";
            Log.e(TAG, errorString);
            mCallback.onError(new CloudAnchorException(errorString));
            return false;
        }

        if (!NetworkUtils.hasInternetConnection(mPetContext)) {
            String errorString = "Cannot resolve anchors. No internet connection";
            Log.e(TAG, errorString);
            mCallback.onError(new CloudAnchorException(errorString, new NetworkException("No internet connection")));
            return false;
        }

        return true;
    }

    public interface OnCloudAnchorCallback<Anchor> {

        void onResult(ManagedAnchor<Anchor> managedAnchors);

        void onError(CloudAnchorException e);
    }
}
