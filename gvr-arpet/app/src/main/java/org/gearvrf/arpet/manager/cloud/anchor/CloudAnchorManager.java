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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CloudAnchorManager {

    private static final String TAG = CloudAnchorManager.class.getSimpleName();

    private final PetContext mPetContext;

    public CloudAnchorManager(PetContext petContext) {
        mPetContext = petContext;
    }

    private boolean isCloudAnchorApiKeySet() {
        boolean isKeySet = ContextUtils.isMetaDataSet(mPetContext.getGVRContext().getContext(),
                PetConstants.GOOGLE_CLOUD_ANCHOR_KEY_NAME);
        if (!isKeySet && BuildConfig.DEBUG) {
            Context context = mPetContext.getActivity().getApplicationContext();
            mPetContext.runOnPetThread(() -> {
                Toast.makeText(context, "Cloud anchor API is not set", Toast.LENGTH_LONG).show();
            });
        }
        return isKeySet;
    }

    private int mPendingToHost;
    private boolean mIsHostingAnchors;
    private List<ManagedAnchor> mHostedAnchors;
    private final Object HOSTING_LOCK = new Object();
    private OnCloudAnchorCallback mHostingCallback;

    public void hostAnchors(@NonNull ManagedAnchor[] anchors, @NonNull OnCloudAnchorCallback callback) {

        mHostingCallback = callback;

        // Already is hosting
        if (mIsHostingAnchors) {
            String errorString = "Already hosting anchors";
            Log.e(TAG, errorString);
            onHostError(errorString, null);
            return;
        }

        if (anchors.length == 0) {
            String errorString = "Nothing to host. Anchor array is empty";
            Log.e(TAG, errorString);
            onHostError(errorString, null);
            return;
        }

        if (!isCloudAnchorApiKeySet()) {
            String errorString = "Unable to host anchors. Cloud anchor API key is not set.";
            Log.e(TAG, errorString);
            onHostError(errorString, null);
            return;
        }

        if (!NetworkUtils.hasInternetConnection(mPetContext)) {
            String errorString = "Cannot host anchors. No internet connection";
            Log.e(TAG, errorString);
            onHostError(errorString, new NetworkException("No internet connection"));
            return;
        }

        mPendingToHost = anchors.length;
        mIsHostingAnchors = true;
        mHostedAnchors = new ArrayList<>(anchors.length);

        for (ManagedAnchor managedAnchor : anchors) {
            try {
                mPetContext.getMixedReality().hostAnchor(managedAnchor.getAnchor(), (GVRAnchor hostedAnchor) -> {
                    synchronized (HOSTING_LOCK) {
                        // Ignore others result if any previous already failed
                        if (!mIsHostingAnchors) {
                            return;
                        }
                        mPendingToHost--;
                        if (hostedAnchor != null && !hostedAnchor.getCloudAnchorId().isEmpty()) {
                            String successString = String.format(Locale.getDefault(),
                                    "Success hosting anchor for %s", managedAnchor);
                            Log.i(TAG, successString);
                            mHostedAnchors.add(new ManagedAnchor(managedAnchor.getObjectType(), hostedAnchor));
                        } else {
                            mIsHostingAnchors = false;
                            mHostedAnchors = null;
                            String errorString = String.format(Locale.getDefault(),
                                    "Failed hosting anchor for %s. Returned empty id.", managedAnchor);
                            mHostingCallback.onError(new CloudAnchorException(errorString));
                            Log.e(TAG, errorString);
                        }
                        if (mIsHostingAnchors && mPendingToHost == 0) {
                            List<ManagedAnchor> result = new ArrayList<>(mHostedAnchors);
                            mIsHostingAnchors = false;
                            mHostedAnchors = null;
                            mHostingCallback.onResult(new ArrayList<>(result));
                        }
                    }
                });
            } catch (Throwable cause) {
                synchronized (HOSTING_LOCK) {
                    mIsHostingAnchors = false;
                    mHostedAnchors = null;
                }
                String errorString = "Error hosting anchor for " + managedAnchor;
                Log.e(TAG, errorString, cause);
                onHostError(errorString, cause);
                break;
            }
        }
    }

    private int mPendingToResolve;
    private boolean mIsResolvingAnchors;
    private List<ManagedAnchor> mResolvedCloudAnchors;
    private final Object RESOLVER_LOCK = new Object();
    private OnCloudAnchorCallback mResolveCallback;

    /**
     * Retrieve from cloud the anchors indicated by the given ids.
     *
     * @param anchors  Anchors to retrieve.
     * @param callback The success callback will be called if and only if all anchors are successfully resolved.
     *                 The error callback will be called if cannot resolve some anchor for any reason.
     */
    public void resolveAnchors(@NonNull CloudAnchor[] anchors, @NonNull OnCloudAnchorCallback callback) {

        mResolveCallback = callback;

        // Already is resolving
        if (mIsResolvingAnchors) {
            String errorString = "Already resolving anchors";
            Log.e(TAG, errorString);
            onResolveError(errorString, null);
            return;
        }

        if (anchors.length == 0) {
            String errorString = "Nothing to resolve. Anchor array is empty";
            Log.e(TAG, errorString);
            onResolveError(errorString, null);
            return;
        }

        if (!isCloudAnchorApiKeySet()) {
            String errorString = "Unable to resolver anchors. Cloud anchor API key is not set.";
            Log.e(TAG, errorString);
            onResolveError(errorString, null);
            return;
        }

        if (!NetworkUtils.hasInternetConnection(mPetContext)) {
            String errorString = "Cannot resolve anchors. No internet connection";
            Log.e(TAG, errorString);
            onResolveError(errorString, new NetworkException("No internet connection"));
            return;
        }

        mPendingToResolve = anchors.length;
        mIsResolvingAnchors = true;
        mResolvedCloudAnchors = new ArrayList<>(anchors.length);

        for (CloudAnchor cloudAnchor : anchors) {
            try {
                mPetContext.getMixedReality().resolveCloudAnchor(cloudAnchor.getCloudAnchorId(), (GVRAnchor resolvedAnchor) -> {
                    synchronized (RESOLVER_LOCK) {
                        // Ignore others result if any previous already failed
                        if (!mIsResolvingAnchors) {
                            return;
                        }
                        mPendingToResolve--;
                        if (resolvedAnchor != null && !resolvedAnchor.getCloudAnchorId().isEmpty()) {
                            String successString = String.format(Locale.getDefault(),
                                    "Success resolving anchor for %s", cloudAnchor);
                            Log.i(TAG, successString);
                            mResolvedCloudAnchors.add(new ManagedAnchor(cloudAnchor.getObjectType(), resolvedAnchor));
                        } else {
                            mIsResolvingAnchors = false;
                            mResolvedCloudAnchors = null;
                            String errorString = String.format(Locale.getDefault(),
                                    "Failed resolving anchor for %s. Returned empty id.", cloudAnchor);
                            mResolveCallback.onError(new CloudAnchorException(errorString));
                            Log.e(TAG, errorString);
                        }
                        if (mIsResolvingAnchors && mPendingToResolve == 0) {
                            List<ManagedAnchor> result = new ArrayList<>(mResolvedCloudAnchors);
                            mIsResolvingAnchors = false;
                            mResolvedCloudAnchors = null;
                            mResolveCallback.onResult(new ArrayList<>(result));
                        }
                    }
                });
            } catch (Throwable cause) {
                synchronized (RESOLVER_LOCK) {
                    mIsResolvingAnchors = false;
                    mResolvedCloudAnchors = null;
                }
                String errorString = "Error resolving anchor for " + cloudAnchor;
                Log.e(TAG, errorString, cause);
                onResolveError(errorString, cause);
                break;
            }
        }
    }

    private void onResolveError(String error, Throwable cause) {
        mResolveCallback.onError(new CloudAnchorException(error, cause));
    }

    private void onHostError(String error, Throwable cause) {
        mHostingCallback.onError(new CloudAnchorException(error, cause));
    }

    public interface OnCloudAnchorCallback {

        void onResult(List<ManagedAnchor> managedAnchors);

        void onError(CloudAnchorException e);
    }
}
