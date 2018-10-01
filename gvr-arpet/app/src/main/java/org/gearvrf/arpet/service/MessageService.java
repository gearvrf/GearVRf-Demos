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

package org.gearvrf.arpet.service;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.service.message.Command;
import org.gearvrf.arpet.service.message.CommandRequestMessage;
import org.gearvrf.arpet.service.message.RequestMessage;
import org.gearvrf.arpet.service.message.ResponseMessage;
import org.gearvrf.arpet.service.message.SceneSharingRequestMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MessageService implements IMessageService {

    private static final String TAG = MessageService.class.getSimpleName();

    private PetContext mContext;
    private IPetConnectionManager mConnectionManager;
    private List<MessageServiceReceiver> mMessageServiceReceivers = new ArrayList<>();
    private SparseArray<PendingResponseInfo<Void>> mPendingCallbacks = new SparseArray<>();

    private static class InstanceHolder {
        private static final IMessageService INSTANCE = new MessageService();
    }

    private MessageService() {
        this.mConnectionManager = PetConnectionManager.getInstance();
        this.mConnectionManager.addEventHandler(this::handleConnectionEvent);
        this.mContext = this.mConnectionManager.getContext();
    }

    public static IMessageService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void shareScene(@NonNull Serializable[] objects, @NonNull MessageServiceCallback<Void> callback) {
        sendRequest(new SceneSharingRequestMessage(objects), callback);
    }

    @Override
    public void sendCommand(@NonNull @Command String command, @NonNull MessageServiceCallback<Void> callback) {
        sendRequest(new CommandRequestMessage(command), callback);
    }

    @Override
    public void addMessageReceiver(MessageServiceReceiver receiver) {
        mMessageServiceReceivers.remove(receiver);
        mMessageServiceReceivers.add(receiver);
    }

    private synchronized void sendRequest(RequestMessage request, MessageServiceCallback<Void> callback) {

        PendingResponseInfo<Void> pendingResponseInfo = new PendingResponseInfo<>(
                request.getId(), mConnectionManager.getTotalConnected(), callback);

        if (mConnectionManager.getTotalConnected() == 0) {
            callbackFailure(pendingResponseInfo, new IllegalStateException("No connection found"));
            return;
        }

        mPendingCallbacks.put(request.getId(), pendingResponseInfo);

        mConnectionManager.sendMessage(request, totalSent -> {
            logForMessage(request, "Request sent to " + totalSent + " remotes");
            if (totalSent == 0) {
                mPendingCallbacks.remove(request.getId());
                callbackFailure(pendingResponseInfo, new RuntimeException("Failure sending request"));
            }
        });
    }

    private void onReceiveSharedScene(Serializable[] objects) throws MessageServiceException {
        for (MessageServiceReceiver receiver : mMessageServiceReceivers) {
            receiver.onReceiveSharedScene(objects);
        }
    }

    private void callbackFailure(PendingResponseInfo callback, Exception e) {
        mContext.runOnPetThread(() -> callback.mCallback.onFailure(e));
    }

    private void callbackSuccessVoid(PendingResponseInfo<Void> callback) {
        mContext.runOnPetThread(() -> callback.mCallback.onSuccess(null));
    }

    private void onReceiveCommand(@Command String command) throws MessageServiceException {
        for (MessageServiceReceiver receiver : mMessageServiceReceivers) {
            receiver.onReceiveCommand(command);
        }
    }

    private void handleConnectionEvent(PetConnectionEvent event) {
        if (event.getType() == PetConnectionEventType.MESSAGE_RECEIVED) {
            // Handle request or response
            onMessageReceived((Message) event.getData());
        } else if (event.getType() == PetConnectionEventType.CONNECTION_ONE_LOST) {
            if (mPendingCallbacks.size() > 0) {
                handleConnectionLost();
            }
        }
    }

    private synchronized void handleConnectionLost() {
        /*
         * This method treat the case where no new connections are made during the app experience.
         * If new connections are accepted during the app experience, you must modify the following
         * code to decrease the pending responses for requests associated with the lost connection.
         */
        for (int i = mPendingCallbacks.size() - 1; i >= 0; i--) {
            PendingResponseInfo<Void> pendingResponseInfo = mPendingCallbacks.valueAt(i);
            pendingResponseInfo.incrementTotalFailure();
            mPendingCallbacks.removeAt(i);
            if (!pendingResponseInfo.hasPending()) {
                callbackSuccessVoid(pendingResponseInfo);
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
                onReceiveSharedScene((Serializable[]) request.getData());
                logForMessage(request, "Shared objects received and processed.");
                sendDefaultResponseForRequest(request);
            } catch (MessageServiceException error) {
                sendResponseError(request, error);
            }

        } else if (request instanceof CommandRequestMessage) {

            try {
                onReceiveCommand((String) request.getData());
                logForMessage(request, "Command received and processed.");
                sendDefaultResponseForRequest(request);
            } catch (MessageServiceException error) {
                sendResponseError(request, error);
            }
        }
    }

    private synchronized void handleResponseMessage(ResponseMessage response) {

        if (mPendingCallbacks.size() == 0) {
            return;
        }

        PendingResponseInfo<Void> pendingResponseInfo = mPendingCallbacks.get(response.getRequestId());

        if (pendingResponseInfo != null) {
            mPendingCallbacks.remove(response.getRequestId());
            pendingResponseInfo.updateTotalPending(response);
            if (!pendingResponseInfo.hasPending()) {
                if (pendingResponseInfo.mTotalFailure == pendingResponseInfo.mTotalExpected) {
                    callbackFailure(pendingResponseInfo, new MessageServiceException("All remotes returned with error"));
                } else {
                    callbackSuccessVoid(pendingResponseInfo);
                }
            }
        }
    }

    private void sendDefaultResponseForRequest(RequestMessage request) {
        ResponseMessage response = ResponseMessage.createDefaultForRequest(request);
        mConnectionManager.sendMessage(response, totalSent ->
                logForMessage(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void sendResponseError(RequestMessage request, MessageServiceException error) {
        ResponseMessage response = new ResponseMessage.Builder(request.getId()).error(error).build();
        mConnectionManager.sendMessage(response, totalSent ->
                logForMessage(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void logForMessage(Message message, CharSequence text) {
        Log.d(TAG, String.format(Locale.getDefault(), "%s(%d): %s",
                message.getClass().getSimpleName(), message.getId(), text));
    }

    private static class PendingResponseInfo<T> {

        final int mTotalExpected, mRequestId;
        int mTotalSuccess, mTotalFailure;
        MessageServiceCallback<T> mCallback;

        PendingResponseInfo(int requestId, int totalExpected, MessageServiceCallback<T> mCallback) {
            this.mRequestId = requestId;
            this.mTotalExpected = totalExpected;
            this.mCallback = mCallback;
        }

        boolean hasPending() {
            return (mTotalSuccess + mTotalFailure) < mTotalExpected;
        }

        void incrementTotalFailure() {
            if (hasPending()) {
                mTotalFailure++;
            }
        }

        void updateTotalPending(ResponseMessage response) {
            if (hasPending()) {
                if (response.getError() == null) {
                    mTotalSuccess++;
                } else {
                    mTotalFailure++;
                }
            }
        }
    }
}