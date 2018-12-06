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

package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.common.Task;
import org.gearvrf.arpet.common.TaskException;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.ManagedAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.exception.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.exception.NetworkException;
import org.gearvrf.arpet.manager.connection.BasePetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.mode.view.IAnchorSharedView;
import org.gearvrf.arpet.mode.view.IConnectionFoundView;
import org.gearvrf.arpet.mode.view.IGuestLookingAtTargetView;
import org.gearvrf.arpet.mode.view.IHostLookingAtTargetView;
import org.gearvrf.arpet.mode.view.ILetsStartView;
import org.gearvrf.arpet.mode.view.INoConnectionFoundView;
import org.gearvrf.arpet.mode.view.ISharingAnchorView;
import org.gearvrf.arpet.mode.view.ISharingErrorView;
import org.gearvrf.arpet.mode.view.ISharingFinishedView;
import org.gearvrf.arpet.mode.view.IWaitingDialogView;
import org.gearvrf.arpet.mode.view.IWaitingForGuestView;
import org.gearvrf.arpet.mode.view.IWaitingForHostView;
import org.gearvrf.arpet.mode.view.impl.ShareAnchorView;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.mixedreality.GVRAnchor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private static final int MODE_NONE = 0;
    private static final int MODE_HOST = 1;
    private static final int MODE_GUEST = 2;

    @IntDef({
            MODE_NONE,
            MODE_HOST,
            MODE_GUEST
    })
    public @interface Mode {
    }

    private IPetConnectionManager mConnectionManager;
    private ShareAnchorView mShareAnchorView;
    private final GVRAnchor mPetAnchor;
    private IMessageService mMessageService;
    private OnBackToHudModeListener mBackToHudModeListener;
    private SharedMixedReality mSharedMixedReality;
    private LocalConnectionEventHandler mConnectionEventHandler;
    private Resources mResources;
    private LocalMessageReceiver mMessageReceiver;

    @Mode
    private int mCurrentMode = MODE_NONE;

    public ShareAnchorMode(PetContext petContext, @NonNull GVRAnchor anchor, OnBackToHudModeListener listener) {
        super(petContext, new ShareAnchorView(petContext));

        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addEventHandler(mConnectionEventHandler = new LocalConnectionEventHandler(TAG));
        mShareAnchorView = (ShareAnchorView) mModeScene;

        mResources = mPetContext.getActivity().getResources();
        showViewLetsStart();

        mPetAnchor = anchor;
        mBackToHudModeListener = listener;
        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(mMessageReceiver = new LocalMessageReceiver(TAG));
        mSharedMixedReality = (SharedMixedReality) petContext.getMixedReality();
    }

    private void showViewLetsStart() {
        ILetsStartView view = mShareAnchorView.makeView(ILetsStartView.class);
        view.setBackClickListener(v -> cancelSharing());
        view.setHostClickListener(v -> {
            mConnectionManager.startInvitation();
        });
        view.setGuestClickListener(v -> {
            // Disable the planes detection
            mPetContext.unregisterPlaneListener();
            // Start to accept invitation from the host
            mConnectionManager.findInvitationThenConnect();
        });
        view.show();
    }

    private void showViewWaitingForGuest() {
        mCurrentMode = MODE_HOST;
        IWaitingForGuestView view = mShareAnchorView.makeView(IWaitingForGuestView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.setContinueClickListener(v -> mConnectionManager.stopInvitation());
        view.show();
    }

    private void showViewWaitingForHost() {
        mCurrentMode = MODE_GUEST;
        IWaitingForHostView view = mShareAnchorView.makeView(IWaitingForHostView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.show();
    }

    private void showViewNoConnectionFound() {
        INoConnectionFoundView view = mShareAnchorView.makeView(INoConnectionFoundView.class);
        view.setStatusText(getNoConnectionFoundString());
        view.setCancelClickListener(v -> cancelSharing());
        view.setRetryClickListener(v -> {
            if (mCurrentMode == MODE_HOST) {
                mConnectionManager.startInvitation();
            } else {
                mConnectionManager.findInvitationThenConnect();
            }
        });
        view.show();
    }

    private void showViewHostLookingAtTarget(@StringRes int stringId) {
        IHostLookingAtTargetView view;
        String text = mResources.getString(stringId);
        if (IHostLookingAtTargetView.class.isInstance(mShareAnchorView.getCurrentView())) {
            view = (IHostLookingAtTargetView) mShareAnchorView.getCurrentView();
            view.setStatusText(text);
        } else {
            view = mShareAnchorView.makeView(IHostLookingAtTargetView.class);
            view.setStatusText(text);
            view.show();
        }
    }

    private void showViewWaitingDialog(String text) {
        IWaitingDialogView view = mShareAnchorView.makeView(IWaitingDialogView.class);
        view.setStatusText(text);
        view.show();
    }

    private void showViewGuestLookingAtTarget() {
        IGuestLookingAtTargetView view = mShareAnchorView.makeView(IGuestLookingAtTargetView.class);
        view.show();
    }

    private void showViewAnchorShared() {
        IAnchorSharedView view = mShareAnchorView.makeView(IAnchorSharedView.class);
        view.show();
    }

    private void showViewSharingError(@NonNull OnCancelCallback cancelCallback, @NonNull OnRetryCallback retryCallback) {
        ISharingErrorView view = mShareAnchorView.makeView(ISharingErrorView.class);
        view.setCancelClickListener(v -> cancelCallback.onCancel());
        view.setRetryClickListener(v -> retryCallback.onRetry());
        view.show();
    }

    private void showViewSharingFinished(String text) {
        ISharingFinishedView view = mShareAnchorView.makeView(ISharingFinishedView.class);
        view.setOkClickListener(v -> cancelSharing());
        view.setStatusText(text);
        view.show();
    }

    private void hostPetAnchor() {

        showViewHostLookingAtTarget(R.string.center_pet);

        final AtomicBoolean isHosting = new AtomicBoolean(false);
        new Handler().postDelayed(() -> {
            if (isHosting.get()) {
                showViewHostLookingAtTarget(R.string.move_around);
            }
        }, 5000);

        isHosting.set(true);
        Log.d(TAG, "Hosting pet anchor");
        ManagedAnchor[] managedAnchors = {new ManagedAnchor(ArPetObjectType.PET, mPetAnchor)};
        new CloudAnchorManager(mPetContext).hostAnchors(managedAnchors, new CloudAnchorManager.OnCloudAnchorCallback() {
            @Override
            public void onResult(List<ManagedAnchor> hostedAnchor) {
                isHosting.set(false);
                showViewHostLookingAtTarget(R.string.stay_in_position);
                sharePetAnchorWithGuests(hostedAnchor.get(0).getAnchor());
            }

            @Override
            public void onError(CloudAnchorException e) {
                isHosting.set(false);
                Log.e(TAG, "Error hosting pet anchor", e);
                showViewSharingError(() -> cancelSharing(), () -> hostPetAnchor());
                handleCloudAnchorException(e);
            }
        });
    }

    private void handleCloudAnchorException(CloudAnchorException e) {
        if (e.getCause() instanceof NetworkException) {
            Toast.makeText(mPetContext.getActivity(),
                    R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    private String getNoConnectionFoundString() {
        String mode = mCurrentMode == MODE_GUEST
                ? mResources.getString(R.string.common_text_guest)
                : mResources.getString(R.string.common_text_host);
        return mResources.getString(
                R.string.view_no_connection_found_status_text, mode.toLowerCase());
    }

    @Override
    protected void onEnter() {
    }

    @Override
    protected void onExit() {
        mConnectionManager.removeEventHandler(mConnectionEventHandler);
        mMessageService.removeMessageReceiver(mMessageReceiver);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void onConnectionEstablished() {

        showViewConnectionFound();

        if (mCurrentMode == MODE_HOST) {
            new Handler().postDelayed(this::startHostSharingFlow, 3000);
        }
    }

    private void showViewConnectionFound() {

        int total = mConnectionManager.getTotalConnected();

        IConnectionFoundView view = mShareAnchorView.makeView(IConnectionFoundView.class);
        int pluralsText = mCurrentMode == MODE_GUEST ? R.plurals.hosts_found : R.plurals.guests_found;
        view.setStatusText(mResources.getQuantityString(pluralsText, total, total));
        view.show();
    }

    private void startHostSharingFlow() {

        // Make the guests wait while host prepare pet anchor
        showRemoteView(ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET);

        hostPetAnchor();
    }

    private void showRemoteView(@ViewCommand.Type String commandType) {
        Log.d(TAG, "Request to show remote view: " + commandType);
        ViewCommand command = new ViewCommand(commandType);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Success showing view on remote: " + commandType);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Failed showing view on remote: " + commandType);
            }
        });
    }

    private void sharePetAnchorWithGuests(GVRAnchor hostedAnchor) {
        Log.d(TAG, "Sharing pet anchor with guests");
        CloudAnchor cloudAnchor = new CloudAnchor(hostedAnchor.getCloudAnchorId(), ArPetObjectType.PET);
        mMessageService.sharePetAnchor(cloudAnchor, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mSharedMixedReality.startSharing(hostedAnchor, SharedMixedReality.HOST);
                // Sharing succeeded, back host to hud view
                gotToHudView();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Error sharing pet anchor with guests", error);
                showViewSharingError(
                        () -> cancelSharing(),
                        () -> sharePetAnchorWithGuests(hostedAnchor));
            }
        });
    }

    private void cancelSharing() {

        // Turn sharing OFF
        mSharedMixedReality.stopSharing();

        // Disconnect from remotes
        if (mCurrentMode == MODE_GUEST) {
            mConnectionManager.stopFindInvitationAndDisconnect();
            mConnectionManager.disconnect();
        } else {
            mConnectionManager.stopInvitationAndDisconnect();
        }

        mCurrentMode = MODE_NONE;
        gotToHudView();

        Log.d(TAG, "Sharing canceled");
    }

    private void gotToHudView() {
        mPetContext.getGVRContext().runOnGlThread(() -> mBackToHudModeListener.OnBackToHud());
    }

    private void updateTotalConnectedUI() {
        if (mCurrentMode == MODE_HOST) {
            ISharingAnchorView view = mShareAnchorView.getCurrentView();
            if (IWaitingForGuestView.class.isInstance(view)) {
                ((IWaitingForGuestView) view).setTotalConnected(
                        mConnectionManager.getTotalConnected());
            }
        }
    }

    private void onNoConnectionFound() {
        if (mCurrentMode == MODE_GUEST) {
            mConnectionManager.findInvitationThenConnect();
        }
    }

    private void onAllConnectionLost() {
        String text = mResources.getString(mCurrentMode == MODE_GUEST
                ? R.string.view_host_disconnected
                : R.string.view_guests_disconnected);
        showViewSharingFinished(text);
    }

    private class LocalConnectionEventHandler extends BasePetConnectionEventHandler {

        LocalConnectionEventHandler(@NonNull String name) {
            super(name);
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public void handleEvent(PetConnectionEvent message) {
            mPetContext.getActivity().runOnUiThread(() -> {
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
            });
        }
    }

    private class LocalMessageReceiver extends SimpleMessageReceiver {

        LocalMessageReceiver(@NonNull String name) {
            super(name);
        }

        @Override
        public void onReceivePetAnchor(CloudAnchor petAnchor) throws MessageException {
            Log.d(TAG, "Pet anchor received: " + petAnchor);

            try {

                PetAnchorResolverTask resolverTask = new PetAnchorResolverTask(petAnchor);
                // This method gets locked and will return after task to finish
                resolverTask.start();

                if (resolverTask.getError() != null) {
                    cancelSharing();
                    return;
                }

                mSharedMixedReality.startSharing(
                        resolverTask.getPetAnchor(), SharedMixedReality.GUEST);

                gotToHudView();

            } catch (Throwable cause) {
                String errorString = "Error processing the shared pet anchor";
                Log.e(TAG, errorString, cause);
                throw new MessageException(errorString, cause);
            }
        }

        @Override
        public void onReceiveViewCommand(ViewCommand command) throws MessageException {
            try {
                Log.d(TAG, "View command received: " + command);
                switch (command.getType()) {
                    case ViewCommand.SHOW_VIEW_LOOKING_AT_TARGET:
                        showViewGuestLookingAtTarget();
                        break;
                    default:
                        Log.d(TAG, "Unknown view command: " + command.getType());
                        break;
                }
            } catch (Throwable t) {
                throw new MessageException("Error processing view command", t);
            }
        }
    }

    private class PetAnchorResolverTask extends Task {

        CloudAnchor mPetCloudAnchor;
        GVRAnchor mPetAnchor;

        PetAnchorResolverTask(CloudAnchor petCloudAnchor) {
            this.mPetCloudAnchor = petCloudAnchor;
        }

        @Override
        public void execute() {
            resolvePetAnchor();
        }

        void resolvePetAnchor() {

            CloudAnchor[] cloudAnchors = {mPetCloudAnchor};
            new CloudAnchorManager(mPetContext).resolveAnchors(cloudAnchors, new CloudAnchorManager.OnCloudAnchorCallback() {
                @Override
                public void onResult(List<ManagedAnchor> managedAnchors) {
                    mPetAnchor = managedAnchors.get(0).getAnchor();
                    finish();
                }

                @Override
                public void onError(CloudAnchorException e) {
                    setError(new TaskException(e));
                    showViewSharingError(
                            () -> finish(),
                            () -> {
                                setError(null);
                                showViewGuestLookingAtTarget();
                                resolvePetAnchor();
                            });
                    handleCloudAnchorException(e);
                }
            });
        }

        GVRAnchor getPetAnchor() {
            return mPetAnchor;
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
