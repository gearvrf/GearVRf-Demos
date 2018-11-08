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

package org.gearvrf.arpet.mode.sharing;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.exceptions.NotTrackingException;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.ManagedAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.exception.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.exception.NetworkException;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.sharing.view.IConnectionFoundView;
import org.gearvrf.arpet.mode.sharing.view.IGuestLookingAtTargetView;
import org.gearvrf.arpet.mode.sharing.view.IHostLookingAtTargetView;
import org.gearvrf.arpet.mode.sharing.view.ILetsStartView;
import org.gearvrf.arpet.mainview.IView;
import org.gearvrf.arpet.mode.sharing.view.ISharingErrorView;
import org.gearvrf.arpet.mainview.IConnectionFinishedView;
import org.gearvrf.arpet.mode.sharing.view.IWaitingForGuestView;
import org.gearvrf.arpet.mode.sharing.view.IWaitingForHostView;
import org.gearvrf.arpet.mode.sharing.view.impl.SharingAnchorViewController;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.data.RequestStatus;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.event.PetAnchorReceivedMessage;
import org.gearvrf.arpet.service.event.RequestStatusReceivedMessage;
import org.gearvrf.arpet.service.event.ViewCommandReceivedMessage;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.EventBusUtils;
import org.gearvrf.mixedreality.GVRAnchor;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_CONNECTION_ESTABLISHED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ENABLE_BLUETOOTH_DENIED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_GUEST_CONNECTION_ESTABLISHED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_HOST_VISIBILITY_DENIED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_NO_CONNECTION_FOUND;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ONE_CONNECTION_LOST;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ON_LISTENING_TO_GUESTS;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ON_REQUEST_CONNECTION_TO_HOST;

public class ShareAnchorMode extends BasePetMode {

    private final String TAG = ShareAnchorMode.class.getSimpleName();

    private IPetConnectionManager mConnectionManager;
    private SharingAnchorViewController mSharingAnchorViewController;
    private final GVRAnchor mPetAnchor;
    private IMessageService mMessageService;
    private OnBackToHudModeListener mBackToHudModeListener;
    private SharedMixedReality mSharedMixedReality;
    private PetAnchorSharingStatusHandler mPetAnchorSharingStatusHandler;
    private Resources mResources;

    @PetConstants.ShareMode
    private int mCurrentMode = PetConstants.SHARE_MODE_NONE;

    public ShareAnchorMode(PetContext petContext, @NonNull GVRAnchor anchor, OnBackToHudModeListener listener) {
        super(petContext, new SharingAnchorViewController(petContext));

        mConnectionManager = PetConnectionManager.getInstance();
        mSharingAnchorViewController = (SharingAnchorViewController) mModeScene;

        mResources = mPetContext.getActivity().getResources();
        showViewLetsStart();

        mPetAnchor = anchor;
        mBackToHudModeListener = listener;
        mMessageService = MessageService.getInstance();
        mSharedMixedReality = (SharedMixedReality) petContext.getMixedReality();
    }

    private void showViewLetsStart() {
        ILetsStartView view = mSharingAnchorViewController.makeView(ILetsStartView.class);
        view.setBackClickListener(v -> cancelSharing());
        view.setHostClickListener(v -> mConnectionManager.startInvitation());
        view.setGuestClickListener(v -> {
            // Disable the planes detection
            mPetContext.unregisterPlaneListener();
            // Start to accept invitation from the host
            mConnectionManager.findInvitationThenConnect();
        });
        view.show();
    }

