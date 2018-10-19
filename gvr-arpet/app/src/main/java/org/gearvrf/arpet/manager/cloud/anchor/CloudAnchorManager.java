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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.arpet.BuildConfig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.mixedreality.GVRAnchor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public void hostAnchor(GVRAnchor anchor) {

        if (!isCloudAnchorApiKeySet()) {
            Log.e(TAG, "Cloud Anchor API key is not set!");
            mListener.onHostFailure();
            return;
        }

        CloudAnchor cloudAnchor = new CloudAnchor(ArPetObjectType.PET);
        mCloudAnchors.add(cloudAnchor);
        Log.d(TAG, "hosting anchor for " + cloudAnchor.getObjectType());

        try {
            mPetContext.getMixedReality().hostAnchor(
                    anchor,
                    (hostedAnchor) -> {
                        String id = hostedAnchor.getCloudAnchorId();
                        if (!id.isEmpty()) {
                            Log.d(TAG, "Success hosting anchor for object of type " + cloudAnchor.getObjectType());
                            cloudAnchor.setCloudAnchorId(id);
                            onHostResult(ResponseType.SUCCESS);
                        } else {
                            Log.d(TAG, "cloud anchor ID is empty");
                            Log.d(TAG, "Failure hosting anchor for object of type " + cloudAnchor.getObjectType());
                            onHostResult(ResponseType.FAILURE);
                        }
                    });
        } catch (Throwable cause) {
            Log.d(TAG, "hostAnchor: cause " + cause);
            mListener.onHostFailure();
        }
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
                Log.d(TAG, "the manager is ready but some anchor was not hosted correctly");
                mListener.onHostFailure();
            }
            mCountSuccess = 0;
            mCountFailure = 0;
        }
    }

    public boolean hasInternetConnection() {
        Context context = mPetContext.getGVRContext().getContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;

        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }

        return activeNetwork != null && activeNetwork.isConnected();
    }

    private int mPendingToResolve;
    private boolean mIsResolvingAnchors;
    private List<ResolvedCloudAnchor> mResolvedCloudAnchors;
    private final Object RESOLVER_LOCK = new Object();
    private OnResolveCallback mResolveCallback;

    /**
     * Retrieve from cloud the anchors indicated by the given ids.
     *
     * @param anchors  Anchors to retrieve.
     * @param callback The success callback will be called if and only if all anchors are successfully resolved.
     *                 The error callback will be called if cannot resolve some anchor for any reason.
     */
    public void resolveAnchors(@NonNull CloudAnchor[] anchors, @NonNull OnResolveCallback callback) {

        // Already is resolving
        if (mIsResolvingAnchors) {
            return;
        }

        if (anchors.length == 0) {
            String errorString = "Nothing to resolve. Anchors array is empty";
            Log.e(TAG, errorString);
            new Handler(Looper.getMainLooper()).post(() -> callback.onError(new CloudAnchorException(errorString)));
            return;
        }

        if (!isCloudAnchorApiKeySet()) {
            String errorString = "Unable to resolver anchors. Cloud anchor API key is not set.";
            Log.e(TAG, errorString);
            new Handler(Looper.getMainLooper()).post(() -> callback.onError(new CloudAnchorException(errorString)));
            return;
        }

        mResolveCallback = callback;
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
                            mResolvedCloudAnchors.add(new ResolvedCloudAnchor(cloudAnchor.getObjectType(), resolvedAnchor));
                        } else {
                            mIsResolvingAnchors = false;
                            mResolvedCloudAnchors = null;
                            String errorString = String.format(Locale.getDefault(),
                                    "Failed resolving anchor for %s. Returned empty id.", cloudAnchor);
                            mResolveCallback.onError(new CloudAnchorException(errorString));
                            Log.e(TAG, errorString);
                        }
                        if (mIsResolvingAnchors && mPendingToResolve == 0) {
                            List<ResolvedCloudAnchor> result = new ArrayList<>(mResolvedCloudAnchors);
                            mIsResolvingAnchors = false;
                            mResolvedCloudAnchors = null;
                            mResolveCallback.onAllResolved(new ArrayList<>(result));
                        }
                    }
                });
            } catch (Throwable cause) {
                synchronized (RESOLVER_LOCK) {
                    mIsResolvingAnchors = false;
                    mResolvedCloudAnchors = null;
                }
                String errorString = "Error resolving anchor for " + cloudAnchor;
                Log.e(TAG, errorString + ". " + cause);
                CloudAnchorException error = new CloudAnchorException(errorString, cause);
                new Handler(Looper.getMainLooper()).post(() -> mResolveCallback.onError(error));
                break;
            }
        }
    }

    public interface OnResolveCallback {

        void onAllResolved(List<ResolvedCloudAnchor> resolvedCloudAnchors);

        void onError(CloudAnchorException e);
    }
}
