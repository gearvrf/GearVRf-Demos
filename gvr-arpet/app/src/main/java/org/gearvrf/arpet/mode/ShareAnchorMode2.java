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
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.OnCloudAnchorManagerListener;
import org.gearvrf.arpet.manager.cloud.anchor.ResolvedCloudAnchor;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.mode.view.IConnectionFoundView;
import org.gearvrf.arpet.mode.view.ILetsStartView;
import org.gearvrf.arpet.mode.view.ILookAtTargetView;
import org.gearvrf.arpet.mode.view.INoConnectionFoundView;
import org.gearvrf.arpet.mode.view.ISharingAnchorView;
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
import java.util.Arrays;
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

    private final String TAG = getClass().getSimpleName();
    private final int DEFAULT_SCREEN_TIMEOUT = 5000;  // 5s to change between screens

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
    private final Handler mHandler;
    private ShareAnchorView mShareAnchorView;
    private ShareAnchorView2 mShareAnchorView2;
    private final GVRAnchor mWorldCenterAnchor;
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
        mHandler = new Handler(petContext.getActivity().getMainLooper());
        mConnectionManager.addEventHandler(mConnectionEventHandler = new LocalConnectionEventHandler());
//        mShareAnchorView = (ShareAnchorView) mModeScene;
//        mShareAnchorView.setListenerShareAnchorMode(new HandlerActionsShareAnchorMode());
        mShareAnchorView2 = (ShareAnchorView2) mModeScene;

        mResources = mPetContext.getActivity().getResources();
        onShowLetsStartView(mShareAnchorView2.makeView(ILetsStartView.class));

        mWorldCenterAnchor = anchor;
        mCloudAnchorManager = new CloudAnchorManager(petContext, new CloudAnchorManagerReadyListener());
        mBackToHudModeListener = listener;
        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new MessageReceiver());
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

    private void onShowLetsStartView(ILetsStartView view) {
        view.setBackClickListener(v -> onCancelSharing());
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

    private void onShowWaitingForGuestView(IWaitingForGuestView view) {
        mCurrentMode = MODE_HOST;
        view.setCancelClickListener(v -> onCancelSharing());
        view.setContinueClickListener(v -> mConnectionManager.stopInvitation());
        view.show();
    }

    private void onShowWaitingForHostView(IWaitingForHostView view) {
        mCurrentMode = MODE_GUEST;
        view.setCancelClickListener(v -> onCancelSharing());
        view.show();
    }

    private void onShowNoConnectionFoundView(INoConnectionFoundView view) {
        view.setStatusText(getNoConnectionFoundString());
        view.setCancelClickListener(v -> onCancelSharing());
        view.setRetryClickListener(v -> {
            if (mCurrentMode == MODE_HOST) {
                onShowWaitingForGuestView(mShareAnchorView2.makeView(IWaitingForGuestView.class));
            } else {
                onShowWaitingForHostView(mShareAnchorView2.makeView(IWaitingForHostView.class));
            }
        });
        view.show();
    }

    private String getNoConnectionFoundString() {
        if (mCurrentMode == MODE_NONE) {
            return "";
        }
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
        mConnectionManager.removeMessageHandler(mConnectionEventHandler);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void showInviteAcceptedScreen() {
        if (mConnectionManager.getConnectionMode() == ConnectionMode.SERVER) {
            Log.d(TAG, "host");
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
            mHandler.postDelayed(() -> {
                mShareAnchorView.centerPetView();
                mHandler.postDelayed(() -> showMoveAround(), DEFAULT_SCREEN_TIMEOUT);
            }, DEFAULT_SCREEN_TIMEOUT);

            doHostAnchor();
        } else {
            Log.d(TAG, "guest");
            mShareAnchorView.inviteAcceptedGuest();
            mHandler.postDelayed(() -> showWaitingScreen(), DEFAULT_SCREEN_TIMEOUT);
        }
    }

    private void doHostAnchor() {
        mCloudAnchorManager.hostAnchor(mWorldCenterAnchor);
    }

    private void showSharedHost() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.sharedHost());
    }

    private void showPairingError() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.pairingError());
    }

    private void onConnectionEstablished() {

        int total = mConnectionManager.getTotalConnected();

        IConnectionFoundView view = mShareAnchorView2.makeView(IConnectionFoundView.class);
        view.setStatusText(mResources.getQuantityString(
                mCurrentMode == MODE_GUEST ? R.plurals.hosts_found : R.plurals.guests_found,
                total, total));
        view.show();

        if (mCurrentMode == MODE_HOST) {
            new Handler().postDelayed(this::showLookAtTargetView, 3000);
        }
    }

    private void showLookAtTargetView() {
        ILookAtTargetView viewLookAt = mShareAnchorView2.makeView(ILookAtTargetView.class);
        viewLookAt.setStatusText(mResources.getString(R.string.center_pet));
        viewLookAt.show();
        // Change text after a few seconds
        new Handler().postDelayed(
                () -> viewLookAt.setStatusText(mResources.getString(R.string.move_around)),
                5000);
    }

    private void showWaitingScreen() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.waitingView());
    }

    private void showStayInPositionToPair() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.toPairView());
    }

    private void showCenterPet() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.centerPetView());
    }

    private void showMoveAround() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.moveAroundView());
    }

    private void showModeShareAnchorView() {
        mPetContext.getActivity().runOnUiThread(() -> mHandler.postDelayed(() -> showStatusModeView(), DEFAULT_SCREEN_TIMEOUT));
    }

    private void showStatusModeView() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.modeView());
    }

    private void showLookingSideBySide() {
        mPetContext.getActivity().runOnUiThread(() -> mShareAnchorView.lookingSidebySide());
    }

    private void sendCommandToShowModeShareAnchorView() {
        ViewCommand command = new ViewCommand(ViewCommand.SHOW_MODE_SHARE_ANCHOR_VIEW);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showModeShareAnchorView();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'paired view' has failed");
            }
        });
    }

    private void sendCommandPairingErrorView() {
        ViewCommand command = new ViewCommand(ViewCommand.PAIRING_ERROR_VIEW);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "command to show 'pairing error  view' was performed successfully");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'pairing error view' has failed");
            }
        });
    }

    private void sendCommandSharedHost() {
        ViewCommand command = new ViewCommand(ViewCommand.SHARED_HOST);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "command to show 'shared host view' was performed successfully");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'shared host view' has failed");
            }
        });
    }

    private void sendCommandLookingSideBySide() {
        ViewCommand command = new ViewCommand(ViewCommand.LOOKING_SIDE_BY_SIDE);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "command to show 'looking side by  view' was performed successfully");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'looking side by  view' has failed");
            }
        });
    }

    private void shareScene(CloudAnchor[] anchors) {
        mMessageService.shareCloudAnchors(anchors, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Inform the guests to change their view
                sendCommandToShowModeShareAnchorView();
                mSharedMixedReality.startSharing(mWorldCenterAnchor, SharedMixedReality.HOST);
                backToHud();
            }

            @Override
            public void onFailure(Exception error) {
                showPairingError();
                backToHud();
            }
        });
    }

    private class CloudAnchorManagerReadyListener implements OnCloudAnchorManagerListener {
        @Override
        public void onHostReady() {
            // Just inform the guests to change their view
            sendCommandSharedHost();
            sendCommandLookingSideBySide();
            // Change the host view
            showStayInPositionToPair();

            Log.d(TAG, "sending a list of CloudAnchor objects to the guests");
            doResolveGuest();
        }

        @Override
        public void onHostFailure() {
            // TODO: handle this error on UI
            Log.d(TAG, "host failure");
            showPairingError();
        }
    }

    private void doResolveGuest() {
        ArrayList<CloudAnchor> cloudAnchors = mCloudAnchorManager.getCloudAnchors();
        int listSize = cloudAnchors.size();
        shareScene(cloudAnchors.toArray(new CloudAnchor[listSize]));
    }

    private void onCancelSharing() {

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
            ISharingAnchorView view = mShareAnchorView2.getCurrentView();
            if (IWaitingForGuestView.class.isInstance(view)) {
                ((IWaitingForGuestView) view).setTotalConnected(
                        mConnectionManager.getTotalConnected());
            }
        }
    }

    private class LocalConnectionEventHandler implements PetConnectionEventHandler {

        private final String HANDLER_ID = ShareAnchorView2.class.getName();

        @SuppressLint("SwitchIntDef")
        @Override
        public void handleEvent(PetConnectionEvent message) {
            mPetContext.getActivity().runOnUiThread(() -> {
                switch (message.getType()) {
                    case EVENT_CONNECTION_ESTABLISHED:
                        onConnectionEstablished();
                        break;
                    case EVENT_NO_CONNECTION_FOUND:
                        onShowNoConnectionFoundView(mShareAnchorView2.makeView(INoConnectionFoundView.class));
                        break;
                    case EVENT_GUEST_CONNECTION_ESTABLISHED:
                    case EVENT_ONE_CONNECTION_LOST:
                        updateTotalConnectedUI();
                        break;
                    case EVENT_ALL_CONNECTIONS_LOST:
                        backToHud();
                        break;
                    case EVENT_ON_LISTENING_TO_GUESTS:
                        onShowWaitingForGuestView(mShareAnchorView2.makeView(IWaitingForGuestView.class));
                        break;
                    case EVENT_ON_REQUEST_CONNECTION_TO_HOST:
                        onShowWaitingForHostView(mShareAnchorView2.makeView(IWaitingForHostView.class));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalConnectionEventHandler that = (LocalConnectionEventHandler) o;
            return HANDLER_ID != null ? HANDLER_ID.equals(that.HANDLER_ID) : that.HANDLER_ID == null;
        }

        @Override
        public int hashCode() {
            return HANDLER_ID != null ? HANDLER_ID.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "LocalConnectionEventHandler{" +
                    "HANDLER_ID='" + HANDLER_ID + '\'' +
                    '}';
        }
    }

    private class MessageReceiver extends SimpleMessageReceiver {

        @Override
        public void onReceiveSharedCloudAnchors(CloudAnchor[] cloudAnchors) throws MessageException {
            Log.d(TAG, "Sharing received: " + Arrays.toString(cloudAnchors));
            CloudAnchorResolverTask task = new CloudAnchorResolverTask(cloudAnchors);
            // This method gets locked and will return after loader finish
            Log.d(TAG, "Resolving anchors...");
            task.start();
            Log.d(TAG, "Task ended");
            // Lock released, now checks thread result
            if (task.getError() != null) {
                sendCommandPairingErrorView();
                throw new MessageException(task.getError());
            } else {
                // Start sharing using resolved pet pose as guest's world center
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
                    case ViewCommand.SHOW_MODE_SHARE_ANCHOR_VIEW:
                        showModeShareAnchorView();
                        break;
                    case ViewCommand.LOOKING_SIDE_BY_SIDE:
                        mHandler.postDelayed(() -> showLookingSideBySide(), DEFAULT_SCREEN_TIMEOUT);
                        break;
                    case ViewCommand.SHARED_HOST:
                        showSharedHost();
                        break;
                    case ViewCommand.PAIRING_ERROR_VIEW:
                        showPairingError();
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

        CloudAnchorResolverTask(CloudAnchor[] cloudAnchors) {
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
}