    private void showViewWaitingForGuest() {
        mCurrentMode = PetConstants.SHARE_MODE_HOST;
        IWaitingForGuestView view = mSharingAnchorViewController.makeView(IWaitingForGuestView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.setContinueClickListener(v -> mConnectionManager.stopInvitation());
        view.show();
    }

    private void showViewWaitingForHost() {
        mCurrentMode = PetConstants.SHARE_MODE_GUEST;
        IWaitingForHostView view = mSharingAnchorViewController.makeView(IWaitingForHostView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.show();
    }

    private void showViewHostLookingAtTarget(@StringRes int stringId) {
        IHostLookingAtTargetView view;
        String text = mResources.getString(stringId);
        if (IHostLookingAtTargetView.class.isInstance(mSharingAnchorViewController.getCurrentView())) {
            view = (IHostLookingAtTargetView) mSharingAnchorViewController.getCurrentView();
            view.setStatusText(text);
        } else {
            view = mSharingAnchorViewController.makeView(IHostLookingAtTargetView.class);
            view.setStatusText(text);
            view.show();
        }
    }

    private void showViewGuestLookingAtTarget() {
        IGuestLookingAtTargetView view = mSharingAnchorViewController.makeView(IGuestLookingAtTargetView.class);
        view.show();
    }

    private void showViewSharingError(@NonNull OnCancelCallback cancelCallback, @NonNull OnRetryCallback retryCallback) {
        ISharingErrorView view = mSharingAnchorViewController.makeView(ISharingErrorView.class);
        view.setCancelClickListener(v -> cancelCallback.onCancel());
        view.setRetryClickListener(v -> retryCallback.onRetry());
        view.show();
    }

    private void showViewSharingFinished(String text) {
        IConnectionFinishedView view = mSharingAnchorViewController.makeView(IConnectionFinishedView.class);
        view.setOkClickListener(v -> cancelSharing());
        view.setStatusText(text);
        view.show();
    }

    private void hostPetAnchor() {

        final AtomicBoolean isHosting = new AtomicBoolean(false);
        new Handler().postDelayed(() -> {
            if (isHosting.get()) {
                showViewHostLookingAtTarget(R.string.move_around);
            }
        }, 5000);

        isHosting.set(true);
        ManagedAnchor<GVRAnchor> managedAnchor = new ManagedAnchor<>(ArPetObjectType.PET, mPetAnchor);

        Log.d(TAG, "Hosting pet anchor");
        new CloudAnchorManager(mPetContext).hostAnchors(managedAnchor, new CloudAnchorManager.OnCloudAnchorCallback<GVRAnchor>() {
            @Override
            public void onResult(ManagedAnchor<GVRAnchor> managedAnchor) {
                isHosting.set(false);
                showViewHostLookingAtTarget(R.string.stay_in_position);
                sharePetAnchorWithGuests(managedAnchor.getAnchor());
            }

            @Override
            public void onError(CloudAnchorException e) {
                isHosting.set(false);
                Log.e(TAG, "Error hosting pet anchor", e);
                showViewSharingError(
                        () -> cancelSharing(),
                        () -> {
                            showViewHostLookingAtTarget(R.string.center_pet);
                            new Handler().postDelayed(() -> hostPetAnchor(), 1500);
                        }
                );
                handleCloudAnchorException(e);
            }
        });
    }

    private void resolvePetAnchor(PetAnchorReceivedMessage message) {

        ManagedAnchor<CloudAnchor> managedAnchor = new ManagedAnchor<>(ArPetObjectType.PET, message.getPetAnchor());

        new CloudAnchorManager(mPetContext).resolveAnchors(managedAnchor, new CloudAnchorManager.OnCloudAnchorCallback<GVRAnchor>() {
            @Override
            public void onResult(ManagedAnchor<GVRAnchor> managedAnchor) {
                Log.d(TAG, "Anchor resolved successfully");

                RequestStatus requestStatus = message.getRequestStatus();
                requestStatus.setStatus(RequestStatus.STATUS_OK);
                mMessageService.sendRequestStatus(requestStatus);

                mSharedMixedReality.startSharing(
                        managedAnchor.getAnchor(), PetConstants.SHARE_MODE_GUEST);

                gotToHudView();
            }

            @Override
            public void onError(CloudAnchorException e) {
                showViewSharingError(
                        () -> cancelSharing(),
                        () -> {
                            showViewGuestLookingAtTarget();
                            new Handler().postDelayed(() -> resolvePetAnchor(message), 1500);
                        });
                handleCloudAnchorException(e);
            }
        });
    }

    private void handleCloudAnchorException(CloudAnchorException e) {
        mPetContext.runOnPetThread(() -> {
            if (NetworkException.class.isInstance(e.getCause())) {
                Toast.makeText(mPetContext.getActivity(),
                        R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            } else if (NotTrackingException.class.isInstance(e.getCause())) {
                Toast.makeText(mPetContext.getActivity(),
                        R.string.not_tracking, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void onConnectionEstablished() {

        showViewConnectionFound();

        if (mCurrentMode == PetConstants.SHARE_MODE_HOST) {
            new Handler().postDelayed(this::startHostSharingFlow, 3000);
        }
    }

    private void showViewConnectionFound() {

        int total = mConnectionManager.getTotalConnected();

        IConnectionFoundView view = mSharingAnchorViewController.makeView(IConnectionFoundView.class);
        int pluralsText = mCurrentMode == PetConstants.SHARE_MODE_GUEST ? R.plurals.hosts_found : R.plurals.guests_found;
        view.setStatusText(mResources.getQuantityString(pluralsText, total, total));
        view.show();
    }

    private void startHostSharingFlow() {

        showViewHostLookingAtTarget(R.string.center_pet);

        // Make the guests wait while host prepare pet anchor
        Log.d(TAG, "Request to show remote view: " + ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET);
        mMessageService.sendViewCommand(new ViewCommand(ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET));

        hostPetAnchor();
    }

    private void sharePetAnchorWithGuests(GVRAnchor hostedAnchor) {
        Log.d(TAG, "Sharing pet anchor with guests");
        CloudAnchor cloudAnchor = new CloudAnchor(hostedAnchor.getCloudAnchorId(), ArPetObjectType.PET);

        int requestId = mMessageService.sharePetAnchor(cloudAnchor);
        mPetAnchorSharingStatusHandler = new PetAnchorSharingStatusHandler(
                mConnectionManager.getTotalConnected(), requestId, hostedAnchor);
    }

    private void cancelSharing() {

        // Turn sharing OFF
        mSharedMixedReality.stopSharing();

        // Disconnect from remotes
        if (mCurrentMode == PetConstants.SHARE_MODE_GUEST) {
            mConnectionManager.stopFindInvitationAndDisconnect();
            mConnectionManager.disconnect();
        } else {
            mConnectionManager.stopInvitationAndDisconnect();
        }

        mCurrentMode = PetConstants.SHARE_MODE_NONE;
        gotToHudView();

        Log.d(TAG, "Sharing canceled");
    }

    private void gotToHudView() {
        mPetContext.getGVRContext().runOnGlThread(() -> mBackToHudModeListener.OnBackToHud());
    }

    private void updateTotalConnectedUI() {
        if (mCurrentMode == PetConstants.SHARE_MODE_HOST) {
            IView view = mSharingAnchorViewController.getCurrentView();
            if (IWaitingForGuestView.class.isInstance(view)) {
                ((IWaitingForGuestView) view).setTotalConnected(
                        mConnectionManager.getTotalConnected());
            }
        }
    }

    private void onNoConnectionFound() {
        if (mCurrentMode == PetConstants.SHARE_MODE_GUEST) {
            mConnectionManager.findInvitationThenConnect();
        }
    }

    private void onAllConnectionLost() {
        String text = mResources.getString(mCurrentMode == PetConstants.SHARE_MODE_GUEST
                ? R.string.view_host_disconnected
                : R.string.view_guests_disconnected);
        showViewSharingFinished(text);
        mPetAnchorSharingStatusHandler = null;
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        switch (message.getType()) {
            case EVENT_CONNECTION_ESTABLISHED:
                onConnectionEstablished();
                break;
            case EVENT_NO_CONNECTION_FOUND:
                //showViewNoConnectionFound();
                onNoConnectionFound();
                break;
            case EVENT_GUEST_CONNECTION_ESTABLISHED:
            case EVENT_ONE_CONNECTION_LOST:
                updateTotalConnectedUI();
                if (mPetAnchorSharingStatusHandler != null) {
                    mPetAnchorSharingStatusHandler.decrementTotalPending();
                }
                break;
            case EVENT_ALL_CONNECTIONS_LOST:
                onAllConnectionLost();
                break;
            case EVENT_ON_LISTENING_TO_GUESTS:
                showViewWaitingForGuest();
                break;
            case EVENT_ON_REQUEST_CONNECTION_TO_HOST:
                showViewWaitingForHost();
                break;
            case EVENT_ENABLE_BLUETOOTH_DENIED:
                Toast.makeText(mPetContext.getActivity(),
                        R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
                break;
            case EVENT_HOST_VISIBILITY_DENIED:
                Toast.makeText(mPetContext.getActivity(),
                        R.string.device_not_visible, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void handleReceivedMessage(ViewCommandReceivedMessage message) {
        ViewCommand command = message.getViewCommand();
        Log.d(TAG, "View command received: " + command);
        switch (command.getType()) {
            case ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET:
                showViewGuestLookingAtTarget();
                break;
            default:
                Log.d(TAG, "Unknown view command: " + command.getType());
                break;
        }
    }

    @Subscribe
    public void handleReceivedMessage(PetAnchorReceivedMessage message) {
        resolvePetAnchor(message);
    }

    @Subscribe
    public void handleReceivedMessage(RequestStatusReceivedMessage message) {
        if (mPetAnchorSharingStatusHandler != null) {
            mPetAnchorSharingStatusHandler.handle(message.getRequestStatus());
        }
    }

    private class PetAnchorSharingStatusHandler {

        int mTotalPendingStatus;
        int mRequestId;
        GVRAnchor mResolvedAnchor;
        ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

        PetAnchorSharingStatusHandler(int mTotalPendingStatus, int mRequestId, GVRAnchor resolvedAnchor) {
            this.mTotalPendingStatus = mTotalPendingStatus;
            this.mRequestId = mRequestId;
            this.mResolvedAnchor = resolvedAnchor;
        }

        void decrementTotalPending() {
            mLock.writeLock().lock();
            try {
                mTotalPendingStatus--;
            } finally {
                mLock.writeLock().unlock();
            }
        }

        void handle(RequestStatus status) {
            Log.d(TAG, "Request status received: " + status);
            if (status.getRequestId() == mRequestId) {
                decrementTotalPending();
                mLock.readLock().lock();
                try {
                    if (mTotalPendingStatus == 0) {
                        mSharedMixedReality.startSharing(mResolvedAnchor, PetConstants.SHARE_MODE_HOST);
                        // Sharing succeeded, back host to hud view
                        gotToHudView();
                        mPetAnchorSharingStatusHandler = null;
                    }
                } finally {
                    mLock.readLock().unlock();
                }
            }
        }
    }

    @FunctionalInterface
    private interface OnCancelCallback {
        void onCancel();
    }

    @FunctionalInterface
    private interface OnRetryCallback {
        void onRetry();
    }
}
