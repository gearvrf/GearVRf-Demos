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
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.common.Task;
import org.gearvrf.arpet.common.TaskException;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.OnCloudAnchorManagerListener;
import org.gearvrf.arpet.manager.cloud.anchor.ResolvedCloudAnchor;
import org.gearvrf.arpet.manager.connection.BasePetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.mode.view.IAnchorSharedView;
import org.gearvrf.arpet.mode.view.IConnectionFoundView;
import org.gearvrf.arpet.mode.view.ILetsStartView;
import org.gearvrf.arpet.mode.view.ILookAtTargetView;
import org.gearvrf.arpet.mode.view.INoConnectionFoundView;
import org.gearvrf.arpet.mode.view.ISharingAnchorView;
import org.gearvrf.arpet.mode.view.ISharingErrorView;
import org.gearvrf.arpet.mode.view.IWaitingDialogView;
import org.gearvrf.arpet.mode.view.IWaitingForGuestView;
import org.gearvrf.arpet.mode.view.IWaitingForHostView;
import org.gearvrf.arpet.mode.view.impl.ShareAnchorView2;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.mixedreality.GVRAnchor;

import java.util.ArrayList;
import java.util.List;

import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_CONNECTION_ESTABLISHED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ENABLE_BLUETOOTH_DENIED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_GUEST_CONNECTION_ESTABLISHED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_HOST_VISIBILITY_DENIED;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_NO_CONNECTION_FOUND;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ONE_CONNECTION_LOST;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ON_LISTENING_TO_GUESTS;
import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ON_REQUEST_CONNECTION_TO_HOST;

public class ShareAnchorMode2 extends BasePetMode {

    private final String TAG = ShareAnchorMode2.class.getSimpleName();

    private static final int MODE_NONE = 0;
    private static final int MODE_HOST = 1;
    private static final int MODE_GUEST = 2;
    private OnHostAnchorCallback mOnHostAnchorCallback;

    @IntDef({
            MODE_NONE,
            MODE_HOST,
            MODE_GUEST
    })
    public @interface Mode {
    }

    private IPetConnectionManager mConnectionManager;
    private ShareAnchorView2 mShareAnchorView;
    private final GVRAnchor mPetAnchor;
    private CloudAnchorManager mCloudAnchorManager;
    private IMessageService mMessageService;
    private OnBackToHudModeListener mBackToHudModeListener;
    private SharedMixedReality mSharedMixedReality;
    private LocalConnectionEventHandler mConnectionEventHandler;
    private Resources mResources;
    @Mode
    private int mCurrentMode = MODE_NONE;

