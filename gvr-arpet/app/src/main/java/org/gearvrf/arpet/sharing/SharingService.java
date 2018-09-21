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

package org.gearvrf.arpet.sharing;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.sharing.message.Command;
import org.gearvrf.arpet.sharing.message.CommandRequestMessage;
import org.gearvrf.arpet.sharing.message.RequestMessage;
import org.gearvrf.arpet.sharing.message.ResponseMessage;
import org.gearvrf.arpet.sharing.message.SceneSharingRequestMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SharingService {

    private static final String TAG = SharingService.class.getSimpleName();

    private PetContext mContext;
    private IPetConnectionManager mConnectionManager;
    private List<SharingServiceMessageReceiver> mSharingServiceMessageReceivers = new ArrayList<>();
    private SparseArray<CallbackInfo<Void>> mRequestCallbacks = new SparseArray<>();

    private static class InstanceHolder {
        private static final SharingService INSTANCE = new SharingService();
    }

    private SharingService() {
        this.mConnectionManager = PetConnectionManager.getInstance();
        this.mConnectionManager.addEventHandler(this::handleConnectionEvent);
        this.mContext = this.mConnectionManager.getContext();
    }

    public static SharingService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @param objects  Scene objects to load in remote side.
     * @param callback Returns nothing.
     */
    public void shareScene(@NonNull Serializable[] objects, @NonNull SharingMessageCallback<Void> callback) {

        if (mConnectionManager.getTotalConnected() == 0) {
            callback.onFailure(new IllegalStateException("No connection found"));
            return;
        }

        RequestMessage request = new SceneSharingRequestMessage(objects);
        CallbackInfo<Void> callbackInfo = new CallbackInfo<>(mConnectionManager.getTotalConnected(), callback);
        mRequestCallbacks.put(request.getId(), callbackInfo);

        mConnectionManager.sendMessage(request, totalSent -> {
            logForMessage(request, "Sharing request sent to " + totalSent + " remotes");
            if (totalSent == 0) {
                mRequestCallbacks.remove(request.getId());
                mContext.runOnPetThread(() -> callback.onFailure(new RuntimeException("Failure sending message to remotes")));
            }
        });
    }

    /**
     * @param command  Command to execute in remote side.
     * @param callback Returns nothing.
     */
    public void sendCommand(@NonNull @Command String command, @NonNull SharingMessageCallback<Void> callback) {

        if (mConnectionManager.getTotalConnected() == 0) {
            callback.onFailure(new IllegalStateException("No connection found"));
            return;
        }

        RequestMessage request = new CommandRequestMessage(command);
        CallbackInfo<Void> callbackInfo = new CallbackInfo<>(mConnectionManager.getTotalConnected(), callback);
        mRequestCallbacks.put(request.getId(), callbackInfo);

        mConnectionManager.sendMessage(request, totalSent -> {
            logForMessage(request, "Command request sent to " + totalSent + " remotes");
            if (totalSent == 0) {
                mRequestCallbacks.remove(request.getId());
                mContext.runOnPetThread(() -> callback.onFailure(new RuntimeException("Failure sending message to remotes")));
            }
        });
    }

    public void addMessageReceiver(SharingServiceMessageReceiver listener) {
        mSharingServiceMessageReceivers.remove(listener);
        mSharingServiceMessageReceivers.add(listener);
    }

    private void onShareScene(Serializable[] objects) {
        for (SharingServiceMessageReceiver listener : mSharingServiceMessageReceivers) {
            listener.onShareScene(objects);
        }
    }

    private void onSenCommand(@Command String command) {
        for (SharingServiceMessageReceiver listener : mSharingServiceMessageReceivers) {
            listener.onSendCommand(command);
        }
    }

    private void handleConnectionEvent(PetConnectionEvent event) {
        if (event.getType() == PetConnectionEventType.MESSAGE_RECEIVED) {
            // Handle request or response
            onMessageReceived((Message) event.getData());
        } else if (event.getType() == PetConnectionEventType.CONNECTION_ONE_LOST) {
            handleConnectionLost();
        }
    }


    private void handleConnectionLost() {
        if (mConnectionManager.getTotalConnected() == 0) {
            int size = mRequestCallbacks.size();
            for (int i = 0; i < size; i++) {
                CallbackInfo<Void> callbackInfo = mRequestCallbacks.get(mRequestCallbacks.keyAt(0));
                mContext.runOnPetThread(() -> callbackInfo.mCallback.onFailure(new RuntimeException("Failure sending message to remotes")));
            }
            mRequestCallbacks.clear();
        } else {
            /*
             * This method handles the case where no new connections are made during the app experience.
             * If new connections are accepted during the app experience, you must modify the following
             * code to decrease the pending responses for requests associated with the lost connection.
             */
            int size = mRequestCallbacks.size();
            for (int i = 0; i < size; i++) {
                CallbackInfo<Void> callbackInfo = mRequestCallbacks.get(mRequestCallbacks.keyAt(0));
                callbackInfo.decreaseTotalPendingResponses();
                if (!callbackInfo.hasPendingResponses()) {
                    callbackInfo.mCallback.onSuccess(null);
                }
            }
        }
    }

    private void onMessageReceived(Message message) {
        if (message instanceof RequestMessage) {
            handleRequestMessage((RequestMessage) message);
        } else if (message instanceof ResponseMessage) {
            handleResponseMessage((ResponseMessage) message);
        }
    }

    private void handleRequestMessage(RequestMessage request) {

        if (request instanceof SceneSharingRequestMessage) {

            try {
                onShareScene((Serializable[]) request.getData());
                logForMessage(request, "Shared objects received and processed.");
                sendDefaultResponseForRequest(request);
            } catch (Exception error) {
                sendResponseError(request, error);
            }

        } else if (request instanceof CommandRequestMessage) {

            try {
                onSenCommand((String) request.getData());
                logForMessage(request, "Command received and processed.");
                sendDefaultResponseForRequest(request);
            } catch (Exception error) {
                sendResponseError(request, error);
            }
        }
    }

    private void handleResponseMessage(ResponseMessage response) {

        if (mRequestCallbacks.size() == 0) {
            return;
        }

        CallbackInfo<Void> callbackInfo = mRequestCallbacks.get(response.getRequestId());

        if (callbackInfo != null) {
            callbackInfo.decreaseTotalPendingResponses();
            if (!callbackInfo.hasPendingResponses()) {
                sendVoidResponseOrError(response, callbackInfo);
                mRequestCallbacks.remove(response.getRequestId());
            }
        }
    }

    private void sendVoidResponseOrError(ResponseMessage response, CallbackInfo<Void> callbackInfo) {
        mContext.runOnPetThread(() -> {
            if (response.getError() == null) {
                callbackInfo.mCallback.onSuccess(null);
            } else {
                callbackInfo.mCallback.onFailure(response.getError());
            }
        });
    }

    private void sendDefaultResponseForRequest(RequestMessage request) {
        ResponseMessage response = ResponseMessage.createDefaultForRequest(request);
        mConnectionManager.sendMessage(response, totalSent ->
                logForMessage(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void sendResponseError(Message request, Exception error) {
        ResponseMessage response = new ResponseMessage.Builder(request.getId()).error(error).build();
        mConnectionManager.sendMessage(response, totalSent ->
                logForMessage(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void logForMessage(Message message, CharSequence text) {
        Log.d(TAG, String.format(Locale.getDefault(), "%s(%d): %s",
                message.getClass().getSimpleName(), message.getId(), text));
    }

    private static class CallbackInfo<T> {

        int mTotalPendingResponses;
        SharingMessageCallback<T> mCallback;

        CallbackInfo(int totalPendingResponses, SharingMessageCallback<T> mCallback) {
            this.mTotalPendingResponses = totalPendingResponses;
            this.mCallback = mCallback;
        }

        synchronized void decreaseTotalPendingResponses() {
            this.mTotalPendingResponses = Math.max(0, this.mTotalPendingResponses - 1);
        }

        synchronized boolean hasPendingResponses() {
            return mTotalPendingResponses > 0;
        }
    }
}