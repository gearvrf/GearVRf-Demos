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
import org.gearvrf.arpet.service.data.SharedScene;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.arpet.service.share.SharedObjectPose;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MessageService implements IMessageService {

    private static final String TAG = MessageService.class.getSimpleName();

    private PetContext mContext;
    private IPetConnectionManager mConnectionManager;
    private List<MessageReceiver> mMessageReceivers = new ArrayList<>();
    private SparseArray<PendingResponseInfo<Void>> mPendingResponseInfos = new SparseArray<>();

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
    public void shareScene(@NonNull SharedScene sharedScene, @NonNull MessageCallback<Void> callback) {
        sendRequest(createRequest(sharedScene), callback);
    }

    @Override
    public void sendViewCommand(@NonNull ViewCommand command, @NonNull MessageCallback<Void> callback) {
        sendRequest(createRequest(command), callback);
    }

    @Override
    public void updatePoses(@NonNull SharedObjectPose[] poses, @NonNull MessageCallback<Void> callback) {
        sendRequest(createRequest(poses), callback);
    }

    @Override
    public void addMessageReceiver(MessageReceiver receiver) {
        mMessageReceivers.remove(receiver);
        mMessageReceivers.add(receiver);
    }

    private <Data extends Serializable> RequestMessage<Data> createRequest(Data data) {
        String actionName = Thread.currentThread().getStackTrace()[3].getMethodName();
        return new RequestMessage<>(actionName, data);
    }

    private synchronized void sendRequest(RequestMessage request, MessageCallback<Void> callback) {

        PendingResponseInfo<Void> pendingResponseInfo = new PendingResponseInfo<>(
                request.getId(), mConnectionManager.getTotalConnected(), callback);

        if (mConnectionManager.getTotalConnected() == 0) {
            onCallbackFailure(pendingResponseInfo, new IllegalStateException("No connection found"));
            return;
        }

        mPendingResponseInfos.put(request.getId(), pendingResponseInfo);

        mConnectionManager.sendMessage(request, totalSent -> {
            logForRequest(request, "Request sent: " + request);
            if (totalSent == 0) {
                mPendingResponseInfos.remove(request.getId());
                onCallbackFailure(pendingResponseInfo, new RuntimeException("Failure sending request: " + request));
            }
        });
    }

    private void onCallbackFailure(PendingResponseInfo callback, Exception e) {
        mContext.runOnPetThread(() -> callback.mCallback.onFailure(e));
    }

    private void onCallbackSuccessVoid(PendingResponseInfo<Void> callback) {
        mContext.runOnPetThread(() -> callback.mCallback.onSuccess(null));
    }

    private void onReceiveShareScene(SharedScene sharedScene) throws MessageException {
        for (MessageReceiver receiver : mMessageReceivers) {
            receiver.onReceiveSharedScene(sharedScene);
        }
    }

    private void onReceiveSendViewCommand(ViewCommand command) throws MessageException {
        for (MessageReceiver receiver : mMessageReceivers) {
            receiver.onReceiveViewCommand(command);
        }
    }

    private void onReceiveUpdatePoses(SharedObjectPose[] poses) throws MessageException {
        for (MessageReceiver receiver : mMessageReceivers) {
            receiver.onReceiveUpdatePoses(poses);
        }
    }

    private void handleConnectionEvent(PetConnectionEvent event) {
        if (event.getType() == PetConnectionEventType.MESSAGE_RECEIVED) {
            // Handle request or response
            onMessageReceived((Message) event.getData());
        } else if (event.getType() == PetConnectionEventType.CONNECTION_ONE_LOST) {
            if (mPendingResponseInfos.size() > 0) {
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
        for (int i = mPendingResponseInfos.size() - 1; i >= 0; i--) {
            PendingResponseInfo<Void> pendingResponseInfo = mPendingResponseInfos.valueAt(i);
            pendingResponseInfo.incrementTotalFailure();
            if (!pendingResponseInfo.hasPending()) {
                mPendingResponseInfos.removeAt(i);
                onCallbackSuccessVoid(pendingResponseInfo);
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

    private void callRequestedAction(RequestMessage request) throws MessageException {

        String actionName = request.getActionName();
        actionName = "onReceive" + Character.toUpperCase(actionName.charAt(0)) + actionName.substring(1);

        try {
            Serializable argument = request.getData();
            this.getClass().getDeclaredMethod(actionName, argument.getClass()).invoke(this, argument);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new MessageException("Error executing the requested action: " + actionName, e);
        }
    }

    private void handleRequestMessage(RequestMessage request) {

        try {
            callRequestedAction(request);
            logForRequest(request, "Request processing succeeded: " + request);
            sendDefaultResponseForRequest(request);
        } catch (MessageException error) {
            logForRequest(request, "Request processing failed: " + getStackTraceString(error));
            sendResponseError(request, error);
        }
    }

    private synchronized void handleResponseMessage(ResponseMessage response) {

        if (mPendingResponseInfos.size() == 0) {
            return;
        }

        PendingResponseInfo<Void> pendingResponseInfo = mPendingResponseInfos.get(response.getRequestId());

        if (pendingResponseInfo != null) {
            pendingResponseInfo.updateTotalPending(response);
            if (!pendingResponseInfo.hasPending()) {
                mPendingResponseInfos.remove(response.getRequestId());
                if (pendingResponseInfo.mTotalFailure == pendingResponseInfo.mTotalExpected) {
                    onCallbackFailure(pendingResponseInfo, new MessageException("All remotes returned with error"));
                } else {
                    onCallbackSuccessVoid(pendingResponseInfo);
                }
            }
        }
    }

    private void sendDefaultResponseForRequest(RequestMessage request) {
        ResponseMessage response = ResponseMessage.createDefaultForRequest(request);
        mConnectionManager.sendMessage(response, totalSent ->
                logForRequest(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void sendResponseError(RequestMessage request, MessageException error) {
        ResponseMessage response = new ResponseMessage.Builder(request.getId()).error(error).build();
        mConnectionManager.sendMessage(response, totalSent ->
                logForRequest(request, "Response " + (totalSent > 0 ? "sent" : "not sent")));
    }

    private void logForRequest(RequestMessage message, CharSequence text) {
        Log.d(TAG, String.format(Locale.getDefault(), "Request(%d): %s", message.getId(), text));
    }

    private static class PendingResponseInfo<T> {

        final int mTotalExpected, mRequestId;
        int mTotalSuccess, mTotalFailure;
        MessageCallback<T> mCallback;

        PendingResponseInfo(int requestId, int totalExpected, MessageCallback<T> mCallback) {
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

    private static String getStackTraceString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();
    }
}