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

package org.gearvrf.arpet.service.message;

import org.gearvrf.arpet.connection.socket.bluetooth.BTMessage;

import java.io.Serializable;

public class ResponseMessage extends BTMessage {

    private int mRequestId;
    private Exception mError;

    public ResponseMessage(int requestId, Serializable data, Exception error) {
        super(data);
        this.mRequestId = requestId;
        this.mError = error;
    }

    public int getRequestId() {
        return mRequestId;
    }

    public Exception getError() {
        return mError;
    }

    public static ResponseMessage createDefaultForRequest(RequestMessage message) {
        return new Builder(message.getId()).build();
    }

    public static class Builder {

        private Serializable mData;
        private int mRequestId;
        private Exception mError;

        public Builder(int requestId) {
            this.mRequestId = requestId;
        }

        public Builder data(Serializable data) {
            this.mData = data;
            return this;
        }

        public Builder requestId(int requestId) {
            this.mRequestId = requestId;
            return this;
        }

        public Builder error(Exception error) {
            this.mError = error;
            return this;
        }

        public ResponseMessage build() {
            return new ResponseMessage(mRequestId, mData, mError);
        }
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "id=" + getId() +
                ",requestId=" + getRequestId() +
                ", device=" + getDevice() +
                ", data=" + getData() +
                ", error=" + getError() +
                "} ";
    }
}