    public ShareAnchorMode2(PetContext petContext, @NonNull GVRAnchor anchor, OnBackToHudModeListener listener) {
        super(petContext, new ShareAnchorView2(petContext));

        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addEventHandler(mConnectionEventHandler = new LocalConnectionEventHandler(TAG));
        mShareAnchorView = (ShareAnchorView2) mModeScene;

        mResources = mPetContext.getActivity().getResources();
        showViewLetsStart();

        mPetAnchor = anchor;
        mCloudAnchorManager = new CloudAnchorManager(petContext, new CloudAnchorManagerReadyListener());
        mBackToHudModeListener = listener;
        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new LocalMessageReceiver(TAG));
        mSharedMixedReality = (SharedMixedReality) petContext.getMixedReality();
    }

    private boolean checkInternetConnection() {
        if (!mCloudAnchorManager.hasInternetConnection()) {
            Toast.makeText(mPetContext.getActivity(),
                    R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showViewLetsStart() {
        ILetsStartView view = mShareAnchorView.makeView(ILetsStartView.class);
        view.setBackClickListener(v -> cancelSharing());
        view.setHostClickListener(v -> {
            if (checkInternetConnection()) {
                mConnectionManager.startInvitation();
            }
        });
        view.setGuestClickListener(v -> {
            if (checkInternetConnection()) {
                // Disable the planes detection
                mPetContext.unregisterPlaneListener();
                // Start to accept invitation from the host
                mConnectionManager.findInvitationThenConnect();
            }
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
            if (checkInternetConnection()) {
                if (mCurrentMode == MODE_HOST) {
                    mConnectionManager.findInvitationThenConnect();
                } else {
                    mConnectionManager.startInvitation();
                }
            }
        });
        view.show();
    }

    private void showViewLookAtTarget(String text) {
        ILookAtTargetView viewLookAt;
        if (ILookAtTargetView.class.isInstance(mShareAnchorView.getCurrentView())) {
            viewLookAt = (ILookAtTargetView) mShareAnchorView.getCurrentView();
            viewLookAt.setStatusText(text, true);
        } else {
            viewLookAt = mShareAnchorView.makeView(ILookAtTargetView.class);
            viewLookAt.setStatusText(text, false);
            viewLookAt.show();
        }
    }

    private void showViewWaitingDialog(String text) {
        IWaitingDialogView view = mShareAnchorView.makeView(IWaitingDialogView.class);
        view.setStatusText(text);
        view.show();
    }

    private void showViewAnchorShared() {
        IAnchorSharedView view = mShareAnchorView.makeView(IAnchorSharedView.class);
        view.show();
    }

    private void showViewSharingError() {
        ISharingErrorView view = mShareAnchorView.makeView(ISharingErrorView.class);
        view.setCancelClickListener(v -> cancelSharing());
        view.setRetryClickListener(v -> {
            if (checkInternetConnection()) {
                if (mCurrentMode == MODE_HOST) {

                } else {

                }
            }
        });
        view.show();
    }

    private void hostPetAnchor(OnHostAnchorCallback callback) {
        Log.d(TAG, "Hosting pet anchor");
        mOnHostAnchorCallback = callback;
        mCloudAnchorManager.hostAnchor(mPetAnchor);
    }

    private void onPetAnchorHosted(CloudAnchor petAnchor) {
        showViewLookAtTarget(mResources.getString(R.string.stay_in_position));
        sharePetAnchorWithGuests(petAnchor);
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
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void onConnectionEstablished() {

        int total = mConnectionManager.getTotalConnected();

        IConnectionFoundView view = mShareAnchorView.makeView(IConnectionFoundView.class);
        view.setStatusText(mResources.getQuantityString(
                mCurrentMode == MODE_GUEST ? R.plurals.hosts_found : R.plurals.guests_found,
                total, total));
        view.show();

        if (mCurrentMode == MODE_HOST) {
            new Handler().postDelayed(() -> {

                showViewLookAtTarget(mResources.getString(R.string.center_pet));

                // Make guests waiting while host prepare pet anchor
                showRemoteView(ViewCommand.SHOW_VIEW_WAITING_DIALOG);

                new Handler().postDelayed(() -> {
                            Log.d(TAG, "Look at pet position and move around it while hosting its anchor");
                            showViewLookAtTarget(mResources.getString(R.string.move_around));
                            hostPetAnchor(this::onPetAnchorHosted);
                        }, 5000
                );
            }, 3000);
        }
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

    private void sharePetAnchorWithGuests(CloudAnchor petAnchor) {
        Log.d(TAG, "Sharing pet anchor with guests");
        mMessageService.sharePetAnchor(petAnchor, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mSharedMixedReality.startSharing(mPetAnchor, SharedMixedReality.HOST);
                backToHud();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "Error sharing pet anchor with guests", error);
                showViewSharingError();
            }
        });
    }

    private class CloudAnchorManagerReadyListener implements OnCloudAnchorManagerListener {
        @Override
        public void onHostReady() {
            CloudAnchor[] cloudAnchors = mCloudAnchorManager.getCloudAnchors().toArray(new CloudAnchor[0]);
            mOnHostAnchorCallback.onHostDone(cloudAnchors[0]);
        }

        @Override
        public void onHostFailure() {
            Log.d(TAG, "host failure");
            showViewSharingError();
        }
    }

    private void cancelSharing() {

        // Turn sharing OFF
        mSharedMixedReality.stopSharing();

        // Disconnect from remotes
        if (mCurrentMode == MODE_GUEST) {
            mConnectionManager.stopFindInvitation();
            mConnectionManager.disconnect();
        } else {
            mConnectionManager.stopInvitationAndDisconnect();
        }

        mCurrentMode = MODE_NONE;
        backToHud();

        Log.d(TAG, "Sharing canceled");
    }

    private void backToHud() {
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
                        showViewNoConnectionFound();
                        break;
                    case EVENT_GUEST_CONNECTION_ESTABLISHED:
                    case EVENT_ONE_CONNECTION_LOST:
                        updateTotalConnectedUI();
                        break;
                    case EVENT_ALL_CONNECTIONS_LOST:
                        cancelSharing();
                        Toast.makeText(mPetContext.getActivity(),
                                R.string.common_text_connection_lost, Toast.LENGTH_LONG).show();
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

            showViewAnchorShared();

            new Handler().postDelayed(() -> {
                String status = mResources.getString(R.string.looking_the_same_thing);
                showViewLookAtTarget(status);
            }, 3000);

            CloudAnchorResolverTask task = new CloudAnchorResolverTask(petAnchor);
            // This method gets locked and will return after loader finish
            task.start();

            // Lock released, now checks thread result
            if (task.getError() != null) {
                showViewSharingError();
                throw new MessageException(task.getError());
            } else {
                ResolvedCloudAnchor cloudAnchor = task.getResolvedCloudAnchorByType(ArPetObjectType.PET);
                if (cloudAnchor != null) {
                    mSharedMixedReality.startSharing(cloudAnchor.getAnchor(), SharedMixedReality.GUEST);
                    backToHud();
                }
            }
        }

        @Override
        public void onReceiveViewCommand(ViewCommand command) throws MessageException {
            try {
                Log.d(TAG, "View command received: " + command);
                switch (command.getType()) {
                    case ViewCommand.SHOW_VIEW_WAITING_DIALOG:
                        showViewWaitingDialog(mResources.getString(R.string.waiting_for_host_sharing));
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

    private class CloudAnchorResolverTask extends Task {

        CloudAnchor[] mCloudAnchors;
        List<ResolvedCloudAnchor> mResolvedCloudAnchors;

        CloudAnchorResolverTask(CloudAnchor... cloudAnchors) {
            this.mCloudAnchors = cloudAnchors;
        }

        @Override
        public void execute() {

            mCloudAnchorManager.resolveAnchors(mCloudAnchors, new CloudAnchorManager.OnResolveCallback() {
                @Override
                public void onAllResolved(List<ResolvedCloudAnchor> resolvedCloudAnchors) {
                    mResolvedCloudAnchors = new ArrayList<>(resolvedCloudAnchors);
                    notifyExecuted();
                }

                @Override
                public void onError(CloudAnchorException e) {
                    setError(new TaskException(e));
                    notifyExecuted();
                }
            });
        }

        ResolvedCloudAnchor getResolvedCloudAnchorByType(@ArPetObjectType String type) {
            return mResolvedCloudAnchors.stream()
                    .filter(a -> a.getObjectType().equals(type))
                    .findFirst()
                    .orElse(null);
        }
    }

    @FunctionalInterface
    private interface OnHostAnchorCallback {
        void onHostDone(CloudAnchor cloudAnchor);
    }
}
